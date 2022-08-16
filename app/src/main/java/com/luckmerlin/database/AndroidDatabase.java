package com.luckmerlin.database;

import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class AndroidDatabase implements Database{
    private final String mDatabasePath;
    private SQLiteDatabase mSQLiteDatabase;

    public AndroidDatabase(String databasePath){
        mDatabasePath=databasePath;
    }

    public boolean open(String path){
        SQLiteDatabase database=mSQLiteDatabase;
        if (null!=database){
            return false;
        }
        String databasePath=mDatabasePath;
        database=mSQLiteDatabase=SQLiteDatabase.openOrCreateDatabase(databasePath,
                (SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query)-> {
                return null;
        }, (SQLiteDatabase dbObj)-> {

        });
        return null!=database;
    }

    public boolean put(){
        SQLiteDatabase database=mSQLiteDatabase;
//        database.execPerConnectionSQL();
        return false;
    }

    public boolean close(){
        SQLiteDatabase database=mSQLiteDatabase;
        if (null!=database){
            database.close();
            mSQLiteDatabase=null;
            return true;
        }
        return false;
    }
}
