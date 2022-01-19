package com.hfa.dodgecars;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * Simply launch the activity GameActivity in order to start a new game.
     */
    public void play(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    /**
     * Go to the CarChooserActivity
     */
    public void chooseCar(View view) {
        Intent intent = new Intent(this, CarChooserActivity.class);
        startActivity(intent);
    }

    /**
     * Go to the scores activity (with empty intent, because we don't have registered any new score)
     */
    public void seeScores(View view) {
        Intent intent = new Intent(this, ScoresTableActivity.class);
        startActivity(intent);
    }

    /**
     * Exit the application after asking user's confirmation
     */
    public void exitApp(View view) {
        //create the confirm dialog pop-up
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirmExitTitle);
        builder.setMessage(R.string.confirmExitMessage);
        //if user click on yes, quit app
        builder.setPositiveButton(R.string.confirmExitYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });
        //else, we don't want to do anything
        builder.setNegativeButton(R.string.confirmExitNo, null);
        builder.show();
    }
}