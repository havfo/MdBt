package net.fosstveit.mdbt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public abstract class MdBtConnector {

	// Connection stuff.
	private Input input;
	private Output output;

	// Details about the last server that we connected to.
	private String server;
	private int port = 6667;

	// Outgoing message stuff.
	private Queue outQueue = new Queue();

	private String nick = "mdbt";

	public MdBtConnector() {
	}

	public final synchronized void connect(String hostname) throws IOException,
			Exception {

		server = hostname;

		Socket socket = new Socket(hostname, port);

		BufferedReader breader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		BufferedWriter bwriter = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));

		output = new Output(bwriter, outQueue);
		output.start();

		String nick = this.nick;
		Output.sendRawLine("NICK " + nick);
		Output.sendRawLine("USER " + this.nick + " 8 * :" + "0.1");

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
					nick = nick + tries;
					Output.sendRawLine("NICK " + nick);
				} else if (code.startsWith("5") || code.startsWith("4")) {
					socket.close();
					input = null;
				}
			}
			this.setNick(nick);

		}

		socket.setSoTimeout(5 * 60 * 1000);

		input.start();
	}

	public final synchronized void reconnect() throws IOException, Exception {
		connect(server);
	}

	public final void joinChannel(String channel) {
		Output.sendRawLine("JOIN " + channel);
	}

	public final void sendMessage(String target, String message) {
		outQueue.add("PRIVMSG " + target + " :" + message);
	}

	protected void handleLine(String line) {
		if (line.startsWith("PING ")) {
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

		if (command.equals("PRIVMSG") && "#&+!".indexOf(target.charAt(0)) >= 0) {
			// This is a normal message to a channel.
			this.onMessage(target, sourceNick, sourceLogin, sourceHostname,
					line.substring(line.indexOf(" :") + 2));
		}

	}

	protected void onServerPing(String response) {
		Output.sendRawLine("PONG " + response);
	}

	protected void onMessage(String channel, String sender, String login,
			String hostname, String message) {
	}

	protected void onDisconnect() {
	}

	public final void setNick(String nick) {
		this.nick = nick;
	}

	public final synchronized boolean isConnected() {
		return input != null && input.isConnected();
	}

	public synchronized void dispose() {
		output.interrupt();
		input.dispose();
	}
}
