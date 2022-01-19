package com.hfa.dodgecars;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.hfa.dodgecars.R.*;

/**
 * Activity to let the player choose which car he want
 * Based on his maximum Score, he can unlock more car
 * @author Maxence
 */
public class CarChooserActivity extends AppCompatActivity {
    private final static String TAG = "CarChooser";
    public final static String CAR_COLOR_REFERENCE = "carColor";
    public final static String CAR_COLOR_DEFAULT = "red";

    private GridLayout carLayout;
    private SharedPreferences sharedPrefs;
    private int highScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_carchooser);
        carLayout = findViewById(id.carChooser);
        initCellsSize();
        sharedPrefs = getSharedPreferences(getString(string.prefs_car_key), Context.MODE_PRIVATE);

        //Now, we show which car the player have selected, base on the value from sharedPrefs
        resetCarBackground();
        String carColor = sharedPrefs.getString(CAR_COLOR_REFERENCE, CAR_COLOR_DEFAULT);
        drawSelectedCar(carColor);
    }



    /**
     * Define the size of each cell of the grid depending on the size of the grid layout
     */
    private void initCellsSize() {
        //we need to wait until the layout is finally drawn in order to get the grid measurements
        carLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "Layout drawn ! Changing cell sizes...");
                int carLayoutWidth = carLayout.getMeasuredWidth();
                int carLayoutHeight = carLayout.getMeasuredHeight();

                //we want a 3x2 grid
                int cellWidth = carLayoutWidth / 3;
                int cellHeight = carLayoutHeight / 2;

                //apply those dimensions to all Linear in the car layout
                View subLayout;
                //each cell of the grid is filled with one image view and one text view (in this order)
                for (int i = 0; i < carLayout.getChildCount(); i++) {
                    subLayout = carLayout.getChildAt(i);
                    if (subLayout instanceof LinearLayout) { // security check
                        ViewGroup.LayoutParams layoutParams = subLayout.getLayoutParams();
                        layoutParams.width = cellWidth;
                        layoutParams.height = cellHeight;
                        subLayout.setLayoutParams(layoutParams);
                    }
                }
                //we don't have to check this event anymore
                carLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        highScore = sharedPrefs.getInt(ScoresTableActivity.BEST_SCORE_SHARED_KEY, 1);
        initHighScoreText();
    }

    private void initHighScoreText(){
        TextView textView = findViewById(id.userMaxScore);
        textView.setText("Current high score : " + highScore);
    }

    /**
     * Called when the user press on an ImageButton to choose a new car
     * @param view which image was pressed
     */
    public void selectCar(View view) {
        String imgTag = view.getTag().toString();
        Log.i(TAG, imgTag);
        Log.i(TAG, String.valueOf(highScore));

        //get the required score to unlock the car
        int requiredScore;
        switch (imgTag) {
            case "red":
                requiredScore = getResources().getInteger(integer.scoreToUnlock_red);
                break;
            case "blue":
                requiredScore = getResources().getInteger(integer.scoreToUnlock_blue);
                break;
            case "brown":
                requiredScore = getResources().getInteger(integer.scoreToUnlock_brown);
                break;
            case "pink":
                requiredScore = getResources().getInteger(integer.scoreToUnlock_pink);
                break;
            case "reverse":
                requiredScore = getResources().getInteger(integer.scoreToUnlock_reverse);
                break;
            case "swag":
                requiredScore = getResources().getInteger(integer.scoreToUnlock_swag);
                break;
            default:
                //in doubt, let them have the car
                requiredScore = -1;
        }

        if (highScore > requiredScore) {
            //draw the selection
            resetCarBackground();
            drawSelectedCar(imgTag);

            //put the chosen car in sharedPrefs
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(CAR_COLOR_REFERENCE, imgTag);
            editor.apply();
        }else{
            // show to user that he has not enough points
            Toast.makeText(this, "You don't have reach the required score to get this car color", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set the background to DarkGrey for all element in carLayout
     */
    private void resetCarBackground() {
        View subLayout;
        //each cell of the grid is filled with one image view and one text view (in this order)
        for (int i = 0; i < carLayout.getChildCount(); i++) {
            subLayout = carLayout.getChildAt(i);
            if (subLayout instanceof LinearLayout) { // security check
                //find the real image in the layout (the image is always the first element in the layout)
                View carImage = ((LinearLayout) subLayout).getChildAt(0);
                if(carImage instanceof ImageView){
                    carImage.setBackgroundColor(Color.DKGRAY);
                }
            }
        }
    }

    /**
     * Set the background to Green for the selected element in carLayout
     * @param selectedCar the car to be shown in Green
     */
    private void drawSelectedCar(String selectedCar) {
        View subLayout;
        //each cell of the grid is filled with one image view and one text view (in this order)
        for (int i = 0; i < carLayout.getChildCount(); i++) {
            subLayout = carLayout.getChildAt(i);
            if (subLayout instanceof LinearLayout) { // security check
                //find the real image in the layout (the image is always the first element in the layout)
                View carImage = ((LinearLayout) subLayout).getChildAt(0);
                if(carImage instanceof ImageView){
                    if (carImage.getTag().equals(selectedCar)) {
                        carImage.setBackgroundColor(Color.GREEN);
                    }
                }
            }
        }
    }

    /**
     * return to the MainActivity
     * @param view button pressed
     */
    public void previous(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
