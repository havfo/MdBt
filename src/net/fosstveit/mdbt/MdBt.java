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

import net.fosstveit.mdbt.utils.MdBtBrain;
import net.fosstveit.mdbt.utils.MdBtConnector;
import net.fosstveit.mdbt.utils.MdBtConstants;
import net.fosstveit.mdbt.utils.MdBtPlugin;
import net.fosstveit.mdbt.utils.OnlyJars;

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

			for (int i = 0; i < MdBtConstants.CHANNELS.length; i++) {
				joinChannel(MdBtConstants.CHANNELS[i]);
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
			message = message.replaceAll(MdBtConstants.BOTNAME, "meg");
			toMe = true;
		}

		if (shouldLearn) {
			brain.add(message);
		}

		if (toMe) {
			sendMessage(channel,
					shouldITalkToYou(sender) + brain.getSentence(message));
		}
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

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedReader(fir);

			// bis.available() returns 0 if the file does not have more lines.
			while ((line = bis.readLine()) != null) {
				brain.add(line);
			}

			// dispose all the resources after using them.
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
			for (String s : new File("plugins/").list(new OnlyJars("jar"))) {
				File file = new File("plugins/" + s);
				URL url = file.toURI().toURL();

				URL[] urls = new URL[] { url };
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
