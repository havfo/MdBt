package net.fosstveit.mdbt.utils;

import java.io.*;
import java.net.*;
import java.util.*;

public abstract class MdBtConnector {

	// Connection stuff.
	private Input input = null;
	private Output output = null;
	private String charset = null;
	private InetAddress inetAddress = null;

	// Details about the last server that we connected to.
	private String server = null;
	private int port = 6667;
	private String password = null;

	// Outgoing message stuff.
	private Queue outQueue = new Queue();
	private long messageDelay = 1000;

	private String name = "mdbt";
	private String nick = name;
	private String login = "mdbt";
	private String finger = "You ought to be arrested for fingering a bot!";

	private String channelPrefixes = "#&+!";

	public MdBtConnector() {
	}

	public final synchronized void connect(String hostname) throws IOException,
			Exception {

		server = hostname;
		this.password = null;

		if (isConnected()) {
			throw new IOException(
					"The MdBtConnector is already connected to an IRC server.  Disconnect first.");
		}

		Socket socket = new Socket(hostname, port);

		inetAddress = socket.getLocalAddress();

		InputStreamReader inputStreamReader = null;
		OutputStreamWriter outputStreamWriter = null;
		if (getEncoding() != null) {
			inputStreamReader = new InputStreamReader(socket.getInputStream(),
					getEncoding());
			outputStreamWriter = new OutputStreamWriter(
					socket.getOutputStream(), getEncoding());
		} else {
			inputStreamReader = new InputStreamReader(socket.getInputStream());
			outputStreamWriter = new OutputStreamWriter(
					socket.getOutputStream());
		}

		BufferedReader breader = new BufferedReader(inputStreamReader);
		BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);

		// Attempt to join the server.
		if (password != null && !password.equals("")) {
			Output.sendRawLine(this, bwriter, "PASS " + password);
		}
		String nick = this.getName();
		Output.sendRawLine(this, bwriter, "NICK " + nick);
		Output.sendRawLine(this, bwriter, "USER " + this.getLogin()
				+ " 8 * :" + "0.1");

		input = new Input(this, socket, breader, bwriter);

		String line = null;
		int tries = 1;
		while ((line = breader.readLine()) != null) {

			this.handleLine(line);

			int firstSpace = line.indexOf(" ");
			int secondSpace = line.indexOf(" ", firstSpace + 1);
			if (secondSpace >= 0) {
				String code = line.substring(firstSpace + 1, secondSpace);

				if (code.equals("004")) {
					// We're connected to the server.
					break;
				} else if (code.equals("433")) {
					tries++;
					nick = getName() + tries;
					Output.sendRawLine(this, bwriter, "NICK " + nick);
				} else if (code.equals("439")) {
					// No action required.
				} else if (code.startsWith("5") || code.startsWith("4")) {
					socket.close();
					input = null;
					throw new Exception("Could not log into the IRC server: "
							+ line);
				}
			}
			this.setNick(nick);

		}

		socket.setSoTimeout(5 * 60 * 1000);

		input.start();

