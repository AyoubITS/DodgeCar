package com.hfa.dodgecars.game;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hfa.dodgecars.GameActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manager for all enemies, sprites and behavior.
 */
public class EnemiesManager {
    private final String TAG = "EnemiesManager";

    /*
     * Constants to change to modify the gameplay
     */
    /**
     * The initial speed of each enemy
     */
    private final float ENEMY_SPEED_BASE = 15;

    /**
     * The speed of an enemy will be speed plus a random number between (-{@code ENEMY_SPEED_THRESHOLD}, {@code ENEMY_SPEED_THRESHOLD})
     */
    private final int ENEMY_SPEED_THRESHOLD = 10;

    /**
     * When wanted, enemy speed can be increased by a specific amount
     */
    private final float ENEMY_SPEED_MULTIPLIER = 1.5f;

    /**
     * The bigger this value is, the more tolerant the game is about collisions.
     * This value MUST be lower than sprite width / 2 and spite height / 2
     */
    private final float COLLISION_TOLERANCE = 10f;

    private List<Enemy> enemies = new ArrayList<>();
    private float currentEnemySpeed = ENEMY_SPEED_BASE;

    private Random random = new Random();
    private int screenWidth;
    private int screenHeight;
    private int spriteWidth;
    private int spriteHeight;

    /**
     * Initializes the manager and create a specific amount of new enemies (i.e the number of enemies that can be on the screen at the same time)
     *
     * @param activity the activity where enemies will live.
     * @param layout   the layout where add sprites.
     */
    public EnemiesManager(GameActivity activity, RelativeLayout layout) {
        screenHeight = activity.getScreenHeight();
        screenWidth = activity.getScreenWidth();
        //init the enemies sprite
        spriteWidth = activity.getSpriteWidth();
        spriteHeight = activity.getSpriteHeight();

        //go in all layout to get all enemies
        View view;
        for (int i = 0; i < layout.getChildCount(); i++) {
            view = layout.getChildAt(i);
            if (view instanceof ImageView && "enemy".equals(view.getTag())) {
                //we have found an enemy, set all his properties and add it to the enemy list
                ImageView enemyImage = (ImageView) view;
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(spriteWidth, spriteHeight);
                enemyImage.setLayoutParams(layoutParams);
                //create a new instance of enemy
                Enemy enemy = new Enemy(enemyImage, screenWidth, screenHeight);
                setRandomSpeed(enemy);
                //set position to random position above screen, and between screen limits
                resetEnemyPosition(enemy);
                Log.d(TAG, "Enemy pos X : " + enemy.getPositionX() + " pos Y : " + enemy.getPositionY());
                //add to the layout and list
                enemies.add(enemy);
            }
        }
    }

    /**
     * Main update call for all enemies.
     */
    public void updateEnemies() {
        //move all enemies
        for (Enemy enemy : enemies) {
            enemy.update();
            //if enemy is at bottom of the screen, put it back to top at random position and speed
            if (enemy.isUnderScreen()) {
                resetEnemyPosition(enemy);
                setRandomSpeed(enemy);
            }
        }
    }

    public boolean isThereCollision(PlayerController playerController) {
        float playerPosX = playerController.getImageView().getX();
        float playerPosY = playerController.getImageView().getY();
        for (Enemy enemy : enemies) {
            float enemyPosX = enemy.getImageView().getX();
            float enemyPosY = enemy.getImageView().getY();

            //we don't want to be cruel with the player, we have some pixels of tolerance
            if (playerPosX + COLLISION_TOLERANCE < enemyPosX + spriteWidth - COLLISION_TOLERANCE
                    && playerPosX + spriteWidth - COLLISION_TOLERANCE > enemyPosX + COLLISION_TOLERANCE
                    && playerPosY + COLLISION_TOLERANCE < enemyPosY + spriteHeight - COLLISION_TOLERANCE
                    && playerPosY + spriteHeight - COLLISION_TOLERANCE > enemyPosY + COLLISION_TOLERANCE) {
                Log.d(TAG, "COLLISION WITH ENEMY ! (player posX = " + playerPosX + " posY = " + playerPosY +
                        " spriteWidth = " + spriteWidth + " spriteHeight = " + spriteHeight + ")");
                Log.d(TAG, "Enemy posX = " + enemyPosX + " posY = " + enemyPosY);
                return true;
            }
        }
        return false;
    }

    /**
     * Multiply the global enemy speed by {@link EnemiesManager#ENEMY_SPEED_MULTIPLIER}
     */
    public void increaseEnemySpeed() {
        currentEnemySpeed *= ENEMY_SPEED_MULTIPLIER;
    }


    /**
     * Set a random speed for an enemy.
     * This speed is between (ENEMY_SPEED_BASE - ENEMY_SPEED_THRESHOLD) and (ENEMY_SPEED_BASE + ENEMY_SPEED_THRESHOLD)
     *
     * @param enemy the enemy we want to change speed
     */
    private void setRandomSpeed(Enemy enemy) {
        //Set a random speed between -ENEMY_SPEED_THRESHOLD and ENEMY_SPEED_THRESHOLD
        float speedModifier = random.nextFloat() * ENEMY_SPEED_THRESHOLD;
        speedModifier *= random.nextBoolean() ? 1 : -1;
        enemy.setSpeed(ENEMY_SPEED_BASE + speedModifier);
    }

    /**
     * Set the position of the enemy above screen. Used when enemy has reached bottom of the screen
     *
     * @param enemy the enemy to ch
     */
    private void resetEnemyPosition(Enemy enemy) {
        // We don't want to have an enemy that go through another,
        // so we have to check the randomly-generated X position in order to avoid this

        float positionX;
        do {
            positionX = random.nextFloat() * (screenWidth - spriteWidth);
        } while (isEnemyInTheAxis(positionX, enemy));

        float positionY = random.nextFloat() * (-screenHeight * 2) - spriteHeight;
        enemy.setPosition(positionX, positionY);

        //we also have to reset his hitbox
        enemy.resetHitboxPosition();
    }

    /**
     * Checks if an enemy is in the X axis specified
     *
     * @param positionX the coordinate to check
     * @param enemy     the enemy who want to change position
     * @return
     */
    private boolean isEnemyInTheAxis(float positionX, Enemy enemy) {
        /**
         * position is valid if
         *      positionX <= otherEnemy.posX - spriteWidth OR positionX >= otherEnemy.posX + spriteWidth
         * So, position is not valid if
         *      positionX > otherEnemy.posX - spriteWidth AND positionX < otherEnemy.posX + spriteWidth
         */
        for (Enemy e : enemies) {
            //we don't care about the enemy who want to set his new position
            if (e != enemy) {
                float otherPositionX = e.getPositionX();
                if (positionX > otherPositionX - spriteWidth && positionX < otherPositionX + spriteWidth) {
                    //problem with an enemy
                    return true;
                }
            }
        }
        //no problem with position if we are here
        return false;
    }


    /**
     * Generate a random number between two limits (inclusive)
     *
     * @param min the lowest value that can be returned
     * @param max the largest value that can be returned
     * @return a float between {@code min} and {@code max}
     */
    private float randomBetween(float min, float max) {
        return random.nextFloat();
    }
}
