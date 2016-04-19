package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rocket.biometrix.Login.LocalAccount;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by alder on 3/3/2016.
 */
public class LocalStorageAccess extends SQLiteOpenHelper {
    protected static final String DATABASE_NAME = "BiometrixLAS";

    //Incremented to 4. Implemented ID fields for sleep, exercise, and mood. Also implemented, needs update
    //Incremented to 5. Diet Table added
    //Incremented to 6. To autoincrement, the primary key must say integer, not int
    //Incremented to 7. Standardizing format to match Excel file, also Medication module is now a thing
    //Incremented to 8. Sync Table now exists, updated columns removed
    //Incremented to 9. Sync table username field changed to avoid same names on inner join
    //10. Sync table given webkey column to uniquely identify removals and updates
    protected static final int DATABASE_VERSION = 10;
    protected static LocalStorageAccess m_instance = null;

    //Strings for the sync table creation
    public static final String SYNC_TABLE_NAME = "Sync";
    public static final String SYNC_TABLE_NAME_COLUMN = "TableName";
    public static final String SYNC_KEY_COLUMN = "Key";
    public static final String SYNC_WEB_KEY_COLUMN = "WebKey";
    public static final String SYNC_STATUS_COLUMN = "Status";

    //Making this one different than other tables, since this is basically the only table that needs
    // to be joined on
    //and it causes more headache to try to have this be Username
    public static final String SYNC_USERNAME_COLUMN = "SyncUsername";

    //Status codes for the sync table
    public static final int SYNC_NEEDS_ADDED = 1;
    public static final int SYNC_NEEDS_UPDATED = 2;
    public static final int SYNC_NEEDS_DELETED = 3;


    public static LocalStorageAccess getInstance(Context c){
        if (m_instance == null){
            m_instance = new LocalStorageAccess(c.getApplicationContext());
        }
        return m_instance;
    }


