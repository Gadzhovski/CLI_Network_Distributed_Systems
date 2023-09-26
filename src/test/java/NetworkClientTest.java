
import server.Server;
import client.NetworkClient;

import org.junit.jupiter.api.*;
import shared.MessageUtils;


public class NetworkClientTest {
    private static Server server;
    private NetworkClient networkClient;

    @BeforeAll
    public static void startServer() {
        // Start the server on port 1500
        server = new Server(1500);
        new Thread(() -> {
            server.start();
        }).start();
    }

    @AfterAll
    public static void stopServer() {
        // Stop the server
        server.stop();
    }

    // Test the ifConnected() method
    @Test
    public void testIfConnected() {
        NetworkClient client = new NetworkClient("localhost", 1500, "tester5");
        Assertions.assertTrue(client.isConnected());

    }

    // Test getting the client's username
    @Test
    public void getClientUsername() {
        NetworkClient client = new NetworkClient("localhost", 1500, "tester6");
        Assertions.assertEquals("tester6", client.getClientUsername());

    }

    // Test the sendMessage() method with a null message
    @org.junit.Test(expected = NullPointerException.class)
    public void testSendMessageNull() {
        MessageUtils msg = null;
        networkClient.sendMessage(msg);
    }

}

