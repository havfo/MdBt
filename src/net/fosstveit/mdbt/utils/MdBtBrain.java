package net.fosstveit.mdbt.utils;

import java.util.*;
import java.io.*;

public class MdBtBrain implements Serializable {

	private static final long serialVersionUID = -917213639484389474L;
	// These are valid chars for words. Anything else is treated as punctuation.
	public static final String WORD_CHARS = "abcdefghijklmnopqrstuvwxyzæøåλ$@/\\|^_-*"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ" + "0123456789";
	public static final String END_CHARS = ".!?";

	// This maps a single word to a HashSet of all the Quads it is in.
	private HashMap<String, HashSet<Quad>> words = new HashMap<String, HashSet<Quad>>();

	// A self-referential HashMap of Quads.
	private HashMap<Quad, Quad> quads = new HashMap<Quad, Quad>();

	// This maps a Quad onto a Set of Strings that may come next.
	private HashMap<Quad, HashSet<?>> next = new HashMap<Quad, HashSet<?>>();

	// This maps a Quad onto a Set of Strings that may come before it.
	private HashMap<Quad, HashSet<?>> previous = new HashMap<Quad, HashSet<?>>();

	private Random rand = new Random();

	public MdBtBrain() {

	}

	public void add(String sentence) {
		sentence = sentence.trim();
		ArrayList<String> parts = new ArrayList<String>();
		char[] chars = sentence.toCharArray();
		int i = 0;
		boolean punctuation = false;
		StringBuffer buffer = new StringBuffer();
		while (i < chars.length) {
			char ch = chars[i];
			if ((WORD_CHARS.indexOf(ch) >= 0) == punctuation) {
				punctuation = !punctuation;
				String token = buffer.toString();
				if (token.length() > 0) {
					parts.add(token);
				}
				buffer = new StringBuffer();
				continue;
			}
			buffer.append(ch);
			i++;
		}
		
		String lastToken = buffer.toString();
		if (lastToken.length() > 0) {
			parts.add(lastToken);
		}

		if (parts.size() >= 4) {
			for (i = 0; i < parts.size() - 3; i++) {
				Quad quad = new Quad((String) parts.get(i),
						(String) parts.get(i + 1), (String) parts.get(i + 2),
						(String) parts.get(i + 3));
				if (quads.containsKey(quad)) {
					quad = (Quad) quads.get(quad);
				} else {
					quads.put(quad, quad);
				}

				if (i == 0) {
					quad.setCanStart(true);
				}
				// else if (i == parts.size() - 4) {
				if (i == parts.size() - 4) {
					quad.setCanEnd(true);
				}

				for (int n = 0; n < 4; n++) {
					String token = (String) parts.get(i + n);
					if (!words.containsKey(token)) {
						words.put(token, new HashSet<Quad>(1));
					}
					HashSet<Quad> set = (HashSet<Quad>) words.get(token);
					set.add(quad);
				}

				if (i > 0) {
					String previousToken = (String) parts.get(i - 1);
					if (!previous.containsKey(quad)) {
						previous.put(quad, new HashSet<Object>(1));
					}
					HashSet<String> set = (HashSet<String>) previous.get(quad);
					set.add(previousToken);
				}

				if (i < parts.size() - 4) {
					String nextToken = (String) parts.get(i + 4);
					if (!next.containsKey(quad)) {
						next.put(quad, new HashSet<Object>(1));
					}
					HashSet<String> set = (HashSet<String>) next.get(quad);
					set.add(nextToken);
				}

			}
		} else {
			// Didn't learn anything.
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
			quads = (Quad[]) ((HashSet<?>) words.get(word)).toArray(new Quad[0]);
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
			String[] nextTokens = (String[]) ((HashSet<?>) next.get(quad))
					.toArray(new String[0]);
			String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
			quad = (Quad) this.quads.get(new Quad(quad.getToken(1), quad
					.getToken(2), quad.getToken(3), nextToken));
			parts.add(nextToken);
		}

		quad = middleQuad;
		while (quad.canStart() == false) {
			String[] previousTokens = (String[]) ((HashSet<?>) previous.get(quad))
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