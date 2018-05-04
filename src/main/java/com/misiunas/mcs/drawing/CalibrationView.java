package com.misiunas.mcs.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.misiunas.mcs.Settings;
import com.misiunas.mcs.game.GameState;


// Last modified on 24/20/2011

public class CalibrationView extends View {

    protected GameState gameState; // current state of the game

    protected int width;	// number of pixels available
    protected int height;

    protected int arr_width; // size of rendered array
    protected int arr_height;

    protected int firstSquareX; // the squere that the rendering begins at
    protected int firstSquareY;

    protected int corrnerX;	// corrner where stationary visable screen starts
    protected int corrnerY; 



    public CalibrationView(Context context, int width, int height) {
        super(context);
        this.width = width;
        this.height = height;  
    }

    //Draws image when asked
    protected void onDraw(Canvas canvas) {
        //white bg
        Paint b = new Paint();
        b.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, b);
        // Test - for x cross 
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        canvas.drawLine(testX-100, testY, testX+100, testY, p);
        canvas.drawLine(testX, testY-100, testX, testY+100, p); 
    }

    /** Testing method for drawing a line where button was pressed */
    public void setTestCross(int x, int y){
        testX=translateX(x);
        testY=translateY(y);
    }
    private int testX, testY; // testing variables



    /** finds offset within one squere, for continuous sqrooling */
    protected int offsetX(){ return (corrnerX/ Settings.zoom(this.getContext()))*Settings.zoom(this.getContext())- corrnerX; }

    /** finds offset within one squere, for continuous sqrooling */
    protected int offsetY(){ return (corrnerY/Settings.zoom(this.getContext()))*Settings.zoom(this.getContext()) - corrnerY; }

    /** translates screen position to the scroll screen position */
    private int translateX(int x){
        return x + width;//-offsetX();
    }

    /** translates screen position to the scroll screen position */
    private int translateY(int y){
        return y + height-35; // number acounts for correction in vertical direction (calibration)
    }

}
