package chat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class ChatClient {

	private static final int CHAT_PORT = 2000;
	private static final String CHAT_IP = "localhost";
	private static final int TIMEOUT = 30 * 60 * 1000; 

	public ChatClient(String[] args) {

		String hostname = args.length > 0 ? args[0] : CHAT_IP;

		int port;
		try {
			port = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
			port = CHAT_PORT;
		}

		// Create the socket
		Socket socket = null;
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(TIMEOUT);

			Scanner systemIn = new Scanner(System.in);
			Scanner socketIn = new Scanner(socket.getInputStream());
			PrintWriter socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"),
					true);

			// Creates a service that connects to the server and start it in a
			// thread
			DisplayService service = new DisplayService(socketIn);
			Thread thread = new Thread(service);
			thread.start();

			while (true) {
				// read message from console
				String input = systemIn.nextLine();

				// write the message to the socket
				socketOut.println(input);
				socketOut.flush();

				if ("/QUIT".equals(input)) {
					break;
				}
			}

			systemIn.close();

		} catch (ConnectException e) {
			System.err.println("Server connection is lost");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (SocketException se) {
					System.out.println("Socket is closed");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		new ChatClient(args);
	}
}