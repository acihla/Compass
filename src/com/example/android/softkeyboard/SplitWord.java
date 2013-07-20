package com.example.android.softkeyboard;

public class SplitWord {
	private String left;
	private String right;
	public SplitWord(String s1, String s2){
		this.left = s1;
		this.right = s2;
	}
	public String getLeft(){
		return left;
	}
	public String getRight(){
		return right;
	}
}

