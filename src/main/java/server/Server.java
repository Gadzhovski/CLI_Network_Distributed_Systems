package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Class responsible for managing the server,
 * accepting incoming connections, and broadcasting messages to clients.
 */
public class Server 
{
    private final AtomicInteger uniqueId = new AtomicInteger(0); 	// Unique id for clients
    private ArrayList<ClientHandler> clientsArray; 					// Array list to hold client handler threads
    private final SimpleDateFormat dateFormat; 						// Date format for logging
    private final int port; 										// Server port number
    private volatile boolean keepGoing = true; 						// Flag to indicate if server is running
    private FileWriter historyFileWriter; 							// File writer for chat history
    private final HashSet<String> badWords = new HashSet<>(); 		// Set of bad words to filter

    // Constructor that receive the port to listen to for connection as parameter
    public Server(int port) {
        this.port = port;
        // Create array list to hold client handler threads
        clientsArray = new ArrayList<>();
        // Set date format
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        
        try {
            // Open history file for writing
            historyFileWriter = new FileWriter("src/main/java/server/ChatHistory.txt", true);
        } 
        catch (IOException e) {
            display("Error opening historyFileWriter file: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Read bad words file and populate bad words set
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/server/BadWords.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                badWords.add(line.trim().toLowerCase());
            }
        } 
        catch (IOException e) {
            System.err.println("Failed to load bad words: " + e.getMessage());
        }
    }
    
