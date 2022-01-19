package com.hfa.dodgecars.scores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HFA
 */
public class DatabaseManager extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseManager";

    // Database version
    private static final int DATABASE_VERSION = 1;

    // Database name
    private static final String DATABASE_NAME = "scoreManager";

    // Table name
    private static final String TABLE_NAME = "scores";

    // Table columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME_USER = "name_user";
    private static final String KEY_SCORE = "score";

    //query for creating table
    private final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            KEY_NAME_USER + " TEXT," +
            KEY_SCORE + " INTEGER)";

    private final String QUERY_SELECT_SCORES = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + KEY_SCORE + " DESC";

    //private final String QUERY_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;


    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    /**
     * test if the table is empty
     * @return true if the table is empty, false else
     */
    public boolean isTableEmpty() {
        return getNumberRows() == 0;
    }

    /**
     * look how many rows the table "scores" get
     * @return the number of row in the table
     */
    public long getNumberRows(){
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }

    /**
     * Initializes the data table with scores assigned to bot
     */
    public void fillTable() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        int scoreBot;
        for (int i = 0; i < 30; i++) {
            scoreBot = i * 200;
            Log.i(TAG, "Insert score " + scoreBot + " for Player" + i);
            cv.put(KEY_NAME_USER, "Player" + i);
            cv.put(KEY_SCORE, scoreBot);
            db.insert(TABLE_NAME, null, cv);
        }
        Log.i(TAG, "Database fully filled");
        db.close();
    }

    /**
     * Get the id of the lowest score in the table "scores". If 2 scores or more are the same, the oldest in the database will be returned.
     * @return the id of the lowest score
     */
    public long retrieveIdLowestScore(){
        SQLiteDatabase db = getReadableDatabase();

        //we want to get only 1 score row that have the lowest score in the database
        String queryLowestScoreID = "SELECT " + KEY_ID + " FROM " + TABLE_NAME + " WHERE " + KEY_SCORE + " IN " +
                                        "(SELECT MIN(" + KEY_SCORE + ") FROM " + TABLE_NAME + ") ORDER BY " + KEY_ID + " LIMIT 1";

        return DatabaseUtils.longForQuery(db, queryLowestScoreID, null);
    }

    /**
     * @param rowID the id of the row that we want to get the score
     * @return the score contained in the database at this row
     */
    public long getSpecificScore(long rowID){
        SQLiteDatabase db = getReadableDatabase();
        String queryGetScore = "SELECT " + KEY_SCORE + " FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=" + rowID;
        return DatabaseUtils.longForQuery(db, queryGetScore, null);
    }

    /**
     * Replace the lowest score by the score of our player if it is higher than the minimum score
     * @param name the name of the player
     * @param score the score to eventually insert
     * @return if score is inserted or not
     */
    public boolean insertNewScore(String name, int score) {
        //first, check if the score is grater than the minimum score in the database
        long idLowestScore = retrieveIdLowestScore();
        long lowestScore = getSpecificScore(idLowestScore);
        if(score >= lowestScore){
            Log.i(TAG, "new score : " + score);
            SQLiteDatabase db = getWritableDatabase();
            //instead of erasing row and add a new one, update information of the lowest score row (replace name and score)
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + KEY_NAME_USER + "='" + name + "' WHERE " + KEY_ID + "=" + idLowestScore);
            db.execSQL("UPDATE " + TABLE_NAME + " SET " + KEY_SCORE + "=" + score + " WHERE " + KEY_ID + "=" + idLowestScore);
            db.close();
            return true;
        }else{
            return false;
        }
    }

    /**
     * Get all scores in the table "scoreManager" and add all in a custom list
     * @return a list of all scores to the table
     */
    public List<PlayerData> getAllRows() {
        List<PlayerData> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursorResult = db.rawQuery(QUERY_SELECT_SCORES, null);

        if (cursorResult != null) {
            //check if have any result
            int index = 1;
            if (cursorResult.moveToFirst()) {
                //go through all rows and extract event from it
                do {
                    PlayerData playerData = new PlayerData(
                            index,
                            cursorResult.getString(1),
                            cursorResult.getInt(2)
                    );
                    list.add(playerData);
                    index++;
                } while (cursorResult.moveToNext());
            }
        }
        cursorResult.close();
        return list;
    }

    /**
     * retrieve the best score of player that is playing now
     * @param name the name of the player to get best score
     * @return the player's best score
     */
    public long getBestScoreOfPlayer(String name){
        SQLiteDatabase db = getReadableDatabase();
        String queryBestScoreOfPlayer = "SELECT " + KEY_SCORE + " FROM " + TABLE_NAME + " WHERE " + KEY_NAME_USER + "=" + name;
        return DatabaseUtils.longForQuery(db, queryBestScoreOfPlayer, null);
    }

}
