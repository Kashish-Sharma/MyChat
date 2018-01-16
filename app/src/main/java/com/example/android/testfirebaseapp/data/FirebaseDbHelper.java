package com.example.android.testfirebaseapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class FirebaseDbHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "firebase.db";
    private static final int DATABASE_VERSION = 3;


    public FirebaseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FIREBASE_TABLE =
                "CREATE TABLE " + FirebaseContract.FirebaseEntry.TABLE_NAME + " ("                      +
                        FirebaseContract.FirebaseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "     +
                        FirebaseContract.FirebaseEntry.COLUMN_USERNAME + " VARCHAR(30) NOT NULL, "      +
                        FirebaseContract.FirebaseEntry.COLUMN_IMAGE_URI + " VARCHAR(100), "             +
                        FirebaseContract.FirebaseEntry.COLUMN_TEXT + " TEXT , "                 +
                        FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP + " BIGINT NOT NULL,"            +
                        "UNIQUE (" + FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP + ") ON CONFLICT REPLACE);";
        sqLiteDatabase.execSQL(SQL_CREATE_FIREBASE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FirebaseContract.FirebaseEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