    private LocalStorageAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Called first time the database is requested to be used
    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            createTables(db);
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }

    /**
     * Calls the create table statements for each of the tables on the passed in database
     * @param db The SQLite database to operate on
     */
    private void createTables(SQLiteDatabase db){
        //Create all the tables
        db.execSQL(LocalStorageAccessExercise.createTable());
        db.execSQL(LocalStorageAccessDiet.createTable());
        db.execSQL(LocalStorageAccessMedication.createTable());
        db.execSQL(LocalStorageAccessSleep.createTable());
        db.execSQL(LocalStorageAccessMood.createTable());
        db.execSQL(getSyncTableCreateSQL());
    }

    /**
     * Calls the drop table statements for each of the tables on the passed in database
     * @param db The SQLite database to operate on
     */
    private void dropTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessExercise.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessDiet.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessMedication.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessMood.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessSleep.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + SYNC_TABLE_NAME);
    }


    //When database version has changed, call the child module implementation of updating the database.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //NEED to allow specific upgrade version for EACH module because a user can have an old database on a NEWly updated app.
        if (oldVersion < DATABASE_VERSION) {
            dropTables(db);
            onCreate(db); //Drop and recreate
        }
    }


    // BEGIN SYNC TABLE METHODS
    /**
     * Return the SQLite create table function for the Sync table
     * @return Returns a string that is the SQLite string for creating the table
     */
    protected String getSyncTableCreateSQL()
    {
        return "CREATE TABLE " + SYNC_TABLE_NAME + " ( " +
                SYNC_USERNAME_COLUMN + " varchar(50) Not Null, " +
                SYNC_TABLE_NAME_COLUMN + " varchar(50) Not Null, " +
                SYNC_KEY_COLUMN + " int Not Null, " +
                SYNC_WEB_KEY_COLUMN + " int Null, " +
                SYNC_STATUS_COLUMN + " int default 0, " +
                " unique (" + SYNC_TABLE_NAME_COLUMN + ", " + SYNC_KEY_COLUMN + ") ON CONFLICT FAIL " +
                ");";
    }

    /**
     * Does either an insert or an update on the sync table depending on whether the key and table
     * information is already there or not
     * @param c The current context which is used to grab the database
     * @param tableName The name of the table that the entry is for
     * @param keyValue The value of the primary key for the table the entry is for
     * @param webKey The value of the web key in order to ensure it is correctly identified on the
     *               webserver. THIS SHOULD BE -1 FOR INSERTS AS THERE IS NOT ONE
     * @param status One of the status codes for the sync table that determines what operation needs
     *               to be performed on the web database
     * @return An integer value for the status. -1 means an error occurred. 0 means that no change
     * was made (which is usually fine) and greater than 0 means that the column was updated or
     * inserted successfully
     */
    public int insertOrUpdateSyncTable(Context c, String tableName, int keyValue, int webKey, int status)
    {
        int returnStatus = -1;
        SQLiteDatabase db = getInstance(c).getWritableDatabase();

        try
        {
            //Select Status From Sync Where TableName = *tableName* AND Key = *keyValue*
            Cursor cursor = db.query(SYNC_TABLE_NAME, new String[]{SYNC_STATUS_COLUMN},
                    SYNC_TABLE_NAME_COLUMN + " = ? AND " + SYNC_KEY_COLUMN + " = ?",
                    new String[]{tableName, Integer.toString(keyValue)}, null, null, null);

            int curStatus = 0;

            //Contains the new status values for insert or update
            ContentValues contentValues = new ContentValues();
            contentValues.put(SYNC_STATUS_COLUMN, status);

            if(webKey == -1)
            {
                contentValues.put(SYNC_WEB_KEY_COLUMN, (String)null);
            }
            else
            {
                contentValues.put(SYNC_WEB_KEY_COLUMN, webKey);
            }

            //If a value was found then an update is needed, not an insert
            if (cursor.moveToFirst()) {
                curStatus = cursor.getInt(0);
                cursor.close();

                //If the status is the same, no operation needed. If the current status is that the row
                //needs to be added, and the new status is the row needs to be updated this also means
                //that no operation is needed because there is nothing to update yet
                if (curStatus != status && (curStatus != SYNC_NEEDS_ADDED || status != SYNC_NEEDS_UPDATED)) {
                    returnStatus = db.update(SYNC_TABLE_NAME, contentValues,
                            SYNC_TABLE_NAME_COLUMN + " = ? AND " + SYNC_KEY_COLUMN + " = ?",
                            new String[]{tableName, Integer.toString(keyValue)});
                } else {
                    returnStatus = 0;
                }
            } else {

                cursor.close();

                try {
                    contentValues.put(SYNC_KEY_COLUMN, keyValue);

                    if (LocalAccount.isLoggedIn())
                    {
                        contentValues.put(SYNC_USERNAME_COLUMN, LocalAccount.GetInstance().GetUsername());
                    }
                    else
                    {
                        contentValues.put(SYNC_USERNAME_COLUMN, LocalAccount.DEFAULT_NAME);
                    }

                    contentValues.put(SYNC_TABLE_NAME_COLUMN, tableName);

                    db.insertOrThrow(SYNC_TABLE_NAME, null, contentValues);
                    returnStatus = 1;
                } catch (SQLException except) {
                    except.getMessage();
                    returnStatus = -1;
                }
            }
        }
        catch (Exception except)
        {
            except.getMessage();
            throw except;
        }

        db.close();
        return returnStatus;
    }

    /**
     * Removes the passed in keyValue and tablename pair from the sync table
     * @param c The current context which is used to grab the database
     * @param tableName The name of the table that the entry is for
     * @param keyValue The value of the primary key for the entry that needs to be removed
     * @param isLocalKey A boolean value, if true then the key referred to is a local key, if false
     *                   the key is a web key
     * @return A boolean for whether the operation succeeded or failed
     */
    public boolean deleteEntryFromSyncTable(Context c, String tableName, int keyValue, boolean isLocalKey)
    {
        boolean returnStatus = false;
        SQLiteDatabase db = getInstance(c).getWritableDatabase();

        String deleteQuery = SYNC_TABLE_NAME_COLUMN + " = ? AND ";

        if (isLocalKey)
        {
            deleteQuery +=  SYNC_KEY_COLUMN + " = ?";
        }
        else
        {
            deleteQuery += SYNC_WEB_KEY_COLUMN + " = ?";
        }

        //Select Status From Sync Where TableName = *tableName* AND Key = *keyValue*
        int deletedRows =  db.delete(SYNC_TABLE_NAME,
                deleteQuery, new String[] {tableName, Integer.toString(keyValue)});

        if (deletedRows == 1)
        {
            returnStatus = true;
        }
        else
        {
            String log = Integer.toString(deletedRows) + " rows deleted from sync table for column " + tableName + ", instead of just 1";
            Log.i("", log);
        }

        return returnStatus;
    }
    //END SYNC TABLE METHODS

    /**
     * Retrieves the largest of the primary key fields and returns it as an int
     * @param c The current context so that the database can be obtained
     * @param idField The name of the id column to search through
     * @param tableName The name of the SQLite table to search
     * @return The ID in the most recent entry
     */
    public int GetLastID(Context c, String idField, String tableName)
    {
        SQLiteDatabase db = getInstance(c).getReadableDatabase();

        //Select idField From tableName DESC LIMIT 1
        Cursor cursor = db.query(tableName, new String[]{idField}, null, null, null, null, idField + " DESC LIMIT 1");

        int id = 0;

        if (cursor.moveToFirst())
        {
            id = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return id;
    }

    /**
     * Performs an insert operation on the requested tablename
     * @param tablename The name of the table to insert into
     * @param nullColumn Any column on the table that can be null. Preferably the one chosen
     *                   by the magic 8 ball.
     *                   http://stackoverflow.com/questions/2662927/android-sqlite-nullcolumnhack-parameter-in-insert-replace-methods
     * @param columnsAndValues The names of the columns to insert into and the values to insert
     * @return A long containing the row number that was inserted
     */
    protected long safeInsert(String tablename, String nullColumn, ContentValues columnsAndValues){

        SQLiteDatabase db = null;

        try
        {
            db = m_instance.getWritableDatabase();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        long rowNumberInserted = -1; //-1 if fail

        if (db != null)
        {
            db.beginTransaction();

            try {
                rowNumberInserted = db.insertOrThrow(tablename, nullColumn, columnsAndValues);
                db.setTransactionSuccessful();

            } catch (SQLException e) {

                e.printStackTrace();

            } finally {
                db.endTransaction(); //rollback is automatic
                db.close(); //breaks sometimes?
            }
        }

        return rowNumberInserted;
    }

    /**
     * Get all rows that match date YYYY-MM-DD (pass in date to search, then table you are looking at...
     * @param date The date that you want all values for
     * @param table The name of the table to grab the values from
     * @param date_col The name of the data column to pull from
     * @return A cursor over the data for that day.
     */
    public static Cursor selectByDate(String date, String table, String date_col){
        SQLiteDatabase db= m_instance.getReadableDatabase();

        Cursor cur = db.query(table, null, date_col + " = ?", new String[]{date}, null, null, null);
        return cur;
    }

    //About the only Query I can think of that all modules will have in common.
    public static String selectALLasStrings(String tableName, String[] gotColumns, String UIDcol)
    {
        SQLiteDatabase db = m_instance.getWritableDatabase();
        String[] columns = gotColumns;
        Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
        StringBuffer buf = new StringBuffer();

        while (cursor.moveToNext()){
            int[] indexArray = new int[columns.length+1];
            indexArray[0] = cursor.getColumnIndex(UIDcol);
            int indexesIndex = 0;

            for (String column : columns) {
                indexArray[indexesIndex] = cursor.getColumnIndex(column);
                buf.append( column+": "+cursor.getString(indexArray[indexesIndex])+" " );
                indexesIndex++;
            }

            // TODO: The below line appears unused, I commented it out.
            //int cid = cursor.getInt(indexArray[0]); //cursor id, references rows by their primary key
        }

        return buf.toString();
    }

    //Give clean date strings in form YYYY-MM-DD (see StringDateTimeConverter Class) as parameters;
    //Returns Cursor of rows, (see EditPastEntries)
    public static Cursor selectAllDatabyDateRange(String tablename, String date_col, String startDate, String endDate){

        SQLiteDatabase db=m_instance.getReadableDatabase();

        //Select * FROM tablename WHERE date_col >= startDate AND date_col <= endDate
        return db.query(tablename, null, date_col + " >= ? AND " + date_col + " <= ?",
                new String[]{startDate, endDate}, null, null, null);
    }

    //Query out all data related to a range of dates, default version
    public static Cursor selectAllDatabyDateRange(String tablename, String date_col){

        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        String startDate; //default to 90 days
        String endDate; //default to week from now


        //Start 90 days back; see business rules
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -90);
        Date today90 = cal.getTime();
        startDate = new SimpleDateFormat("YYYY-MM-DD").format(today90);


        //One week into the future
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date nextWeek = cal.getTime();
        endDate = new SimpleDateFormat("YYYY-MM-DD").format(nextWeek);


        SQLiteDatabase db=m_instance.getReadableDatabase();;

        return db.query(tablename, null, date_col + " >= ? AND " + date_col + " <= ?",
                new String[]{startDate, endDate}, null, null, null);
    }

    /**
     * Returns all rows for the currently logged in user. If no user is logged in, returns the
     * columns for the user "default". Returns all columns.
     * @param c The current context which is used by the database queries
     * @param tableName The name of the table to select entries on
     * @param orderBy A string to determine how the data is ordered by SQLite statement
     * @param curUserOnly True means return only data for the currently logged in user, false means
     *                    return all data for local users
     * @return A Cursor to all of the columns on the table for the current user
     */
    public static Cursor selectAllEntries(Context c, String tableName, String orderBy, boolean curUserOnly)
    {
        SQLiteDatabase database = getInstance(c).getReadableDatabase();

        if (curUserOnly)
        {
            String username = LocalAccount.DEFAULT_NAME;

            if (LocalAccount.isLoggedIn()) {
                username = LocalAccount.GetInstance().GetUsername();
            }

            return database.query(tableName, null, "Username = ?", new String[]{username}, null, null, orderBy);
        }
        else
        {
            return database.query(tableName, null, null, null, null, null, orderBy);
        }
    }

    /**
     * Returns all rows for the currently logged in user. If no user is logged in, returns the
     * columns for the user "default"
     * @param c The current context which is used by the database queries
     * @param tableName The name of the table to select entries on
     * @param orderBy A string to determine how the data is ordered by SQLite statement
     * @param columns An array of strings corresponding to the column names to return
     * @param curUserOnly True means return only data for the currently logged in user, false means
     *                    return all data for local users
     * @return A Cursor to all of the columns on the table for the current user
     */
    public static Cursor selectAllEntries(Context c, String tableName, String orderBy, String columns[],
                                          boolean curUserOnly)
    {
        SQLiteDatabase database = getInstance(c).getReadableDatabase();

        if (curUserOnly)
        {
            String username = LocalAccount.DEFAULT_NAME;

            if (LocalAccount.isLoggedIn()) {
                username = LocalAccount.GetInstance().GetUsername();
            }

            return database.query(tableName, columns, "Username = ?", new String[]{username}, null, null, orderBy);
        }
        else
        {
            return database.query(tableName, columns, null, null, null, null, orderBy);
        }
    }

    /**
     * Selects all of the entries for the passed in table that match the type passed
     * @param c The current context which is used by the database queries
     * @param tableName The name of the table to select entries on
     * @param localKey The name of the primary key for the local table
     * @param columns The names of the columns that are to be returned, should be all for update and
     *                add, and the primary keys for delete
     * @param curUserOnly True means return only data for the currently logged in user, false means
     *                    return all data for local users
     * @return A Cursor to all of the columns that need to be updated
     */
    public static Cursor selectPendingEntries(Context c, String tableName, String localKey, String columns[]
            , boolean curUserOnly, int syncType)
    {
        SQLiteDatabase database = getInstance(c).getReadableDatabase();

        //e.g. Mood INNER JOIN SYNC ON MOOD.LocalMoodID = Sync.Key AND Sync.TableName = Mood
        String joinedTable = tableName + " INNER JOIN " + SYNC_TABLE_NAME + " ON " + tableName +
                "." + localKey + " = " + SYNC_TABLE_NAME + "." + SYNC_KEY_COLUMN + " AND " +
                SYNC_TABLE_NAME + "." + SYNC_TABLE_NAME_COLUMN + " = '" + tableName + "'";

        String statusCheck = SYNC_STATUS_COLUMN + " = ";

        switch (syncType)
        {
            case SYNC_NEEDS_ADDED:
                statusCheck += SYNC_NEEDS_ADDED;
                break;
            case SYNC_NEEDS_UPDATED:
                statusCheck += SYNC_NEEDS_UPDATED;
                break;
            case SYNC_NEEDS_DELETED:
                statusCheck += SYNC_NEEDS_DELETED;
                break;
            default:
                return null;
        }

        try
        {
            if (curUserOnly)
            {
                String username = LocalAccount.DEFAULT_NAME;

                if (LocalAccount.isLoggedIn())
                {
                    username = LocalAccount.GetInstance().GetUsername();
                }

                //e.g. select all mood columns for the username where the status is needs added
                return database.query(joinedTable, columns, "Username = ? AND " + statusCheck,
                        new String[]{username}, null, null, null);
            }
            else
            {
                return database.query(joinedTable, columns, statusCheck,
                        null, null, null, null);
            }
        }
        catch (Exception except)
        {
            except.getMessage();
            return null;
        }
    }

    /**
     * Selects all of the entries for the passed in table that match the type passed
     * @param c The current context which is used by the database queries
     * @param tableName The name of the table to select entries on
     * @param localKey The name of the primary key for the local table
     * @param columns The names of the columns that are to be returned, should be the local key
     *                and the web key
     * @param curUserOnly True means return only data for the currently logged in user, false means
     *                    return all data for local users
     * @return A Cursor to all of the columns that need to be updated
     */
    public static Cursor selectNonPendingEntries(Context c, String tableName, String localKey, String columns[]
            , boolean curUserOnly)
    {
        SQLiteDatabase database = getInstance(c).getReadableDatabase();

        //e.g. Mood LEFT INNER JOIN SYNC ON MOOD.LocalMoodID = Sync.Key AND Sync.TableName = Mood
        String joinedTable = tableName + " LEFT OUTER JOIN " + SYNC_TABLE_NAME + " ON " + tableName +
                "." + localKey + " = " + SYNC_TABLE_NAME + "." + SYNC_KEY_COLUMN + " AND " +
                SYNC_TABLE_NAME + "." + SYNC_TABLE_NAME_COLUMN + " = '" + tableName + "'";

        String statusCheck = SYNC_KEY_COLUMN + " is null";

        try
        {
            if (curUserOnly)
            {
                String username = LocalAccount.DEFAULT_NAME;

                if (LocalAccount.isLoggedIn())
                {
                    username = LocalAccount.GetInstance().GetUsername();
                }

                //e.g. select all mood columns for the username where the status is needs added
                return database.query(joinedTable, columns, "Username = ? AND " + statusCheck,
                        new String[]{username}, null, null, null);
            }
            else
            {
                return database.query(joinedTable, columns, statusCheck,
                        null, null, null, null);
            }
        }
        catch (Exception except)
        {
            except.getMessage();
            return null;
        }
    }
}
