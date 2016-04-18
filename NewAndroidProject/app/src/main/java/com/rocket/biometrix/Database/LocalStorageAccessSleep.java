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

    //Exercise Add Entry Table strings
    public static final String[] columns = {LOCAL_SLEEP_ID, USER_NAME, WEB_SLEEP_ID, DATE,
            TIME, DURATION, QUALITY, NOTES, HEALTH};

    public LocalStorageAccessSleep(Context context){
    }


    public static String createTable()
    {
        //Creates the SQL string to make the SLEEP table
        return "CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_SLEEP_ID + " integer primary key autoincrement, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_SLEEP_ID + " int Null, " +
                DATE + " date, " +
                TIME + " time Not Null, " +
                DURATION + " time Not Null, " +
                QUALITY + " int Not Null, " +
                NOTES + " varchar(300), " +
                HEALTH + " varchar(20)" +");";
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
     * @param c The current context
     * @param curUserOnly A boolean value representing whether all users should be displayed (false)
     *                    or only the currently logged in user (true)
     * @return A Cursor to all of the columns for the sleep table for the current user
     */
    public static Cursor selectAll(Context c, boolean curUserOnly)
    {
        return LocalStorageAccess.selectAllEntries(c, TABLE_NAME, DATE + " DESC, " + TIME + " DESC", curUserOnly);
    }

    /**
     * Updates the ID that is stored locally for reference to the entry on the webserver
     * @param localID The ID number locally
     * @param webID The ID number on the web
     * @param context The context for database updates
     * @param makeToasts Whether to make toasts on failure or not
     * @return True if succeeded, false otherwise
     */
    public static boolean updateWebIDReference(Integer localID, Integer webID, Context context, boolean makeToasts)
    {
        boolean success = true;
        SQLiteDatabase db = LocalStorageAccess.getInstance(context).getWritableDatabase();

        ContentValues webCV = new ContentValues();

        webCV.put(WEB_SLEEP_ID, webID);

        int num_rows = db.update(TABLE_NAME, webCV, LOCAL_SLEEP_ID + " = ?", new String[]{localID.toString()});
        db.close();

        if (num_rows < 1)
        {
            Toast.makeText(context, "Could not create reference between web database and local database", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (!LocalStorageAccess.getInstance(context).deleteEntryFromSyncTable(context, TABLE_NAME, localID, true) )
            {
                success = false;
                if(makeToasts) Toast.makeText(context, "Could not update synchronization table", Toast.LENGTH_LONG).show();
            }
        }
        return success;
    }

    public static Cursor getMonthEntries(Context c, int year, int month)
    {
        String date = year + "-";
        if(month <10)
            date +="0";
        date += month + "-01";

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE, TIME, DURATION, QUALITY},
                DATE + " BETWEEN (date(?)) AND (date(?, '+1 month','-1 day'))", new String[]{date, date}, null, null, DATE);


        return cursor;
    }

}
