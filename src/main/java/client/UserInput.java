package client;

import shared.MessageUtils;
//import server.Server;
import java.util.Scanner;

/*
 * Class responsible for handling user input and sending messages to the server.
 */
public class UserInput 
{

    private final NetworkClient client; // NetworkClient object
    private final Scanner scanner; 		// Scanner object to read user input

    // Constructor initializes the instance variables.
    public UserInput(NetworkClient client)
    {
        this.client = client;
        scanner = new Scanner(System.in);
    }

    // Method to display the welcome message and instructions to the user.
    public void displayCommands() 
    {
        final String usernameColor = "\033[35m";  // ANSI color code for purple.
        final String resetColor = "\033[0m";   // ANSI color code to reset color.

        // Print the welcome message and instructions to the user.
        System.out.println(usernameColor + "Hey " + "\033[32m" + client.getClientUsername() + usernameColor + ", welcome to the Chatroom!" + "\n");
        System.out.println(usernameColor + "Instructions:" + resetColor);
        System.out.println(usernameColor + "1. To send a message to all active clients, simply type your message." + resetColor);
        System.out.println(usernameColor + "2. To send a message to a specific client, type \"@username message\", to text admin type '@(admin)username'." + resetColor);
        System.out.println(usernameColor + "3. To see a list of active clients, type \"USERS\"." + resetColor);
        System.out.println(usernameColor + "4. To log off from the server, type \"LOGOUT\"." + resetColor);
        System.out.println(usernameColor + "5. To kick a client from the server, type \"KICK username\" (admin only)." + resetColor);
    } 
    
    
    public void handleInput()
    {
    	try
    	{
    		// Keep reading user input until the user logs out.
            while (true)
            {
                System.out.print("> ");
                String msg = scanner.nextLine();
                
                // If the message is empty, print an error message and continue.
                if (msg.isEmpty()) 
                {
                    System.out.println("*** Message cannot be empty. ***");
                    continue;
                }
                // If the user types "LOGOUT", send a LOGOUT message to the server and break out of the loop.
                else if (msg.equals("LOGOUT")) 
                {
                    client.sendMessage(new MessageUtils(MessageUtils.LOGOUT, ""));
                    break;
                }
                // If the user types "USERS", send a USERS message to the server.
                else if (msg.equals("USERS")) 
                {
                    client.sendMessage(new MessageUtils(MessageUtils.USERS, ""));
                }
                // If the admin types "KICK username", send a KICK message to the server with the username of the client to be kicked.
                else if (msg.startsWith("KICK")) 
                {
                    if (msg.length() > 5) 
                    {
                        client.sendMessage(new MessageUtils(MessageUtils.KICK, msg.substring(5)));
                    }

                    // Otherwise, send a "MESSAGE" message to the server with the user's input.
                    else 
                    {
                        System.out.println("*** KICK command must include a username. ***");
                    }
                } 
                else 
                {
                	// If no other conditions are satisfied, assume message is to be broadcasted
                    client.sendMessage(new MessageUtils(MessageUtils.MESSAGE, msg));
                }
            }
        } 
        catch (Exception e) 
        {
            // If an exception occurs, print an error message
            System.out.println("Error: " + e.getMessage());
        } 
        finally 
        {
            // Close the scanner
            scanner.close();
        }
    }
}
