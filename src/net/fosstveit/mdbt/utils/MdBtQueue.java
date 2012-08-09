package net.fosstveit.mdbt.utils;

import java.util.ArrayList;

public class MdBtQueue {

	private ArrayList<String> queue = new ArrayList<String>();

	public MdBtQueue() {
	}

	public void add(String s) {
		synchronized (queue) {
			queue.add(s);
			queue.notify();
		}
	}

	public void addFront(String s) {
		synchronized (queue) {
			queue.add(0, s);
			queue.notify();
		}
	}

	public String next() {

		String s = null;

		synchronized (queue) {
			if (queue.size() == 0) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					return null;
				}
			}

			try {
				s = queue.get(0);
				queue.remove(0);
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		}

		return s;
	}

	public boolean hasNext() {
		return (this.size() != 0);
	}

	public void clear() {
		synchronized (queue) {
			queue.clear();
		}
	}

	public int size() {
		return queue.size();
	}
}
