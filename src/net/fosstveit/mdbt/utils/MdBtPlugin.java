package net.fosstveit.mdbt.utils;

import java.util.regex.Pattern;

import net.fosstveit.mdbt.MdBt;

public abstract class MdBtPlugin {

	private Pattern triggerRegex;
	
	private MdBt mdbt;
	
	public MdBtPlugin() {
	}
	
	public void setMdBT(MdBt mdbt) {
		this.mdbt = mdbt;
	}
	
	public MdBt getMdBt() {
		return mdbt;
	}

	public Pattern getTriggerRegex() {
		return triggerRegex;
	}
	
	public void setTriggerRegex(String triggerRegex) {
		this.triggerRegex = Pattern.compile(triggerRegex);
	}
	
	public synchronized void sendMessage(String channel, String message) {
		mdbt.sendMessage(channel, message);
	}

	public abstract String onMessage(String channel, String sender, String message);

}
