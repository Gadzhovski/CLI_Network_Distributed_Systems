package server;

import shared.MessageUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

/*
 * The ClientHandler class is a thread that handles communication with a single client in a chat room.
 * It contains instance variables for the client socket, input and output streams,
 * and username, among others. The class has methods for broadcasting messages, kicking users, listing connected clients, closing the socket and streams, and writing messages to the client's output stream.
 */
public class ClientHandler extends Thread {
    private final Socket socket;  // The socket for the client connection
    private ObjectInputStream sInput;  // The input stream for the socket
    private ObjectOutputStream sOutput;  // The output stream for the socket
    private final long id;  // A unique identifier for the client thread
    private String username;   // The username for the client
    private final String date;  // The date and the time when the client connected
    private Server server;   // A reference to the chat server
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");  // A formatter for timestamps
    private final String ipAddress;  // The IP address of the client
    private HashSet<String> usernames;  // A set of all usernames currently in use WHAT THIS FOR


    // Constructor for the client handler thread
    public ClientHandler(Socket socket, boolean isAdmin, Server server, HashSet<String> usernames) {
        this.socket = socket;
        this.server = server;
        this.username = "client" + server.getUniqueId();
        this.ipAddress = socket.getInetAddress().getHostAddress();
        this.id = server.getUniqueId();
        this.date = new Date() + "\n";
        this.usernames = usernames;


        // If the client is an admin, add the "(admin)" suffix to their username
        if (isAdmin) {
            this.username += "(admin)";
        }
        try {
            // Initialize the input and output streams for the socket
            this.sOutput = new ObjectOutputStream(socket.getOutputStream());
            this.sInput = new ObjectInputStream(socket.getInputStream());

            // Checking if the username is already taken
            boolean isUsernameTaken = true;
            while (isUsernameTaken) {
                this.username = (String) sInput.readObject();
                if (usernames.contains(this.username)) {
                    // If the username is taken, notify the client and close the socket
                    writeMsg("*** Username already taken. Please choose a different username. ***");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        server.display("Exception closing the socket: " + e.getMessage());
                    }
                } 
                else {
                    isUsernameTaken = false;
                }
            }

            // Add the client's username to the set of usernames in use, and notify all clients that the client has joined
            usernames.add(this.username);
            server.broadcast("*** " + username + " has joined the chat room." + " *** ");
        } 
        catch (IOException | ClassNotFoundException e) {
            server.display("Exception creating new Input/output Streams: " + e.getMessage()); //HERE!! error
        }
    }


    // Getter for the client thread's unique identifier
    public long getId()
    {
        return id;
    }

    
    // Getter for the client's username
    public String getUsername() 
    {
        return username;
    }

    
    // Setter for the client's username
    public void setUsername(String username) 
    {
        this.username = username;
    }

    
    // Getter for the chat server
    public Server getServer() 
    {
        return server;
    }

    
    // Setter for the chat server
    public void setServer(Server server) 
    {
        this.server = server;
    }

    
    // Getter for the client's IP address
    public String getIpAddress() 
    {
        return ipAddress;
    }

    
    // Getter for the input stream
    public ObjectInputStream getInputStream()
    {
        return sInput;
    }

    
    // Getter for the output stream
    public ObjectOutputStream getOutputStream() 
    {
        return sOutput;
    }

    
    // Socket getter
    public Socket getSocket() 
    {
        return socket;
    }

    
    // The main entry point for the client thread
    public void run() {
        // A flag indicating whether to keep the thread running
        boolean keepGoing = true;

        // Loop until the flag is false
        while (keepGoing) {
            // Read a message from the input stream
            MessageUtils chatMessage;
            try {
                chatMessage = (MessageUtils) sInput.readObject();
            } 
            catch (SocketException | ClassNotFoundException e) {
                // If there is an error reading from the stream, break out of the loop and end the thread
                break;
            } 
            catch (IOException e) {
                // If there is an error reading from the stream, notify the server and break out of the loop
                server.display(username + " Exception reading Streams: " + e);
                break;
            }

            // Extract the message and its type from the message object
            String message = chatMessage.getMessage();
            switch (chatMessage.getType()) {
                case MessageUtils.MESSAGE -> {
                    // If the message type is a regular message, broadcast it to all clients in the chat room
                    boolean confirmation = server.broadcast(username + ": " + message);
                    if (!confirmation && !getServer().containsBadWord(message)) {
                        // If there was an error broadcasting the message, and it does not contain a banned word, notify the client
                        String msg = "*** " + "Sorry. No such user exists." + " *** ";
                        writeMsg(msg);
                    }
                }
                case MessageUtils.LOGOUT -> keepGoing = false;
                case MessageUtils.KICK -> {
                    // if the message type is a kick message, check if the client is authorized to kick users
                    if (!username.startsWith("(admin)")) {
                        // if the client is not an admin, notify them and break out of the switch statement
                        String msg = "*** " + "You are not authorized to kick users." + " *** ";
                        writeMsg(msg);
                        break;
                    }

                    // Extract the username to kick from the message
                    if (!kickUser(message)) {
                        // If there is no client with that username, notify the admin
                        String msg = "*** " + "Sorry. No such user exists." + " *** ";
                        writeMsg(msg);
                    }
                }
                case MessageUtils.USERS -> {
                    // If the message type is a users message, list all connected clients and their join times
                    writeMsg("List of the users connected at " + sdf.format(new Date()));
                    for (int i = 0; i < server.getClients().size(); ++i) {
                        ClientHandler ct = server.getClients().get(i);
                        writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                    }
                }
            }
        }
        // If the loop exits, remove the client from the server and close the socket
        server.remove(id, true);

        // if username contains(admin) remove admin part from it and remove it from usernames
        if (username.startsWith("(admin)"))
        {
            username = username.substring(7);
            usernames.remove(username);
        }
        else
        {
            usernames.remove(username);
        }
        close();
    }

    
    // Kick a user with the given username
    public boolean kickUser(String username) {
        // Search for the client with the given username in the list of connected clients
        for (ClientHandler clientThread : server.getClients()) {
            if (clientThread.getUsername().equals(username)) {
                // If the client is found, notify them that they have been kicked and remove them from the server
                clientThread.writeMsg("\033[33m*** You have been kicked by the admin. ***\033[0m");
                server.remove(clientThread.id, false);
                clientThread.close();
                server.broadcast("*** " + username + " has been kicked by the admin." + " *** ");
                return true;
            }
        }
        // If the client is not found, return false
        return false;
    }


    // Close the socket and input/output streams
    private void close() {
        try {
            if (sOutput != null) sOutput.close();
        } 
        catch (Exception e) {
        }
        try {
            if (sInput != null) sInput.close();
        } catch (Exception e) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }


    // Write a message to the client's output stream
    public boolean writeMsg(String msg) 
    {
        // If the client is still connected, send the message to them
        if (!socket.isConnected()) {
            close();
            return false;
        }
        // Write the message to the output stream
        try {
            sOutput.writeObject(msg);
        }
        // If an error occurs, do not abort just inform the user
        catch (IOException e) {
            // If there is an error writing to the stream, notify the server
            System.out.println("*** " + "Error sending message to " + username + " *** ");
            System.out.println(e.toString());
        }
        return true;
    }
}

