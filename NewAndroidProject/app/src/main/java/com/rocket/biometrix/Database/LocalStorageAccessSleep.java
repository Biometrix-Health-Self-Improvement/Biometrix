package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.rocket.biometrix.Login.LocalAccount;
import com.rocket.biometrix.SleepModule.SleepData;

import java.util.LinkedList;
import java.util.List;

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

    //Exercise Add Entry Table strings

    private static final String[] columns = {LOCAL_SLEEP_ID, USER_NAME, WEB_SLEEP_ID, DATE, TIME, DURATION, QUALITY, NOTES, HEALTH};

    public LocalStorageAccessSleep(Context context){
    }


    public static String createTable()
    {
        //Creates the SQL string to make the SLEEP table
        return "CREATE TABLE " + TABLE_SLEEP
                //Integer primary key gives auto-increment for free
                + " ( " + LOCAL_SLEEP_ID + " int primary key, "
                + USER_NAME + " int Not Null, "
                + WEB_SLEEP_ID + " int NULL, "
                + DATE + " date Not Null, "
                + TIME + " time Not Null, "
                + DURATION + " time Not Null, "
                + QUALITY + " int Not Null, "
                + NOTES + " varchar(300), "
                + HEALTH + " varchar(20) " + ");";
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
    public static void insertFromContentValues(ContentValues cv) {
        LocalStorageAccess.safeInsert(TABLE_SLEEP, columns[1], cv);
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


    /**
     * Creates an SQL entry for the passed in sleep data
     * @param sleepData The data to be stored.
     */
/*
    public void AddSleepEntry(SleepData sleepData)
    {
        ContentValues values = new ContentValues();
        values.put(DATE, sleepData.getStartTime());
        values.put(DURATION, sleepData.getDuration());
        values.put(HEALTH, sleepData.getHealthStatus());
        values.put(QUALITY, sleepData.getSleepQuality());
        values.put(NOTES, sleepData.getNotes());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_SLEEP, null, values);
        db.close();
    }*/


    /**
     * Returns the top row from the database sleep table
     * @return Returns a sleepdata object with the information from the database
     */
   /* public SleepData GetTopSleepEntry()
    {
        //Select Top 1 * From Sleep Order By StartDate
        String query = "Select * FROM " + TABLE_SLEEP + " Order By " + DATE;

        SQLiteDatabase db = this.getWritableDatabase();


        Cursor cursor = db.rawQuery(query, null);

        String date;
        String duration;
        int quality;
        String status;
        String notes;

        SleepData data = null;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            date = cursor.getString(0);
            duration = cursor.getString(1);
            quality = cursor.getInt(2);
            notes = cursor.getString(3);
            status = cursor.getString(4);


            data = new SleepData(date, duration, quality, status, notes);
        }

        cursor.close();

        db.close();
        return data;
    }*/

    /**
     * Returns the rows from the sleep data table
     * @return Returns a list of sleepdata objects with the information from the database
     */
    /*(public List<SleepData> GetSleepEntries()
    {
        String query = "Select " + DATE + ", " + SLEEP_COLUMN_DURATION + ", " +
                QUALITY + ", " + HEALTH + ", " + NOTES +
                " FROM " + TABLE_SLEEP + " Order By " + DATE;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        String date;
        String duration;
        int quality;
        String status;
        String notes;

        List<SleepData> sleepDataList = new LinkedList<SleepData>();
        SleepData data = null;

        //If there is a valid entry move to it
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast())
            {
                date = cursor.getString(0);
                duration = cursor.getString(1);
                quality = cursor.getInt(2);
                status = cursor.getString(3);
                notes = cursor.getString(4);

                data = new SleepData(date, duration, quality, status, notes);
                sleepDataList.add(data);

                cursor.moveToNext();
            }
        }

        cursor.close();

        db.close();

        return sleepDataList;
    }*/
}
