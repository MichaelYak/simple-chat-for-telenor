package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ChatService implements Runnable, Comparable<ChatService> {

	private static final int TIMEOUT = 30 * 60 * 1000; 
	private static final String HELP = ">> List of available commands:\n"
			+ ">> /JOIN <channel>  Makes the client join the channel. If the channel doesn't exist, a new channel will be created.\n" 
			+ ">> /PART <channel>  Makes the client leave the channel.\n"
			+ ">> /PRIVMSG <recipient> <message>  Sends a private <message> to a <recipient>.\n"
			+ ">> /NAMES  Returns a list of online users or users in a channel if you belong to the channel.\n"
			+ ">> /QUIT   Makes the client leave the chat.\n" 
			+ ">> /HELP   Displays list of commands.";

	private Mediator mediator;
	private Socket socket;

	private String channel;
	private String username;

	private BufferedReader in;
	private PrintWriter out;

	public String getUsername() {
		return username;
	}

	public String getChannel() {
		return channel;
	}

	public PrintWriter getOut() {
		return out;
	}

	public ChatService(Mediator mediator, Socket socket) {
		// Save parameters
		this.mediator = mediator;
		this.socket = socket;
	}

	public void run() {

		try {
			socket.setSoTimeout(TIMEOUT);

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1"), true);

			doService();

		} catch (SocketTimeoutException e) {
			System.out.println("User timed out: " + this);
		} catch (SocketException e) {
			System.out.println("User throwed socket exception: " + this);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// The client connection is closed
			System.out.println("User disconnected: " + this);
			mediator.removeService(this);

			if (socket != null) {
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public String toString() {
		return username;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (other == null || this.getClass() != other.getClass())
			return false;

		ChatService ct = (ChatService) other;
		return username.equals(ct.getUsername());
	}

	@Override
	public int compareTo(ChatService ct) {
		return username.compareTo(ct.getUsername());
	}

	private void doService() throws IOException {

		out.println(">> You are connected to server: " + socket.getLocalAddress());
		out.println(">> Login with your username:");

		String message;
		do {
			if ((message = in.readLine()) != null)
				username = message;
		} while (!isUsernameValid(username));

		mediator.addService(this);
		out.println(HELP);

		while ((message = in.readLine()) != null) {
			String[] args = message.split(" ", 3);
			String command = args[0];

			if (command.equals("/JOIN")) {
				// Makes the client join the channel i.e. JOIN <channel> If the channel doesn't exist, a new channel will be created
				if (args.length < 2) {
					out.println(">> Channel name is missing");
					continue;
				}

				channel = args[1];
				mediator.joinChannel(this, channel);
				out.println(">> You joined channel: " + channel);
				
			} else if (command.equals("/PRIVMSG")) {
				// Sends <message> to <recipient> i.g. PRIVMSG <recipient> <message>
				if (args.length < 3) {
					out.println(">> Receipent or message is missing");
					continue;
				}

				String recipient = args[1];
				if (recipient.equals(username)) {
					out.println(">> It is forbidden to send a message to yourself.");
					continue;
				}

				String msg = args[2];
				String response = mediator.sendToOne(username, recipient, msg) 
						? ">> To " + recipient + ": " + msg 
						: ">> No such a user: " + recipient;
				out.println(response);
				
			} else if (command.equals("/NAMES")) {
				// Returns a list of online users or users in a channel if you are in this channel.
				mediator.listConnectedUsers(this);
				
			} else if (command.equals("/LIST")) {
				//TODO:: list all channels
				
			} else if (command.equals("/PART")) {
				// Makes the client leave the channel i.e. PART <channel> 
				if (channel == null) {
					out.println(">> You have not joined the channel.");
					continue;
				}

				mediator.leaveChannel(this);
				out.println(">> You have left the channel: " + channel);
				channel = null;
				
			} else if (command.equals("/HELP")) {
				out.println(HELP);
				
			} else if (command.equals("/QUIT")) {
				out.println(">> You are now disconnected from the chat.");
				break;
				
			} else if (channel != null) {
				mediator.sendToAllInChannel(this, username + ": " + message);
			}
		}
	}

	private boolean isUsernameValid(String username) {
		synchronized (mediator) {
			for (ChatService service : mediator.getServices()) {
				if (username.equals(service.getUsername()) && this != service) {
					out.println("Username already exists. Enter a new username:");
					return false;
				}
			}
		}
		return true;
	}
}
