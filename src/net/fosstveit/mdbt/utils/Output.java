package net.fosstveit.mdbt.utils;

import java.io.BufferedWriter;

public class Output extends Thread {

	private Queue outQueue;
	private static BufferedWriter bwriter;

	public Output(BufferedWriter bwriter, Queue outQueue) {
		Output.bwriter = bwriter;
		this.outQueue = outQueue;
	}

	static void sendRawLine(String line) {
		if (line.length() > 510) {
			line = line.substring(0, 510);
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
				Thread.sleep(1000);

				String line = (String) outQueue.next();
				if (line != null) {
					sendRawLine(line);
				} else {
					running = false;
				}
			}
		} catch (InterruptedException e) {
			// Just let the method return naturally...
		}
	}

}
