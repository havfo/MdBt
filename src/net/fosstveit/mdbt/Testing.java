package net.fosstveit.mdbt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Testing {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			File file = new File("bot.txt");
			FileReader fir = new FileReader(file);
			;
			BufferedReader bis = new BufferedReader(fir);

			BufferedWriter out = new BufferedWriter(new FileWriter("bot2.txt"));

			String line;

			while ((line = bis.readLine()) != null) {
				line = line.trim();
				String[] parts = line.split("(?!^)\\b");

				if (parts.length >= 4) {
					out.write(line.toLowerCase());
					out.newLine();
				}
			}

			fir.close();
			bis.close();
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
