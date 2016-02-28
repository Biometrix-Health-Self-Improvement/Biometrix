package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Exercise Module's implementation of the SQLite database adapter: LocalStorageAccessBase
 */
public class LocalStorageAccessExercise extends LocalStorageAccessBase{

    //Strings that represent table and column names in the database for Exercise X
    public static final String TABLE_NAME = "Exercise";
    public static final String UID = "Exercise_id"; //ID used for primary key
    //private static final String USER_ID = "not implemented yet, will be in shared preferences";
    public static final String MODE = "Mode"; //String to for mode which is not implemented yet. Think: Belly fat reduction mode - probably needs laps, won't need weight. Yada yada YODA
    //Columns
    public static final String TITLE = "Title"; //Title will help co-determine the module mode e.g. Simple mode (yay I walked to the fridge), Gainz mode (weight and reps etc.)
    public static final String TYPE = "Type"; //light, cardio, etc.
    public static final String MINUTES = "Minutes"; //minutes exercised
    public static final String REPS = "Reps"; //Reps or laps, data significance determined by module mode WHICH IS NOT IMPLEMENTED YET
    public static final String LAPS = "Laps";
    public static final String WEIGHT = "Weight";
    public static final String INTY = "Intensity";
    public static final String NOTES = "Notes";
    public static final String DATE = "DateEx";
    public static final String TIME = "TimeEx";

    // All the columns above, see getColumns() below
    private static final String[] columns = {TITLE, TYPE, MINUTES, REPS, LAPS, WEIGHT, INTY, NOTES, DATE, TIME};

    //Later, we'll hopefully get to a shared preferences class that stores BMI and weight information.

    public LocalStorageAccessExercise(Context context) {
        super(context);
    }


    //onCreate in parent will call this.
    @Override
    protected String createTable() {
        //Some SQL
        String createTableSQL = "CREATE TABLE " + TABLE_NAME +
                " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TITLE + " VARCHAR(255), " +
                TYPE + " VARCHAR(140), " +
                MINUTES + " TINYINT, " +
                REPS + " TINYINT, " +
                LAPS + " TINYINT, " +
                WEIGHT + " SMALLINT, " +
                INTY + " TINYINT, " +
                NOTES + " VARCHAR(255), " +
                DATE + " DATE, " +
                TIME + " VARCHAR(50)" +
                ");";

        return createTableSQL;
    }

    //Update table on user upgrade
    protected boolean onUpgradeAlter(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean versionDetected = true; //version of db is found in parent class

        //In future, will need to test version to upgrade properly.
        if (oldVersion < getDBVersion() - 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db); //Drop and recreate
        }
//        else if (oldVersion < 2) {
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//            onCreate(db); //Drop and recreated
//            //update to appropriate version (e.g. if user has skipped updates)
//        }
        else {
            versionDetected = false;
        }

        return versionDetected;
    }


    //Prints all column names and returns a string array with them in it.
    @Override
    public String[] getColumns() {
        for (String s : columns) {
            System.out.println(s);
            Log.d("column: ", s);
        }
        return columns;
    }

    @Override
    public String getUIDColumn(){
        return UID;
    }

    public String getTableName(){
        return TABLE_NAME;
    }

    /**
     * Tests the passed in ContentValues against the private Strings
     * that represent columns in this class. Then calls parent's insert method
     * <p>
     * If the key values of the passed in cv EXACTLY match your column titles,
     * they will be placed into a new cv that is used as a parameter for the
     * safeInsert method.
     *
     * @param  cv  a ContentValues map with key values that match the private String column names
     * {@see safeInsert} in parent is called from
     */
    public void insertFromContentValues(ContentValues cv) {

        //Real ContentValues that will be passed to the base class' insert method.
        ContentValues dataToBeInserted = new ContentValues();

        //This is inefficient for more than 100 columns, but we've got a glorified file system so we'll be fine.
        //for each String returned by getColumns method, check if the parameter cv contains a Column String  as a key
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
        safeInsert(TABLE_NAME, columns[1], dataToBeInserted );
    }//end insert

    //Return cursor
    public Cursor selectByDate(String dayte){
        //Module specific error checking could go here.
        return super.selectByDate(dayte, TABLE_NAME, DATE);
    }

    @Override
    public Cursor selectAllDatabyDateRange(String tablename, String date_col ) {
        return super.selectAllDatabyDateRange(TABLE_NAME, DATE);
    }

    //TODO: Pull from database the exercise table.



}