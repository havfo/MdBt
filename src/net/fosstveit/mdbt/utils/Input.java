package net.fosstveit.mdbt.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InterruptedIOException;
import java.net.Socket;

public class Input extends Thread {

	private MdBtConnector bot;
	private Socket socket;
	private BufferedReader breader;
	private boolean isConnected;
	private boolean disposed;

	public Input(MdBtConnector bot, Socket socket, BufferedReader breader,
			BufferedWriter bwriter) {
		this.bot = bot;
		this.socket = socket;
		this.breader = breader;
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
					Output.sendRawLine("PING "
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
