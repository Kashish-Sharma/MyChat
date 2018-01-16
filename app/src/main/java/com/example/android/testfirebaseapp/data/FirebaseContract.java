package com.example.android.testfirebaseapp.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class FirebaseContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.testfirebaseapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);
    public static final String PATH_FIREBASE = "firebase";

    public static final class FirebaseEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FIREBASE)
                .build();
        public static final String TABLE_NAME = "firebase";

        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_TEXT = "message_text";
        public static final String COLUMN_IMAGE_URI = "image_uri";
        public static final String COLUMN_TIMESTAMP = "time_stamp";

    }


}
