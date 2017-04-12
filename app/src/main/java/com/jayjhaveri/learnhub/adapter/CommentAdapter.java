package com.jayjhaveri.learnhub.adapter;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.jayjhaveri.learnhub.Fragments.CommentFragment;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.VideoDetailActivity;
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
    private Context context;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    private FirebaseAuth auth;

    private EmptyCommentListListener emptyCommentListListener;

    private List<String> commentIds = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    public CommentAdapter(Context context, DatabaseReference reference, EmptyCommentListListener emptyCommentListListener) {
        this.context = context;
        databaseReference = reference;
        auth = FirebaseAuth.getInstance();
        this.emptyCommentListListener = emptyCommentListListener;
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
                commentIds.add(dataSnapshot.getKey());
                comments.add(comment);
                notifyItemInserted(comments.size() - 1);
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
                int commentIndex = commentIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Replace with the new data
                    comments.set(commentIndex, newComment);

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
                int commentIndex = commentIds.indexOf(commentKey);
                if (commentIndex > -1) {
                    // Remove data from the list
                    commentIds.remove(commentIndex);
                    comments.remove(commentIndex);

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
                Toast.makeText(CommentAdapter.this.context, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        reference.addChildEventListener(childEventListener);
        // [END child_event_listener_recycler]

        // Store reference to listener so it can be removed on app stop
        this.childEventListener = childEventListener;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentViewHolder holder, int position) {
        final Comment comment = comments.get(position);


        holder.tv_name.setText(comment.author);
        holder.tv_comment.setText(comment.text);

        if (auth.getCurrentUser() == null) {
            holder.iv_popUp_menu.setVisibility(View.GONE);

        } else {

            if (!(auth.getCurrentUser().getUid()).equals(comment.uid)) {
                holder.iv_popUp_menu.setVisibility(View.GONE);
            } else {
                holder.iv_popUp_menu.setVisibility(View.VISIBLE);
            }

            holder.iv_popUp_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(context, view);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.menu_comment_option, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit:
                                    edit(comment, context, commentIds.get(holder.getAdapterPosition()));
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
                    Log.d("adapter", databaseReference.child(commentIds.get(holder.getAdapterPosition())).getKey());
                    databaseReference.child(commentIds.get(holder.getAdapterPosition())).removeValue();
                }
            });
        }


        if (comment.imageUrl != null) {
            Glide.with(context)
                    .load(comment.imageUrl)
                    .crossFade()
                    .into(holder.ci_comment_image);
        }


    }

    private void edit(Comment comment, Context context, String commentId) {
        if (context instanceof VideoDetailActivity) {
            CommentFragment fragment = CommentFragment.newInstance(comment.imageUrl, commentId);
            FragmentTransaction transaction = ((VideoDetailActivity) context).getSupportFragmentManager().beginTransaction();
            fragment.show(transaction, context.getString(R.string.dialog_transaction));
        }
    }

    @Override
    public int getItemCount() {
        emptyCommentListListener.getCommentListSize(comments.size());
        return comments.size();
    }

    public void cleanupListener() {
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
        }
    }

    public interface EmptyCommentListListener {
        void getCommentListSize(int size);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ci_comment_image)
        CircleImageView ci_comment_image;
        @BindView(R.id.tv_comment)
        TextView tv_comment;
        @BindView(R.id.tv_name)
        TextView tv_name;

        @BindView(R.id.iv_popUp_menu)
        ImageView iv_popUp_menu;

        CommentViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
