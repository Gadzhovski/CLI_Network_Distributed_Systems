
# CLI-based Network Distributed System
<img src="https://cdn-icons-png.flaticon.com/512/4505/4505323.png" width="80" height="80"/>

This project implements a CLI-based network distributed chat system, allowing multiple clients to communicate in real-time. It is developed in Java and offers a variety of features to ensure efficient and smooth communication.

## Setup and Installation
1. Ensure you have Java and Maven installed on your system.
2. Navigate to the project directory.
3. Use Maven to compile and build the project: `mvn clean install`
4. Run the server using the command: `java -jar <server-jar-name>.jar`
5. Run the client using the command: `java -jar <client-jar-name>.jar`

## Features and Functionalities

### Non-Functional Requirements
- Modularity through design patterns.
- Testing via JUnit.
- Ensuring fault tolerance.
- Component-based development approach.

### Server-side Functionalities
- Specify the listening port before server initiation.
- Handle multiple client connections concurrently.
- Designate the first client as the coordinator.
- Regularly verify the coordinator's online status and assign a new one if offline.
- Notify all clients about members who disconnect.

### Client-side Functionalities
- Specify necessary details like server IP, server port, unique username, client listening port, and client IP before initiating the client.
- Send public (broadcast) messages.
- Send private (direct) messages to specific users.

## Additional Features
- **Bad Word Checker**: Ensures appropriate language by checking for any inappropriate words in messages.
- **History Writer**: Records all broadcast messages for future reference.

## License
This project is licensed under the MIT License. For more details, see the LICENSE file.
