
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;
import java.util.Stack;

public class ViterbiTagger {

	private static Map<String, Map<String, Double>> transition;
	private static Map<String, Map<String, Double>> emission;
	private static final String start = "start";

	public static void BuildMap(String sentence) {
		String[] tokens = sentence.split("\\n");
		for (int i = 0; i < tokens.length; i++) {
			String word = tokens[i].split("\\t")[0];
			String tag = tokens[i].split("\\t")[1];
			// String nextTag = tokens[i+1].split("\\t")[1];
			// build up emission hashmap
			if (emission.containsKey(tag)) {
				if (emission.get(tag).containsKey(word)) {
					double count = emission.get(tag).get(word) + 1;
					emission.get(tag).put(word, count);
				} else {
					emission.get(tag).put(word, 1.0);
				}
			} else {
				Map<String, Double> wordcount = new HashMap<String, Double>();
				wordcount.put(word, 1.0);
				emission.put(tag, wordcount);
			}

			// build up transition hashmap
			if (i < tokens.length - 1) {
				// check if start of sentence
				if (i == 0) {
					if (transition.get(start).containsKey(tag)) {
						double count = transition.get(start).get(tag) + 1;
						transition.get(start).put(tag, count);
					} else {
						transition.get(start).put(tag, 1.0);
					}
				}

				if (transition.containsKey(tag)) {
					if (transition.get(tag).containsKey(tokens[i + 1].split("\\t")[1])) {
						double count = transition.get(tag).get(tokens[i + 1].split("\\t")[1]) + 1;
						transition.get(tag).put(tokens[i + 1].split("\\t")[1], count);
					} else {
						transition.get(tag).put(tokens[i + 1].split("\\t")[1], 1.0);
					}
				} else {
					Map<String, Double> tag_count = new HashMap<String, Double>();
					tag_count.put(tokens[i + 1].split("\\t")[1], 1.0);
					transition.put(tag, tag_count);
				}
			}
		}
	}

