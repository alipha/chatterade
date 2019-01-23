import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.liph.chatterade.messaging.enums.MessageType;
import com.liph.chatterade.parsing.IrcParser;
import com.liph.chatterade.parsing.models.TokenizedMessage;
import java.util.Optional;
import org.junit.jupiter.api.Test;


public class IrcParserTest {

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

        assertFalse(m.getTargetName().isPresent());
        assertTrue(m.getTargetNames().isEmpty());
        
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

        assertFalse(m1.getTargetName().isPresent());
        assertTrue(m1.getTargetNames().isEmpty());

        // m2 test
        assertFalse(m2.getSenderName().isPresent());
        assertFalse(m2.getMessageType().isPresent());
        assertEquals("TEST", m2.getMessageTypeText());

        assertEquals(3, m2.getArguments().size());
        assertEquals("#test", m2.getArguments().get(0));
        assertEquals("hello", m2.getArguments().get(1));
        assertEquals("world", m2.getArguments().get(2));
        assertFalse(m2.hasTrailingArgument());

        assertFalse(m2.getTargetName().isPresent());
        assertTrue(m2.getTargetNames().isEmpty());

        // m3 test
        assertEquals(Optional.of("bob"), m3.getSenderName());
        assertFalse(m3.getMessageType().isPresent());
        assertEquals("TEST:again", m3.getMessageTypeText());

        assertEquals(1, m3.getArguments().size());
        assertEquals("foo bar", m3.getArguments().get(0));
        assertTrue(m3.hasTrailingArgument());

        assertFalse(m3.getTargetName().isPresent());
        assertTrue(m3.getTargetNames().isEmpty());
    }

    @Test
    public void TargetTest() {
        IrcParser parser = new IrcParser();

        TokenizedMessage m1 = parser.tokenizeMessage("JOIN #foo,#bar,#baz", false);
    }
}
