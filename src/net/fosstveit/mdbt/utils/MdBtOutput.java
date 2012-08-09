package net.fosstveit.mdbt.utils;

import java.io.BufferedWriter;

public class MdBtOutput extends Thread {

	private MdBtQueue outQueue;
	private static BufferedWriter bwriter;

	public MdBtOutput(BufferedWriter bwriter, MdBtQueue outQueue) {
		MdBtOutput.bwriter = bwriter;
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
			}
		}
	}

	public void run() {
		try {
			boolean running = true;
			while (running) {
				Thread.sleep(1000);
				String line = outQueue.next();
				
				if (line != null) {
					sendRawLine(line);
				} else {
					running = false;
				}
			}
		} catch (InterruptedException e) {
		}
	}

}
