package net.fosstveit.mdbt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public abstract class MdBtConnector {

	private MdBtInput mdBtInput;
	private MdBtOutput mdBtOutput;
	private boolean isConnected = false;
	private String server;
	private int port = 6667;
	private MdBtQueue outQueue = new MdBtQueue();
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

		mdBtOutput = new MdBtOutput(bwriter, outQueue);
		mdBtOutput.start();

		String nick = this.nick;
		MdBtOutput.sendRawLine("NICK " + nick);
		MdBtOutput.sendRawLine("USER " + this.nick + " 8 * :" + "0.1");

		mdBtInput = new MdBtInput(this, breader);

		String line = null;
		int tries = 1;
		while ((line = breader.readLine()) != null) {

			this.handleLine(line);

			int firstSpace = line.indexOf(" ");
			int secondSpace = line.indexOf(" ", firstSpace + 1);
			if (secondSpace >= 0) {
				String code = line.substring(firstSpace + 1, secondSpace);

				if (code.equals("004")) {
					isConnected = true;
					break;
				} else if (code.equals("433")) {
					tries++;
					nick = nick + tries;
					MdBtOutput.sendRawLine("NICK " + nick);
				} else if (code.startsWith("5") || code.startsWith("4")) {
					socket.close();
					mdBtInput = null;
				}
			}
			this.setNick(nick);

		}

		socket.setSoTimeout(5 * 60 * 1000);

		mdBtInput.start();
	}

	public final synchronized void reconnect() throws IOException, Exception {
		connect(server);
	}

	public final void joinChannel(String channel) {
		MdBtOutput.sendRawLine("JOIN " + channel);
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

		StringTokenizer tokenizer = new StringTokenizer(line);
		String senderInfo = tokenizer.nextToken();
		String command = tokenizer.nextToken();
		String target = null;

		int exclamation = senderInfo.indexOf("!");
		int at = senderInfo.indexOf("@");
		if (senderInfo.startsWith(":")) {
			if (exclamation > 0 && at > 0 && exclamation < at) {
				sourceNick = senderInfo.substring(1, exclamation);
			} else {

				if (tokenizer.hasMoreTokens()) {
					String token = command;

					int code = -1;
					try {
						code = Integer.parseInt(token);
					} catch (NumberFormatException e) {
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
			this.onMessage(target, sourceNick,
					line.substring(line.indexOf(" :") + 2));
		}

	}

	protected void onServerPing(String response) {
		MdBtOutput.sendRawLine("PONG " + response);
	}

	protected void onMessage(String channel, String sender, String message) {
	}

	protected void onDisconnect() {
		isConnected = false;
	}

	public final void setNick(String nick) {
		this.nick = nick;
	}

	public final synchronized boolean isConnected() {
		return mdBtInput != null && isConnected;
	}

	public synchronized void dispose() {
		mdBtOutput.interrupt();
		mdBtInput.interrupt();
	}
}
