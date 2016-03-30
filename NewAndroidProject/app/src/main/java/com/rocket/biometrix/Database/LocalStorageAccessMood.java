package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tannalynn on 1/22/2016.
 */
public class LocalStorageAccessMood {

    private static final String LOCAL_DB_NAME = "BiometrixLocal";
    private static final int LOCAL_DB_VERSION = 1;

    public static final String TABLE_NAME = "Mood";
    public static final String LOCAL_MOOD_ID = "LocalMoodID";
    public static final String USER_NAME = "UserName";
    public static final String WEB_MOOD_ID = "WebMoodID";
    public static final String DATE= "Date";
    public static final String TIME= "Time";
    public static final String DEP = "Depression";
    public static final String ELEV= "Elevated";
    public static final String IRR = "Irritable";
    public static final String ANX = "Anxiety";
    public static final String NOTE= "Notes";

    //Updated = Has the field changed from what the webserver has? This has to be an int, so 0 =false 1 =true
    public static final String UPDATED = "Updated";

    private final static String[] cols = {LOCAL_MOOD_ID, USER_NAME, WEB_MOOD_ID, DATE, TIME, DEP, ELEV, IRR, ANX, NOTE, UPDATED};

    private LocalStorageAccessMood(){}

    public static String createTable() {
        //Creates the SQL string to make the SLEEP table
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_MOOD_ID + " integer primary key, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_MOOD_ID + " int Null, " +
                DATE + " date Not Null, " +
                TIME + " time Not null, " +
                DEP + " VARCHAR(50), " +
                ELEV + " VARCHAR(50), " +
                IRR + " VARCHAR(50), " +
                ANX + " VARCHAR(50), " +
                NOTE + " varchar(255), " +
                UPDATED + " int default 0" +");";
        return CREATE_TABLE;
    }

    public static String getTableName() {return  TABLE_NAME;}


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
     * @return The integer value of the last primary key entered.
     */
    public static int GetLastID(Context c)
    {
        return LocalStorageAccess.getInstance(c).GetLastID(c, LOCAL_MOOD_ID, TABLE_NAME);
    }

    public static List<String[]> getEntries(Context c){
        String query = "Select " + DATE + ", " + TIME + ", " +
                DEP + ", " + ELEV + ", " + IRR + ", " + ANX + ", " + NOTE +
                " FROM " + TABLE_NAME + " Order By " + DATE + " DESC";


        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

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

    public static Cursor getCurrentMonthEntries(Context c)
    {
        String query = "Select " + DATE + ", " + TIME + ", " +
                DEP + ", " + ELEV + ", " + IRR + ", " + ANX + ", " + NOTE +
                " FROM " + TABLE_NAME + " WHERE " + DATE+ " BETWEEN (date('now', 'start of month')) AND (date('now')) Order By " + DATE;

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        return cursor;
    }
}
