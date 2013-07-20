package com.example.android.softkeyboard;

public class Word implements Comparable<Word> {
	private final String _word;
	private int _frequency;
	
	public Word(String word, int freq) {
		_word = word;
		_frequency = freq;
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

}