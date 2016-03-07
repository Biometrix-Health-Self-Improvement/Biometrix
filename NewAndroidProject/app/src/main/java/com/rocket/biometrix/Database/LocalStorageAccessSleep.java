package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rocket.biometrix.Login.LocalAccount;

public class LocalStorageAccessSleep {

    //Sleep table and columns
    public static final String TABLE_SLEEP = "Sleep";
    public static final String USER_NAME = "UserName";

    //The local sleep ID is a unique identifier for each entry on the local datbase
    //The web sleep ID is a unique identifier for each entry on the BIOMETRIX database
    //These two references are needed to allow easier updating when a user changes values
    public static final String LOCAL_SLEEP_ID = "LocalSleepID";
    public static final String WEB_SLEEP_ID = "WebSleepID";
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String DURATION  = "Duration";
    public static final String QUALITY = "Quality";
    public static final String NOTES = "Notes";
    public static final String HEALTH = "Health";

    //Updated = Has the field changed from what the webserver has? This has to be an int, so 0 =false 1 =true
    static final String UPDATED = "Updated";

    //Exercise Add Entry Table strings

    private static final String[] columns = {LOCAL_SLEEP_ID, USER_NAME, WEB_SLEEP_ID, DATE, TIME, DURATION, QUALITY, NOTES, HEALTH, UPDATED};

    public LocalStorageAccessSleep(Context context){
    }


    public static String createTable()
    {
        //Creates the SQL string to make the SLEEP table
        return "CREATE TABLE " + TABLE_SLEEP
                //Integer primary key gives auto-increment for free
                + " ( " + LOCAL_SLEEP_ID + " int primary key, "
                + USER_NAME + " varchar(50) Not Null, "
                + WEB_SLEEP_ID + " int NULL, "
                + DATE + " date Not Null, "
                + TIME + " time Not Null, "
                + DURATION + " time Not Null, "
                + QUALITY + " int Not Null, "
                + NOTES + " varchar(300), "
                + HEALTH + " varchar(20), "
                + UPDATED + " int default 0" +
                ");";
    }

    public static String getTableName(){ return TABLE_SLEEP; }


    //Returns the columns for the table
    public static String[] getColumns()
    {
        return columns;
    }

    public static String getUIDColumn()
    {
        return USER_NAME;
    }


    /**
     * Inserts into the database based on the passed in content values. If the column does not exist
     * it is ignored
     * @param cv The content values to insert.
     */
    public static void insertFromContentValues(ContentValues cv, Context c) {
        LocalStorageAccess.getInstance(c).safeInsert(TABLE_SLEEP, columns[1], cv);
    }



    public static Cursor selectAll(Context c)
    {
        SQLiteDatabase database = LocalStorageAccess.getInstance(c).getReadableDatabase();

        String username = "default";

        if (LocalAccount.isLoggedIn())
        {
            username = LocalAccount.GetInstance().GetUsername();
        }

        String[] usernameArgs = {username};

        return database.query(TABLE_SLEEP, null, "Username = ?", usernameArgs, null, null, DATE);
    }

}
