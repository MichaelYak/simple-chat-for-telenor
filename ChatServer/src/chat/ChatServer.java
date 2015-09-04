package chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

	private static final int CHAT_PORT = 2000;

	// Mediator stores and maintains a list of the chat services
	private Mediator mediator;
	private ServerSocket serverSocket;

	public ChatServer(String[] args) {
		int port;

		// Get the port either from command line or use default
		try {
			port = Integer.parseInt(args[0]);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
			port = CHAT_PORT;
		}

		try {
			listen(port);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void listen(int port) throws IOException {

		mediator = new Mediator();
		serverSocket = new ServerSocket(port);

		while (true) {

			Socket socket = serverSocket.accept();
			System.out.println("User connected.");

			// Create ChatService to handle the connection
			ChatService service = new ChatService(mediator, socket);

			// Run ChatService in a thread
			Thread thread = new Thread(service);
			thread.start();
		}
	}

	static public void main(String[] args) {
		new ChatServer(args);
	}
}