	public static void ReadTestFile(String filename) {
		BufferedReader input = null;
		BufferedWriter output = null;
		String line = "";
		String tagline = "";
		String word = "";

		try {
			input = new BufferedReader(new FileReader(filename));
			output = new BufferedWriter(new FileWriter("Viterbi.pos"));

			while ((word = input.readLine()) != null) {
				if (!word.isEmpty()) {
					line = line + word + " ";
				} else {

					tagline = Viterbi(line);
					String[] words = line.split(" ");
					String[] tags = tagline.split(" ");

					// known words algo
					for (int i = 0; i < words.length; i++) {
						if (!(emission.get(tags[i]).containsKey(words[i]))) {

							if (words[i].length() >= 3) {
								if (i == 0)
									tags[i] = UnknownRules("", words[i], tags[i]);
								else
									tags[i] = UnknownRules(words[i - 1], words[i], tags[i]);
							}
						}
					}

					// output
					for (int i = 0; i < words.length; i++) {
						output.write(words[i] + "\t" + tags[i] + "\n");
					}

					output.write("\n");
					line = "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			input.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String Viterbi(String input) {
		String outputString = "";
		String endTag = "";
		final double unknown = -10.0;
		double max_prob = -1000;

		// record V path
		Stack<String> path = new Stack<String>();
		List<Map<String, String>> backtrace = new ArrayList<Map<String, String>>();
		Set<String> preState = new HashSet<>();
		Map<String, Double> preProb = new HashMap<String, Double>();

		preState.add(start);
		preProb.put(start, 0.0);

		String[] words = input.split(" ");

		for (int i = 0; i < words.length; i++) {
			Set<String> nextState = new HashSet<>();
			Map<String, Double> nextProb = new HashMap<String, Double>();
			Map<String, String> backTags = new HashMap<String, String>();
			double probability;

			for (String state : preState) {
				if (transition.containsKey(state) && !transition.get(state).isEmpty()) {
					for (String st : transition.get(state).keySet()) {
						nextState.add(st);

						if (emission.containsKey(st) && emission.get(st).containsKey(words[i])) {

							probability = preProb.get(state) + transition.get(state).get(st)
							    + emission.get(st).get(words[i]);
						} else {

							probability = preProb.get(state) + transition.get(state).get(st) + unknown;
						}

						if (!nextProb.containsKey(st) || probability > nextProb.get(st)) {
							nextProb.put(st, probability);
							backTags.put(st, state);

							if (backtrace.size() > i)
								backtrace.remove(i);

							backtrace.add(backTags);
						}
					}
				}
			}

			preProb = nextProb;
			preState = nextState;
		}

		for (String prob : preProb.keySet()) {
			if (max_prob < preProb.get(prob)) {
				max_prob = preProb.get(prob);
				endTag = prob;
			}
		}

		path.push(endTag);

		for (int i = words.length - 1; i > 0; i--) {
			path.push(backtrace.get(i).get(path.peek()));
		}

		while (!path.isEmpty()) {
			String token;
			token = path.pop();

			if (outputString == null)
				outputString = (token + " ");
			else
				outputString += (token + " ");
		}

		return outputString;
	}

	// treat unknown words
	public static String UnknownRules(String pre, String word, String tag) {
		int length = word.length();

		if (Character.isUpperCase(word.charAt(0))) {
			tag = "NNP";
		} else if (pre.equals("be")) {
			tag = "JJ";
		} else if (pre.equals("it")) {
			tag = "VBZ";
		} else if (pre.equals("would")) {
			tag = "VB";
		} else if (word.substring(length - 2, length).equals("ss")) {
			tag = "NN";
		} else if (word.substring(length - 1, length).equals("s")) {
			tag = "NNS";
		} else if (word.contains("-")) {
			tag = "JJ";
		} else if (word.contains(".")) {
			tag = "CD";
		} else if (pre.equals("$")) {
			tag = "CD";
		} else if (length >= 3 && word.substring(length - 3, length).equals("ble")) {
			tag = "JJ";
		} else if (length >= 3 && word.substring(length - 3, length).equals("ive")) {
			tag = "JJ";
		} else if (word.substring(length - 2, length).equals("us")) {
			tag = "JJ";
		} else {
			tag = "NN";
		}

		return tag;
	}

	public static void main(String[] args) {
		String trainfile = "/Users/di/Documents/16Spring/NLP/HW4/WSJ_POS_CORPUS_FOR_STUDENTS/WSJ_02-21.pos";
		String testfile = "/Users/di/Documents/16Spring/NLP/HW4/WSJ_POS_CORPUS_FOR_STUDENTS/WSJ_23.words";

		emission = new HashMap<String, Map<String, Double>>();
		transition = new HashMap<String, Map<String, Double>>();
		transition.put("start", new HashMap<String, Double>());

		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(trainfile));
			String line, sentence = "";

			while ((line = br.readLine()) != null) {

				if (!line.isEmpty()) {
					sentence += line + "\n";
				} else {
					BuildMap(sentence);
					// fw.write(sentence + "\n");
					sentence = "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// transform into log probabilities
		for (String a : transition.keySet()) {
			double count = 0;

			for (String b : transition.get(a).keySet())
				count += transition.get(a).get(b);

			for (String b : transition.get(a).keySet()) {
				// compute log probabilities
				double logprob = Math.log10(transition.get(a).get(b) / count);
				transition.get(a).put(b, logprob);
			}
		}

		for (String a : emission.keySet()) {
			double count = 0;

			for (String b : emission.get(a).keySet())
				count += emission.get(a).get(b);

			for (String b : emission.get(a).keySet()) {
				// log calculate
				double logprob = Math.log10(emission.get(a).get(b) / count);
				emission.get(a).put(b, logprob);
			}
		}

		ReadTestFile(testfile);
	}
}