package com.rocket.biometrix.Database;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
    public static final String DATE= "Date";
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
    public static final String NOTE= "Notes";

    //Updated = Has the field changed from what the webserver has? This has to be an int, so 0 =false 1 =true
    public static final String UPDATED = "Updated";

    //Every single column that is available in the table
    private final static String[] cols = {LOCAL_DIET_ID, USER_NAME, WEB_DIET_ID, DATE, TYPE, MEAL,
            SERVING, CALORIES, TOTALFAT, SATFAT, TRANSFAT, CHOLESTEROL, SODIUM, TOTALCARBS, FIBER,
            SUGARS, PROTEIN, VITAMINA, VITAMINB, CALCIUM, IRON, NOTE, UPDATED};

    //Group together all int columns to avoid a super long create table function
    public static final String[] intCols = {CALORIES, TOTALFAT, SATFAT, TRANSFAT, CHOLESTEROL, SODIUM, TOTALCARBS, FIBER,
    SUGARS, PROTEIN, VITAMINA, VITAMINB, CALCIUM, IRON};

    private LocalStorageAccessDiet(){}

    public static String createTable() {

        StringBuilder tableSQL = new StringBuilder();

        tableSQL.append("CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_DIET_ID + " integer primary key, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_DIET_ID + " int Null, " +
                DATE + " date Not Null, " +
                TYPE + " varchar(40) Not null, " +
                MEAL + " varchar(20) Null, " +
                SERVING + " VARCHAR(20) Null, ");

        //Loops through each collumn and makes it an int Null column
        for (String col : intCols)
        {
            tableSQL.append(col + " int Null, ");
        }
        tableSQL.append(NOTE + " varchar(255), " +
                UPDATED + " int default 0" +");");

        return tableSQL.toString();
    }

    public static String getTableName() {return  TABLE_NAME;}

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
     * Returns a list of all of the entries that have been made in this module. Shows meal, date,
     * and calories for now
     * @param c The current context
     * @return A list of the entries with the matching data
     */
    public static List<String[]> getEntries(Context c)
    {
        String query = "Select " + DATE + ", " + MEAL + ", " + CALORIES +
                " FROM " + TABLE_NAME + " Order By " + DATE;

        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

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
}
