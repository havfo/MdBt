package net.fosstveit.mdbt.utils;

import java.util.regex.Pattern;

import net.fosstveit.mdbt.MdBt;

public abstract class MdBtPlugin {

	private Pattern triggerRegex;

	public MdBtPlugin() {
	}

	public Pattern getTriggerRegex() {
		return triggerRegex;
	}

	public void setTriggerRegex(String triggerRegex) {
		this.triggerRegex = Pattern.compile(triggerRegex);
	}

	public synchronized void sendMessage(String channel, String message) {
		MdBt.getInstance().sendMessage(channel, message);
	}

	public abstract String onMessage(String channel, String sender,
			String message);

	public abstract void cleanup();

}
