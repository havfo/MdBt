package net.fosstveit.mdbt.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class MdBtBrain implements Serializable {

	private static final long serialVersionUID = -917213639484389474L;
	private HashMap<String, HashSet<Quad>> words = new HashMap<String, HashSet<Quad>>();
	private HashMap<Quad, Quad> quads = new HashMap<Quad, Quad>();
	private HashMap<Quad, HashSet<String>> next = new HashMap<Quad, HashSet<String>>();
	private HashMap<Quad, HashSet<String>> previous = new HashMap<Quad, HashSet<String>>();
	private Random rand = new Random();

	public MdBtBrain() {

	}

	public void add(String sentence) {
		sentence = sentence.trim();
		String[] parts = sentence.split("(?!^)\\b");

		if (parts.length >= 4) {
			for (int i = 0; i < parts.length - 3; i++) {
				Quad quad = new Quad((String) parts[i],
						(String) parts[i + 1], (String) parts[i + 2],
						(String) parts[i + 3]);
				if (quads.containsKey(quad)) {
					quad = (Quad) quads.get(quad);
				} else {
					quads.put(quad, quad);
				}

				if (i == 0) {
					quad.setCanStart(true);
				}
				
				if (i == parts.length - 4) {
					quad.setCanEnd(true);
				}

				for (int n = 0; n < 4; n++) {
					String token = (String) parts[i + n];
					if (!words.containsKey(token)) {
						words.put(token, new HashSet<Quad>(1));
					}
					HashSet<Quad> set = (HashSet<Quad>) words.get(token);
					set.add(quad);
				}

				if (i > 0) {
					String previousToken = (String) parts[i - 1];
					if (!previous.containsKey(quad)) {
						previous.put(quad, new HashSet<String>(1));
					}
					HashSet<String> set = (HashSet<String>) previous.get(quad);
					set.add(previousToken);
				}

				if (i < parts.length - 4) {
					String nextToken = (String) parts[i + 4];
					if (!next.containsKey(quad)) {
						next.put(quad, new HashSet<String>(1));
					}
					HashSet<String> set = (HashSet<String>) next.get(quad);
					set.add(nextToken);
				}

			}
		}
	}

	/**
	 * Generate a random sentence from the brain.
	 */
	public String getSentence() {
		return getSentence(null);
	}

	/**
	 * Generate a sentence that includes (if possible) the specified word.
	 */
	public String getSentence(String word) {
		LinkedList<String> parts = new LinkedList<String>();

		Quad[] quads;
		if (words.containsKey(word)) {
			quads = (Quad[]) ((HashSet<Quad>) words.get(word)).toArray(new Quad[0]);
		} else {
			quads = (Quad[]) this.quads.keySet().toArray(new Quad[0]);
		}

		if (quads.length == 0) {
			return "";
		}

		Quad middleQuad = quads[rand.nextInt(quads.length)];
		Quad quad = middleQuad;

		for (int i = 0; i < 4; i++) {
			parts.add(quad.getToken(i));
		}

		while (quad.canEnd() == false) {
			String[] nextTokens = (String[]) ((HashSet<String>) next.get(quad))
					.toArray(new String[0]);
			String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
			quad = (Quad) this.quads.get(new Quad(quad.getToken(1), quad
					.getToken(2), quad.getToken(3), nextToken));
			parts.add(nextToken);
		}

		quad = middleQuad;
		while (quad.canStart() == false) {
			String[] previousTokens = (String[]) ((HashSet<String>) previous.get(quad))
					.toArray(new String[0]);
			String previousToken = previousTokens[rand
					.nextInt(previousTokens.length)];
			quad = (Quad) this.quads.get(new Quad(previousToken, quad
					.getToken(0), quad.getToken(1), quad.getToken(2)));
			parts.addFirst(previousToken);
		}

		StringBuffer sentence = new StringBuffer();
		Iterator<String> it = parts.iterator();
		while (it.hasNext()) {
			String token = (String) it.next();
			sentence.append(token);
		}

		return sentence.toString();
	}

}