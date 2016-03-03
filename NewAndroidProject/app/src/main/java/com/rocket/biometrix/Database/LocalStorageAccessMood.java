package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rocket.biometrix.SleepModule.SleepData;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by tannalynn on 1/22/2016.
 */
public class LocalStorageAccessMood {

    private static final String LOCAL_DB_NAME = "BiometrixLocal";
    private static final int LOCAL_DB_VERSION = 1;

    static final String TABLE_NAME = "Mood";
    static final String UID = "Mood_id";

    static final String DATEL= "DateLong";
    static final String DATES= "DateShort";
    static final String TIME= "Time";
    static final String DEP = "Depression";
    static final String ELEV= "Elevated";
    static final String IRR = "Irritable";
    static final String ANX = "Anxiety";
    static final String NOTE= "Notes";

    private final static String[] cols = {DATEL, TIME, DEP, ELEV, IRR, ANX, NOTE, DATES};

    private LocalStorageAccessMood(){}

    public static String createTable() {
        //Creates the SQL string to make the SLEEP table
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                DATEL + " date Not Null, " +
                DATES + " date Not Null, "+
                TIME + " time Not null, " +
                DEP + " VARCHAR(50), " +
                ELEV + " VARCHAR(50), " +
                IRR + " VARCHAR(50), " +
                ANX + " VARCHAR(50), " +
                NOTE + " varchar(255) " + ");";
        return CREATE_TABLE;
    }

    public static String getTableName() {return  TABLE_NAME;}


    public static void AddEntry(ContentValues cv, Context c){
        LocalStorageAccess.safeInsert(TABLE_NAME, null, cv);
    }

    public static String[] getColumns(){
        return cols;
    }


    public static List<String[]> getEntries(Context c){
        String query = "Select " + DATEL + ", " + DATES + ", " + TIME + ", " +
                DEP + ", " + ELEV + ", " + IRR + ", " + ANX + ", " + NOTE +
                " FROM " + TABLE_NAME + " Order By " + DATES;


        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        List<String[]> lst = new LinkedList<String[]>();

        String datel, dates, time, dep, elev, irr, anx, note;

        //If there is a valid entry move to it
        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast())
            {
                datel = cursor.getString(0);
                dates = cursor.getString(1);
                time = cursor.getString(2);
                dep = cursor.getString(3);
                elev = cursor.getString(4);
                irr = cursor.getString(5);
                anx = cursor.getString(6);
                note = cursor.getString(7);

                String[] data = {datel, dates, time, dep, elev, irr, anx, note};
                lst.add(data);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return lst;
    }
}
