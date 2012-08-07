package net.fosstveit.mdbt.utils;

import java.io.*;

public class Output extends Thread {

	private MdBtConnector bot = null;
	private Queue outQueue = null;

	public Output(MdBtConnector bot, Queue outQueue) {
		this.bot = bot;
		this.outQueue = outQueue;
		this.setName(this.getClass() + "-Thread");
	}

	static void sendRawLine(MdBtConnector bot, BufferedWriter bwriter, String line) {
		if (line.length() > bot.getMaxLineLength() - 2) {
			line = line.substring(0, bot.getMaxLineLength() - 2);
		}
		synchronized (bwriter) {
			try {
				bwriter.write(line + "\r\n");
				bwriter.flush();
			} catch (Exception e) {
				// Silent response - just lose the line.
			}
		}
	}

	public void run() {
		try {
			boolean running = true;
			while (running) {
				// Small delay to prevent spamming of the channel
				Thread.sleep(bot.getMessageDelay());

				String line = (String) outQueue.next();
				if (line != null) {
					bot.sendRawLine(line);
				} else {
					running = false;
				}
			}
		} catch (InterruptedException e) {
			// Just let the method return naturally...
		}
	}

}
