package com.hfa.dodgecars;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hfa.dodgecars.game.BackgroundManager;
import com.hfa.dodgecars.game.EnemiesManager;
import com.hfa.dodgecars.game.GameState;
import com.hfa.dodgecars.game.PlayerController;
import com.hfa.dodgecars.game.Score;
import com.hfa.dodgecars.helpers.ScreenCalculator;

import java.util.Timer;
import java.util.TimerTask;


public class GameActivity extends AppCompatActivity {
    private final String TAG = "GameActivity";

    private BackgroundManager backgroundManager;
    private PlayerController playerController;
    private Score score;
    private EnemiesManager enemiesManager;

    private RelativeLayout relativeLayout;
    private LinearLayout uiLayout;
    private LinearLayout pauseLayout;
    private TextView scoreTextView;

    private int screenWidth;
    private int screenHeight;

    private int spriteWidth;
    private int spriteHeight;

    private Handler handler = new Handler();
    private Timer timer;
    private GameState currentGameState;
    private boolean isPlaying = false;

    private final int TIME_BETWEEN_FRAMES = 16;

    public static final String SCORE_INTENT_EXTRA = "Score";


    /*APP LIFECYCLE*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        relativeLayout = (RelativeLayout) findViewById(R.id.frameLayout);
        uiLayout = (LinearLayout) findViewById(R.id.ui_layout);
        pauseLayout = (LinearLayout) findViewById(R.id.pause_layout);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);

        //we have to keep user from changing phone orientation (only portrait mode)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //init all dimensions used for displaying
        screenWidth = ScreenCalculator.getScreenWidth(this);
        screenHeight = ScreenCalculator.getScreenHeight(this);
        spriteWidth = screenWidth / 6;
        spriteHeight = 5 * spriteWidth / 3;

        backgroundManager = new BackgroundManager(this, relativeLayout, spriteHeight);
        playerController = new PlayerController(this);
        score = new Score();
        enemiesManager = new EnemiesManager(this, relativeLayout);

        //hide and disable the pause layout, and start the game thread
        setGameState(GameState.PLAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(currentGameState == GameState.PLAY){
            setGameState(GameState.PAUSE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(currentGameState == GameState.PLAY){
            setGameState(GameState.PAUSE);
        }
    }

    /**
     * We don't want that the user could return to the previous screen easily
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    /**
     * Set all behavior relative to the game pause and play.
     * It will manage game thread state and pause layout visibility.
     * If game is already in the right state, nothing will happen
     * @param isActive if the game has to be isActive or not
     * @deprecated use {@link GameActivity#setGameState(GameState)} instead
     */
    private void setGameActive(boolean isActive) {
        //check if the pause state is not already set
        if (isActive != isPlaying) {
            //if isActive == false, this will also stop the current thread
            isPlaying = isActive;
            if (isActive) {
                //change the ui layout and enable the player controller
                uiLayout.setVisibility(View.VISIBLE);
                pauseLayout.setVisibility(View.INVISIBLE);
                playerController.enableSensors();
                //start a loop for the game with no delay and 16 milliseconds between each iteration (60 fps)
                // ==> see inner classes GameTask and GameThread
                timer.schedule(new GameTask(), 0, TIME_BETWEEN_FRAMES);

            } else {
                //change the ui layout and disable the player controller (for power saving)
                uiLayout.setVisibility(View.INVISIBLE);
                pauseLayout.setVisibility(View.VISIBLE);
                playerController.disableSensors();
                //handle the timer
                stopTimer();
            }
        }
    }

