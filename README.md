# simple-chat-for-telenor

This is a basic implementation of Internet Relay Chat.

The chat supports a limited number of IRC commands https://en.wikipedia.org/wiki/List_of_Internet_Relay_Chat_commands


How to run:

Default port both for client and server is 2000. Default client IP is localhost.

Run example server:
1) Navigate to /bin folder.
2) In terminal run java chat.ChatServer to run server on default port, or java chat.ChatServer <port> to run on a specific port.

Run example client:
1) Navigate to /bin folder
2) In terminal run java chat.ChatClient to run a client on default IP & port, or java chat.ClientServer <host> <port> to run on a specific IP & port.


List of available commands:

/JOIN <channel>  Makes the client join the channel. If the channel doesn't exist, a new channel will be created.
/PART <channel>  Makes the client leave the channel.
/PRIVMSG <recipient> <message>  Sends a private <message> to a <recipient>.
/NAMES  Returns a list of online users or users in a channel if you belong to the channel.
/QUIT   Makes the client leave the chat.
/HELP   Displays list of commands.


Limitations:

The protocol is designed for one server and multiple clients. In the future the implementation can be extended to support multiple servers. Where servers have a copy if the global state information and are connected in IRC network that forms an undirected acyclic graph.
