package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by tannalynn on 1/22/2016.
 */
public class LocalStorageAccessMood extends LocalStorageAccessBase{

    static final String TABLE_NAME = "Mood";
    static final String UID = "Mood_id";

    static final String DATE= "Date";
    static final String TIME= "Time";
    static final String DEP = "Depression";
    static final String ELEV= "Elevated";
    static final String IRR = "Irritable";
    static final String ANX = "Anxiety";
    static final String NOTE= "Notes";

    private final static String[] cols = {DATE, TIME, DEP, ELEV, IRR, ANX, NOTE};


    public LocalStorageAccessMood(Context context){
        super(context);
    }

    //A module's table create sql statement.
    protected  String createTable(){
        String createTbl =  "CREATE TABLE " + TABLE_NAME + "(" +
                UID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                DATE + " DATE, "+
                TIME + " VARCHAR(50), "+
                DEP + " TINYINT, " +
                ELEV + " TINYINT, "+
                IRR + " TINYINT, " +
                ANX + " TINYINT, " +
                NOTE + " VARCHAR(255) );";
        return createTbl;
    }

    //Returns string array of private string variables representing Columns in child module class
    public   String[] getColumns(){
        for (String s : cols) {
            System.out.println(s);
            Log.d("column: ", s);
        }
        return cols;
    }

    //Returns UID (primary key) column name
    protected  String getUIDColumn(){
        return UID;

    }

    //Version safe Alter table SQL called in onUpgrade, eventually might return some kind of error checking information...
    //Returns true if oldVersion was detected
    protected  boolean onUpgradeAlter(SQLiteDatabase db, int oldVersion, int newVersion){
        boolean versionDetected = true; //version of db is found in parent class

        //In future, will need to test version to upgrade properly.
        if (oldVersion < getDBVersion() - 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db); //Drop and recreate
        }
        else {
            versionDetected = false;
        }

        return versionDetected;
    }

    public void insertFromContentValues(ContentValues cv) {

        //Real ContentValues that will be passed to the base class' insert method.
        ContentValues dataToBeInserted = new ContentValues();

        for (String columnName : getColumns()) {
            if (cv.containsKey(columnName)) {
                //if the key pulled out of the parameter cv is equal to any string inside columns:
                dataToBeInserted.put(columnName,cv.getAsString(columnName)); //put the key and its value into the new CV
            }
            else{
                Log.d("insertFromContentValues"," Key " + columnName+" not found");
            }
        }

        //WHERE THE MAGIC HAPPENS //Table name is a string above "Exercise", columns[1] is just any column that can be null, then we pass in the clean cv
        safeInsert(TABLE_NAME, cols[1], dataToBeInserted );
    }

}
