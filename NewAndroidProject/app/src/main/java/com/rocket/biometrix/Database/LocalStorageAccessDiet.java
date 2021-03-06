package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.rocket.biometrix.Login.LocalAccount;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Troy Riblett on 3/6/2016
 */
public class LocalStorageAccessDiet {

    //All of the fields for the diet table.
    public static final String TABLE_NAME = "Diet";
    public static final String LOCAL_DIET_ID = "LocalDietID";
    public static final String USER_NAME = "UserName";
    public static final String WEB_DIET_ID = "WebDietID";
    public static final String DATE = "Date";
    public static final String TYPE = "FoodType";
    public static final String MEAL = "Meal";
    public static final String SERVING = "ServingSize";
    public static final String CALORIES = "Calories";
    public static final String TOTALFAT = "TotalFat";
    public static final String SATFAT = "SaturatedFat";
    public static final String TRANSFAT = "TransFat";
    public static final String CHOLESTEROL = "Cholesterol";
    public static final String SODIUM = "Sodium";
    public static final String TOTALCARBS = "TotalCarbs";
    public static final String FIBER = "DietaryFiber";
    public static final String SUGARS = "Sugars";
    public static final String PROTEIN = "Protein";
    public static final String VITAMINA = "VitaminA";
    public static final String VITAMINB = "VitaminB";
    public static final String CALCIUM = "Calcium";
    public static final String IRON = "Iron";
    public static final String NOTE = "Notes";

    //Every single column that is available in the table
    public static final String[] cols = {LOCAL_DIET_ID, USER_NAME, WEB_DIET_ID, DATE,
            TYPE, MEAL, SERVING, CALORIES, TOTALFAT, SATFAT, TRANSFAT, CHOLESTEROL, SODIUM, TOTALCARBS,
            FIBER, SUGARS, PROTEIN, VITAMINA, VITAMINB, CALCIUM, IRON, NOTE};

    private LocalStorageAccessDiet(){}

    public static String createTable() {

        return "CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_DIET_ID + " integer primary key autoincrement, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_DIET_ID + " int Null, " +
                DATE + " date Not Null, " +
                TYPE + " varchar(40) Null, " +
                MEAL + " varchar(20) Null, " +
                SERVING + " varchar(20) Null, " +
                CALORIES + " int Null, " +
                TOTALFAT + " int Null, " +
                SATFAT + " int Null, " +
                TRANSFAT + " int Null, " +
                CHOLESTEROL + " int Null, " +
                SODIUM + " int Null, " +
                TOTALCARBS + " int Null, " +
                FIBER + " int Null, " +
                SUGARS + " int Null, " +
                PROTEIN + " int Null, " +
                VITAMINA + " int Null, " +
                VITAMINB + " int Null, " +
                CALCIUM + " int Null, " +
                IRON + " int Null, " +
                NOTE + " varchar(255)" +");";
    }

    /**
     * Makes a call to the base class with the needed parameters to pull out the last primary key
     * entered
     * @param c
     * @return The integer value of the last primary key entered.
     */
    public static int GetLastID(Context c)
    {
        return LocalStorageAccess.getInstance(c).GetLastID(c, LOCAL_DIET_ID, TABLE_NAME);
    }

    public static String[] getColumns(){
        return cols;
    }

    /**
     * Inserts into the database based on the passed in content values. If the column does not exist
     * it is ignored
     * @param cv The content values to insert.
     */
    public static void insertFromContentValues(ContentValues cv, Context c) {
        LocalStorageAccess.getInstance(c).safeInsert(TABLE_NAME, cols[2], cv);
    }

    /**
     * Deletes a the row with the local key that has the passed in value
     * @param context A reference to the context, used to grab database access
     * @param value The value of the key on the row to delete
     * @return The number of rows deleted. Should be 0 on fail and 1 on success. Greater than 1
     * means something went wrong.
     */
    public static int deleteByLocalKeyValue(Context context, int value)
    {
        return LocalStorageAccess.deleteEntryByID(context, TABLE_NAME, LOCAL_DIET_ID, value);
    }

