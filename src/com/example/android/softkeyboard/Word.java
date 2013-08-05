package com.example.android.softkeyboard;

import java.util.HashMap;
import java.util.Map;

public class Word implements Comparable<Word> {
	private final String _word;
	private int _frequency;
	private Map<String, Double> _futureWords;
	
	public Word(String word, int freq) {
		_word = word;
		_frequency = freq;
		_futureWords = new HashMap<String, Double>();
	}
	
	public String getWord()
	{
		return _word;
	}
	
	public int getFrequency()
	{
		return _frequency;
	}
	
	public void setFrequency(int value)
	{
		_frequency = value;
	}
	
	public void incrementFrequency(int value)
	{
		_frequency += value;
	}
	
	public void incrementFrequency()
	{
		_frequency++;
	}
	
	@Override
	public int compareTo(Word other) {
		int otherFreq = other.getFrequency();
		
		if (otherFreq > _frequency)
		{
			return -1;
		} 
		if (otherFreq < _frequency)
		{
			return 1;
		}
		return 0;
	}
	
	public void updateFutureWords(String word)
	{
		if (_futureWords.containsKey(word))
		{
			double current = _futureWords.get(word);
			_futureWords.put(word, current+1);
		} else {
			_futureWords.put(word, 1.0);
		}
	}
	
	public Map<String, Double> getFutureWords()
	{
		return _futureWords;
	}

}