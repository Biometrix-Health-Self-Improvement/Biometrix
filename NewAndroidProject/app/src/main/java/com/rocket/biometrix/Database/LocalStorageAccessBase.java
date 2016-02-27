package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * What even is this?
 * LocalStorageAccessBase uses SQLiteOpenHelper to write/read from the local-on-device SQLite database
 * It is an abstract base class that kinda uses the adapter pattern to have maximum code-reuse
 * The class assumes that the WebServer is used as a file access system which 'backs up' the local DB, Loads into the local DB from the server, stores User logins, analytics, etc. The True master database resides on the WebServer
 * The SQLite database is assumed to be storage for 'entries' where each module IS one table in the database.
 *
 * HOW TO USE IN YOUR MODULE:
 * 1) Make new class that Extends this class and implement (@Override) the methods you're forced to (abstract methods like getColumns())
 * 2) Hardcode your Columns as constant strings (Look at LocalStorageAccessExercise or LSA Sleep for examples) {Or create a manager class if have a TON of columns}
 * 3) Write the CreateTable statement (Examples in Exercise or Sleep)
 * 4) Write the onUpgradeAlter function (probably just want to drop table if it exists, this method is called when the version of the db is changed IE when we update the app when users are already using it, so not super important now)
 * 5) Override base class methods that need to be more specific, for example JP calls the base class insert method only after he checks that every column has data to be inserted, he did not have to do this.
 * 6) Write custom methods / Database Queries that make sense only in your module E.G. Totaling the Fiber of a given day
 *
 * HOW TO USE IN YOUR UI:
 * 1) Decide how to prepare a ContentValues set to use an insert method like safeInsert() (Getting from UI, Error checking, Allowed values, etc.)
 *
 * 2) Pull data from the UI, StringDateTimeConverter has very helpful methods like GetStringFromEditText()
 *    and if your using the DateTimePopulateTextView class in your UI it has methods to prepare the Dates and times
 *    correctly for SQLite. Because SQLite has implicit conversion, you can pull a string from the UI then insert() it as a
 *    String in the format YYYY-MM-DD and if you specified that column to be of DATETIME type (in createTable()), it will just work.
 *    Consider making a helper class specific to your module to do this [don't have to] E.G. SleepData and things like in SleepEntry where you set up Views, and listeners all at once in one method
 *
 * 3) Tie-in your done button onClick event to the navigation drawer: in your UI layout file, go to the done button
 *    and look at its onClick, use the drop down menu to reference "EntryDoneOnClick" should see a red M&M next to it
 *    Don't even try to do this programmatically do this in the layout UI Design tab, see fragment_sleep_entry.xml for example
 *
 * 4) Go to the function EntryDoneOnClick in the NavigationDrawerActivity and hook in a method like 'DoneonClick()' that
 *    you will implement in the your Entry Fragment. (This is one way to make the entry go back to 'parent' it just replaces the current fragment)
 *
 * 5) Implement that onClick method in your Entry fragment, Probably want to use an Insert method from this class
 *    Create an instance of LocalStorageAccess and use its methods on the data you pulled from the UI
 *    JP has comments explaining how he did it in ExerciseEntry.java
 *
 * 6) You can also use LSA to prepare data to be displayed or read from the database, see EditPastEntries
 */
public abstract class LocalStorageAccessBase  extends SQLiteOpenHelper {

    //Name of database that won't change throughout all the implementations of this class.
    protected static final String DATABASE_NAME = "BiometrixLAS";
    /*
    * Please increment by 1 each time major changes are made in the database, document your change here
     * Version 1 on 1/08/16
     * Version 2 1/17 testing oncreate exercise
     * Version 3 2/26/2016 adding sleep table.
    */
    protected static final int DATABASE_VERSION = 3;

    //TODO: Pull user login information from SharedPreference Class and hash it somehow or use the webservice to get a UserID for all the tables.

