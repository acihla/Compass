package com.example.android.softkeyboard;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Combo {
	private final String _hash;
	private List<Word> _words;
	private List<String> _neighbors;
	
	
	public Combo(String hash, List<Word> words){
		_hash = hash;
		_words = words;
		generateNeighbors(_hash);
		
	}
	
	public List<Word> getWords()
	{
		return _words;
	}
	
	public List<Word> getWordsSorted()
	{
		Collections.sort(_words);
		return _words;
	}


	public void addWord(String word)
	{
		int mid = (int) Math.round(3/4*_words.size());
		Word newWord  = new Word(word, _words.get(mid).getFrequency());
		_words.add(newWord);
	}
	
	public void addWordWNewCombo(Combo wordCombo, String hash, DataBuilder databuilder)
	{
		databuilder.addToCombos(hash,wordCombo);
		//_words.add(newWord);
	}

	
	private void generateNeighbors(String hash) {
		// TODO: implement more than 1st degree neighbor and possible split hashes
		_neighbors = new LinkedList<String>();
		for (int i = 0; i < hash.length(); i++)
		{
			int val = Integer.parseInt(hash.substring(i, i+1));
			int up = val + 1;
			if (up > 7) { up = 0; }
			
			int down = val - 1;
			if (down < 0) { down = 7; }
			
			String before = hash.substring(0, i);
			String after = hash.substring(i+1);
			_neighbors.add(before+up+after);
			_neighbors.add(before+down+after);
		}
	}

}