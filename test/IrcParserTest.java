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
}
