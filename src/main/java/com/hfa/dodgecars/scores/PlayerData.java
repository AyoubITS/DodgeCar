package com.hfa.dodgecars.scores;

import java.io.Serializable;

/**
 * This class represents data to display in "ScoresTableActivity" and stored in the database.
 */
public class PlayerData implements Serializable {

    int id;
    String name_user;
    int score;

    public PlayerData(int id, String name_user, int score) {
        this.id = id;
        this.name_user = name_user;
        this.score = score;

    }

    // ID
    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    // User Name
    public String getName() {
        return this.name_user;
    }

    public void setName(String name_user) {
        this.name_user = name_user;
    }

    // Score
    public int getScore(){ return this.score; }

    public void setScore(int score){ this.score = score; }


    @Override
    public String toString() {
        return  "           "+ id +
                "               " + name_user +
                "  :  " + score ;
    }
}