    /**
     * Set all behavior relative to the game pause and play.
     * It will manage game thread state and pause layout visibility.
     * If game is already in the right state, nothing will happen
     *
     * @param gameState the state of the game, either GameState.PLAY or GameState.PAUSE or GameState.QUIT
     */
    private void setGameState(GameState gameState) {
        if (gameState != currentGameState) {
            currentGameState = gameState;
            switch (gameState) {
                case PLAY:
                    //change the ui layout and enable the player controller
                    uiLayout.setVisibility(View.VISIBLE);
                    pauseLayout.setVisibility(View.INVISIBLE);
                    playerController.enableSensors();
                    //start a loop for the game with no delay and 16 milliseconds between each iteration (60 fps)
                    // ==> see inner classes GameTask and GameThread
                    timer = new Timer();
                    timer.schedule(new GameTask(), 0, TIME_BETWEEN_FRAMES);
                    break;
                case PAUSE:
                    //change the ui layout and disable the player controller (for power saving)
                    uiLayout.setVisibility(View.INVISIBLE);
                    pauseLayout.setVisibility(View.VISIBLE);
                    playerController.disableSensors();
                    //handle the timer
                    stopTimer();
                    break;
                case QUIT:
                    //just handle the timer
                    stopTimer();
                    break;
                default:
                    Log.wtf(TAG, "How did you get there with a non-existent game state ?");
                    throw new IllegalArgumentException("Game state non recognized");
            }
        }
    }

    /**
     * Stop the game flow by stopping the timer scheduled.
     * If the timer reference is null, nothing will be done.
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /*LAYOUT METHODS*/



    /*LISTENERS*/

    /**
     * Suspend the game thread and display the pause screen.
     */
    public void pauseGame(View view) {
        setGameState(GameState.PAUSE);
    }


    public void resumeGame(View view) {
        setGameState(GameState.PLAY);
    }


    public void quitGame(View view) {
        Log.d(TAG, "Return to menu.");
        setGameState(GameState.QUIT);
        //return to menu, without forgetting to kill this game activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    /*GETTERS AND SETTERS*/
    public int getScreenHeight() {
        return screenHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public int getSquareSizeY() {
        return backgroundManager.getSquareSizeY();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    /*GAME RELATIVE METHODS*/

    /**
     * Tries to detect any collision between the player and enemies.
     * If it happens, go to the score activity creating intent
     */
    private void checkForCollision() {
        if (enemiesManager.isThereCollision(playerController)) {
            //we have to stop the game (i.e the game thread)
            setGameState(GameState.QUIT);
            Log.d(TAG, "Game finished. Final score : " + score.getScore());
            //launch score activity (with score in extras) and destroy the game activity
            Intent intent = new Intent(GameActivity.this, ScoresTableActivity.class);
            intent.putExtra(SCORE_INTENT_EXTRA, score.getScore());
            finish();
            startActivity(intent);
        }
    }

    private void updateScore() {
        //increase score, it it returns true, that means that we have to increase enemies speed
        if (score.addScore()) {
            enemiesManager.increaseEnemySpeed();
        }
        //update the score text label
        //we can't do this on another thread, we have to do this on the ui thread
        runOnUiThread(() -> scoreTextView.setText(Integer.toString(score.getScore())));
    }

    class GameTask extends TimerTask {
        @Override
        public void run() {
            handler.post(new GameThread());
        }
    }

    class GameThread implements Runnable {
        @Override
        public void run() {
            /*
             * We will keep track of the elapsed time to do the logic and render a frame.
             * Instead of only sleep for fixed amount of time, we will wait depending on the time needed to render the previous frame
             */
            //long timeStart, deltaTime;
            //get the time at the beginning of the rendering
            //timeStart = System.nanoTime();

            /*logic and rendering*/
            backgroundManager.nextStep();
            playerController.updatePlayerMovement();
            enemiesManager.updateEnemies();
            checkForCollision();
            updateScore();


            //end of rendering : we check the time elapsed during this frame
                /*deltaTime = (System.nanoTime() - timeStart) / 1000000;
                try {
                    if (deltaTime < TIME_BETWEEN_FRAMES) {
                        Thread.sleep(TIME_BETWEEN_FRAMES - deltaTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
        }
    }

}