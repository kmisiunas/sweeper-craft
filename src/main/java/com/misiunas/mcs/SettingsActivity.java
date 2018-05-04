package com.misiunas.mcs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.analytics.Tracker;
import java.util.Locale;

import com.misiunas.mcs.highscore.GoogleSignIn;
import com.misiunas.mcs.minor.ShareOnSocialMedia;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by kmisiunas on 30/03/2014.
 */
public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener ,
        Preference.OnPreferenceClickListener {

    protected Tracker tracker = null;
    protected GoogleSignIn gameHelper = null;
    private Preference loginButton = null;
    private Preference shareButton = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        tracker = ((SweeperCraftApp)this.getApplication()).getDefaultTracker();
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        gameHelper = ((SweeperCraftApp)this.getApplication()).getGameHelper(this);
        loginButton = findPreference("mcs.settings_login");
        loginButton.setOnPreferenceClickListener(this);
        shareButton = findPreference("mcs.settings_spread_the_word");
        shareButton.setOnPreferenceClickListener(this);
        renderVersions();
    }


    @Override
    protected void onResume() {
        super.onResume();
        gameHelper.onStart(this);
        EventBus.getDefault().register(this);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateLoginButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //gameHelper.onStop(); // no point having it here
        EventBus.getDefault().unregister(this);
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        gameHelper.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Language", "trigger key="+key);
        if (key.equals("mcs.settings_language")){
            Locale myLocale = Settings.language(this);
            Log.d("Language", "interpreted="+myLocale.getLanguage());
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, SettingsActivity.class);
            startActivity(refresh);
            finish();

        }

    }


    private void updateLoginButton(){
        if (gameHelper.isSignedInSafe()) {
            loginButton.setTitle(R.string.settings_logout);
            loginButton.setSummary(R.string.settings_logout_explanation);
        } else if (!gameHelper.canSignIn()) { // show button but don't built expectations
            loginButton.setTitle(R.string.settings_login);
            loginButton.setSummary(R.string.highscores_go_online);
        } else { // can sign in
            loginButton.setTitle(R.string.settings_login);
            loginButton.setSummary(R.string.settings_login_explanation);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == loginButton){
            if (gameHelper.isSignedInSafe()) { //
                gameHelper.signOut();
                updateLoginButton();
                Log.d("Sign In", "Sign Out button clicked");
            } else if (gameHelper.canSignIn()) {
                gameHelper.onStart(this);
                gameHelper.beginUserInitiatedSignIn(); // todo does not work like expected...
                //loginButton.setSummary(R.string.settings_in_progress);
                Log.d("Sign In", "Sign In button clicked");
            } else {
                Log.d("Sign In", "Sign In button clicked, but no action");
            }
        }
        else if (preference == shareButton){
            ShareOnSocialMedia.share(this);
        }
        return true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GoogleSignIn.SignInFailed event)  {
        Toast.makeText(this, getResources().getText(R.string.gamehelper_sign_in_failed), Toast.LENGTH_SHORT).show();
        updateLoginButton();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GoogleSignIn.SignInSucceeded event)  {
        updateLoginButton();
    }


    private void renderVersions(){
        try{
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            Preference about = findPreference("mcs.settings_about");
            String raw = about.getSummary().toString();
            String fin = raw.replace("[versionName]", versionName).replace("[versionCode]", ""+versionCode);
            about.setSummary(fin);

        } catch (Exception e){ }
    }

}