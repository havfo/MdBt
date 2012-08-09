package net.fosstveit.mdbt.utils;

import java.io.File;
import java.io.FilenameFilter;

public class MdBtFileFilter implements FilenameFilter {

	String ext;

	public MdBtFileFilter(String ext) {
		this.ext = "." + ext;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(ext);
	}

}
