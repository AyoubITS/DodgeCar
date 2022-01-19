package com.hfa.dodgecars.game;

import android.widget.ImageView;


public class Enemy {
    private final String TAG = "Enemy";

    /**
     * Hitbox ratio of the enemy (depends on the size of the enemy's sprite)
     * ONLY BETWEEN 0f and 1f.
     */
    private final float HITBOX_RATIO = 0.95f;

    private ImageView imageView;
    private float speed;

    private int screenWidth;
    private int screenHeight;

    public Enemy(ImageView imageView, int screenWidth, int screenHeight){
        this.imageView = imageView;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;
    }

    public void update(){
        imageView.setY(imageView.getY() + speed);
        //we also want to move the hitbox of the enemy
    }

    public float getSpeed() {
        return speed;
    }

    public ImageView getImageView(){
        return imageView;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setPosition(float positionX, float positionY){
        imageView.setX(positionX);
        imageView.setY(positionY);
    }

    public float getPositionX(){
        return imageView.getX();
    }

    public float getPositionY(){
        return imageView.getY();
    }

    /**
     * Check if enemy is under the screen.
     */
    public boolean isUnderScreen(){
        return imageView.getY() > screenHeight;
    }

    /**
     * move the hitbox of the enemy depending on his new position
     */
    public void resetHitboxPosition() {
        float centerX = imageView.getX() + imageView.getLayoutParams().width / 2;
        float centerY = imageView.getY() + imageView.getLayoutParams().height / 2;
    }

    @Override
    public String toString() {
        return "Enemy{" +
                ", imageX=" + imageView.getX() +
                ", imageY=" + imageView.getY() +
                ", speed=" + speed +
                '}';
    }
}
