package com.misiunas.mcs;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.FrameLayout;


/**
 * Wrapper class to show a single Fragment.
 */
public class NewGameActivity extends FragmentActivity {

    private final static int FRAME_ID = 917364123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(FRAME_ID);
        setContentView(frame, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        getSupportFragmentManager().beginTransaction()
                .add(FRAME_ID, new NewGameFragment())
                .commit();
    }
}
