package net.fosstveit.mdbt.utils;

import java.io.File;
import java.io.FilenameFilter;

public class OnlyJars implements FilenameFilter {

	String ext;

	public OnlyJars(String ext) {
		this.ext = "." + ext;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(ext);
	}

}
