package com.example.android.softkeyboard;

import android.graphics.Canvas;
import android.graphics.Paint;

public class MyPoint  {

    public float x; 
    public float y; 
    Paint pWhite = new Paint(1);

    public MyPoint(float x, float y) {

           this.x = x;
           this.y = y;
    }//end const
    public void draw(Canvas canvas) {
        canvas.drawPoint(this.x, this.y, pWhite);
    }//end method
}//end MyPoint Class