package com.rocket.biometrix.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tannalynn on 3/3/2016.
 */
public class LocalStorageAccess extends SQLiteOpenHelper {
    protected static final String DATABASE_NAME = "BiometrixLAS";
    protected static final int DATABASE_VERSION = 3;
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

    //Called when app is installed
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
        //db.execSQL(LocalStorageAccessDiet.createTable()); TODO: uncomment when written
        //db.execSQL(LocalStorageAccessMedication.createTable());
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
        //For now call onUpgradeAlter. I know this breaks OO principles, but seems like a good solution since the modules tables can be so different, and the whole db has to be updated at once.
        if (oldVersion < DATABASE_VERSION - 1) {
            dropTables(db);
            onCreate(db); //Drop and recreate
        }
    }

    protected static long safeInsert(String tablename, String nullColumn, ContentValues columnsAndValues){

        SQLiteDatabase db = m_instance.getWritableDatabase();
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

    //Get all rows that match date YYYY-MM-DD (pass in date to search, then table you are looking at...)
    public static Cursor selectByDate(String dayte, String tbl, String date_col){
        SQLiteDatabase db= m_instance.getReadableDatabase();
        Cursor cur=db.rawQuery("SELECT * FROM "+tbl+" WHERE "+date_col+ " == "+dayte, null);

        return cur;
    }
}
