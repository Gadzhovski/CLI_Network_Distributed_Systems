import client.NetworkClient;
import client.UserInput;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import server.Server;


import java.io.*;

import static org.hamcrest.CoreMatchers.containsString;


public class UserInputTest {
    private static final InputStream stdin = System.in;
    private static final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private static Server server;

    @BeforeAll
    public static void setUpStreamsAndServer() {
        System.setOut(new PrintStream(stdout));
        server = new Server(1500);
        new Thread(() -> {
            server.start();
        }).start();
    }

    @AfterAll
    public static void restoreStreamsAndStopServer() {
        System.setIn(stdin);
        System.setOut(System.out);
        server.stop();
    }


    // Test the handleInput() method with a KICK command with no username
    @Test
    public void testHandleInputKickNoUsername() {
        // Simulate user input
        System.setIn(new ByteArrayInputStream("KICK\n".getBytes()));

        NetworkClient client = new NetworkClient("localhost", 1500, "tester");
        UserInput userInput = new UserInput(client);
        userInput.handleInput();

        // Verify that no message was sent to the server
        String message = "*** KICK command must include a username. ***";
        MatcherAssert.assertThat(stdout.toString(), containsString(message));
    }


}
