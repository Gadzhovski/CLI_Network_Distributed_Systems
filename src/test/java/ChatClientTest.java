import client.ChatClient;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChatClientTest {

    @Test
    void testSetIPValid() {
        // Test that a valid IP address is correctly parsed
        String[] args = {"127.0.0.1", "1500", "testuser"};
        String expected = "127.0.0.1";
        String actual = ChatClient.setIP(args);
        assertEquals(expected, actual);
    }

    @Test
    void testSetIPInvalid() {
        // Test that an invalid IP address throws an IllegalArgumentException
        String[] args = {"invalidip", "1500", "testuser"};
        assertThrows(IllegalArgumentException.class, () -> ChatClient.setIP(args));
    }

    @Test
    void testSetPortValid() {
        // Test that a valid port number is correctly parsed
        String[] args = {"127.0.0.1", "1500", "testuser"};
        int expected = 1500;
        int actual = ChatClient.setPort(args);
        assertEquals(expected, actual);
    }

    @Test
    void testSetPortInvalid() {
        // Test that an invalid port number throws an IllegalArgumentException
        String[] args = {"127.0.0.1", "999", "testuser"};
        assertThrows(IllegalArgumentException.class, () -> ChatClient.setPort(args));
    }

    @Test
    void testSetPortNotANumber() {
        // Test that a non-numeric port number throws a NumberFormatException
        String[] args = {"127.0.0.1", "notanumber", "testuser"};
        assertThrows(NumberFormatException.class, () -> ChatClient.setPort(args));
    }

    @Test
    void testSetUsernameValid() {
        // Test that a valid username is correctly parsed
        String[] args = {"127.0.0.1", "1500", "testuser"};
        String expected = "testuser";
        String actual = ChatClient.setUsername(args);
        assertEquals(expected, actual);
    }

    @Test
    void testSetUsernameEmpty() {
        // Test that an empty username throws an IllegalArgumentException
        String[] args = {"127.0.0.1", "1500", ""};
        assertThrows(IllegalArgumentException.class, () -> ChatClient.setUsername(args));
    }

    @Test
    void testSetUsernameTooLong() {
        // Test that a username that is too long throws an IllegalArgumentException
        String[] args = {"127.0.0.1", "1500", "abcdefghijklmnopqrstuvwxyz"};
        assertThrows(IllegalArgumentException.class, () -> ChatClient.setUsername(args));
    }

    @Test
    void testSetUsernameContainsAdmin() {
        // Test that a username containing "(admin)" throws a SecurityException
        String[] args = {"127.0.0.1", "1500", "(admin)user"};
        assertThrows(SecurityException.class, () -> ChatClient.setUsername(args));
    }

}
