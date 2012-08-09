package net.fosstveit.mdbt.utils;

import java.io.BufferedReader;
import java.io.InterruptedIOException;

public class MdBtInput extends Thread {

	private MdBtConnector bot;
	private BufferedReader breader;

	public MdBtInput(MdBtConnector bot, BufferedReader breader) {
		this.bot = bot;
		this.breader = breader;
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
					MdBtOutput.sendRawLine("PING "
							+ (System.currentTimeMillis() / 1000));
				}
			}
		} catch (Exception e) {
		}
	}
}
