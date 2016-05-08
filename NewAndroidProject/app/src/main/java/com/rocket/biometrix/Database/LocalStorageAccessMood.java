package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tannalynn on 1/22/2016.
 */
public class LocalStorageAccessMood {

    //Table name and all columns
    public static final String TABLE_NAME = "Mood";
    public static final String LOCAL_MOOD_ID = "LocalMoodID";
    public static final String USER_NAME = "UserName";
    public static final String WEB_MOOD_ID = "WebMoodID";
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String DEP = "Depression";
    public static final String ELEV = "Elevated";
    public static final String IRR = "Irritable";
    public static final String ANX = "Anxiety";
    public static final String NOTE = "Notes";

    public static final String[] cols = {LOCAL_MOOD_ID, USER_NAME, WEB_MOOD_ID, DATE,
            TIME, DEP, ELEV, IRR, ANX, NOTE};

    private LocalStorageAccessMood(){}

    public static String createTable() {
        //Creates the SQL string to make the SLEEP table
        return "CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_MOOD_ID + " integer primary key autoincrement, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_MOOD_ID + " int Null, " +
                DATE + " date Not Null, " +
                TIME + " time Not Null, " +
                DEP + " int Null, " +
                ELEV + " int Null, " +
                IRR + " int Null, " +
                ANX + " int Null, " +
                NOTE + " varchar(255)" +");";
    }

    public static void AddEntry(ContentValues cv, Context c){
        LocalStorageAccess.getInstance(c).safeInsert(TABLE_NAME, null, cv);
    }

    public static String[] getColumns(){
        return cols;
    }

    /**
     * Makes a call to the base class with the needed parameters to pull out the last primary key
     * entered
     * @param c
     * @return The integer value of the last primary key entered.cc
     */
    public static int GetLastID(Context c)
    {
        return LocalStorageAccess.getInstance(c).GetLastID(c, LOCAL_MOOD_ID, TABLE_NAME);
    }

    public static List<String[]> getEntries(Context c)
    {

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        //Select DATE, TIME, DEP, ELEV, IRR, ANX, NOTE FROM TABLE_NAME ORDER BY DATE DESC
        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE, TIME, DEP, ELEV, IRR, ANX, NOTE}, null, null, null, null, DATE + " DESC, " + TIME + " DESC");

        List<String[]> lst = new LinkedList<String[]>();

        String date, time, dep, elev, irr, anx, note;

        //If there is a valid entry move to it
        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast())
            {
                date = cursor.getString(0);
                time = cursor.getString(1);
                dep = cursor.getString(2);
                elev = cursor.getString(3);
                irr = cursor.getString(4);
                anx = cursor.getString(5);
                note = cursor.getString(6);

                String[] data = {date, time, dep, elev, irr, anx, note};
                lst.add(data);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return lst;
    }

    public static Cursor getMonthEntries(Context c, int year, int month)
    {
        String date = year + "-";
        if(month <10)
            date +="0";
        date += month + "-01";

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE, TIME, DEP, ELEV, IRR, ANX, NOTE},
                DATE + " BETWEEN (date(?)) AND (date(?, '+1 month','-1 day'))", new String[]{date, date}, null, null, DATE);


        return cursor;
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

        webCV.put(WEB_MOOD_ID, webID);

        int num_rows = db.update(TABLE_NAME, webCV, LOCAL_MOOD_ID + " = ?", new String[]{localID.toString()});
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
}
