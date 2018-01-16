package com.example.android.testfirebaseapp;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.Volley;
import com.example.android.testfirebaseapp.data.FirebaseContract;
import com.example.android.testfirebaseapp.data.FirebaseDbHelper;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String URI_TAG = "uriTag";


    public static final String[] MAIN_PROJECTION = {
            FirebaseContract.FirebaseEntry._ID,
            FirebaseContract.FirebaseEntry.COLUMN_USERNAME,
            FirebaseContract.FirebaseEntry.COLUMN_IMAGE_URI,
            FirebaseContract.FirebaseEntry.COLUMN_TEXT,
            FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP
    };
    public static final String[] TIMESTAMP_PROJECTION = {
            FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP
    };

    private static final int TASK_LOADER_ID = 0;
    public static final String MyPREFERENCES = "sharedPreferences";
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    public static final int RC_PHOTO_PICKER = 2;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly-msg-length";
    private int mPosition = RecyclerView.NO_POSITION;

    private RecyclerViewAdapter mAdapter;
    private SQLiteDatabase mDb;
    RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private String mUsername;



    private FirebaseDatabase mFirebaseDAtabase;
    private DatabaseReference mMessageDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private RequestQueue requestQueue;
    private FirebaseDbHelper mDbHelper;
    private DividerItemDecoration dividerItemDecoration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("MyChat");


        mDbHelper = new FirebaseDbHelper(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        mUsername = ANONYMOUS;
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        mDbHelper = new FirebaseDbHelper(this);
        mDb = mDbHelper.getWritableDatabase();

        mFirebaseDAtabase = FirebaseDatabase.getInstance();
        mMessageDatabaseReference = mFirebaseDAtabase.getReference().child("messages");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");


        mRecyclerView = (RecyclerView)findViewById(R.id.chatRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),layoutManager.getOrientation());
        //layoutManager.setReverseLayout(true);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom<oldBottom){
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount()-1);
                        }
                    });
                }
            }
        });


        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        getSupportLoaderManager().initLoader(TASK_LOADER_ID,null,this);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                Intent chooser = Intent.createChooser(intent,"Complete action using");
                startActivityForResult(chooser,RC_PHOTO_PICKER);
            }
        });


        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(),mUsername,null);
                mMessageDatabaseReference.push().setValue(friendlyMessage);


                // Clear input box
                mMessageEditText.setText("");
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null){
                   //user signed-in
                    onSignedInInitialised(user.getDisplayName());
                } else{
                    //user signed-out
                    onSignedOutCleanUp();
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();
                removeChat(id);
                mAdapter.swapCursor(getAllChat());
            }
        }).attachToRecyclerView(mRecyclerView);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Toast.makeText(MainActivity.this,"Signed-In!",Toast.LENGTH_SHORT).show();
            } else if(resultCode == RESULT_CANCELED){
                Toast.makeText(MainActivity.this,"Signed-In Cancelled!",Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode ==RESULT_OK){
            Uri selectedImageUri = data.getData();
            StorageReference photoRef =
                    mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(
                    this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            FriendlyMessage friendlyMessage =
                                    new FriendlyMessage(null,mUsername,downloadUrl.toString());
                            mMessageDatabaseReference.push().setValue(friendlyMessage);
                        }
                    }
            );
        }
    }

    private boolean removeChat(long id){
        return mDb.delete(FirebaseContract.FirebaseEntry.TABLE_NAME, FirebaseContract.FirebaseEntry._ID + "=" + id,null) > 0;
    }

    private Cursor getAllChat(){
        return mDb.query(
                FirebaseContract.FirebaseEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //sign-out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mRecyclerView.removeAllViewsInLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.swapCursor(getAllChat());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.swapCursor(getAllChat());
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void onSignedInInitialised(String username){
        mUsername = username;
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("THEUSERNAME",username);
        editor.apply();
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanUp(){
        mUsername = ANONYMOUS;
        mRecyclerView.removeAllViewsInLayout();
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener(){
        if (mChildEventListener!=null){
            mMessageDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final SQLiteDatabase db = mDbHelper.getReadableDatabase();
                    Cursor cursor = db.query(FirebaseContract.FirebaseEntry.TABLE_NAME, TIMESTAMP_PROJECTION,
                            null, null, null, null, FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP + " DESC", "1");
                    long databaseTimeStamp;

                    if (cursor != null && cursor.moveToLast()) {
                        int databaseTimeStampId = cursor.getColumnIndex(FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP);
                        databaseTimeStamp = cursor.getLong(databaseTimeStampId);
                    } else
                        databaseTimeStamp = 0;
                    cursor.close();

                    final FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    long firebaseTimeStamp = friendlyMessage.getTimeInMillis();

                    ContentValues contentValues = new ContentValues();

                    if (firebaseTimeStamp > databaseTimeStamp) {
                        contentValues.put(FirebaseContract.FirebaseEntry.COLUMN_USERNAME, friendlyMessage.getName());
                        contentValues.put(FirebaseContract.FirebaseEntry.COLUMN_TEXT, friendlyMessage.getText());
                        contentValues.put(FirebaseContract.FirebaseEntry.COLUMN_IMAGE_URI, friendlyMessage.getPhotoUrl());
                        contentValues.put(FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP, friendlyMessage.getTimeInMillis());
                        Uri uri = getContentResolver().insert(FirebaseContract.FirebaseEntry.CONTENT_URI, contentValues);
                        if (uri != null)
                            Log.i(URI_TAG,uri.getPathSegments().get(1)+" column Inserted");
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessageDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case TASK_LOADER_ID:
                Uri firebaseQueryUri = FirebaseContract.FirebaseEntry.CONTENT_URI;
                return new CursorLoader(this,
                        firebaseQueryUri,
                        MAIN_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        //mRecyclerView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
