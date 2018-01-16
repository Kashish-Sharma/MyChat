package com.example.android.testfirebaseapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.testfirebaseapp.data.FirebaseContract;
import com.example.android.testfirebaseapp.data.FirebaseDbHelper;
import com.github.library.bubbleview.BubbleImageView;
import com.github.library.bubbleview.BubbleLinearLayout;
import com.github.library.bubbleview.BubbleTextView;
import com.google.firebase.database.ChildEventListener;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>{

    private Cursor mCursor;
    private Context mContext;
    private String mUsernameCheck;
    public static final String MyPREFERENCES = "sharedPreferences";

    public RecyclerViewAdapter(Context context)
    {
        this.mContext=context;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_message,parent,false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mCursor.getColumnIndex(FirebaseContract.FirebaseEntry.COLUMN_USERNAME));
        int imageColumnId = mCursor.getColumnIndex(FirebaseContract.FirebaseEntry.COLUMN_IMAGE_URI);
        boolean isPhoto = mCursor.getString(imageColumnId)!=null;
        int messageColumnId = mCursor.getColumnIndex(FirebaseContract.FirebaseEntry.COLUMN_TEXT);
        int nameColumnId = mCursor.getColumnIndex(FirebaseContract.FirebaseEntry.COLUMN_USERNAME);
        long id = mCursor.getLong(mCursor.getColumnIndex(FirebaseContract.FirebaseEntry._ID));
        holder.itemView.setTag(id);
        SharedPreferences sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mUsernameCheck = sharedpreferences.getString("THEUSERNAME","NoUsername");
        Log.i("MyUserName",mUsernameCheck);
        Log.i("MyUserNameDatabase",name);

        if (mUsernameCheck != null && mUsernameCheck.equals(name)){

            holder.textLayoutOther.setVisibility(View.GONE);
            holder.nameTextImageOther.setVisibility(View.GONE);
            holder.photoOther.setVisibility(View.GONE);
            holder.messageTextOther.setVisibility(View.GONE);
            holder.nameTextOther.setVisibility(View.GONE);
            holder.imageLayoutOther.setVisibility(View.GONE);

            if (isPhoto) {
                holder.textLayout.setVisibility(View.GONE);
                holder.imageLayout.setVisibility(View.VISIBLE);
                holder.nameTextImage.setText(mCursor.getString(nameColumnId));
                Glide.with(holder.photo.getContext())
                        .load(mCursor.getString(imageColumnId))
                        .override(350,350)
                        .fitCenter()
                        .into(holder.photo);
            } else {
                holder.textLayout.setVisibility(View.VISIBLE);
                holder.imageLayout.setVisibility(View.GONE);
                holder.nameText.setText(mCursor.getString(nameColumnId));
                holder.messageText.setText(mCursor.getString(messageColumnId));
            }
        }
        else {

            holder.textLayoutOther.setVisibility(View.VISIBLE);
            holder.nameTextImageOther.setVisibility(View.VISIBLE);
            holder.photoOther.setVisibility(View.VISIBLE);
            holder.messageTextOther.setVisibility(View.VISIBLE);
            holder.nameTextOther.setVisibility(View.VISIBLE);
            holder.imageLayoutOther.setVisibility(View.VISIBLE);

            holder.photo.setVisibility(View.GONE);
            holder.messageText.setVisibility(View.GONE);
            holder.nameText.setVisibility(View.GONE);
            holder.imageLayout.setVisibility(View.GONE);
            holder.nameTextImage.setVisibility(View.GONE);
            holder.textLayout.setVisibility(View.GONE);

            if (isPhoto) {
                holder.textLayoutOther.setVisibility(View.GONE);
                holder.imageLayoutOther.setVisibility(View.VISIBLE);
                holder.nameTextImageOther.setText(mCursor.getString(nameColumnId));
                Glide.with(holder.photo.getContext())
                        .load(mCursor.getString(imageColumnId))
                        .override(350,350)
                        .fitCenter()
                        .into(holder.photoOther);
            } else {

                holder.textLayoutOther.setVisibility(View.VISIBLE);
                holder.imageLayoutOther.setVisibility(View.GONE);
                holder.nameTextOther.setText(mCursor.getString(nameColumnId));
                holder.messageTextOther.setText(mCursor.getString(messageColumnId));
            }

        }


    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c){
        if (mCursor == c)
            return null;
        Cursor temp = mCursor;
        this.mCursor = c;

        if (c!=null){
            this.notifyDataSetChanged();
        }
        return temp;
    }

//    public boolean checkToInsert(long timeStamp){
//        Cursor cursor = sqLiteDatabase.query(TABLE, allColluns, null, null, null, null, ID +" DESC", "1");
//        if (mCursor.moveToLast()) {
//            int databaseTimeStampId = mCursor.getColumnIndex(FirebaseContract.FirebaseEntry.COLUMN_TIMESTAMP);
//            long databaseTimeStamp = mCursor.getLong(databaseTimeStampId);
//
//            if (databaseTimeStamp < timeStamp) {
//                return true;
//            } else
//                return false;
//        }
//    }

    class MessageViewHolder extends RecyclerView.ViewHolder{
        ImageView photo,photoOther;
        TextView messageText,messageTextOther;
        CardView imageLayout, textLayout,imageLayoutOther,textLayoutOther;
        TextView nameText,nameTextImage,nameTextOther,nameTextImageOther;
        public MessageViewHolder(View itemView) {
            super(itemView);

            photo = (ImageView) itemView.findViewById(R.id.photoImageView);
            messageText = (TextView) itemView.findViewById(R.id.messageTextView);
            nameText = (TextView)itemView.findViewById(R.id.nameTextView);
            imageLayout = (CardView) itemView.findViewById(R.id.imageLayout);
            nameTextImage = (TextView) itemView.findViewById(R.id.nameTextViewImage);
            textLayout = (CardView) itemView.findViewById(R.id.textLayout);


            textLayoutOther = (CardView) itemView.findViewById(R.id.textLayoutOther);
            nameTextImageOther = (TextView) itemView.findViewById(R.id.nameTextViewImageOther);
            photoOther = (ImageView) itemView.findViewById(R.id.photoImageViewOther);
            messageTextOther = (TextView) itemView.findViewById(R.id.messageTextViewOther);
            messageTextOther = (TextView) itemView.findViewById(R.id.messageTextViewOther);
            nameTextOther = (TextView)itemView.findViewById(R.id.nameTextViewOther);
            imageLayoutOther = (CardView) itemView.findViewById(R.id.imageLayoutOther);

        }
    }

}