    public LocalStorageAccessBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //SQLiteOpenHelper default Ctor
    public LocalStorageAccessBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    //Called when app is installed
    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(createTable());
        }
        catch(SQLException e){
            e.printStackTrace();
            //TODO: Error handling, validation...
        }

    }
    //When database version has changed, call the child module implementation of updating the database.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //For now call onUpgradeAlter. I know this breaks OO principles, but seems like a good solution since the modules tables can be so different, and the whole db has to be updated at once.
        boolean oldVersionDetected = onUpgradeAlter(db, oldVersion, newVersion);
    }

    //Way to insert values into a table
    //Child class (a module's implementation will fill a ContentValues and call this function after double checking the keys=column names)
    protected long safeInsert(String tablename, String nullColumn, ContentValues columnsAndValues){

        SQLiteDatabase db = this.getWritableDatabase();
        long rowNumberInserted = -1; //-1 if fail

        db.beginTransaction();

        try {
            rowNumberInserted = db.insertOrThrow(tablename, nullColumn, columnsAndValues);
            db.setTransactionSuccessful();

        } catch(SQLException e) {

            e.printStackTrace();

        } finally {
            db.endTransaction(); //rollback is automatic
            db.close(); //breaks sometimes?
        }

        return rowNumberInserted;
    }

    //Get int database version for testing in the onUpgrade methods
    protected int getDBVersion(){
        return DATABASE_VERSION;
    }

    //About the only Query I can think of that all modules will have in common.
    protected String selectALLasStrings(String tableName){
        SQLiteDatabase db = this.getWritableDatabase(); //Readable?
        String[] columns = getColumns();
        Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
        StringBuffer buf = new StringBuffer();

        while (cursor.moveToNext()){
            int[] indexArray = new int[columns.length+1];
            indexArray[0] = cursor.getColumnIndex(getUIDColumn());
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

    //Select * From tbl, returned as cursor.
    protected Cursor selectALL(String tbl)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cur=db.rawQuery("SELECT * FROM "+tbl, null);

        return cur;
    }

    //Get all rows that match date YYYY-MM-DD (pass in date to search, then table you are looking at...)
    protected Cursor selectByDate(String dayte, String tbl, String date_col){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cur=db.rawQuery("SELECT * FROM "+tbl+" WHERE "+date_col+ " == "+dayte, null);

        return cur;
    }


    //Query out all data related to a range of dates, default version
    protected Cursor selectAllDatabyDateRange(String tablename, String date_col){

        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        String startDate; //default to 90 days
        String endDate; //default to week from now


        //Start 90 days back; see business rules
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -90);
        Date today90 = cal.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        startDate = dateFormat.format(today90);

        //One week into the future
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date nextWeek = cal.getTime();
        endDate = dateFormat.format(nextWeek);


        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cur=db.rawQuery("SELECT * FROM " + tablename + " WHERE " + date_col
                + " >= " + startDate + " AND " + date_col + " <= " + endDate, null);

        return cur;
    }


    //Give clean date strings in form YYYY-MM-DD (see StringDateTimeConverter Class) as parameters;
    //Returns Cursor of rows, (see EditPastEntries)
    protected Cursor selectAllDatabyDateRange(String tablename, String date_col, String startDate, String endDate){

        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cur=db.rawQuery("SELECT * FROM " + tablename + " WHERE " + date_col
                + " >= " + startDate + " AND " + date_col + " <= " + endDate, null);

        return cur;

    }


    //A module's table create sql statement.
    protected abstract String createTable();

    //Returns string array of private string variables representing Columns in child module class
    protected abstract String[] getColumns();

    //Returns UID (primary key) column name
    protected abstract String getUIDColumn();

    //Version safe Alter table SQL called in onUpgrade, eventually might return some kind of error checking information...
    //Returns true if oldVersion was detected
    protected abstract boolean onUpgradeAlter(SQLiteDatabase db, int oldVersion, int newVersion);




}