		if (output == null) {
			output = new Output(this, outQueue);
			output.start();
		}
	}

	public final synchronized void reconnect() throws IOException, Exception {
		if (getServer() == null) {
			throw new Exception(
					"Cannot reconnect to an IRC server because we were never connected to one previously!");
		}
		connect(getServer());
	}

	public final synchronized void disconnect() {
		this.quitServer();
	}

	public final void joinChannel(String channel) {
		this.sendRawLine("JOIN " + channel);
	}

	public final void joinChannel(String channel, String key) {
		this.joinChannel(channel + " " + key);
	}

	public final void partChannel(String channel) {
		this.sendRawLine("PART " + channel);
	}

	public final void partChannel(String channel, String reason) {
		this.sendRawLine("PART " + channel + " :" + reason);
	}

	public final void quitServer() {
		this.quitServer("");
	}

	public final void quitServer(String reason) {
		this.sendRawLine("QUIT :" + reason);
	}

	public final synchronized void sendRawLine(String line) {
		if (isConnected()) {
			input.sendRawLine(line);
		}
	}

	public final synchronized void sendRawLineViaQueue(String line) {
		if (line == null) {
			throw new NullPointerException(
					"Cannot send null messages to server");
		}
		if (isConnected()) {
			outQueue.add(line);
		}
	}

	public final void sendMessage(String target, String message) {
		outQueue.add("PRIVMSG " + target + " :" + message);
	}

	public final void sendNotice(String target, String notice) {
		outQueue.add("NOTICE " + target + " :" + notice);
	}

	public final void changeNick(String newNick) {
		this.sendRawLine("NICK " + newNick);
	}

	public final void identify(String password) {
		this.sendRawLine("NICKSERV IDENTIFY " + password);
	}

	public final void listChannels() {
		this.listChannels(null);
	}

	public final void listChannels(String parameters) {
		if (parameters == null) {
			this.sendRawLine("LIST");
		} else {
			this.sendRawLine("LIST " + parameters);
		}
	}

	protected void handleLine(String line) {
		// Check for server pings.
		if (line.startsWith("PING ")) {
			// Respond to the ping and return immediately.
			this.onServerPing(line.substring(5));
			return;
		}

		String sourceNick = "";
		String sourceLogin = "";
		String sourceHostname = "";

		StringTokenizer tokenizer = new StringTokenizer(line);
		String senderInfo = tokenizer.nextToken();
		String command = tokenizer.nextToken();
		String target = null;

		int exclamation = senderInfo.indexOf("!");
		int at = senderInfo.indexOf("@");
		if (senderInfo.startsWith(":")) {
			if (exclamation > 0 && at > 0 && exclamation < at) {
				sourceNick = senderInfo.substring(1, exclamation);
				sourceLogin = senderInfo.substring(exclamation + 1, at);
				sourceHostname = senderInfo.substring(at + 1);
			} else {

				if (tokenizer.hasMoreTokens()) {
					String token = command;

					int code = -1;
					try {
						code = Integer.parseInt(token);
					} catch (NumberFormatException e) {
						// Keep the existing value.
					}

					if (code != -1) {
						return;
					} else {
						sourceNick = senderInfo;
						target = token;
					}
				} else {
					return;
				}

			}
		}

		command = command.toUpperCase();
		if (sourceNick.startsWith(":")) {
			sourceNick = sourceNick.substring(1);
		}
		if (target == null) {
			target = tokenizer.nextToken();
		}
		if (target.startsWith(":")) {
			target = target.substring(1);
		}

		// Check for CTCP requests.
		if (command.equals("PRIVMSG") && line.indexOf(":\u0001") > 0
				&& line.endsWith("\u0001")) {
			String request = line.substring(line.indexOf(":\u0001") + 2,
					line.length() - 1);
			if (request.equals("VERSION")) {
				// VERSION request
				this.onVersion(sourceNick, sourceLogin, sourceHostname, target);
			} else if (request.startsWith("PING ")) {
				// PING request
				this.onPing(sourceNick, sourceLogin, sourceHostname, target,
						request.substring(5));
			} else if (request.equals("TIME")) {
				// TIME request
				this.onTime(sourceNick, sourceLogin, sourceHostname, target);
			} else if (request.equals("FINGER")) {
				// FINGER request
				this.onFinger(sourceNick, sourceLogin, sourceHostname, target);
			} else {
				// Nothing
			}
		} else if (command.equals("PRIVMSG")
				&& channelPrefixes.indexOf(target.charAt(0)) >= 0) {
			// This is a normal message to a channel.
			this.onMessage(target, sourceNick, sourceLogin, sourceHostname,
					line.substring(line.indexOf(" :") + 2));
		} else {
			// Nothing
		}

	}

	protected void onVersion(String sourceNick, String sourceLogin,
			String sourceHostname, String target) {
		this.sendRawLine("NOTICE " + sourceNick + " :\u0001VERSION " + "0.1"
				+ "\u0001");
	}

	protected void onPing(String sourceNick, String sourceLogin,
			String sourceHostname, String target, String pingValue) {
		this.sendRawLine("NOTICE " + sourceNick + " :\u0001PING " + pingValue
				+ "\u0001");
	}

	protected void onServerPing(String response) {
		this.sendRawLine("PONG " + response);
	}

	protected void onTime(String sourceNick, String sourceLogin,
			String sourceHostname, String target) {
		this.sendRawLine("NOTICE " + sourceNick + " :\u0001TIME "
				+ new Date().toString() + "\u0001");
	}

	protected void onFinger(String sourceNick, String sourceLogin,
			String sourceHostname, String target) {
		this.sendRawLine("NOTICE " + sourceNick + " :\u0001FINGER " + finger
				+ "\u0001");
	}

	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
	}

	protected void onDisconnect() {
	}

	protected final void setName(String name) {
		this.name = name;
	}

	private final void setNick(String nick) {
		this.nick = nick;
	}

	protected final void setLogin(String login) {
		this.login = login;
	}

	protected final void setFinger(String finger) {
		this.finger = finger;
	}

	public final String getName() {
		return name;
	}

	public String getNick() {
		return nick;
	}

	public final String getLogin() {
		return login;
	}

	public final String getFinger() {
		return finger;
	}

	public final synchronized boolean isConnected() {
		return input != null && input.isConnected();
	}

	public final void setMessageDelay(long delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("Cannot have a negative time.");
		}
		messageDelay = delay;
	}

	public final long getMessageDelay() {
		return messageDelay;
	}

	public final int getMaxLineLength() {
		return Input.MAX_LINE_LENGTH;
	}

	public final int getOutgoingQueueSize() {
		return outQueue.size();
	}

	public final String getServer() {
		return server;
	}

	public final int getPort() {
		return port;
	}

	public final String getPassword() {
		return password;
	}

	public void setEncoding(String charset) throws UnsupportedEncodingException {
		// Just try to see if the charset is supported first...
		"".getBytes(charset);

		this.charset = charset;
	}

	public String getEncoding() {
		return charset;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public synchronized void dispose() {
		output.interrupt();
		input.dispose();
	}
}
