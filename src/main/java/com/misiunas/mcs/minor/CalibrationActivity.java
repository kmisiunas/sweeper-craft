package com.misiunas.mcs.minor;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.Tracker;
import com.misiunas.mcs.SweeperCraftApp;
import com.misiunas.mcs.R;
import com.misiunas.mcs.Settings;
import com.misiunas.mcs.drawing.CalibrationView;
import com.misiunas.mcs.game.Pos;

public class CalibrationActivity extends Activity {

    protected Tracker googleTracker = null;


    private RelativeLayout container;

    protected CalibrationView view; // Main game view

    protected int nr = 0; // number of calibration points
    protected int offsetSumY = 0; // sum of offsets in y direction
    protected int drawPositionY; //position to draw line in


    protected void onCreate(Bundle savedInstanceState) {
        googleTracker = ((SweeperCraftApp)this.getApplication()).getDefaultTracker();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate);

        view = new CalibrationView(this, getWindowWidth(), getWindowHeight());

        /** Handles back button */
        Button b_back = (Button) findViewById(R.id.button_reset_calibration);
        b_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveCalibrationY(0);
                finish();
            }
        });

        // Drawing main view container.
        container = (RelativeLayout)findViewById(R.id.container);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(-getWindowWidth(), -getWindowHeight(), -getWindowWidth(), -getWindowHeight()); 
        container.addView(view, layoutParams);
        drawNewLine();
    }  

    /** Method for savin some calibration parameter */
    public void saveCalibrationY(int y){
        Log.d("touch", "calibration: "+y);
        Settings.setTouchCalibration(this, Pos.apply(0, y) );
    }

    /** Method for drawing a line on the screen */
    public void drawNewLine(){
        int h1 = 50;
        int ry = (int)((getWindowHeight()-h1-120)*Math.random()+h1);
        view.setTestCross(getWindowWidth()/2, ry); // test
        this.drawPositionY = ry;
        view.invalidate(); // updates wiew
    }



    @Override 
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            offsetSumY = (int)(offsetSumY  - event.getRawY() + drawPositionY);
            nr++;
            if(nr==3){
                saveCalibrationY(offsetSumY / nr);
                this.finish();
            }
            drawNewLine();
            break;
        }
        }
        return true; 	
    }


    // ################ CUSTOM METHODS ################


    public int getWindowWidth(){ 
        Display display = getWindowManager().getDefaultDisplay(); 
        return display.getWidth();
    }

    public int getWindowHeight(){ 
        Display display = getWindowManager().getDefaultDisplay(); 
        return display.getHeight();
    }


}