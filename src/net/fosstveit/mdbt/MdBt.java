package net.fosstveit.mdbt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

import net.fosstveit.mdbt.utils.MdBtBrain;
import net.fosstveit.mdbt.utils.MdBtConnector;
import net.fosstveit.mdbt.utils.MdBtConstants;
import net.fosstveit.mdbt.utils.MdBtPlugin;

public class MdBt extends MdBtConnector {

	private MdBtBrain brain = new MdBtBrain();

	private HashMap<Pattern, MdBtPlugin> plugins = new HashMap<Pattern, MdBtPlugin>();

	private ArrayList<String> pluginList = new ArrayList<String>();

	public MdBt() {
		try {
			loadNewPlugins();
			trainBot();
			setName(MdBtConstants.BOTNAME);
			connect(MdBtConstants.HOST);

			for (int i = 0; i < MdBtConstants.CHANNELS.length; i++) {
				joinChannel(MdBtConstants.CHANNELS[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {

		String filter = MdBtConstants.BOTNAME + ": ";

		boolean toMe = false;
		boolean shouldLearn = !sender.equals("habbes")
				&& !sender.equals("mlvn");

		if (message.startsWith(filter)) {
			toMe = true;
			message = message.replaceFirst(filter, "");
		}

		if (message.equals("load plugins")) {
			loadNewPlugins();

			String ret = "";

			for (Pattern s : plugins.keySet()) {
				ret += s.pattern() + " ";
			}

			sendMessage(channel, "Plugins loaded: " + ret);
			return;
		}
		
		MdBtPlugin plug = null;
		boolean hasPluginMatch = false;
		
		for (Pattern p : plugins.keySet()) {
			if (p.matcher(message).matches()) {
				plug = plugins.get(p);
				hasPluginMatch = true;
				break;
			}
		}

		if (toMe && hasPluginMatch) {
			sendMessage(
					channel,
					plug.onMessage(channel,
							sender, message));
		} else {
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

	// private void loadPlugins() {
	// try {
	//
	// for (String plug : MdBtConstants.PLUGINS) {
	// MdBtPlugin p = (MdBtPlugin) (Class.forName(plug).newInstance());
	// p.setMdBT(this);
	//
	// plugins.put(p.getTriggerWord(), p);
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	private synchronized void loadNewPlugins() {
		try {
			for (String s : new File("plugins/").list()) {
				File file = new File("plugins/" + s);
				URL url;

				url = file.toURI().toURL();

				URL[] urls = new URL[] { url };
				ClassLoader cl = new URLClassLoader(urls);

				if (!pluginList.contains("plugins."
						+ s.substring(0, s.length() - 4))) {

					MdBtPlugin p = (MdBtPlugin) cl.loadClass(
							"plugins."
									+ s.substring(0, s.length() - 4))
							.newInstance();
					plugins.put(p.getTriggerRegex(), p);
					pluginList.add("plugins."
									+ s.substring(0, s.length() - 4));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new MdBt();
	}

}
