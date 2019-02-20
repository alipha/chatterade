import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.messaging.enums.TargetType;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.models.Target;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;


public class IrcParserTest {
    @Test
    public void test() {
        assertEquals(1, 1);
    }

    @Test
    public void BasicTokenizingTest() {
        IrcParser parser = new IrcParser();

        String input = "  USER myusername myhostname   myservername :My Real  Name ";
        TokenizedMessage m = parser.tokenizeMessage(input, true);

        assertFalse(m.getSenderName().isPresent());
        assertEquals(Optional.of(MessageType.USER), m.getMessageType());
        assertEquals("USER", m.getMessageTypeText());

        assertEquals(4, m.getArguments().size());
        assertEquals("myusername", m.getArguments().get(0));
        assertEquals("myhostname", m.getArguments().get(1));
        assertEquals("myservername", m.getArguments().get(2));
        assertEquals("My Real  Name", m.getArguments().get(3));
        assertTrue(m.hasTrailingArgument());

        assertFalse(m.getTargetText().isPresent());
        assertTrue(m.getTargets().isEmpty());
        
        assertEquals(input, m.getRawMessage());
    }

    @Test
    public void SenderTest() {
        IrcParser parser = new IrcParser();

        String input1 = "JOIN #test";
        String input2 = ":foo!bar@host.com JOIN #test";

        TokenizedMessage m1 = parser.tokenizeMessage(input1, true);
        TokenizedMessage m2 = parser.tokenizeMessage(input1, false);
        TokenizedMessage m3 = parser.tokenizeMessage(input2, true);
        TokenizedMessage m4 = parser.tokenizeMessage(input2, false);

        assertFalse(m1.getSenderName().isPresent());
        assertFalse(m2.getSenderName().isPresent());
        assertFalse(m3.getSenderName().isPresent());
        assertEquals(Optional.of("foo!bar@host.com"), m4.getSenderName());

        assertEquals(input2, m4.getRawMessage());
    }

    @Test
    public void UnknownMessageTypeTest() {
        IrcParser parser = new IrcParser();

        TokenizedMessage m1 = parser.tokenizeMessage("FOO", false);
        TokenizedMessage m2 = parser.tokenizeMessage("TEST #test hello world", false);
        TokenizedMessage m3 = parser.tokenizeMessage(":bob TEST:again :foo bar", false);

        // m1 test
        assertFalse(m1.getSenderName().isPresent());
        assertFalse(m1.getMessageType().isPresent());
        assertEquals("FOO", m1.getMessageTypeText());

        assertEquals(0, m1.getArguments().size());
        assertFalse(m1.hasTrailingArgument());

        assertFalse(m1.getTargetText().isPresent());
        assertTrue(m1.getTargets().isEmpty());

        // m2 test
        assertFalse(m2.getSenderName().isPresent());
        assertFalse(m2.getMessageType().isPresent());
        assertEquals("TEST", m2.getMessageTypeText());

        assertEquals(3, m2.getArguments().size());
        assertEquals("#test", m2.getArguments().get(0));
        assertEquals("hello", m2.getArguments().get(1));
        assertEquals("world", m2.getArguments().get(2));
        assertFalse(m2.hasTrailingArgument());

        assertFalse(m2.getTargetText().isPresent());
        assertTrue(m2.getTargets().isEmpty());

        // m3 test
        assertEquals(Optional.of("bob"), m3.getSenderName());
        assertFalse(m3.getMessageType().isPresent());
        assertEquals("TEST:again", m3.getMessageTypeText());

        assertEquals(1, m3.getArguments().size());
        assertEquals("foo bar", m3.getArguments().get(0));
        assertTrue(m3.hasTrailingArgument());

        assertFalse(m3.getTargetText().isPresent());
        assertTrue(m3.getTargets().isEmpty());
    }

