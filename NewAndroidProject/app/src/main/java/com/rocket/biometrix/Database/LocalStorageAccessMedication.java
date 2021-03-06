package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.rocket.biometrix.Login.LocalAccount;

/**
 * Created by tannalynn on 1/22/2016.
 */
public class LocalStorageAccessMedication {

    //Medication Table and columns
    public static final String TABLE_NAME = "Medication";
    public static final String LOCAL_MEDICATION_ID = "LocalMedicationID";
    public static final String USER_NAME = "UserName";
    public static final String WEB_MEDICATION_ID = "WebMedicationID";
    public static final String DATE = "Date";
    public static final String TIME = "Time";
    public static final String BRAND_NAME = "BrandName";
    public static final String PRESCRIBER = "Prescriber";
    public static final String DOSE = "Dose";
    public static final String INSTRUCTIONS = "Instructions";
    public static final String WARNINGS = "Warnings";
    public static final String NOTES = "Notes";


    public static final String[] columns = {LOCAL_MEDICATION_ID, USER_NAME, WEB_MEDICATION_ID,
            DATE, TIME, BRAND_NAME, PRESCRIBER, DOSE, INSTRUCTIONS, WARNINGS, NOTES};


    public LocalStorageAccessMedication(Context context){
    }


    public static String createTable()
    {
        //Creates the SQL string to make the Medication table
        return "CREATE TABLE " + TABLE_NAME + " ( " +
                LOCAL_MEDICATION_ID + " integer primary key autoincrement, " +
                USER_NAME + " varchar(50) Not Null, " +
                WEB_MEDICATION_ID + " int Null, " +
                DATE + " date Not Null, " +
                TIME + " time Not Null, " +
                BRAND_NAME + " varchar(255) null, " +
                PRESCRIBER + " varchar(255) null, " +
                DOSE + " varchar(255) null, " +
                INSTRUCTIONS + " varchar(255) null, " +
                WARNINGS + " varchar(255) null, " +
                NOTES + " varchar(255) null" +");";
    }

    /**
     * Makes a call to the base class with the needed parameters to pull out the last primary key
     * entered
     * @param c
     * @return The integer value of the last primary key entered.
     */
    public static int GetLastID(Context c)
    {
        return LocalStorageAccess.getInstance(c).GetLastID(c, LOCAL_MEDICATION_ID, TABLE_NAME);
    }

    //Returns the columns for the table
    public static String[] getColumns()
    {
        return columns;
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
     * Deletes a the row with the local key that has the passed in value
     * @param context A reference to the context, used to grab database access
     * @param value The value of the key on the row to delete
     * @return The number of rows deleted. Should be 0 on fail and 1 on success. Greater than 1
     * means something went wrong.
     */
    public static int deleteByLocalKeyValue(Context context, int value)
    {
        return LocalStorageAccess.deleteEntryByID(context, TABLE_NAME, LOCAL_MEDICATION_ID, value);
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
        return LocalStorageAccess.getWebKeyFromLocalKey(context, TABLE_NAME, LOCAL_MEDICATION_ID, WEB_MEDICATION_ID, value);
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
        return LocalStorageAccess.updateTableFromContentValues(context, contentValues, localPrimaryKey, TABLE_NAME, LOCAL_MEDICATION_ID);
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

        webCV.put(WEB_MEDICATION_ID, webID);

        int num_rows = db.update(TABLE_NAME, webCV, LOCAL_MEDICATION_ID + " = ?", new String[]{localID.toString()});

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

}
