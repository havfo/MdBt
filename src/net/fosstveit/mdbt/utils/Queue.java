package net.fosstveit.mdbt.utils;

import java.util.ArrayList;

public class Queue {

	private ArrayList<Object> queue = new ArrayList<Object>();

	public Queue() {

	}

	public void add(Object o) {
		synchronized (queue) {
			queue.add(o);
			queue.notify();
		}
	}

	public void addFront(Object o) {
		synchronized (queue) {
			queue.add(0, o);
			queue.notify();
		}
	}

	public Object next() {

		Object o = null;

		// Block if the Queue is empty.
		synchronized (queue) {
			if (queue.size() == 0) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					return null;
				}
			}

			// Return the Object.
			try {
				o = queue.get(0);
				queue.remove(0);
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new InternalError("Race hazard in Queue object.");
			}
		}

		return o;
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
