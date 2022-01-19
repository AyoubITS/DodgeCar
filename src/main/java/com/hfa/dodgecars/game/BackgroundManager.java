package com.hfa.dodgecars.game;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hfa.dodgecars.GameActivity;
import com.hfa.dodgecars.R;

/**
 * Class used to manage all sprites relative to the lines on the road
 */
public class BackgroundManager {
    private final String TAG = "BackgroundManager";

    private GameActivity activity;

    /*Change this to have different number of lines displayed on the road*/
    private final int NUMBER_LINES_ON_ROAD = 4;
    private ImageView squares[] = new ImageView[NUMBER_LINES_ON_ROAD];

    int squareSizeX;
    int squareSizeY;

    /**
     * Scrolling speed in dp / sec. Bigger the number is, faster the scrolling will be
     */
    private final int BACKGROUND_SPEED = 20;

    public BackgroundManager(GameActivity activity, RelativeLayout layout, int carSpriteHeight){
        this.activity = activity;

        //create all squares needed, and set size and position
        squareSizeX = activity.getScreenWidth() / 11;
        squareSizeY = activity.getScreenHeight() / (NUMBER_LINES_ON_ROAD - 1) - carSpriteHeight;
        int posX = activity.getScreenWidth() / 2 - squareSizeX / 2;
        int posY = activity.getScreenHeight() - squareSizeY;

        for (int i = 0; i < NUMBER_LINES_ON_ROAD; i++){
            ImageView square = new ImageView(activity);
            square.setImageDrawable(activity.getDrawable(R.drawable.white_square));
            //create the layout params before adding it to the image
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(squareSizeX, squareSizeY);
            square.setLayoutParams(layoutParams);
            //positioning depends on the last positioned element
            //(the first line will be stuck at the bottom of the screen  and the last will be above screen)
            //but all will be at the middle of the screen
            square.setZ(-1);
            square.setX(posX);
            square.setY(posY);


            //decrease position by [square height] + [half square height] in order to have space between each other (as in a real road)
            posY -= squareSizeY + carSpriteHeight;

            //add this view to the layout
            layout.addView(square);
            //and store it in the array for later
            squares[i] = square;
        }
    }

    /**
     * Move all the squares down by a specific value {@link BackgroundManager#BACKGROUND_SPEED}. This method is used in the game thread in order to show a movement
     */
    public void nextStep(){
        /**
         * Move all squares to bottom, if one is not visible anymore, put it back on top
         */
        for (ImageView square : squares) {
            square.setY(square.getY() + BACKGROUND_SPEED);
            if(square.getY() > activity.getScreenHeight() + squareSizeY){
                square.setY(-squareSizeY);
            }
        }
    }

    public int getSquareSizeX() {
        return squareSizeX;
    }

    public int getSquareSizeY() {
        return squareSizeY;
    }
}
