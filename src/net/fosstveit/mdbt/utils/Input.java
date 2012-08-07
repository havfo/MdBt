package net.fosstveit.mdbt.utils;

import java.io.*;
import java.net.*;

public class Input extends Thread {

	private MdBtConnector bot = null;
	private Socket socket = null;
	private BufferedReader breader = null;
	private BufferedWriter bwriter = null;
	private boolean isConnected = true;
	private boolean disposed = false;

	public static final int MAX_LINE_LENGTH = 512;

	public Input(MdBtConnector bot, Socket socket, BufferedReader breader,
			BufferedWriter bwriter) {
		this.bot = bot;
		this.socket = socket;
		this.breader = breader;
		this.bwriter = bwriter;
		this.setName(this.getClass() + "-Thread");
	}

	void sendRawLine(String line) {
		Output.sendRawLine(bot, bwriter, line);
	}

	boolean isConnected() {
		return isConnected;
	}

	public void run() {
		try {
			boolean running = true;
			while (running) {
				try {
					String line = null;
					while ((line = breader.readLine()) != null) {
						try {
							bot.handleLine(line);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
					if (line == null) {
						running = false;
					}
				} catch (InterruptedIOException iioe) {
					this.sendRawLine("PING "
							+ (System.currentTimeMillis() / 1000));
				}
			}
		} catch (Exception e) {
			// Do nothing.
		}

		try {
			socket.close();
		} catch (Exception e) {
			// Just assume the socket was already closed.
		}

		if (!disposed) {
			isConnected = false;
		}

	}

	public void dispose() {
		try {
			disposed = true;
			socket.close();
		} catch (Exception e) {
			// Do nothing.
		}
	}

}
