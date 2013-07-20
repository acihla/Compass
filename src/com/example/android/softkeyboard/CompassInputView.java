package com.example.android.softkeyboard;





import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CompassInputView extends RelativeLayout {
	private final Context mContext;
	private Button mSymbolsButton;
  	private Button mShiftButton;
 	private Button mBackspaceButton;
  	private Button mSpaceButton;
  	
  	private int previousXIndex;
  	private int previousYIndex;
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
	
	
	private ImageView compass;
	private Bitmap nb = BitmapFactory.decodeResource(getResources(), R.drawable.nb);
	private Bitmap sb = BitmapFactory.decodeResource(getResources(), R.drawable.sb);
	private Bitmap eb = BitmapFactory.decodeResource(getResources(), R.drawable.eb);
    private Bitmap wb = BitmapFactory.decodeResource(getResources(), R.drawable.wb);
    private Bitmap ng = BitmapFactory.decodeResource(getResources(), R.drawable.ng);
    private Bitmap neg = BitmapFactory.decodeResource(getResources(), R.drawable.neg);
    private Bitmap eg = BitmapFactory.decodeResource(getResources(), R.drawable.eg);
    private Bitmap seg = BitmapFactory.decodeResource(getResources(), R.drawable.seg);
    private Bitmap sg = BitmapFactory.decodeResource(getResources(), R.drawable.sg);
    private Bitmap swg = BitmapFactory.decodeResource(getResources(), R.drawable.swg);
    private Bitmap wg = BitmapFactory.decodeResource(getResources(), R.drawable.wg);
    private Bitmap nwg = BitmapFactory.decodeResource(getResources(), R.drawable.nwg);
    private Bitmap center = BitmapFactory.decodeResource(getResources(), R.drawable.center);
    
    
    private HashSet<String> words = new HashSet<String>();
	private HashMap<String, Double> distribution = new HashMap<String, Double>();
	private HashSet<String> combos = new HashSet<String>();
	private HashMap<String, LinkedList<String>> combosToWords = new HashMap<String, LinkedList<String>>();
	private HashMap<String, HashMap<String, Double>> prevWordDist = new HashMap<String, HashMap<String, Double>>();
	private final double alpha = .0001;
	private final double beta = .01;
	private int count = 0;
	private SoftKeyboard parent;

  	public CompassInputView(Context context, AttributeSet attrs) {
  		super(context, attrs);
  		mContext = context;
  		
  		
  	}

  	@Override
  	protected void onFinishInflate() {
/*
  		compass = (ImageView) findViewById(R.id.compass);
        
	  	compass.setOnTouchListener(new OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
	                int eventPadTouch = event.getAction();
	                int iX = (int) event.getX(); //might have to add 1
	                int currY = (int) event.getY(); //might have to add 1
	                
	                
	                boolean isXYPositive = iX>=0 && currY>=0;
	                newMyPoint = new MyPoint(iX, currY);
	               // if (event.getEventTime() - 100 > prevTouchTime )
	                pointsQ.add(newMyPoint);
	              //  Log.d("onTouch is continuous" , "" + iX + "  " + currY);
	                switch (eventPadTouch) {
	                	
	                    case MotionEvent.ACTION_DOWN:
		                    if(!gliding) {
		                        if (isXYPositive && iX<nb.getWidth() && currY<nb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (nb.getPixel(iX,currY)!=0) {
		                                // NORTH BUMPER
		                            	prevButton = "northBump";
		                            	//prevTouchTime = (int)event.getEventTime();
		                            	Log.d("NB", "was picked");
		                            	
		                            }               
		                        }
		                        if (isXYPositive && iX<sb.getWidth() && currY<sb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (sb.getPixel(iX,currY)!=0) {
		                                // SOUTH BUMPER
		                            	prevButton = "southBump";
		                            	//prevTouchTime = (int)event.getEventTime();
		                            	Log.d("SB", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<eb.getWidth() && currY<eb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (eb.getPixel(iX,currY)!=0) {
		                                // EAST BUMPER
		                            	prevButton = "eastBump";
		                            	//prevTouchTime = (int)event.getEventTime();
		                            	Log.d("EB", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<wb.getWidth() && currY<wb.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (wb.getPixel(iX,currY)!=0) {
		                                // WEST BUMPER
		                            	prevButton = "westBump";
		                            	//prevTouchTime = (int)event.getEventTime();
		                            	Log.d("WB", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<ng.getWidth() && currY<ng.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (ng.getPixel(iX,currY)!=0) {
		                                // NORTH GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "0";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("NG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<neg.getWidth() && currY<neg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (neg.getPixel(iX,currY)!=0) {
		                                // NORTH EAST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "1";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("NEG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<eg.getWidth() && currY<eg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (eg.getPixel(iX,currY)!=0) {
		                                // EAST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "2";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("EG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<seg.getWidth() && currY<seg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (seg.getPixel(iX,currY)!=0) {
		                                // SOUTH EAST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "3";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("SEG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<sg.getWidth() && currY<sg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (sg.getPixel(iX,currY)!=0) {
		                                // SOUTH GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "4";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("SG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<swg.getWidth() && currY<swg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (swg.getPixel(iX,currY)!=0) {
		                                // SOUTH WEST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "5";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("SWG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<wg.getWidth() && currY<wg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (wg.getPixel(iX,currY)!=0) {
		                                // WEST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "6";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("WG", "was picked");
		                            }               
		                        }
		                        if (isXYPositive && iX<nwg.getWidth() && currY<nwg.getHeight()) { // ** Makes sure that X and Y are not less than 0, and no more than the height and width of the image.                
		                            if (nwg.getPixel(iX,currY)!=0) {
		                                // NORTH WEST GROUPING
		                            	prevButton = "text";
		                            	currentSequence += "7";
		                            	prevTouchTime = (int)event.getEventTime();
		                            	Log.d("NWG", "was picked");
		                            }            
		                        }
		                        if (isXYPositive && iX<center.getWidth() && currY<center.getHeight()) {
		                        	if(center.getPixel(iX,currY) != 0) {
		                        		prevButton = "center";
		                        		prevXTouch = iX;
		                        		prevYTouch = currY;
		                        		Log.d("CENTER", "was picked");
		                        	}
		                        }
		                        
		                        String[] temp;
		                        if (prevWordDist.containsKey(lastWord)){
		                        	temp = ComboChecker.getWords(currentSequence, distribution, combos, combosToWords, prevWordDist.get(lastWord));
		                        } else {
		                        	temp = ComboChecker.getWords(currentSequence, distribution, combos, combosToWords);
		                        }
		                       
		                        if (count%10 == 0){
		                        	distribution = ComboChecker.updateDist(distribution); 
		                        	Log.d("Dist", "Distribution updated");
		                        }
		                        if (prevButton.equals("text") && temp.length>0) {
		                        	//TextView possibilities = (TextView) findViewById(R.id.textView2);
		                        	//possibilities.setText("");
		                        	ArrayList<String> candidateIn = new ArrayList<String>(); 
		                        	for (int k = 0; k < temp.length; k++) {
		                        		if (temp[k] != null) {
		                        			candidateIn.add(temp[k]);
		                        			
		                        		}
		                        	}
		                        	Log.v("babysteps", "bitch");
		                        }
		                    }
	                        return true;     
	                        
	                    case MotionEvent.ACTION_UP:
	                    	prevTouchTime = (int) event.getEventTime() - (int) event.getDownTime(); 
	                    	if(gliding) {
	                    		Log.d("gliding action done", "handle possibilties!");
	                    		currentSequence = "";
	                    		////////////assessFlow(pointsQ);
	                    		pointsQ.clear();
	                    		gliding=false;
	                    	}
	                    	else if(isXYPositive && prevButton.equals("northBump") && iX<sb.getWidth() && currY<sb.getHeight()) {
	                    		if (sb.getPixel(iX, currY) !=0){
	                    			//Swipe north to south LOWERCASE
	                    			if(prevTouchTime > 2000) {
	                    				Log.d("SWIPING", "LOWERCASE LONG");
	                    			}
	                    			else {
	                    				Log.d("SWIPING", "LOWERCASE ACTION");
	                    			}
	                    			prevButton = "lowCase";
	                    		}
	                    	}
	                    	else if(isXYPositive && prevButton.equals("southBump") && iX<nb.getWidth() && currY<nb.getHeight()) {
	                    		if (nb.getPixel(iX, currY) !=0){
	                    			//Swipe south to north UPPERCASE
	                    			if(prevTouchTime > 2000) {
	                    				Log.d("SWIPING", "CAPSLOCK");
	                    			}
	                    			else {
	                    				Log.d("SWIPING", "UPPERCASE ACTION");
	                    			}
	                    			prevButton = "upCase";
	                    		}
	                    	}
	                    	else if(isXYPositive && prevButton.equals("eastBump") && iX<wb.getWidth() && currY<wb.getHeight()) {
	                    		if (wb.getPixel(iX, currY) !=0){
	                    			//Swipe east to west DELETE TO LEFT
	                    			if(prevTouchTime > 2000) {
	                    				Log.d("SWIPING", "DELETE ENTIRE WORD TO LEFT");
	                    			}
	                    			else {
	                    				Log.d("SWIPING", "DELETE TO LEFT");
	                    				
	                    			}
	                    			prevButton = "lDel";
	                    		}
	                    	}
	                    	else if(isXYPositive && prevButton.equals("westBump") && iX<eb.getWidth() && currY<eb.getHeight()) {
	                    		if (eb.getPixel(iX, currY) !=0){
	                    			//Swipe west to east DELETE TO RIGHT
	                    			if(prevTouchTime > 2000) {
	                    				Log.d("SWIPING", "DELETE ENTIRE WORD TO RIGHT");
	                    			}
	                    			else {
	                    				Log.d("SWIPING", "DELETE TO RIGHT");
	                    			}
	                    			prevButton = "rDel";
	                    		}
	                    	}
	                    	else if (isXYPositive && iX<center.getWidth() && currY<center.getHeight()) {
	                        	if(center.getPixel(iX,currY) != 0) {
	                        		prevButton = "center";
	                        		prevXTouch = iX;
	                        		prevYTouch = currY;
	                        		Log.d("CENTER", "was picked");
	                        	}
	                        }
	                    	//return true;
	                    	
	                    case MotionEvent.ACTION_MOVE:
	                    	if (isXYPositive && prevButton.equals("center")) {
	                    		RelativeLayout.LayoutParams layoutCoords = (RelativeLayout.LayoutParams) compass.getLayoutParams();
	                    		int leftMarg = layoutCoords.leftMargin + iX - prevXTouch;
	                    		int topMarg = layoutCoords.topMargin + currY - prevYTouch;
	                    		layoutCoords.leftMargin = leftMarg;
	                    		layoutCoords.topMargin = topMarg;
	                    		compass.setLayoutParams(layoutCoords);
	                    		break;
	                    	}
	                    	if (isXYPositive && prevButton.equals("text")){
	                    		//newMyPoint = new MyPoint(iX, currY);
	                           // pointsQ.add(newMyPoint);
	                            Log.d("moving", "Swyping");
	                            gliding = true;
	                            prevButton = "gliding";
	                            break;
	                    	}
	                        //return true;   
	                }           
	               // return false;
				}
	        }); 
	  		*/
	  		/**
	  		 * Listener handling pressing all buttons.
	  		 */
	  		
  		}
  	}

                
                /*
                
                
	                        String[] temp;
	                        if (prevWordDist.containsKey(lastWord)){
	                        	temp = ComboChecker.getWords(currentSequence, distribution, combos, combosToWords, prevWordDist.get(lastWord));
	                        } else {
	                        	temp = ComboChecker.getWords(currentSequence, distribution, combos, combosToWords);
	                        }
	                       
	                        if (count%10 == 0){
	                        	distribution = ComboChecker.updateDist(distribution); 
	                        	Log.d("Dist", "Distribution updated");
	                        }
	                        if (prevButton.equals("text") && temp.length>0) {
	                        	//TextView possibilities = (TextView) findViewById(R.id.textView2);
	                        	//possibilities.setText("");
	                        	for (int k = 0; k < temp.length; k++) {
	                        		TextView tv;
	                        		if (temp[k] != null) {
	                        			if(k == 0){
	                        				tv = (TextView)findViewById(R.id.textView3);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			if(k == 1){
	                        				tv = (TextView)findViewById(R.id.textView4);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			if(k == 2){
	                        				tv = (TextView)findViewById(R.id.textView5);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			if(k == 3){
	                        				tv = (TextView)findViewById(R.id.textView6);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			if(k == 4){
	                        				tv = (TextView)findViewById(R.id.textView7);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			if(k == 5){
	                        				tv = (TextView)findViewById(R.id.textView8);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			if(k == 6){
	                        				tv = (TextView)findViewById(R.id.textView9);
	                        				tv.setText(temp[k].toString());
	                        			}
	                        			
	                        		}
	                        	}
	                        	
	                        }
	                    }
                        return true;     
                        
                    case MotionEvent.ACTION_UP:
                    	prevTouchTime = (int) event.getEventTime() - (int) event.getDownTime(); 
                    	if(gliding) {
                    		Log.d("gliding action done", "handle possibilties!");
                    		currentSequence = "";
                    		assessFlow(pointsQ);
                    		pointsQ.clear();
                    		gliding=false;
                    	}
                    	else if(isXYPositive && prevButton.equals("northBump") && iX<sb.getWidth() && currY<sb.getHeight()) {
                    		if (sb.getPixel(iX, currY) !=0){
                    			//Swipe north to south LOWERCASE
                    			if(prevTouchTime > 2000) {
                    				Log.d("SWIPING", "LOWERCASE LONG");
                    			}
                    			else {
                    				Log.d("SWIPING", "LOWERCASE ACTION");
                    			}
                    			prevButton = "lowCase";
                    		}
                    	}
                    	else if(isXYPositive && prevButton.equals("southBump") && iX<nb.getWidth() && currY<nb.getHeight()) {
                    		if (nb.getPixel(iX, currY) !=0){
                    			//Swipe south to north UPPERCASE
                    			if(prevTouchTime > 2000) {
                    				Log.d("SWIPING", "CAPSLOCK");
                    			}
                    			else {
                    				Log.d("SWIPING", "UPPERCASE ACTION");
                    			}
                    			prevButton = "upCase";
                    		}
                    	}
                    	else if(isXYPositive && prevButton.equals("eastBump") && iX<wb.getWidth() && currY<wb.getHeight()) {
                    		if (wb.getPixel(iX, currY) !=0){
                    			//Swipe east to west DELETE TO LEFT
                    			if(prevTouchTime > 2000) {
                    				Log.d("SWIPING", "DELETE ENTIRE WORD TO LEFT");
                    			}
                    			else {
                    				Log.d("SWIPING", "DELETE TO LEFT");
                    				textOutput = textOutput.substring(0, textOutput.length() - lastWord.length());
                    				Log.d("new textoutput", textOutput);
                    				textOutputView.setText(textOutput);
                    			}
                    			prevButton = "lDel";
                    		}
                    	}
                    	else if(isXYPositive && prevButton.equals("westBump") && iX<eb.getWidth() && currY<eb.getHeight()) {
                    		if (eb.getPixel(iX, currY) !=0){
                    			//Swipe west to east DELETE TO RIGHT
                    			if(prevTouchTime > 2000) {
                    				Log.d("SWIPING", "DELETE ENTIRE WORD TO RIGHT");
                    			}
                    			else {
                    				Log.d("SWIPING", "DELETE TO RIGHT");
                    			}
                    			prevButton = "rDel";
                    		}
                    	}
                    	else if (isXYPositive && iX<center.getWidth() && currY<center.getHeight()) {
                        	if(center.getPixel(iX,currY) != 0) {
                        		prevButton = "center";
                        		prevXTouch = iX;
                        		prevYTouch = currY;
                        		Log.d("CENTER", "was picked");
                        	}
                        }
                    	return true;
                    	
                    case MotionEvent.ACTION_MOVE:
                    	if (isXYPositive && prevButton.equals("center")) {
                    		RelativeLayout.LayoutParams layoutCoords = (RelativeLayout.LayoutParams) compass.getLayoutParams();
                    		int leftMarg = layoutCoords.leftMargin + iX - prevXTouch;
                    		int topMarg = layoutCoords.topMargin + currY - prevYTouch;
                    		layoutCoords.leftMargin = leftMarg;
                    		layoutCoords.topMargin = topMarg;
                    		compass.setLayoutParams(layoutCoords);
                    		break;
                    	}
                    	if (isXYPositive && prevButton.equals("text")){
                    		//newMyPoint = new MyPoint(iX, currY);
                           // pointsQ.add(newMyPoint);
                            Log.d("moving", "Swyping");
                            gliding = true;
                            prevButton = "gliding";
                            break;
                    	}
                        return true;   
                }           
                return false;
			}
			
        }); 

	  }
	}
                
                */