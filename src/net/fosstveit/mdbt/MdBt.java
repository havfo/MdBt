package net.fosstveit.mdbt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import net.fosstveit.mdbt.utils.MdBtBrain;
import net.fosstveit.mdbt.utils.MdBtConnector;
import net.fosstveit.mdbt.utils.MdBtConstants;
import net.fosstveit.mdbt.utils.MdBtFileFilter;
import net.fosstveit.mdbt.utils.MdBtPlugin;

public class MdBt extends MdBtConnector {

	private static final MdBt INSTANCE = new MdBt();
	private MdBtBrain brain = new MdBtBrain();
	private HashMap<String, MdBtPlugin> plugins = new HashMap<String, MdBtPlugin>();

	public MdBt() {
		try {
			loadPlugins();
			trainBot();
			setNick(MdBtConstants.BOTNAME);
			connect(MdBtConstants.HOST);

			for (String s : MdBtConstants.CHANNELS) {
				joinChannel(s);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MdBt getInstance() {
		return INSTANCE;
	}

	@Override
	public void onMessage(String channel, String sender, String message) {

		String filter = MdBtConstants.BOTNAME + ": ";

		boolean toMe = false;
		boolean shouldLearn = !sender.equals("habbes")
				&& !sender.equals("mlvn");

		if (message.startsWith(filter)) {
			toMe = true;
			message = message.replaceFirst(filter, "");
		}

		if (message.equals("reload plugins")) {
			String ret = reloadPlugins();

			sendMessage(channel, "Plugins loaded: " + ret);
			return;
		}

		for (MdBtPlugin p : plugins.values()) {
			if (p.getTriggerRegex().matcher(message).matches()) {
				sendMessage(channel, p.onMessage(channel, sender, message));
				return;
			}
		}

		if (message.indexOf(MdBtConstants.BOTNAME) != -1) {
			// Replace botname with "meg"
			message = message.replaceAll(MdBtConstants.BOTNAME, "meg");
			toMe = true;
		}

		if (shouldLearn) {
			brain.add(message);
		}

		if (toMe) {
			sendMessage(channel,
					shouldITalkToYou(sender) + brain.getSentence(findWordOfInterest(message)));
		}
	}

	private String findWordOfInterest(String sentence) {
		StringTokenizer st = new StringTokenizer(sentence,
				" \t\n\r\f!;:'\",.?");
		String longestWord = "";
		while (st.hasMoreTokens()) {
			String currentWord = st.nextToken();

			if (currentWord.length() > longestWord.length()) {
				longestWord = currentWord;
			}
		}

		return longestWord;
	}

	private String shouldITalkToYou(String you) {
		Random r = new Random();

		if (r.nextDouble() < 0.5) {
			return you + ": ";
		} else {
			return "";
		}
	}

	@Override
	public void onDisconnect() {
		while (!isConnected()) {
			try {
				reconnect();
			} catch (Exception e) {
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void trainBot() {
		File file = new File("bot.txt");
		FileReader fir = null;
		BufferedReader bis = null;

		String line = null;

		try {
			fir = new FileReader(file);
			bis = new BufferedReader(fir);
			
			while ((line = bis.readLine()) != null) {
				 brain.add(line);
			}

			fir.close();
			bis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String reloadPlugins() {
		for (MdBtPlugin p : plugins.values()) {
			p.cleanup();
		}

		plugins.clear();

		loadPlugins();

		String ret = "";

		for (MdBtPlugin p : plugins.values()) {
			ret += p.getTriggerRegex().pattern() + " ";
		}

		return ret;
	}

	private synchronized void loadPlugins() {
		try {
			for (String s : new File("plugins/")
					.list(new MdBtFileFilter("jar"))) {
				File file = new File("plugins/" + s);
				URL[] urls = new URL[] { file.toURI().toURL() };
				ClassLoader cl = new URLClassLoader(urls);

				if (!plugins.containsKey("plugins."
						+ s.substring(0, s.length() - 4))) {

					MdBtPlugin p = (MdBtPlugin) cl.loadClass(
							"plugins." + s.substring(0, s.length() - 4))
							.newInstance();
					plugins.put("plugins." + s.substring(0, s.length() - 4), p);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

	}
}
