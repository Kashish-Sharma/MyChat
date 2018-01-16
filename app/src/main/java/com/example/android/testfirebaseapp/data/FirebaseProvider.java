package com.example.android.testfirebaseapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class FirebaseProvider extends ContentProvider {

    public static final int FIREBASE = 100;
    public static final int FIREBASE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FirebaseContract.CONTENT_AUTHORITY,FirebaseContract.PATH_FIREBASE,FIREBASE);
        uriMatcher.addURI(FirebaseContract.CONTENT_AUTHORITY,FirebaseContract.PATH_FIREBASE + "/#",FIREBASE_WITH_ID);
        return uriMatcher;
    }

    private FirebaseDbHelper mFirebaseDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFirebaseDbHelper = new FirebaseDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mFirebaseDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match){
            case FIREBASE:
                retCursor = db.query(FirebaseContract.FirebaseEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mFirebaseDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri retUri;

        switch (match){
            case FIREBASE:
                long id = db.insert(FirebaseContract.FirebaseEntry.TABLE_NAME,null,contentValues);
                if (id>0){
                    retUri = ContentUris.withAppendedId(FirebaseContract.FirebaseEntry.CONTENT_URI,id);
                } else {
                    throw new SQLException("Failed to insert rows into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mFirebaseDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int firebaseDeleted;

        switch (match){
            case FIREBASE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                firebaseDeleted = db.delete(FirebaseContract.FirebaseEntry.TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("UNknown Uri: " + uri);
        }
        if (firebaseDeleted!=0)
            getContext().getContentResolver().notifyChange(uri,null);

        return firebaseDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