    /**
     * Retrieves the web primary key associated with the local primary key that is associated with
     * value.
     * @param context The current context. Used for database access
     * @param value The value of the local primary key on the desired row
     * @return -1 on failure. Otherwise returns the web primary key
     */
    public static int getWebKeyFromLocalKey(Context context, int value)
    {
        return LocalStorageAccess.getWebKeyFromLocalKey(context, TABLE_NAME, LOCAL_DIET_ID, WEB_DIET_ID, value);
    }

    /**
     * Calls an update for this table using the passed in params
     * @param contentValues The content values that determine the new values for the row
     * @param context The current context, used for database access
     * @param localPrimaryKey The value of the primary key on the row to update
     * @return The number of rows that were updated. Should be either 1 or 0.
     */
    public static int updateFromContentValues(ContentValues contentValues, Context context, Integer localPrimaryKey)
    {
        return LocalStorageAccess.updateTableFromContentValues(context, contentValues, localPrimaryKey, TABLE_NAME, LOCAL_DIET_ID);
    }
    /**
     * Returns a list of all of the entries that have been made in this module. Shows meal, date,
     * and calories for now
     * @param c The current context
     * @return A list of the entries with the matching data
     */
    public static List<String[]> getEntries(Context c)
    {
        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        //Select DATE, MEAL, CALORIES from TABLE_NAME Order By DATE DESC
        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE, MEAL, CALORIES}, null, null, null, null, DATE + " DESC");

        List<String[]> lst = new LinkedList<String[]>();

        String date, meal, calories;

        //If there is a valid entry move to it
        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast())
            {
                date = cursor.getString(0);
                meal = cursor.getString(1);
                calories = cursor.getString(2);


                String[] data = {date, meal, calories};
                lst.add(data);

                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return lst;
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

        webCV.put(WEB_DIET_ID, webID);

        int num_rows = db.update(TABLE_NAME, webCV, LOCAL_DIET_ID + " = ?", new String[]{localID.toString()});

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
        return LocalStorageAccess.selectAllEntries(c, TABLE_NAME, DATE + " DESC", curUserOnly);
    }

    public static Cursor getMonthEntries(Context c, int year, int month) {
        String date = year + "-";
        if(month <10)
            date +="0";
        date += month + "-01";

        //Evolution is a 'only good enough' solution.
        String username = LocalAccount.DEFAULT_NAME;
        if (LocalAccount.isLoggedIn()) {
            username = LocalAccount.GetInstance().GetUsername();
        }

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE, "SUM("+CALORIES+")", "SUM("+TOTALFAT+")", "SUM("+TOTALCARBS+")", "SUM("+FIBER+")", "SUM("+PROTEIN+")", "count(*)"},
                DATE + " BETWEEN (date(?)) AND (date(?, '+1 month','-1 day')) AND UserName = ?",
                new String[]{date, date, username}, DATE, null, DATE);



        return cursor;
    }

    /**
     * Slight change to the above method, this returns an array with the information in the
     * column passed in
     * @param c Current context used for database access
     * @param year The year as an int
     * @param month Current month of year as an int
     * @param columnName The name of the column to return
     * @return Returns a cursor that contains the date in column one and the requested column in
     * column index 1
     */
    public static Cursor getMonthEntriesForColumn(Context c, int year, int month, String columnName) {
        String date = year + "-";
        if(month <10)
            date +="0";
        date += month + "-01";

        //Evolution is a 'only good enough' solution.
        String username = LocalAccount.DEFAULT_NAME;
        if (LocalAccount.isLoggedIn()) {
            username = LocalAccount.GetInstance().GetUsername();
        }

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{DATE, "SUM("+columnName+")"},
                DATE + " BETWEEN (date(?)) AND (date(?, '+1 month','-1 day')) AND UserName = ?",
                new String[]{date, date, username}, DATE, null, DATE);

        return cursor;
    }
}