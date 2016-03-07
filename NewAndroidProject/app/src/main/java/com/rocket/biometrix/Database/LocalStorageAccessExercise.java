package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Exercise Module's implementation of the SQLite database adapter: LocalStorageAccessBase_OLD
 */
public class LocalStorageAccessExercise{

    //Strings that represent table and column names in the database for Exercise X
    public static final String TABLE_NAME = "Exercise";
    public static final String LOCAL_EXERCISE_ID = "LocalExerciseID"; //ID used for primary key
    public static final String USER_NAME = "UserName"; //Username from shared preferences
    public static final String WEB_EXERCISE_ID = "WebExerciseID";
    //public static final String MODE = "Mode"; //String to for mode which is not implemented yet. Think: Belly fat reduction mode - probably needs laps, won't need weight. Yada yada YODA
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

    //Updated = Has the field changed from what the webserver has? This has to be an int, so 0 =false 1 =true
    public static final String UPDATED = "Updated";

    // All the columns above, see getColumns() below
    private static final String[] columns = {LOCAL_EXERCISE_ID, USER_NAME, WEB_EXERCISE_ID, TITLE, TYPE, MINUTES, REPS, LAPS, WEIGHT, INTY, NOTES, DATE, TIME, UPDATED};

    //Later, we'll hopefully get to a shared preferences class that stores BMI and weight information.

    public LocalStorageAccessExercise(Context context) {    }

    protected static String createTable() {
        //Some SQL
        String createTableSQL = "CREATE TABLE " + TABLE_NAME +
                " (" + LOCAL_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_NAME + " VARCHAR(50) not null, " +
                WEB_EXERCISE_ID + " INT NULL, " +
                TITLE + " VARCHAR(255), " +
                TYPE + " VARCHAR(140), " +
                MINUTES + " TINYINT, " +
                REPS + " TINYINT, " +
                LAPS + " TINYINT, " +
                WEIGHT + " SMALLINT, " +
                INTY + " TINYINT, " +
                NOTES + " VARCHAR(255), " +
                DATE + " DATE, " +
                TIME + " VARCHAR(50), " +
                UPDATED + " int default 0" +
                ");";

        return createTableSQL;
    }


    //Prints all column names and returns a string array with them in it.
    public static String[] getColumns() {
        for (String s : columns) {
            System.out.println(s);
            Log.d("column: ", s);
        }
        return columns;
    }

    public static String getTableName() {return TABLE_NAME;}

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
    public void insertFromContentValues(ContentValues cv, Context c) {

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
        LocalStorageAccess.getInstance(c).safeInsert(TABLE_NAME, columns[1], dataToBeInserted);

    }//end insert


    //Get all rows that match date YYYY-MM-DD (pass in date to search, then table you are looking at...)
    public static Cursor selectByDate(String date){
        return LocalStorageAccess.selectByDate(date, TABLE_NAME, DATE);
    }

    public static Cursor selectAllDatabyDateRange(String startDate, String endDate){

        return LocalStorageAccess.selectAllDatabyDateRange(TABLE_NAME, DATE, startDate, endDate);
    }

    public static String selectAllasStrings(){
        return LocalStorageAccess.selectALLasStrings(TABLE_NAME, getColumns(), LOCAL_EXERCISE_ID );
    }


}