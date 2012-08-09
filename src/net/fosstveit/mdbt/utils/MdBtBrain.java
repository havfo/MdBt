package net.fosstveit.mdbt.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MdBtBrain implements Serializable {

	private static final long serialVersionUID = -917213639484389474L;
	private HashMap<String, ArrayList<MdBtQuad>> words = new HashMap<String, ArrayList<MdBtQuad>>();
	private ArrayList<MdBtQuad> mdBtQuads = new ArrayList<MdBtQuad>();
	private HashMap<MdBtQuad, ArrayList<String>> next = new HashMap<MdBtQuad, ArrayList<String>>();
	private HashMap<MdBtQuad, ArrayList<String>> previous = new HashMap<MdBtQuad, ArrayList<String>>();
	private Random rand = new Random();

	public MdBtBrain() {
	}

	public void add(String sentence) {
		sentence = sentence.trim();
		String[] parts = sentence.split("(?!^)\\b");

		if (parts.length >= 4) {
			for (int i = 0; i < parts.length - 3; i++) {
				MdBtQuad mdBtQuad = new MdBtQuad((String) parts[i],
						(String) parts[i + 1], (String) parts[i + 2],
						(String) parts[i + 3]);
				if (mdBtQuads.contains(mdBtQuad)) {
					mdBtQuad = mdBtQuads.get(mdBtQuads.indexOf(mdBtQuad));
				} else {
					mdBtQuads.add(mdBtQuad);
				}

				if (i == 0) {
					mdBtQuad.setCanStart(true);
				}

				if (i == parts.length - 4) {
					mdBtQuad.setCanEnd(true);
				}

				for (int n = 0; n < 4; n++) {
					String token = parts[i + n];
					if (!words.containsKey(token)) {
						words.put(token, new ArrayList<MdBtQuad>(1));
					}

					words.get(token).add(mdBtQuad);
				}

				if (i > 0) {
					String previousToken = parts[i - 1];
					if (!previous.containsKey(mdBtQuad)) {
						previous.put(mdBtQuad, new ArrayList<String>(1));
					}
					previous.get(mdBtQuad).add(previousToken);
				}

				if (i < parts.length - 4) {
					String nextToken = parts[i + 4];
					if (!next.containsKey(mdBtQuad)) {
						next.put(mdBtQuad, new ArrayList<String>(1));
					}
					next.get(mdBtQuad).add(nextToken);
				}

			}
		}
	}

	public String getSentence() {
		return getSentence(null);
	}

	public String getSentence(String word) {
		ArrayList<String> parts = new ArrayList<String>();

		MdBtQuad[] quads;
		if (words.containsKey(word)) {
			quads = words.get(word).toArray(new MdBtQuad[0]);
		} else {
			quads = mdBtQuads.toArray(new MdBtQuad[0]);
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
			String[] nextTokens = next.get(mdBtQuad).toArray(new String[0]);
			String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
			mdBtQuad = mdBtQuads.get(mdBtQuads.indexOf(new MdBtQuad(mdBtQuad
					.getToken(1), mdBtQuad.getToken(2), mdBtQuad.getToken(3),
					nextToken)));
			parts.add(nextToken);
		}

		mdBtQuad = middleQuad;
		while (mdBtQuad.canStart() == false) {
			String[] previousTokens = previous.get(mdBtQuad).toArray(
					new String[0]);
			String previousToken = previousTokens[rand
					.nextInt(previousTokens.length)];
			mdBtQuad = mdBtQuads.get(mdBtQuads.indexOf(new MdBtQuad(
					previousToken, mdBtQuad.getToken(0), mdBtQuad.getToken(1),
					mdBtQuad.getToken(2))));
			parts.add(0, previousToken);
		}

		StringBuffer sentence = new StringBuffer();
		for (String s : parts) {
			sentence.append(s);
		}

		return sentence.toString();
	}

}