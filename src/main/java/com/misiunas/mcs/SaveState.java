package com.misiunas.mcs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.misiunas.mcs.game.GameActivity;
import com.misiunas.mcs.game.GameState;
import com.misiunas.mcs.game.internal.GameActivityAbstract;

/** Must make sure Hashes are the same in both functions
 *
 */
public class SaveState {

    private static GameState gameState;

    private SaveState(){}

    public static boolean isThereASave(Activity activity){
        if(gameState != null) return true;
        load(activity);
        if(gameState == null) return false;
        else return true;	
    }


    public static boolean save(GameState gameState, Activity activity){
        if(!checkOwner(activity)) return false;
        SaveState.gameState = gameState;
        int hash = gameState.hashCode();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        editor.putInt("mcs.last_game",  hash * 1231 + 3432361);
        editor.commit();
        try{
            FileOutputStream fos = activity.openFileOutput("GameState", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(gameState);
            out.close();
            fos.close();
            return true;
        }catch(IOException ex){
            Log.e("SaveState", "Could not save the state", ex);
            return false;
        }
    }

    public static GameState get(Activity activity){
        if(!checkOwner(activity)) return  null;
        else if(gameState != null) return gameState;
        else return load(activity);
    }

    private static GameState load(Activity activity){
        int oldHash = PreferenceManager.getDefaultSharedPreferences(activity).getInt("mcs.last_game", 0);
        try{ // load state from file
            FileInputStream fis = activity.openFileInput("GameState");
            ObjectInputStream in = new ObjectInputStream(fis);
            gameState = (GameState) in.readObject();
            in.close();
            fis.close();
            int newHash = gameState.hashCode() * 1231 + 3432361;
            if(oldHash != newHash){
                gameState = null;
                Log.e("Tampering", "Detected tampering with GameState");
            }
        }catch(Exception ex){
            Log.e("SaveState.load", "Error while loading GameState", ex);
        }
        return gameState;
    }

    private static boolean checkOwner(Activity activity){
        return  activity instanceof MainMenuActivity ||
                activity instanceof GameActivity ||
                activity instanceof GameActivityAbstract ||
                activity instanceof NewGameActivity ;
    }

}
