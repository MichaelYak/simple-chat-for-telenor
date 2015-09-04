package chat;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Mediator {
	
	private HashSet<ChatService> services = new HashSet<ChatService>();
	private Map<String, HashSet<ChatService>> channels = new HashMap<String, HashSet<ChatService>>();
	
	public Set<ChatService> getServices() {
		return new HashSet<ChatService>(services);
	}
			
	public void addService(ChatService service) {
		sendToAll(String.format(">> User %s is connected", service.getUsername()));
		services.add(service);
	}
	
	public void removeService(ChatService service) {
		synchronized (services) {
			services.remove(service);
		}
		
		Set<ChatService> servicesInChannel;
		if ((servicesInChannel = channels.get(service.getChannel())) != null)
			servicesInChannel.remove(service);
		
		sendToAll(String.format(">> User %s is disconnected", service.getUsername()));
	}
	
	public void joinChannel(ChatService service, String channel) {
		
		if (channels.containsKey(channel)) {
			channels.get(channel).add(service);
			sendToAllInChannel(service, String.format(">> %s joined the %s channel", service, channel));
		} else {
			HashSet<ChatService> servicesInChannel = new HashSet<ChatService>();
			servicesInChannel.add(service);
			channels.put(channel, servicesInChannel);
		}
	}
	
	public void leaveChannel(ChatService service) {
		
		sendToAllInChannel(service, String.format(">> %s left the channel", service));
		HashSet<ChatService> servicesInChannel = channels.get(service.getChannel());
		servicesInChannel.remove(service);
	}
	
	/**
     * Send message to a specific client
     * @param sender 	Name of a sender.
     * @param recipient Name of a recipient.
     * @param message	Message to send.
     */	
	public boolean sendToOne(String sender, String recipient, String message) {
		synchronized (services) {
			for (ChatService service : services) {
				if (service.getUsername().equals(recipient)) {
					service.getOut().println(sender + ":> " + message);
					return true;
				}
			}
		}

		return false;
	}
	
	/**
     * Send message to all of the clients in a channel.
     * @param service 	Service that hold the channel.
     * @param message	Message to send.
     */	
	public void sendToAllInChannel(ChatService service, String message) {
		
		HashSet<ChatService> servicesInChannel = new HashSet<ChatService>(channels.get(service.getChannel()));
		servicesInChannel.remove(service);

		for (ChatService serviceInChannel : servicesInChannel) {
			serviceInChannel.getOut().println(message);
		}
	}
	
	/**
     * Send message to all of the clients that are connected to the server.
     * @param message	Message to send.
     */	
	public void sendToAll(String message) {
		synchronized (services) {
			for (ChatService service : services) {
				service.getOut().println(message);
			}
		}
	}
	
	public void listConnectedUsers(ChatService service) {
		
		boolean connectedInChannel = service.getChannel() != null;
		HashSet<ChatService> connectedUsers = connectedInChannel ? channels.get(service.getChannel()) : services;
		
		PrintWriter output = service.getOut();
		output.format(">> Connected users in %s:\n", connectedInChannel ? "channel" : "server");
		synchronized (connectedUsers) {
			for (ChatService user : connectedUsers) {
				output.println(">> " + user);
			}
		}
	}
}
