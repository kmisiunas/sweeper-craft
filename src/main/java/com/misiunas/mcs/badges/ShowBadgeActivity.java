package com.misiunas.mcs.badges;


import com.misiunas.mcs.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;

public class ShowBadgeActivity extends Activity {

    public static final String KEY_BADGE = "com.misiunas.mcs.badges.badge";

    protected long timeActivityStarted;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_badge);

        BadgeUI badge = (BadgeUI) getIntent().getSerializableExtra(KEY_BADGE);
        if (badge == null) {
            finish();
        }
        configure(badge);

        timeActivityStarted = System.currentTimeMillis();

        // todo configureGoogleSignInButton();
    }

    private void configureGoogleSignInButton() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return; //no network
        }

//        final GameHelper gameHelper = ((MCSweeperApp) getApplication()).getGameHelper();
//        gameHelper.setActivity(this);
//        if (!gameHelper.isSignedIn()) {
//            final SignInButton button = (SignInButton) findViewById(R.id.sign_in_button);
//            final LinearLayout buttonBackground = (LinearLayout) findViewById(R.id.linearLayout_signin);
//            button.setSize(SignInButton.SIZE_WIDE);
//            buttonBackground.setVisibility(View.VISIBLE);
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    gameHelper.beginUserInitiatedSignIn(ShowBadgeActivity.this, new GameHelper.GameHelperListener() {
//                        @Override
//                        public void onSignInFailed() {
//                            button.setClickable(true);
//                        }
//
//                        @Override
//                        public void onSignInSucceeded() {
//                            buttonBackground.setVisibility(View.GONE);
//                        }
//                    });
//                    button.setClickable(false);
//                }
//            });
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        ((MCSweeperApp)getApplication()).getGameHelper().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP: {
                if (System.currentTimeMillis() >= timeActivityStarted + 1000) // check that activity was shown at least for 1 sec
                    finish();
                break;
            }
        }
        return true;
    }

    /**
     * Method for configuring the show badge by a custom badge
     */
    public void configure(BadgeUI badge) {
        ((ImageView) findViewById(R.id.imageViewBadge)).setImageResource(badge.image());
        ((TextView) findViewById(R.id.textViewBadgeTitle)).setText(badge.title());
        ((TextView) findViewById(R.id.textViewBadgeMessage)).setText(badge.message());
    }
}
