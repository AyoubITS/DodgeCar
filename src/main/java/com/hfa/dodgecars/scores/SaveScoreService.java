package com.hfa.dodgecars.scores;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hfa.dodgecars.R;
import com.hfa.dodgecars.ScoresTableActivity;



/**
 * Allows to do the service, that consists in updating the best score in "SharedPreferences" if necessary,
 * or in inserting in the SQLite if the score is quite high and launch the "BroadcastReceiver" that
 * display the table score in the layout.
 */
public class SaveScoreService extends IntentService {
    private static final String TAG = "SaveScoreService";
    private SharedPreferences sharedPrefs;
    private DatabaseManager db = new DatabaseManager(this);
    private int bestScore;

    //Default name when add to scoreboard
    private static final String DEFAULT_NAME = "You";

    public SaveScoreService() {
        super("SaveScoreService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Get the information from the intent
        if (intent != null) {
            sharedPrefs = getSharedPreferences(getString(R.string.prefs_car_key), Context.MODE_PRIVATE);

            int oldPlayerScore = intent.getIntExtra(ScoresTableActivity.SCORE_INTENT_EXTRA, -1);
            if (oldPlayerScore == -1) {
                //we don't have any score to register, don't do anything
                Log.w(TAG, "No score provided, service cancelled");
            } else {
                //if we have a player name in the shared preferences, get it too (we have default value else)
                String playerName = sharedPrefs.getString(ScoresTableActivity.NAME_SHARED_KEY, DEFAULT_NAME);

                this.bestScore = sharedPrefs.getInt(ScoresTableActivity.BEST_SCORE_SHARED_KEY, 1);

                //if the player done a new best score
                if (this.bestScore < oldPlayerScore) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putInt(ScoresTableActivity.BEST_SCORE_SHARED_KEY, oldPlayerScore);
                    if (editor.commit()) {
                        Log.i(TAG, "New best score registered !");
                    } else {
                        Log.e(TAG, "An error occurred while register new score");
                    }
                }

                // We will try to insert this score in the database (only if it is at least higher than the lowest score in the table)
                if (db.insertNewScore(playerName, oldPlayerScore)) {
                    Log.i(TAG, "A new score has been registered.");
                } else {
                    Log.i(TAG, "The player score was not high enough to enter in the database");
                }
            }

            // If all of this is done, say to the activity that we finished here
            Log.d(TAG, "Done !");
            Intent scoreIntent = new Intent(ScoresTableActivity.INTENT_FILTER);
            LocalBroadcastManager.getInstance(this).sendBroadcast(scoreIntent);
        }
    }
}