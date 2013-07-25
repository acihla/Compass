package com.example.android.softkeyboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is dedicated to Combo from Breaking Bad. RIP.
 * @author tbrown126
 *
 */
public class Combo {
	private static final int DEFAULT_FREQUENCY = 10000;
	private final String _hash;
	private List<Word> _words;
	private List<String> _neighbors;
	private final double ALPHA = .75;
	
	
	public Combo(String hash, String word)
	{
		this(hash, new LinkedList<Word>());
		addWord(word);
		
		DataBuilder.addToCombos(hash,this);
	}

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
		Collections.sort(_words, Collections.reverseOrder());
		return _words;
	}
	
	public List<Word> getWordsTap()
	{
		HashMap<String, Combo> combos = DataBuilder.getCombos();
		List<Word> combinedWords = new LinkedList<Word>(_words);
		for (String neighbor : _neighbors)
		{
			if (combos.containsKey(neighbor))
			{
				for (Word word : combos.get(neighbor).getWords())
				{
					combinedWords.add(new Word(word.getWord(), (int) Math.round(word.getFrequency()*ALPHA)));
				}
			}
		}
		Collections.sort(combinedWords, Collections.reverseOrder());
		return combinedWords;
	}
	
	public List<Word> getWordsSloppy(int offBy)
	{
		if (offBy == 0)
		{
			return _words;
		} else {
			HashMap<String, Combo> combos = DataBuilder.getCombos();
			List<Word> combinedWords = new LinkedList<Word>(_words);
			HashSet<String> currentWords = new HashSet<String>();
			for (Word word : _words)
			{
				currentWords.add(word.getWord());
			}
			for (String neighbor : _neighbors)
			{
				if (combos.containsKey(neighbor))
				{
					for (Word word : combos.get(neighbor).getWordsSloppy(offBy - 1))
					{
						String current = word.getWord();
						if (!currentWords.contains(current))
						{
							combinedWords.add(new Word(word.getWord(), (int) Math.round(word.getFrequency()*ALPHA)));
							currentWords.add(current);
						}
					}
				}
			}
			Collections.sort(combinedWords, Collections.reverseOrder());
			return combinedWords;	
		}
	}


	public void addWord(String word)
	{
		if (_words.size() > 0)
		{
			Word w = findWord(word);
			if (w == null)
			{
				int mid = (int) Math.floor(3/4*_words.size());
				Word newWord  = new Word(word, _words.get(mid).getFrequency());
				_words.add(newWord);
			} else {
				w.incrementFrequency();
			}
		} else {
			Word newWord = new Word(word, DEFAULT_FREQUENCY);
			_words.add(newWord);
		}
	}
	
	public void addWordWNewCombo(Combo wordCombo, String hash)
	{
		DataBuilder.addToCombos(hash,wordCombo);
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
	
	private Word findWord(String word)
	{
		Word target = null;
		for (Word w : _words)
		{
			if (w.getWord().equals(word))
			{
				target = w;
				continue;
			}
		}
		return target;
	}

}