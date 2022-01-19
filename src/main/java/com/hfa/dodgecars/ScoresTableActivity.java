package com.hfa.dodgecars;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hfa.dodgecars.scores.DatabaseManager;
import com.hfa.dodgecars.scores.ListViewAdapter;
import com.hfa.dodgecars.scores.PlayerData;
import com.hfa.dodgecars.scores.SaveScoreService;

import java.util.List;

/**
 * Class allowing to display the score in the "activity_scores.xml" and use the service of our application.
 */
public class ScoresTableActivity extends AppCompatActivity {
    private static final String TAG = "ScoreTableActivity";
    private DatabaseManager databaseManager = new DatabaseManager(this);
    public static final String INTENT_FILTER = "filter";

    //Intent extra names for service
    public static final String SCORE_INTENT_EXTRA = "playerScore";

    //fields in shared preferences
    public static final String NAME_SHARED_KEY = "playerName";
    public static final String BEST_SCORE_SHARED_KEY = "bestScore";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        //if database is empty, add 30 random scores from 30 random players
        if (databaseManager.isTableEmpty()) {
            databaseManager.fillTable();
        }

        //now, check if we have a score in intent : if it is the case, the player was in game before
        Intent intent = getIntent();
        int playerScore = intent.getIntExtra(GameActivity.SCORE_INTENT_EXTRA,-1);
        //if playerScore is the same as the default value, we know that we don't have any score in the intent
        if(playerScore >= 0) {
            processPlayerScore(playerScore);
        }else{
            //we don't have to do anything (we came from main menu)
            displayScores();
        }
        databaseManager.close();
    }

    /**
     * Start the service that will manage the player score.
     * @param score the score that player obtained during his last game
     */
    private void processPlayerScore(int score){
        Log.d(TAG, "Starting save score service...");
        Intent intent = new Intent(this, SaveScoreService.class);
        //don't forget to add the score in the intent
        intent.putExtra(SCORE_INTENT_EXTRA, score);
        startService(intent);
    }

    /**
     * Start a new game
     */
    public void returnInGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(scoreReceiver, new IntentFilter(INTENT_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(scoreReceiver);
    }

    /**
     *  BroadcastReceiver that waits for a signal from SaveScoreService
     */
    private BroadcastReceiver scoreReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Score registration finished. Display scores.");
            displayScores();
        }

    };

    /**
     * Go back to main menu activity
     */
    public void returnToMainMenu(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    /**
     * Retrieve all scores in a list and display them on the screen
     */
    public void displayScores(){
        List<PlayerData> scores = databaseManager.getAllRows();

        // Create ListView
        ListView listView = (ListView) findViewById(R.id.Scores_Table);
        listView.setAdapter(new ListViewAdapter(ScoresTableActivity.this, android.R.layout.simple_list_item_1, scores));
    }
}