    // Method to start the server
    public void start() {
        keepGoing = true;
        // Set of usernames
        HashSet<String> usernames = new HashSet<>();
        try {
            // Create server socket
            ServerSocket serverSocket = new ServerSocket(port);
            display("Server waiting for Clients on port " + port + ".");
            while (keepGoing) {
                // Accept incoming connection
                Socket socket = serverSocket.accept();
            
                // Create client handler thread
                ClientHandler thread = new ClientHandler(socket, true,this, usernames);
                // Add thread to array list
                clientsArray.add(thread);
                thread.start();
                
                if(clientsArray.size() == 1) {
                    // Add (admin) tag to first client
                    addAdminToUsername();
                }
            }
            
            try {
                // Close server socket and client connections
                serverSocket.close();
                for (ClientHandler thread : clientsArray) {
                    try {
                        thread.getInputStream().close();
                        thread.getOutputStream().close();
                        thread.getSocket().close();
                    } 
                    catch (IOException ioE) {
                    	System.out.println(ioE);
                    }
                }
            } 
            catch (Exception e) {
                display("Exception occurred while attempting to  closing the server and clients: " + e);
            }
        } 
        catch (IOException e) {
            String msg = dateFormat.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }


    // Getter for unique id
    public int getUniqueId() {return uniqueId.incrementAndGet();}

    // Getter for array list of client handler threads
    public ArrayList<ClientHandler> getClients() {return clientsArray;}

    // Method to stop the server using ctrl + c in console
    public void stop()
    {
        keepGoing = false;
    }


    // Method to display massages in the console with timestamp
    public void display(String msg) {
        String time = dateFormat.format(new Date()) + " " + msg;
        System.out.println(time);
    }

    
    // Method to write messages to the clients
    public synchronized boolean broadcast(String message) {
        if (message.trim().isEmpty()) {
            // Do nothing if message is empty
            return false;
        }
        
        // Add timestamp to the message
        String timestamp = dateFormat.format(new Date());
        // To check if message is private i.e. client to client message
        String[] splitMessage = message.split(" ",3);
        //
        if(splitMessage.length < 2) {
            return false;
        }
        
        boolean isPrivate = splitMessage[1].charAt(0) == '@';

        // Check for bad word in message
        if (containsBadWord(message)) {
            ClientHandler currentClient = (ClientHandler)Thread.currentThread();
            currentClient.writeMsg("\033[33mWarning: Your message contains a bad word.\033[0m");
            return false;
        }

        // If private message, send message to mentioned username only
        if(isPrivate) {
            String toCheck = splitMessage[1].substring(1);
            message = splitMessage[0]  + splitMessage[2];
            boolean found = false;
            
            // Loop in reverse order to find the mentioned username
            for(int y = clientsArray.size(); --y>=0;) {
                ClientHandler currentClient = clientsArray.get(y);
                String check = currentClient.getUsername();
                String clientInfo = "(" + currentClient.getIpAddress() + ")";
                String[] splitMessageOnly = message.split(":");
                String messageLf = "\033[31m" + timestamp + " *** private *** " + splitMessageOnly[0].replace(":","") + clientInfo + ": " + splitMessageOnly[1] + "\033[0m";
                
                if(check.equals(toCheck)) {
                    // Try to write to the Client if it fails remove it from the list
                    if(!currentClient.writeMsg(messageLf)) {
                        clientsArray.remove(y);
                        display("Disconnected Client " + currentClient.getUsername() + " removed from list.");
                    }
                    // Username found and delivered the message
                    found = true;
                    break;
                }
            }
            // Mentioned user not found, return false
            return found;
        }

        // If message is a broadcast message
        else {
            display(message);
            // Write the message to the ChatHistory.txt file
            writeHistory(message);
            
            for(int i = clientsArray.size(); --i >= 0;) {
                ClientHandler currentClient = clientsArray.get(i);
                String clientInfo = "(" + currentClient.getIpAddress() + ")";
                String[] splitMessageOnly = message.split(" ",2);
                String messageLf = "\033[34m" + timestamp + " " + splitMessageOnly[0].replace(":", "") + clientInfo + ": " + splitMessageOnly[1] + "\033[0m";
                // Check if message contains "has joined the chat" or " has left the chat" and don't broadcast the ip address
                
                if (message.contains("has joined the chat") || message.contains("has left the chat")) {
                    messageLf = "\033[34m" + timestamp + " " + splitMessageOnly[0] + " " + splitMessageOnly[1] + "\033[0m";
                }
                if (message.contains("has been kicked by")) {
                    messageLf = "\033[34m" + timestamp + " " + splitMessageOnly[0] + " " + splitMessageOnly[1] + "\033[0m";
                }
                if(!currentClient.writeMsg(messageLf)) {
                    clientsArray.remove(i);
                    display("Disconnected Client " + currentClient.getUsername() + " removed from list.");
                }
            }
        }
        return true;
    }

    
    // Method to check if message contains a bad word
    public boolean containsBadWord(String message) {
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (badWords.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    
    // Method to write messages to the ChatHistory.txt file
    private synchronized void writeHistory(String message) {
        try {
            historyFileWriter.write(message + "\n");
            historyFileWriter.flush();
        } 
        catch(IOException e) {
            display("Error writing to historyFileWriter file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    // Method to add the string "admin " to the username of the first client in arraylist
    private void addAdminToUsername() {
        // Get the first client in arraylist
        ClientHandler first_client = clientsArray.get(0);
        String username = first_client.getUsername();
        String new_username = "(admin)" + username;
        first_client.setUsername(new_username);
        display("*** " + "The coordinator is " + new_username + " *** ");
        // tell the admin that he is the admin
        first_client.writeMsg("*** " + "You are the coordinator" + " *** ");
    }


    // Method to remove a client from clientsArray by its ID
    public synchronized void remove(long id, boolean broadcastMsg) {
        String disconnectedClient = "";
        
        // Scan the array list until we find the ID
        for(int i = 0; i < clientsArray.size(); ++i) {
            ClientHandler currentClient = clientsArray.get(i);
            // If found remove it
            if(currentClient.getId() == id) {
                disconnectedClient = currentClient.getUsername();
                clientsArray.remove(i);
                // Check if the disconnected client has "(admin)" in its username set the next client as admin
                if (disconnectedClient.contains("(admin)")) {
                    if (clientsArray.size() > 0) {
                        addAdminToUsername();
                    }
                }
                break;
            }
        }
        
        if (!disconnectedClient.isEmpty() && broadcastMsg) {
            broadcast("*** " + disconnectedClient + " has left the chat room." + " *** ");
            // if the client left was admin print the new admin
            if (disconnectedClient.contains("(admin)") && clientsArray.size() > 0) {
                broadcast(" *** " + "New coordinator is " + clientsArray.get(0).getUsername() + " *** ");
            }
        }
    }

    
    // Driver class
    public static void main(String[] args) {
        // Default port number we are going to use
        int portNumber = 1500;

        // Switch statement to handle arguments passed to the program
        switch (args.length) {
            case 1:
                try {
                    // Attempt to parse the argument as an integer and set it as the port number
                    portNumber = Integer.parseInt(args[0]);

                    // Check if the port number is within the valid range of 1024 to 65535
                    if (portNumber < 1024 || portNumber > 65535) {
                        // If the port number is not within the valid range, print an error message and usage instructions, and exit the program
                        System.out.println("Invalid port number. Port number should be between 1024 and 65535.");
                        System.out.println("Usage is:>java Server [portNumber]");
                        return;
                    }
                } 
                catch (NumberFormatException e) {
                    // If the argument cannot be parsed as an integer, print an error message and usage instructions, and exit the program
                    System.out.println("Invalid port number. Port number should be a number.");
                    System.out.println("Usage is:>java Server [portNumber]");
                    return;
                }
                break;

            // If no arguments are passed to the program, use the default port number of 1500
            case 0:
                break;
            default:
                System.out.println("Usage is:>java Server [portNumber]");
                return;
        }

        // Create a new Server object with the specified or default port number
        Server server = new Server(portNumber);

        // Start the server
        server.start();
    }

}