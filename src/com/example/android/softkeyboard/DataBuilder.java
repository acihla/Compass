package com.example.android.softkeyboard;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import android.app.Activity;
import android.util.Log;


public class DataBuilder extends Activity {
	private static HashMap<String, Combo> _currentCombos = new HashMap<String, Combo>();
	private static HashMap<String, List<String>> _hashesToWords = new HashMap<String, List<String>>();
	private static HashMap<String, Integer> _frequencies = new HashMap<String, Integer>();
	
	public static void initializeData(InputStream inputStream1) 
	{
		if (_currentCombos.size() == 0)
		{
			readData(inputStream1);
			for (String hash : _hashesToWords.keySet())
			{
				List<Word> words = new LinkedList<Word>();
				for (String word : _hashesToWords.get(hash))
				{
					int freq = _frequencies.get(word);
					Word current = new Word(word, freq);
					words.add(current);
				}
				Combo combo = new Combo(hash, words);
				_currentCombos.put(hash, combo);
			}
		}
	}
	
	private static void readData(InputStream myInputStream)
	{
        
        try {
			HSSFWorkbook workbook = new HSSFWorkbook(myInputStream);
			HSSFSheet worksheet = workbook.getSheet("10kOutput");
			
			for(int i = 0; i < worksheet.getLastRowNum(); i++) {
				HSSFRow currentRow = worksheet.getRow(i);
				String currentCombo = currentRow.getCell(3).getStringCellValue();
				String currentWord = currentRow.getCell(0).getStringCellValue();
				Integer currentFreq = (int)currentRow.getCell(4).getNumericCellValue();
				if (!_hashesToWords.containsKey(currentCombo)){
					LinkedList<String> temp = new LinkedList<String>();
					temp.add(currentWord);
					_hashesToWords.put(currentCombo, temp);
					_frequencies.put(currentWord, currentFreq);
				} else {
					List<String> temp = _hashesToWords.get(currentCombo);
					if (!temp.contains(currentWord))
					{
						temp.add(currentWord);
					}
					_hashesToWords.put(currentCombo, temp);
					_frequencies.put(currentWord, currentFreq);
				}
			}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void saveData()
	{
		//save the user data somehow
	}
	
	public static void addToCombos(String hash, Combo newCombo){
		_currentCombos.put(hash, newCombo);
	}
	
	public static HashMap<String, Combo> getCombos()
	{
		return _currentCombos;
	}

}