package com.hfa.dodgecars.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hfa.dodgecars.CarChooserActivity;
import com.hfa.dodgecars.GameActivity;
import com.hfa.dodgecars.R;

/**
 * Used to control player with the orientation of the phone
 * <pre>
 *   Class created with the help of
 *   https://google-developer-training.github.io/android-developer-advanced-course-practicals/unit-1-expand-the-user-experience/lesson-3-sensors/3-2-p-working-with-sensor-based-orientation/3-2-p-working-with-sensor-based-orientation.html
 * </pre>
 */
public class PlayerController{
    private final String TAG = "PlayerController";

    /**
     * Hitbox ratio of the player's car (depends on the size of the player's sprite)
     * ONLY BETWEEN 0f and 1f.
     */
    private final float HITBOX_RATIO = 0.9f;


    private GameActivity activity;
    private SharedPreferences sharedPrefs;
    private ImageView playerImage;

    /**
     * All attributes relative to phone orientation management
     */
    private OrientationSensorListener orientationSensorListener = new OrientationSensorListener();
    SensorManager sensorManager;
    Sensor sensorAccelerometer;
    float accelerometerValues[] = new float[3];
    Sensor sensorMagneticField;
    float magneticValues[] = new float[3];
    float rotationMatrix[] = new float[9];
    //we only care about roll, since we could only move the car from left to right
    float rollOrientation = 0f;

    float leftLimitPosX;
    float rightLimitPosX;

    /**
     * Constants used for speed and sensitivity, modify them to change gameplay feelings.
     */
    private final int PLAYER_SPEED = 30;
    //all rotation values under this constant will not be taken account
    private final float ROTATION_THRESHOLD = 0.1f;

    /**
     * Instantiate the player controller : initiate the player sprite and the sensor
     * @param activity the activity the player controller is related to
     */
    public PlayerController(GameActivity activity){
        this.activity = activity;
        this.sharedPrefs = activity.getSharedPreferences(activity.getString(R.string.prefs_car_key), Context.MODE_PRIVATE);
        initPlayerSprite();
        initSensor();
    }

    /**
     * Initialize the sprite of the player used in game
     */
    private void initPlayerSprite(){
        int spriteWidth = activity.getSpriteWidth();
        int spriteHeight = activity.getSpriteHeight();
        int screenWidth = activity.getScreenWidth();

        //load the player sprite (already added to the view)
        playerImage = activity.findViewById(R.id.playerCar);
        String carColor = sharedPrefs.getString(CarChooserActivity.CAR_COLOR_REFERENCE, CarChooserActivity.CAR_COLOR_DEFAULT);
        String carSkinName = "@drawable/car_"+carColor;

        //now that we have a reference to the selected car, change the skin to it
        int imageResource = activity.getResources().getIdentifier(carSkinName, String.valueOf(R.drawable.car_red), activity.getPackageName());
        playerImage.setImageDrawable(activity.getDrawable(imageResource));
      
        //we have to set size of sprites depending on the screen size
        playerImage.setScaleType(ImageView.ScaleType.FIT_XY);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(spriteWidth, spriteHeight);
        playerImage.setLayoutParams(layoutParams);

        //put player at bottom center of the screen
        playerImage.setZ(1);
        playerImage.setY(activity.getScreenHeight() - spriteHeight - activity.getSquareSizeY());
        playerImage.setX(screenWidth / 2 - spriteWidth / 2);

        //calculate the player movement limits too
        leftLimitPosX = 0f;
        rightLimitPosX = (float)(screenWidth - spriteWidth);
    }

    private void initSensor(){
        /*Sensor type TYPE_ORIENTATION is deprecated, so we have to get those values with accelerometer and magnetic sensor*/
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        enableSensors();
    }

    /**
     * Set the new position of the player depending on the orientation of the device
     */
    public void updatePlayerMovement(){
        //check if rotation value is greater than threshold (in order to don't count very little movements)
        if(Math.abs(rollOrientation) > ROTATION_THRESHOLD){
            //store the initial position for hitbox movement
            float initialPositionX = playerImage.getX();

            //the speed of the movement depends on the angle of the rotation (a small rotation will make the player go slowly and vice versa)
            float movementVectorX = rollOrientation * PLAYER_SPEED;

            //now calculate the new position of the player : it must not be out of the screen of course
            float newPlayerPosX = initialPositionX;
            newPlayerPosX += movementVectorX;

            if(newPlayerPosX < leftLimitPosX) {
                newPlayerPosX = leftLimitPosX;
            }else if(newPlayerPosX > rightLimitPosX){
                newPlayerPosX = rightLimitPosX;
            }

            //assign this new position to the player
            playerImage.setX(newPlayerPosX);
        }
    }

    /**
     * Enables the sensors used to control player. Call this method after disabling them
     */
    public void enableSensors(){
        sensorManager.registerListener(orientationSensorListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(orientationSensorListener, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Disables sensors used to control player. Call this method when activity is paused.
     */
    public void disableSensors(){
        sensorManager.unregisterListener(orientationSensorListener);
    }

    public ImageView getImageView() {
        return playerImage;
    }

    class OrientationSensorListener implements SensorEventListener{
        private final String TAG_LISTENER = "OrientationSensorListener";
        @Override
        public void onSensorChanged(SensorEvent event) {
            synchronized (this){
                //update values from one or another type
                switch(event.sensor.getType()){
                    case Sensor.TYPE_ACCELEROMETER:
                        accelerometerValues = event.values.clone();
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        magneticValues = event.values.clone();
                        break;
                    default:
                        return;
                }

                //calculate the rotation of the device
                boolean isRotationSuccessful = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerValues, magneticValues);
                //SensorManager#getRotationMatrix can not return result under certain conditions (as a free fall of the device, unlikely to produce when playing though)
                if(isRotationSuccessful){
                    float orientationValues[] = new float[3];
                    SensorManager.getOrientation(rotationMatrix, orientationValues);
                    rollOrientation = orientationValues[2];
                }
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG_LISTENER, "Accuracy changed, new accuracy level : " + accuracy);
        }
    }
}
