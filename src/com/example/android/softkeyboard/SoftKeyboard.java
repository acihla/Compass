/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.softkeyboard;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Handler;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;



/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SoftKeyboard extends InputMethodService 
        /*implements KeyboardView.OnKeyboardActionListener OLDSHIT*/ {
    static final boolean DEBUG = false;
    
    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on 
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;

    private InputMethodManager mInputMethodManager;

    //private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;
    
    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private LatinKeyboard mQwertyKeyboard;
    private String mWordSeparators;
    
    //New shit
    private View mInputView;
	private String prevButton;
	private int prevXTouch;
	private int prevYTouch;
	private int prevTouchTime; 
	private String currentSequence = "";
	private Map<String, String[]> keygonSequences; 
	private String textOutput;
	private String lastWord;
	private TextView[] currentPossibilties;
	private MyPoint newMyPoint = null;
	private List<MyPoint> pointsQ = new ArrayList<MyPoint>();
	private boolean gliding = false;
	
	
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    
    private HashMap<String, Combo> currentCombos;
    
    
    private HashSet<String> words = new HashSet<String>();
	private HashMap<String, Double> distribution = new HashMap<String, Double>();
	private HashSet<String> combos = new HashSet<String>();
	private HashMap<String, LinkedList<String>> combosToWords = new HashMap<String, LinkedList<String>>();
	private HashMap<String, HashMap<String, Double>> prevWordDist = new HashMap<String, HashMap<String, Double>>();
	private final double alpha = .0001;
	private final double beta = .01;
	private int count = 0;
	private ArrayList<String> candidates;
	private Boolean isPotentialNewWord = false;
	private Boolean creatingNewWord = false;
	private Boolean isUpperCase = false;
	public String potentialNewWord = ""; 
	private ImageView compass;
	private DataBuilder dataBuilder = new DataBuilder();
	private int LONG_PRESS_TIME = 900;
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
        
        /*
        File file = getFileStreamPath("someshit.txt");

        
        try{
        	if (!file.exists()) {
                file.createNewFile();
             }
        FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
    	cursor.moveToFirst();

    	do{
    	   String msgData = "";
    	   for(int idx=0;idx<cursor.getColumnCount();idx++)
    	   {
    	       	msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
    		    
    	       	writer.write(msgData.getBytes());
       	    	writer.flush();
    	   }
    	   Log.d("reading sms" , msgData);
    	}while(cursor.moveToNext());
        
    	writer.close();
    	
    	FileInputStream readable = openFileInput(file.getName());
        } catch (Exception e) {
        Log.d("fuck me", "sms reading failed fuck " + e.toString());
        
        } */
    	
    	
        InputStream myInputStream = getResources().openRawResource(R.raw.keygonsequenceoutput10k1);
        InputStream myInputStream2 = getResources().openRawResource(R.raw.book1);

        
        dataBuilder.initializeData(myInputStream, myInputStream2);
        currentCombos = dataBuilder.getCombos();
    }
    
    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }

    }
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
    	mInputView = getLayoutInflater().inflate(R.layout.compass, null);
    	
    	final Bitmap nb = BitmapFactory.decodeResource(getResources(), R.drawable.nb);
        final Bitmap sb = BitmapFactory.decodeResource(getResources(), R.drawable.sb);
        final Bitmap eb = BitmapFactory.decodeResource(getResources(), R.drawable.eb);
        final Bitmap wb = BitmapFactory.decodeResource(getResources(), R.drawable.wb);
        final Bitmap ng = BitmapFactory.decodeResource(getResources(), R.drawable.ng);
        final Bitmap neg = BitmapFactory.decodeResource(getResources(), R.drawable.neg);
        final Bitmap eg = BitmapFactory.decodeResource(getResources(), R.drawable.eg);
        final Bitmap seg = BitmapFactory.decodeResource(getResources(), R.drawable.seg);
        final Bitmap sg = BitmapFactory.decodeResource(getResources(), R.drawable.sg);
        final Bitmap swg = BitmapFactory.decodeResource(getResources(), R.drawable.swg);
        final Bitmap wg = BitmapFactory.decodeResource(getResources(), R.drawable.wg);
        final Bitmap nwg = BitmapFactory.decodeResource(getResources(), R.drawable.nwg);
        final Bitmap center = BitmapFactory.decodeResource(getResources(), R.drawable.center);
    	compass = (ImageView) mInputView.findViewById(R.id.compass);
    	gestureDetector = new GestureDetector(this, new MyGestureDetector());
    	final Handler _handler = new Handler(); 
    	final Runnable _longPressed = new Runnable() { 
    	    public void run() {
    	    	Log.d("AJ", "LONG PRESSED RUNNING");
    	        creatingNewWord = true;
    	    }   
    	};
    	
        compass.setOnTouchListener(new OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
	                int eventPadTouch = event.getAction();
	                int iX = (int) event.getX(); //might have to add 1
	                int currY = (int) event.getY(); //might have to add 1
	                
	                ////
	                gestureDetector.onTouchEvent(event);
	                ////
	                boolean isXYPositive = iX>=0 && currY>=0;
	                newMyPoint = new MyPoint(iX, currY);
	                pointsQ.add(newMyPoint);
	              //  Log.d("onTouch is continuous" , "" + iX + "  " + currY);
	                switch (eventPadTouch) {
	                	
	                    case MotionEvent.ACTION_DOWN:
	                    	
		                    if(!gliding) {
		                        if (isXYPositive && iX<nb.getWidth() && currY<nb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (nb.getPixel(iX,currY)!=0) {
		                                // NORTH BUMPER
		                            	prevButton = "northBump";
		                            	Log.d("NB", "was picked");
		                            	
		                            }               
		                        }
		                        if (isXYPositive && iX<sb.getWidth() && currY<sb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (sb.getPixel(iX,currY)!=0) {
		                                // SOUTH BUMPER
		                            	prevButton = "southBump";
		                            	Log.d("SB", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<eb.getWidth() && currY<eb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (eb.getPixel(iX,currY)!=0) {
		                                // EAST BUMPER
		                            	prevButton = "eastBump";
		                            	getCurrentInputConnection().deleteSurroundingText(0, 1);
	                    				getSuggestions();
	                    				//candidates = null;
	                    				//updateCandidates(candidates);
		                            	Log.d("EB", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<wb.getWidth() && currY<wb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (wb.getPixel(iX,currY)!=0) {
		                                // WEST BUMPER
		                            	prevButton = "westBump";
		                            	if (currentSequence.length() == 0) {
		                            		getCurrentInputConnection().deleteSurroundingText(1, 0);
	                    					currentSequence = "";
	                    					candidates = null;
		                            	}
		                            	else {
		                            		currentSequence = currentSequence.substring(0, currentSequence.length() - 1);
		                            	}
		                            	getSuggestions();
	                    				//updateCandidates(candidates);
		                            	Log.d("WB", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<ng.getWidth() && currY<ng.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (ng.getPixel(iX,currY)!=0) {
		                                // NORTH GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "0";
		                            	Log.d("NG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<neg.getWidth() && currY<neg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (neg.getPixel(iX,currY)!=0) {
		                                // NORTH EAST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "1";
		                            	Log.d("NEG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<eg.getWidth() && currY<eg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (eg.getPixel(iX,currY)!=0) {
		                                // EAST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "2";
		                            	Log.d("EG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<seg.getWidth() && currY<seg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (seg.getPixel(iX,currY)!=0) {
		                                // SOUTH EAST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "3";
		                            	Log.d("SEG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<sg.getWidth() && currY<sg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (sg.getPixel(iX,currY)!=0) {
		                                // SOUTH GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "4";
		                            	Log.d("SG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<swg.getWidth() && currY<swg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (swg.getPixel(iX,currY)!=0) {
		                                // SOUTH WEST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "5";
		                            	Log.d("SWG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<wg.getWidth() && currY<wg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (wg.getPixel(iX,currY)!=0) {
		                                // WEST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "6";
		                            	Log.d("WG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<nwg.getWidth() && currY<nwg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (nwg.getPixel(iX,currY)!=0) {
		                                // NORTH WEST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "7";
		                            	Log.d("NWG", "was picked");
		                            }            
		                        }
		                        if (isXYPositive && iX<center.getWidth() && currY<center.getHeight()) {
		                        	if(center.getPixel(iX,currY) != 0) {
		                        		// CENTER GROUPING
		                        		prevXTouch = iX;
		                        		prevYTouch = currY;
		                        		Log.d("AJ", "CENTER was picked and creatingNewWord is " + creatingNewWord.toString() + " AND potentialNewWord is " + potentialNewWord.toString());
		                        		//if (prevButton.equals("moving")) {
		                        			
		                        		//}
		                        		//else {	 
			                        		if (creatingNewWord == true) {
			                        			Log.v("AJ" ,"Potential new word being added " + potentialNewWord.toString());
			                        			addNewWord(potentialNewWord);
			                        			creatingNewWord = false;
			                        			getCurrentInputConnection().commitText(" ", 1);
			                        			potentialNewWord = "";
			                        			isPotentialNewWord = false;
			                        			creatingNewWord = false;
			                        		}
			                        		else if (candidates != null && currentSequence.length() > 0) {
			                        			pickDefaultCandidate();
			                        			candidates = null;
			                        			updateCandidates(null);
			                        			currentSequence = "";
			                        			if (mCandidateView != null) {
			                                        mCandidateView.clear();
			                                    }
			                        		}
			                        		else {
			                        			getCurrentInputConnection().commitText(" ", 1);
			                        		}
		                        	//	}
		                        	
		                        		prevButton = "center";
		                        		currentSequence = "";
		                        	}
		                        }
		                        
		                        if (prevButton.equals("text")) {
		                        	_handler.postDelayed(_longPressed, LONG_PRESS_TIME);
		                        }
		                        /*
		                        String[] temp;
		                        if (prevWordDist.containsKey(lastWord)){
		                        	//temp = ComboChecker.getWords(currentSequence, distribution, combos, combosToWords, prevWordDist.get(lastWord));
		                        } else {
		                        	//temp = ComboChecker.getWords(currentSequence, distribution, combos, combosToWords);
		                        }
		                       
		                        if (count%10 == 0){
		                        	//distribution = ComboChecker.updateDist(distribution); 
		                        	Log.d("Dist", "Distribution updated");
		                        }*/
		                        
		                        
		                         
		                        
		                        if (prevButton.equals("text") && (currentSequence.length() == 1 || creatingNewWord == true)) {
		                        	candidates = new ArrayList<String>();
		                        	Log.v("AJ" ,"creating a new word " + potentialNewWord + " or we are just typing a single character");
		                        	int seqInt = Integer.parseInt(currentSequence.substring(currentSequence.length() - 1));
		                        	switch (seqInt){
		                        		case 0:
		                        			candidates.add("r");
		                        			candidates.add("u");
		                        			candidates.add("t");
		                        			candidates.add("y");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 1:
		                        			candidates.add("i");
		                        			candidates.add("o");
		                        			candidates.add("p");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 2:
		                        			candidates.add("j");
		                        			candidates.add("k");
		                        			candidates.add("l");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 3:
		                        			candidates.add("h");
		                        			candidates.add("n");
		                        			candidates.add("m");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 4:
		                        			candidates.add("v");
		                        			candidates.add("g");
		                        			candidates.add("b");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 5:
		                        			candidates.add("z");
		                        			candidates.add("x");
		                        			candidates.add("c");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 6:
		                        			candidates.add("a");
		                        			candidates.add("s");
		                        			candidates.add("d");
		                        			candidates.add("f");
		                        			updateCandidates(candidates);
		                        			return true;
		                        			
		                        		case 7:
		                        			candidates.add("q");
		                        			candidates.add("w");
		                        			candidates.add("e");
		                        			updateCandidates(candidates);
		                        			return true;
		                        	}
		                        		
		                        }
		                        getSuggestions();
		                    }
	                        return true;     
	                        
	                    case MotionEvent.ACTION_UP:
	                    	_handler.removeCallbacks(_longPressed);
	                    	if(gliding) {
	                    		Log.d("AJ", "gliding action done handle possibilties!");
	                    		currentSequence = "";
	                    		////////////assessFlow(pointsQ);
	                    		pointsQ.clear();
	                    		gliding=false;
	                    	}
	                    	
	                    	else if (isXYPositive && iX<center.getWidth() && currY<center.getHeight()) {
	                        	if(center.getPixel(iX,currY) != 0) {
	                        		prevButton = "center";
	                        		prevXTouch = iX;
	                        		prevYTouch = currY;
	                        		Log.d("AJ", "CENTER was picked");
	                        	}
	                        }
	                    	return true;
	                    	
	                    case MotionEvent.ACTION_MOVE:
	                    	//prevButton = "moving";
	                    	//_handler.removeCallbacks(_longPressed);
	                    	if (isXYPositive && prevButton.equals("center")) {
	                    		
	                    		RelativeLayout.LayoutParams layoutCoords = (RelativeLayout.LayoutParams) compass.getLayoutParams();
	                    		int leftMarg = layoutCoords.leftMargin + iX - prevXTouch;
	                    		int topMarg = layoutCoords.topMargin + currY - prevYTouch;
	                    		if(leftMarg < 0)
	                    			leftMarg = 0;
	                    		if(leftMarg > mInputView.getWidth() - compass.getWidth())
	                    			leftMarg = mInputView.getWidth() - compass.getWidth();
	                    		if(topMarg < 0)
	                    			topMarg = 0;
	                    		if(topMarg > mInputView.getHeight() - compass.getHeight())
	                    			topMarg = mInputView.getHeight() - compass.getHeight();
	                    		layoutCoords.leftMargin = leftMarg;
	                    		layoutCoords.topMargin = topMarg;
	                    		compass.setLayoutParams(layoutCoords);
	                    		break;
	                    	}
	                    	if (isXYPositive && prevButton.equals("text")){
	                    		//newMyPoint = new MyPoint(iX, currY);
	                           // pointsQ.add(newMyPoint);
	                            Log.d("AJ", "moving Swyping");
	                            //something weird right now gliding = true;
	                            prevButton = "gliding";
	                            break;
	                    	}
	                        return true;   
	                }           
	                return false;
				}
	        }); 
        return mInputView;
    }
    
    
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if ((e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	compass.setImageResource(R.drawable.compassupper);
                	isUpperCase = true;
                } 
                else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                	compass.setImageResource(R.drawable.compasslower);
                	isUpperCase = false;
                }
                // right to left swipe DELETE FULL WORD
                else if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    deleteLastWord();
                }  
                // left to right swipe DELETE FULL WORD 
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    deletePreviousWord();
                }
                if (prevButton.equals("text")) {
            		currentSequence = currentSequence.substring(0,currentSequence.length() - 1);
            		getSuggestions();
            	}
            } catch (Exception e) {
                // nothing
            }
            return false;
        }
    }
        
    
    private void changetoCapital() {
    	
    }
    /**
	 * Deletes one word before the cursor.
	 */
	private void deleteLastWord() {
		final int charactersToGet = 20;
		final String splitRegexp = " ";
		
		if (currentSequence.length() <= 1) {
			// delete trailing spaces
			while (getCurrentInputConnection().getTextBeforeCursor(1, 0).toString().equals(splitRegexp)) {
				getCurrentInputConnection().deleteSurroundingText(1, 0);
			}

			// delete last word letters
			final String[] words = getCurrentInputConnection().getTextBeforeCursor(charactersToGet, 0).toString()
					.split(splitRegexp);
			//Log.d("words to delete", "" + words.toString());
			getCurrentInputConnection().deleteSurroundingText(words[words.length - 1].length(), 0);
		}
		
		currentSequence = "";
		getSuggestions();
	}
	
	/**
	 * Deletes one word after the cursor.
	 */
	private void deletePreviousWord() {
		final int charactersToGet = 20;
		final String splitRegexp = " ";
		Log.v("AJ" , "In deletePreviousWord current sequence is: " + currentSequence);
		// delete trailing spaces
		while (getCurrentInputConnection().getTextAfterCursor(1, 0).toString().equals(splitRegexp)) {
			getCurrentInputConnection().deleteSurroundingText(0, 1);
		}

		// delete previous word letters
		final String[] words = getCurrentInputConnection().getTextAfterCursor(charactersToGet, 0).toString()
				.split(splitRegexp);
		//Log.d("words to delete", "" + words.toString());
		getCurrentInputConnection().deleteSurroundingText(0, words[0].length());

	}

	public void addNewWord(String newWord) {
		Log.v("AJ" ,"creating a new word " + currentSequence);
		Combo temp = currentCombos.get(currentSequence);
		if (temp == null) {
			Word newWordObj = new Word(newWord, 9001);
			List<Word> newWords = new LinkedList<Word>();
			newWords.add(newWordObj);
			Combo combo = new Combo(currentSequence, newWords);
			combo.addWordWNewCombo(combo, currentSequence, dataBuilder);
		}
		else {
			temp.addWord(newWord);
		}
		
	}
	
	/**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    public void getSuggestions() {
    	
        	Combo temp = currentCombos.get(currentSequence);
        	
            Log.d("AJ", "In getSuggestions CURRENT SEQUENCE " + currentSequence);
            List<Word> words = new LinkedList<Word>();
            
            if (temp != null) {
            	words = temp.getWordsSorted();
            	Log.d("AJ", "In getSuggestions getting words " + words.toString());
            	if(words.get(0).getWord() != null)
            		candidates = new ArrayList<String>();
            	for (int k = 0; k < words.size() && k < 12; k++) {
            		if (words.get(k).getWord() != null) {
            			candidates.add(words.get(k).getWord());
            			updateCandidates(candidates);
            			//Log.d("currentgetWord is ", words.get(k).getWord());
            		}
            	}
            	if(words.size() > 0) {
            		
                	//Log.v("babysteps", candidates.toString() + " and words is " + words.toString());
            	}	
            } 
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }
    
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        
        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates(null);
         /*
        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }*/
        
        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        
        
        
    }


    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();
        
        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates(null);
        
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        
    }
    
    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates(null);
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }
    
    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }
    
    
    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates(null);
        }
    }


    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    public void updateCandidates(ArrayList<String> poss) {
    	//Log.v("debugger", "in update Candidates");
    	if(currentSequence.length() > 0) {
	    	ArrayList<String> bs = new ArrayList<String>();
	    	
	        setSuggestions(poss, false, false);
    	}
    	else {
    		setSuggestions(null, false, false);
    	}
        /*if (!mCompletionOn && poss == null) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                setSuggestions(poss, true, true);
            } else {
            	
            }
        }*/
    }
    
    public void setSuggestions(List<String> suggestions, boolean completions,
            boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }
    
    

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            currentSequence = currentSequence.substring(0, length - 1);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates(null);
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates(null);
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
       //////////////////////// updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }
    }
    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
    	Log.d("Handling character", "biatch");
        if (isInputViewShown()) {

        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append( primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
           ///////////////////////////// updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates(null);
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf(primaryCode), 1);
        }
    }


    private String getWordSeparators() {
        return mWordSeparators;
    }
    
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
        Log.v("AJ" , "picking default candidate");
    }
    
    public void pickSuggestionManually(int index) {
    	//Log.d("picking suggestions manually" , "mCompletionOn" + mCompletionOn +"   candidates" + candidates.toString() +" index is " + index + " with length of candidates " + candidates.size());
        //if (mCompletionOn && mCompletions != null && index >= 0 && index < mCompletions.length) {
    	if(candidates != null){
            CharSequence ci = candidates.get(index);
            getCurrentInputConnection().commitText(ci, 1);
            Log.v("AJ" ,"the ci is " + "" + ci.toString());
            
          
            if(creatingNewWord == true) {
            	potentialNewWord = potentialNewWord + ci.toString();
            }
            else {//if(currentSequence.length() > 1) {
            	getCurrentInputConnection().commitText(" ", 1);
            	currentSequence = "";
            	//Log.v("we are in", "fourth cond");
            }
            
    		candidates = null;
    		updateCandidates(candidates);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
    	}
            /////////////////////////updateShiftKeyState(getCurrentInputEditorInfo());
        } 
    

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
    }
    


    public void swipeUp() {
    }
    
    public void onPress(int primaryCode) {
    }
    
    public void onRelease(int primaryCode) {
    }

}