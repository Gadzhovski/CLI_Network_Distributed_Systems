package shared;

import java.io.Serializable;

/*
 * This package contains classes that are shared between the client and the server packages.
 */


public class MessageUtils implements Serializable {//?????????!!!
	// The different types of message sent by the Client
	public static final int USERS = 0;   // Message type to request list of connected users
	public static final int MESSAGE = 1; // Message type for regular text messages
	public static final int LOGOUT = 2;  // Message type to disconnect from the server
	public static final int KICK = 3;    // Message type to remove a user from the server

	// Fields to hold the message type and content
	private final int type;
	private final String message;

	
	// Constructor for creating a new message
	public MessageUtils(int type, String message) 
	{
		this.type = type;
		this.message = message;
	}

	
	// Getter method for retrieving the message type
	public int getType() 
	{
		return type;
	}

	
	// Getter method for retrieving the message content
	public String getMessage() 
	{
		return message;
	}
}
