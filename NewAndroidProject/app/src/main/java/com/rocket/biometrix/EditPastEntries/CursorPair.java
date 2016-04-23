package com.rocket.biometrix.EditPastEntries;

import android.database.Cursor;

/**
 * Created by JP on 4/9/2016.
 * To avoid creating custom dictionary map
 */
public class CursorPair {
    protected String tableName;
    protected Cursor query;

    public CursorPair(String tbl, Cursor curse){
        this.tableName = tbl;
        this.query = curse;
    }

    public String getTableName (){
        return tableName;
    }
}
