package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.rocket.biometrix.Login.LocalAccount;

public class LocalStorageAccessSleep {

    //Sleep table and columns
    public static final String TABLE_NAME = "Sleep";
    public static final String LOCAL_SLEEP_ID = "LocalSleepID";//The local sleep ID is a unique identifier for each entry on the local database
    public static final String USER_NAME = "UserName";
    public static final String WEB_SLEEP_ID = "WebSleepID";//The web sleep ID is a unique identifier for each entry on the BIOMETRIX database
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String DURATION = "Duration";
    public static final String QUALITY = "Quality";
    public static final String NOTES = "Notes";
    public static final String HEALTH = "Health";
    public static final String UPDATED = "Updated";//Updated = Has the field changed from what the webserver has? This has to be an int, so 0 =false 1 =true

    //Exercise Add Entry Table strings

    public static final String[] columns = {LOCAL_SLEEP_ID, USER_NAME, WEB_SLEEP_ID, DATE,
            TIME, DURATION, QUALITY, NOTES, HEALTH, UPDATED};

    public LocalStorageAccessSleep(Context context){
    }


    public static String createTable()
    {
        //Creates the SQL string to make the SLEEP table
        return "CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_SLEEP_ID + " integer primary key, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_SLEEP_ID + " int Null, " +
                DATE + " date, " +
                TIME + " time Not Null, " +
                DURATION + " time Not Null, " +
                QUALITY + " int Not Null, " +
                NOTES + " varchar(300), " +
                HEALTH + " varchar(20), " +
                UPDATED + " int default 0" +");";
    }

    public static String getTableName(){ return TABLE_NAME; }

    /**
     * Makes a call to the base class with the needed parameters to pull out the last primary key
     * entered
     * @param c
     * @return The integer value of the last primary key entered.
     */
    public static int GetLastID(Context c)
    {
        return LocalStorageAccess.getInstance(c).GetLastID(c, LOCAL_SLEEP_ID, TABLE_NAME);
    }

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
        LocalStorageAccess.getInstance(c).safeInsert(TABLE_NAME, columns[2], cv);
    }


    /**
     * Returns all rows for the currently logged in user. If no user is logged in, returns the
     * columns for the user "default"
     * @param c
     * @return A Cursor to all of the columns for the sleep table for the current user
     */
    public static Cursor selectAll(Context c)
    {
        SQLiteDatabase database = LocalStorageAccess.getInstance(c).getReadableDatabase();

        String username = "default";

        if (LocalAccount.isLoggedIn())
        {
            username = LocalAccount.GetInstance().GetUsername();
        }

        return database.query(TABLE_NAME, null, "Username = ?", new String[] {username}, null, null, DATE + " DESC, " + TIME + " DESC");
    }

    /**
     * Updates the ID that is stored locally for reference to the entry on the webserver
     * @param localID The ID number locally
     * @param webID The ID number on the web
     */
    public static void updateWebIDReference(Integer localID, Integer webID, Context context)
    {
        SQLiteDatabase db = LocalStorageAccess.getInstance(context).getWritableDatabase();

        ContentValues webCV = new ContentValues();

        webCV.put(WEB_SLEEP_ID, webID);

        int num_rows = db.update(TABLE_NAME, webCV, LOCAL_SLEEP_ID + " = ?", new String[]{localID.toString()});

        if (num_rows < 1)
        {
            Toast.makeText(context, "Could not create reference between web database and local database", Toast.LENGTH_LONG).show();
        }

        db.close();
    }
}
