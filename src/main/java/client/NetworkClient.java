package client;

import shared.MessageUtils;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/*
 * This is a class for a client that can connect to a server via a socket and send/receive messages.
 * It has methods for connecting to the server, sending messages, and disconnecting.
 * There is an inner class, ListenFromServer, that listens for
 * messages from the server on a separate thread.
 */

//This class contains the network code for the chat client, handling connections
public class NetworkClient {
    private ObjectOutputStream sOutput; 	// Output stream to the server
    private ObjectInputStream sInput;  		// Input stream from the server
    private Socket socket;  				// Socket to connect to the server
    private final String server;  			// Server hostname
    private final String username;  		// Client username
    private final int port;  				// Port number to connect to

    // Constructor for the NetworkClient class
    public NetworkClient(String server, int port, String username) 
    {
        //instance VARS
        this.server = server;
        this.port = port;
        this.username = username;
    }

    
    // Method to connect to the server
    public boolean isConnected()
    {
        try 
        {
        	// create socket object 
            socket = new Socket(server, port);
            // create the input and output streams
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sInput = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
            // Start a new thread to listen for messages from the server
            new ListenFromServer().start();
            
            // Send the username to the server via object stream
            sOutput.writeObject(username);
        } 
        catch (IOException e) 
        {
            System.err.println("Error connecting to server: " + e);
            closeAll();
            return false;  
        }
        return true;
    }

    
    // Method to get the client's username
    public String getClientUsername() 
    {
        return username;
    }



    // Method to send a message to the server
    public void sendMessage(MessageUtils msg) 
    {
        if (msg == null) 
        {
            throw new NullPointerException("Message cannot be null.");
        }
        try 
        {
            sOutput.writeObject(msg);
        }
        catch (IOException e)
        {
            System.err.println("Exception occured while attempting to write to the server: " + e);
        }
    }


    // Method to disconnect from the server
    public void closeAll()
    {
        try 
        {
            if (sInput != null) 
            {
                sInput.close();
            }
            if (sOutput != null)
            {
                sOutput.close();
            }
            if (socket != null) {
                socket.close();
            }
        } 
        catch (IOException e) 
        {
            System.err.println("Exception occurred while attempting to close socket and open streams: " + e);
        }
    }

    
    // Inner thread class, used to listen for incoming messages sent from the server
    class ListenFromServer extends Thread 
    {
        public void run() 
        {
            while (true)
            {
                try 
                {
                	// receive via stream object stream
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                    System.out.print("> ");
                } 
                catch (IOException | ClassNotFoundException e) 
                {
                    System.err.println("*** " + "Server has closed the connection: " + e + " *** ");
                    closeAll();
                    
                    // Prevents exception printing by terminating program
                    System.exit(0); 
                    
                } 	
            }
        }
    }


}
