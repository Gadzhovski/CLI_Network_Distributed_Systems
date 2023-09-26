
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ClientHandler;
import server.Server;

import java.util.ArrayList;


class ServerTest {
    private Server server;

    // This method is run before each test
    @BeforeEach
    void setUp() {
        server = new Server(1500);
    }

    // Test starting a server
    @Test
    public void testStart() {
        // Start server
        Thread serverThread = new Thread(() -> server.start());
        serverThread.start();

        // Wait for server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Test that server socket is open
        Assertions.assertTrue(serverThread.isAlive());
    }


    // Test if unique id is unique
    @Test
    void testGetUniqueId() {
        int id1 = server.getUniqueId();
        int id2 = server.getUniqueId();
        Assertions.assertNotEquals(id1, id2);
    }

    // Test the getter for the list of clients when server is empty
    @Test
    void testGetClients() {
        ArrayList<ClientHandler> clientsArray = server.getClients();
        Assertions.assertEquals(0, clientsArray.size());
    }

    // Test the method for the list of banned users
    @Test
    void testContainsBadWord() {
        Assertions.assertFalse(server.containsBadWord("This is a good message"));
        Assertions.assertTrue(server.containsBadWord("This is bad message: Shit"));
    }


}
