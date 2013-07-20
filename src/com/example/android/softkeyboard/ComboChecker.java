package com.example.android.softkeyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

public class ComboChecker {
	private static HashMap<String, Double> distributions;
	private static HashSet<String> validCombos;
	private static HashMap<String, LinkedList<String>> combosToWords;
	private static final double distanceBonus = 2;

	public static String[] getWords(String currentSeq, HashMap<String, Double> dist, HashSet<String> valid, HashMap<String, LinkedList<String>> combos, HashMap<String, Double> prevWordDist){
		distributions = dist;
		validCombos = valid;
		combosToWords = combos;
		HashMap<String, Double> output2 = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(output2);
		TreeMap<String, Double> sorted = new TreeMap<String, Double>(bvc);
		double min = Collections.min(prevWordDist.values())/2;
		if (validCombos.contains(currentSeq)){
			LinkedList<String> words = combosToWords.get(currentSeq);
			Iterator<String> wordIter = words.iterator();
			while (wordIter.hasNext()){
				String word = wordIter.next();
				double prevDist;
				if (prevWordDist.containsKey(word)){
					prevDist = prevWordDist.get(word);
				} else {
					prevDist = min;
				}
				output2.put(word, prevDist*distributions.get(word)*distanceBonus*distanceBonus);
			}
		}
		HashSet<String> finalResult = new HashSet<String>();
		/*HashSet<String> intermediate = enumerate(currentSeq);
		HashSet<String> finalResult = new HashSet<String>();
		Iterator<String> iter = intermediate.iterator();
		while (iter.hasNext()){
			String temp = iter.next();
			finalResult.addAll(enumerate(temp));
			if (validCombos.contains(temp)){
				LinkedList<String> words = combosToWords.get(temp);
				Iterator<String> wordIter = words.iterator();
				while (wordIter.hasNext()){
					String word = wordIter.next();
					if (!output2.containsKey(word)){
						double prevDist;
						if (prevWordDist.containsKey(word)){
							prevDist = prevWordDist.get(word);
						} else {
							prevDist = min;
						}
						output2.put(word, prevDist*distributions.get(word)*distanceBonus);
					}
				}
			}
		}*/
		finalResult.retainAll(validCombos);
		Iterator<String> iter2 = finalResult.iterator();
		while (iter2.hasNext()){
			String current = iter2.next();
			LinkedList<String> words = combosToWords.get(current);
			Iterator<String> wordIter = words.iterator();
			while (wordIter.hasNext()){
				String word = wordIter.next();
				if (!output2.containsKey(word)){
					double prevDist;
					if (prevWordDist.containsKey(word)){
						prevDist = prevWordDist.get(word);
					} else {
						prevDist = min;
					}
					output2.put(word, prevDist*distributions.get(word));
				}
			}
		}
		sorted.putAll(output2);
		LinkedList<String> output = new LinkedList<String>();
		Iterator<String> outIter = sorted.keySet().iterator();
		while (outIter.hasNext()){
			output.add(outIter.next());
		}
		String[] outArray = new String[output.size()];
		for (int i=0; i<output.size(); i++){
			outArray[i] = output.get(i);
		}
		return outArray;
	}
	public static String[] getWords(String currentSeq, HashMap<String, Double> dist, HashSet<String> valid, HashMap<String, LinkedList<String>> combos){
		distributions = dist;
		validCombos = valid;
		combosToWords = combos;
		HashMap<String, Double> output2 = new HashMap<String, Double>();
		ValueComparator bvc = new ValueComparator(output2);
		TreeMap<String, Double> sorted = new TreeMap<String, Double>(bvc);
		if (validCombos.contains(currentSeq)){
			LinkedList<String> words = combosToWords.get(currentSeq);
			Iterator<String> wordIter = words.iterator();
			while (wordIter.hasNext()){
				String word = wordIter.next();
				output2.put(word, distributions.get(word)*distanceBonus*distanceBonus);
			}
		}
		HashSet<String> intermediate = enumerate(currentSeq);
		HashSet<String> finalResult = new HashSet<String>();
		Iterator<String> iter = intermediate.iterator();
		while (iter.hasNext()){
			String temp = iter.next();
			finalResult.addAll(enumerate(temp));
			if (validCombos.contains(temp)){
				LinkedList<String> words = combosToWords.get(temp);
				Iterator<String> wordIter = words.iterator();
				while (wordIter.hasNext()){
					String word = wordIter.next();
					if (!output2.containsKey(word)){
						output2.put(word, distributions.get(word)*distanceBonus);
					}
				}
			}
		}
		finalResult.retainAll(validCombos);
		Iterator<String> iter2 = finalResult.iterator();
		while (iter2.hasNext()){
			String current = iter2.next();
			LinkedList<String> words = combosToWords.get(current);
			Iterator<String> wordIter = words.iterator();
			while (wordIter.hasNext()){
				String word = wordIter.next();
				if (!output2.containsKey(word)){
					output2.put(word, distributions.get(word));
				}
			}
		}
		sorted.putAll(output2);
		LinkedList<String> output = new LinkedList<String>();
		Iterator<String> outIter = sorted.keySet().iterator();
		while (outIter.hasNext()){
			output.add(outIter.next());
		}
		String[] outArray = new String[output.size()];
		for (int i=0; i<output.size(); i++){
			outArray[i] = output.get(i);
		}
		return outArray;
	}
	
	public static HashSet<String> enumerate(String combo){
		HashSet<String> results = new HashSet<String>();
		LinkedList<SplitWord> splits = new LinkedList<SplitWord>();
		for (int i=0; i<=combo.length(); i++){
			if (i==0){
				splits.add(new SplitWord("", combo));
			} else if (i==combo.length()){
				splits.add(new SplitWord(combo, ""));
			} else {
				splits.add(new SplitWord(combo.substring(0, i), combo.substring(i)));
			}
		}
		Iterator<SplitWord> iter = splits.iterator();
		while (iter.hasNext()){
			SplitWord current = iter.next();
			String right = current.getRight();
			String left = current.getLeft();
			if (right.length() > 0){
				results.add(left + right.substring(1));
				int temp = Integer.parseInt(right.substring(0, 1));
				int tempU = temp + 1;
				if (tempU > 7){
					tempU = 0;
				}
				int tempD =  temp - 1;
				if (tempD < 0){
					tempD = 7;
				}
				results.add(left+tempD+right.substring(1));
				results.add(left+tempU+right.substring(1));
			}
			if (right.length() > 1){
				results.add(left+right.charAt(1)+right.charAt(0) + right.substring(2));
			}
		}
		return results;
	}
	public static HashMap<String, Double> updateDist(HashMap<String, Double> map){
		Iterator<Double> iter = map.values().iterator();
		double total = 0;
		while (iter.hasNext()){
			total += iter.next();
		}
		Iterator<String> iter2 = map.keySet().iterator();
		while (iter2.hasNext()){
			String current = iter2.next();
			map.put(current, map.get(current)/total);
		}
		return map;
	}

}

