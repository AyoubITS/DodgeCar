package com.hfa.dodgecars.game;

import android.util.Log;

/**
 *  Simple score system that calculates depending on the frame rate
 */
public class Score {

    /**
     * Points to give to the player each frame
     */
    private final int SCORE_INCREASE_PER_FRAME = 1;
    /**
     * Each SCORE_BETWEEN_SPEED_UP points earned, enemies will speed up
     */
    private final int SCORE_STEP = 1000;

    private int currentScore;
    private int nextScoreStep = SCORE_STEP;

    public Score(){
        this(0);
    }

    public Score(int initialScore){
        this.currentScore = initialScore;
    }

    public int getScore() {
        return currentScore;
    }

    /**
     * Increase total score by {@link Score#SCORE_INCREASE_PER_FRAME}, if it exceeds the current score step, returns true
     */
    public boolean addScore(){
        currentScore += SCORE_INCREASE_PER_FRAME;
        if(currentScore >= nextScoreStep){
            nextScoreStep += SCORE_STEP;
            Log.d("Score", "Score steap reached, next step : " + nextScoreStep);
            return true;
        }
        return false;
    }
}
