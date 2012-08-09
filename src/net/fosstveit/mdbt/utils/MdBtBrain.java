package net.fosstveit.mdbt.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class MdBtBrain implements Serializable {

	private static final long serialVersionUID = -917213639484389474L;
	private HashMap<String, HashSet<MdBtQuad>> words = new HashMap<String, HashSet<MdBtQuad>>();
	private HashMap<MdBtQuad, MdBtQuad> mdBtQuads = new HashMap<MdBtQuad, MdBtQuad>();
	private HashMap<MdBtQuad, HashSet<String>> next = new HashMap<MdBtQuad, HashSet<String>>();
	private HashMap<MdBtQuad, HashSet<String>> previous = new HashMap<MdBtQuad, HashSet<String>>();
	private Random rand = new Random();

	public MdBtBrain() {
	}

	public void add(String sentence) {
		sentence = sentence.trim();
		String[] parts = sentence.split("(?!^)\\b");

		if (parts.length >= 4) {
			for (int i = 0; i < parts.length - 3; i++) {
				MdBtQuad mdBtQuad = new MdBtQuad((String) parts[i], (String) parts[i + 1],
						(String) parts[i + 2], (String) parts[i + 3]);
				if (mdBtQuads.containsKey(mdBtQuad)) {
					mdBtQuad = (MdBtQuad) mdBtQuads.get(mdBtQuad);
				} else {
					mdBtQuads.put(mdBtQuad, mdBtQuad);
				}

				if (i == 0) {
					mdBtQuad.setCanStart(true);
				}

				if (i == parts.length - 4) {
					mdBtQuad.setCanEnd(true);
				}

				for (int n = 0; n < 4; n++) {
					String token = (String) parts[i + n];
					if (!words.containsKey(token)) {
						words.put(token, new HashSet<MdBtQuad>(1));
					}
					HashSet<MdBtQuad> set = (HashSet<MdBtQuad>) words.get(token);
					set.add(mdBtQuad);
				}

				if (i > 0) {
					String previousToken = (String) parts[i - 1];
					if (!previous.containsKey(mdBtQuad)) {
						previous.put(mdBtQuad, new HashSet<String>(1));
					}
					HashSet<String> set = (HashSet<String>) previous.get(mdBtQuad);
					set.add(previousToken);
				}

				if (i < parts.length - 4) {
					String nextToken = (String) parts[i + 4];
					if (!next.containsKey(mdBtQuad)) {
						next.put(mdBtQuad, new HashSet<String>(1));
					}
					HashSet<String> set = (HashSet<String>) next.get(mdBtQuad);
					set.add(nextToken);
				}

			}
		}
	}

	public String getSentence() {
		return getSentence(null);
	}

	public String getSentence(String word) {
		LinkedList<String> parts = new LinkedList<String>();

		MdBtQuad[] quads;
		if (words.containsKey(word)) {
			quads = (MdBtQuad[]) ((HashSet<MdBtQuad>) words.get(word))
					.toArray(new MdBtQuad[0]);
		} else {
			quads = (MdBtQuad[]) this.mdBtQuads.keySet().toArray(new MdBtQuad[0]);
		}

		if (quads.length == 0) {
			return "";
		}

		MdBtQuad middleQuad = quads[rand.nextInt(quads.length)];
		MdBtQuad mdBtQuad = middleQuad;

		for (int i = 0; i < 4; i++) {
			parts.add(mdBtQuad.getToken(i));
		}

		while (mdBtQuad.canEnd() == false) {
			String[] nextTokens = (String[]) ((HashSet<String>) next.get(mdBtQuad))
					.toArray(new String[0]);
			String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
			mdBtQuad = (MdBtQuad) this.mdBtQuads.get(new MdBtQuad(mdBtQuad.getToken(1), mdBtQuad
					.getToken(2), mdBtQuad.getToken(3), nextToken));
			parts.add(nextToken);
		}

		mdBtQuad = middleQuad;
		while (mdBtQuad.canStart() == false) {
			String[] previousTokens = (String[]) ((HashSet<String>) previous
					.get(mdBtQuad)).toArray(new String[0]);
			String previousToken = previousTokens[rand
					.nextInt(previousTokens.length)];
			mdBtQuad = (MdBtQuad) this.mdBtQuads.get(new MdBtQuad(previousToken, mdBtQuad
					.getToken(0), mdBtQuad.getToken(1), mdBtQuad.getToken(2)));
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