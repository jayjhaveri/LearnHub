package com.jayjhaveri.learnhub.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.model.Comment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ADMIN-PC on 31-03-2017.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final String TAG = CommentAdapter.class.getSimpleName();
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mCommentIds = new ArrayList<>();
    private List<Comment> mComments = new ArrayList<>();

    public CommentAdapter(Context context, DatabaseReference reference) {
        mContext = context;
        mDatabaseReference = reference;

        // Create child event listener
        // [START child_event_listener_recycler]
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Comment comment = dataSnapshot.getValue(Comment.class);

                // [START_EXCLUDE]
                // Update RecyclerView
                mCommentIds.add(dataSnapshot.getKey());
                mComments.add(comment);
                notifyItemInserted(mComments.size() - 1);
                // [END_EXCLUDE]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Comment newComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int commentIndex = mCommentIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Replace with the new data
                    mComments.set(commentIndex, newComment);

                    // Update the RecyclerView
                    notifyItemChanged(commentIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int commentIndex = mCommentIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Remove data from the list
                    mCommentIds.remove(commentIndex);
                    mComments.remove(commentIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(commentIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Comment movedComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        reference.addChildEventListener(childEventListener);
        // [END child_event_listener_recycler]

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.list_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, final int position) {
        final Comment comment = mComments.get(position);
        holder.mTv_name.setText(comment.author);
        holder.mTv_comment.setText(comment.text);

        holder.mIv_popUp_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mContext, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_comment_option, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                edit(item);
                                return true;
                            case R.id.delete:
                                delete(item);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }

            private void delete(MenuItem item) {
                Log.d("adapter", mDatabaseReference.child(mCommentIds.get(position)).getKey());
                mDatabaseReference.child(mCommentIds.get(position)).removeValue();
            }
        });

        if (comment.imageUrl != null) {
            Glide.with(mContext)
                    .load(comment.imageUrl)
                    .crossFade()
                    .into(holder.mCi_comment_image);
        }


    }

    private void edit(MenuItem item) {

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ci_comment_image)
        CircleImageView mCi_comment_image;
        @BindView(R.id.tv_comment)
        TextView mTv_comment;
        @BindView(R.id.tv_name)
        TextView mTv_name;

        @BindView(R.id.iv_popUp_menu)
        ImageView mIv_popUp_menu;

        public CommentViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