    @Test
    public void TargetTest() {
        IrcParser parser = new IrcParser();

        TokenizedMessage m1 = parser.tokenizeMessage("JOIN #foo,#bar,#baz", false);
        TokenizedMessage m2 = parser.tokenizeMessage("JOIN &foo,&bar,#baz key1,key2", false);
        TokenizedMessage m3 = parser.tokenizeMessage(":Alice!alice@localhost JOIN #foo,Bob,#baz", false);
        TokenizedMessage m4 = parser.tokenizeMessage("JOIN #foo", false);
        TokenizedMessage m5 = parser.tokenizeMessage("PRIVMSG Bob :hello there", false);
        TokenizedMessage m6 = parser.tokenizeMessage("PRIVMSG :hello there", false);
        TokenizedMessage m7 = parser.tokenizeMessage("JOIN", false);
        TokenizedMessage m8 = parser.tokenizeMessage("PASS mypass", false);

        // m1 test
        assertTrue(m1.getArguments().isEmpty());
        assertFalse(m1.hasTrailingArgument());
        assertEquals(TargetType.MULTIPLE_CHANNELS, m1.getTargetType());

        assertEquals(Optional.of("#foo,#bar,#baz"), m1.getTargetText());

        assertEquals(3, m1.getTargets().size());
        verifyTargets(m1.getTargets());
        assertEquals(Optional.of("#foo"), m1.getTargets().get(0).getChannel());
        assertEquals(Optional.of("#bar"), m1.getTargets().get(1).getChannel());
        assertEquals(Optional.of("#baz"), m1.getTargets().get(2).getChannel());

        // m2 test
        assertEquals(1, m2.getArguments().size());
        assertEquals("key1,key2", m2.getArguments().get(0));
        assertFalse(m2.hasTrailingArgument());
        assertEquals(TargetType.MULTIPLE_CHANNELS, m2.getTargetType());

        assertEquals(Optional.of("&foo,&bar,#baz"), m2.getTargetText());

        assertEquals(3, m2.getTargets().size());
        verifyTargets(m2.getTargets());
        assertEquals(Optional.of("&foo"), m2.getTargets().get(0).getChannel());
        assertEquals(Optional.of("&bar"), m2.getTargets().get(1).getChannel());
        assertEquals(Optional.of("#baz"), m2.getTargets().get(2).getChannel());
        
        // m3 test
        assertTrue(m3.getArguments().isEmpty());
        assertFalse(m3.hasTrailingArgument());
        assertEquals(TargetType.INVALID, m3.getTargetType());

        assertEquals(Optional.of("#foo,Bob,#baz"), m3.getTargetText());

        assertEquals(3, m3.getTargets().size());
        verifyTargets(m3.getTargets());
        assertEquals(Optional.of("#foo"), m3.getTargets().get(0).getChannel());
        assertEquals(Optional.of("Bob"), m3.getTargets().get(1).getNick());
        assertEquals(Optional.of("#baz"), m3.getTargets().get(2).getChannel());

        // m4 test
        assertTrue(m4.getArguments().isEmpty());
        assertFalse(m4.hasTrailingArgument());
        assertEquals(TargetType.CHANNEL, m4.getTargetType());

        assertEquals(Optional.of("#foo"), m4.getTargetText());

        assertEquals(1, m4.getTargets().size());
        verifyTargets(m4.getTargets());
        assertEquals(Optional.of("#foo"), m4.getTargets().get(0).getChannel());

        // m5 test
        assertEquals(1, m5.getArguments().size());
        assertEquals("hello there", m5.getArguments().get(0));
        assertTrue(m5.hasTrailingArgument());
        assertEquals(TargetType.USER, m5.getTargetType());

        assertEquals(Optional.of("Bob"), m5.getTargetText());

        assertEquals(1, m5.getTargets().size());
        verifyTargets(m5.getTargets());
        assertEquals(Optional.of("Bob"), m5.getTargets().get(0).getNick());
    }


    private void verifyTargets(List<Target> targets) {
        for(Target target : targets) {
            switch(target.getTargetType()) {
                case CHANNEL:
                    assertTrue(target.getChannel().isPresent());
                    assertFalse(target.getNick().isPresent());
                    break;
                case USER:
                    assertFalse(target.getChannel().isPresent());
                    assertTrue(target.getNick().isPresent());
                    break;
                default:
                    fail(format("Invalid target: %s", target.toString()));
                /*case NONE:
                case CHANNEL:
                case USER:
                case CHANNEL_OR_USER:
                case MULTIPLE_CHANNELS:
                case INVALID:*/
            }
        }
    }
}
