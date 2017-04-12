package com.jayjhaveri.learnhub.Fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jayjhaveri.learnhub.R;
import com.jayjhaveri.learnhub.VideoDetailActivity;
import com.jayjhaveri.learnhub.model.Comment;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentFragment extends DialogFragment {

    public static String EXTRA_PROFILE_IMAGE_URL = "extra_image";

    @BindView(R.id.et_comment)
    EditText mEt_comment;

    @BindView(R.id.ci_user_image)
    CircleImageView mCi_user_image;

    @BindView(R.id.ci_send_comment)
    CircleImageView mCi_send_comment;
    String mImageUrl;
    VideoDetailActivity parent;
    DatabaseReference databaseReference;
    //Comment Id
    String commentId;

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance(String imageUrl) {
        CommentFragment commentFragment = new CommentFragment();

        Bundle args = new Bundle();
        args.putString(EXTRA_PROFILE_IMAGE_URL, imageUrl);
        commentFragment.setArguments(args);

        return commentFragment;
    }

    public static CommentFragment newInstance(String imageUrl, String commentId) {
        CommentFragment commentFragment = new CommentFragment();

        Bundle args = new Bundle();
        args.putString(EXTRA_PROFILE_IMAGE_URL, imageUrl);
        args.putString("commentId", commentId);
        commentFragment.setArguments(args);

        return commentFragment;
    }

    @OnClick(R.id.ci_send_comment)
    public void sendComment() {

        if (commentId != null) {
            editVideoComment();
        } else {

            videoComment();
        }
        dismissAllowingStateLoss();
    }

    private void editVideoComment() {
        DatabaseReference databaseReference = parent.commentsRef.child(commentId);
        Map<String, Object> updateChildren = new HashMap<>();
        updateChildren.put("text", mEt_comment.getText().toString());
        databaseReference.updateChildren(updateChildren);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Panel);
        mImageUrl = getArguments().getString(EXTRA_PROFILE_IMAGE_URL);
        if (getArguments().containsKey("commentId")) {
            commentId = getArguments().getString("commentId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_comment, container, false);
        ButterKnife.bind(this, rootView);

        mCi_send_comment.setImageDrawable(new IconicsDrawable(getActivity())
                .icon(GoogleMaterial.Icon.gmd_send)
                .colorRes(R.color.colorPrimary)
                .sizeDp(24));

        if (commentId != null) {
            parent.commentsRef.child(commentId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    mEt_comment.setText(comment.text);
                    mEt_comment.setSelection(mEt_comment.getText().length());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mEt_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCi_send_comment.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCi_send_comment.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 0 || mEt_comment.getText().toString().trim().isEmpty()) {
                    mCi_send_comment.setVisibility(View.GONE);
                } else {
                    mCi_send_comment.setVisibility(View.VISIBLE);
                }

            }
        });


        Glide.with(this)
                .load(mImageUrl)
                .into(mCi_user_image);


        return rootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        getDialog().getWindow().getDecorView().setFitsSystemWindows(true);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        getDialog().getWindow().setAttributes(p);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VideoDetailActivity) {
            parent = (VideoDetailActivity) context;
        }
    }

    private void videoComment() {
        if (parent.firebaseAuth.getCurrentUser() != null) {

            FirebaseDatabase.getInstance().getReference().child("users").child(parent.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user information

                            // Create new comment object
                            String author = parent.firebaseAuth.getCurrentUser().getDisplayName();
                            String profileImage = null;
                            if (parent.firebaseAuth.getCurrentUser().getPhotoUrl() != null) {

                                profileImage = parent.firebaseAuth.getCurrentUser().getPhotoUrl().toString();
                            }

                            String commentText = mEt_comment.getText().toString().trim();

                            Comment comment = new Comment(parent.getUid(), author, commentText, profileImage);

                            // Push the comment, it will appear in the list
                            parent.commentsRef.push().setValue(comment);

                            // Clear the field
                            mEt_comment.setText(null);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }
}
