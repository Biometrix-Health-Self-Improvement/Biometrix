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
    protected static final int DATABASE_VERSION = 7;
    protected static LocalStorageAccess m_instance = null;


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

    private void createTables(SQLiteDatabase db){
        //Create all the tables
        db.execSQL(LocalStorageAccessExercise.createTable());
        db.execSQL(LocalStorageAccessDiet.createTable());
        db.execSQL(LocalStorageAccessMedication.createTable());
        db.execSQL(LocalStorageAccessSleep.createTable());
        db.execSQL(LocalStorageAccessMood.createTable());

    }

    private void dropTables(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessExercise.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessDiet.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessMedication.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessMood.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + LocalStorageAccessSleep.getTableName());
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

    /**
     * Retrieves the largest of the primary key fields and returns it as an int
     * @param c
     * @param idField
     * @param tableName
     * @return
     */
    public int GetLastID(Context c, String idField, String tableName)
    {
        //String query = "Select " + idField +
        //       " FROM " + tableName + " Order By " + idField + " DESC LIMIT 1";


        SQLiteDatabase db = LocalStorageAccess.getInstance(c).getReadableDatabase();

        //Select idField From tableName DESC LIMIT 1
        Cursor cursor = db.query(tableName, new String[]{idField}, null, null, null, null, idField + " DESC LIMIT 1");

        //Cursor cursor = db.rawQuery(query, null);

        int id = 0;

        if (cursor.moveToFirst())
        {
            id = cursor.getInt(0);
        }

        return id;
    }

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

    //Get all rows that match date YYYY-MM-DD (pass in date to search, then table you are looking at...)
    public static Cursor selectByDate(String date, String table, String date_col){
        SQLiteDatabase db= m_instance.getReadableDatabase();

        //Cursor cur=db.rawQuery("SELECT * FROM "+table+" WHERE "+date_col+ " == "+date, null);

        //SELECT * From table WHERE date_col = date
        Cursor cur = db.query(table, null, date_col + " = ?", new String[]{date}, null, null, null);

        return cur;
    }

    //About the only Query I can think of that all modules will have in common.
    public static String selectALLasStrings(String tableName, String[] gotColumns, String UIDcol){
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

            int cid = cursor.getInt(indexArray[0]); //cursor id, references rows by their primary key
        }

        return buf.toString();
    }

    //Give clean date strings in form YYYY-MM-DD (see StringDateTimeConverter Class) as parameters;
    //Returns Cursor of rows, (see EditPastEntries)
    public static Cursor selectAllDatabyDateRange(String tablename, String date_col, String startDate, String endDate){

        SQLiteDatabase db=m_instance.getReadableDatabase();
        //Cursor cur=db.rawQuery("SELECT * FROM " + tablename + " WHERE " + date_col
        //        + " >= " + startDate + " AND " + date_col + " <= " + endDate, null);

        //Select * FROM tablename WHERE date_col >= startDate AND date_col <= endDate
        Cursor cur = db.query(tablename, null, date_col + " >= ? AND " + date_col + " <= ?",
                new String[]{startDate, endDate}, null, null, null);

        return cur;
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


        SQLiteDatabase db=m_instance.getReadableDatabase();
        //Cursor cur=db.rawQuery("SELECT * FROM " + tablename + " WHERE " + date_col
        //        + " >= " + startDate + " AND " + date_col + " <= " + endDate, null);

        return db.query(tablename, null, date_col + " >= ? AND " + date_col + " <= ?",
                new String[]{startDate, endDate}, null, null, null);
    }

    /**
     * Returns all rows for the currently logged in user. If no user is logged in, returns the
     * columns for the user "default"
     * @param c
     * @return A Cursor to all of the columns for the sleep table for the current user
     */
    public static Cursor selectAllEntries(Context c, String tableName, String orderBy, boolean curUserOnly)
    {
        SQLiteDatabase database = getInstance(c).getReadableDatabase();

        if (curUserOnly)
        {
            String username = "default";

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
}
