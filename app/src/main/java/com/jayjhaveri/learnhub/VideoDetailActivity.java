package com.jayjhaveri.learnhub;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.danikula.videocache.HttpProxyCacheServer;
import com.firebase.ui.auth.AuthUI;
import com.github.rubensousa.previewseekbar.PreviewSeekBar;
import com.github.rubensousa.previewseekbar.PreviewSeekBarLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.jayjhaveri.learnhub.Fragments.CommentFragment;
import com.jayjhaveri.learnhub.adapter.CommentAdapter;
import com.jayjhaveri.learnhub.exoplayer.ExoPlayerManager;
import com.jayjhaveri.learnhub.model.VideoDetail;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class VideoDetailActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        Toolbar.OnMenuItemClickListener, ExoPlayerManager.AfterGetDuration {

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_VIDEO_DETAIL = "videoDetail_extra";
    private static final int RC_SIGN_IN_LIKE = 102;
    private static final int RC_SIGN_IN_DISLIKE = 103;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private static final String TAG = VideoDetail.class.getSimpleName();
    public FirebaseAuth mAuth;
    public DatabaseReference mCommentsRef;
    Toolbar toolbar;
    //Database Reference
    DatabaseReference mDatabase;
    //VideoDetail Instance from VideoListFragment
    VideoDetail mVideoDetail;
    //Boolean for fullscreen
    boolean isFullScreen = false;
    //
    boolean isContainKey = false;
    @BindView(R.id.coordinator)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.ns_nestedView)
    NestedScrollView mNs_nestedView;
    @BindView(R.id.iv_like)
    ImageView mIv_like;
    @BindView(R.id.tv_like_count)
    TextView mTv_like_count;
    @BindView(R.id.tv_dislike_count)
    TextView mTv_dislike_count;
    @BindView(R.id.iv_dislike)
    ImageView mIv_dislike;
    @BindView(R.id.ci_uploader_image)
    CircleImageView mCi_uploader_image;
    @BindView(R.id.tv_video_title)
    TextView mTv_video_title;
    @BindView(R.id.tv_uploader_name)
    TextView mTv_uplader_name;
    @BindView(R.id.detailTopView)
    View topView;
    @BindView(R.id.tv_video_description)
    TextView mTv_video_description;
    @BindView(R.id.ci_user_image)
    CircleImageView mCi_user_image;
    @BindView(R.id.rv_comments)
    RecyclerView mRv_comments;
    @BindView(R.id.tv_comment_here)
    TextView mTv_comment_here;
    @BindView(R.id.detail_text_content_layout)
    RelativeLayout mDetail_text;
    //view count
    @BindView(R.id.tv_view_count)
    TextView mTv_view_count;
    TextView mTv_video_title_control;
    ImageButton mExoFullscreen;
    private GestureDetector gestureDetector;
    //Database references
    private DatabaseReference mGlobalVideoRef;
    private DatabaseReference mUserVideoRef;
    private DatabaseReference mCategoryRef;
    //
    private String mPostKey;

    //duration for update views
    private long VIEW_DURATION = 0;

    private ExoPlayerManager exoPlayerManager;
    private PreviewSeekBarLayout seekBarLayout;
    private PreviewSeekBar seekBar;
    private SimpleExoPlayerView playerView;
    private View decorView;

    //Adapter
    private CommentAdapter mAdapter;


    @OnClick(R.id.tv_comment_here)
    public void activeEditText() {
        if (mAuth.getCurrentUser() != null) {
            CommentFragment fragment = CommentFragment.newInstance(mAuth.getCurrentUser().getPhotoUrl().toString());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment.show(transaction, "dialog");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();

        setContentView(R.layout.activity_video_detail);

        ButterKnife.bind(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mVideoDetail = (VideoDetail) getIntent().getSerializableExtra(EXTRA_VIDEO_DETAIL);
        isContainKey = getIntent().getBooleanExtra("keyLike", false);


        HttpProxyCacheServer proxy = BaseApplication.getProxy(this);
        String proxyUrl = proxy.getProxyUrl(mVideoDetail.videoUrl);
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        SimpleExoPlayerView previewPlayerView
                = (SimpleExoPlayerView) findViewById(R.id.previewPlayerView);
        seekBar = (PreviewSeekBar) playerView.findViewById(R.id.exo_progress);
        seekBarLayout = (PreviewSeekBarLayout) findViewById(R.id.previewSeekBarLayout);
        mExoFullscreen = (ImageButton) findViewById(R.id.exo_fullscreen);
        seekBarLayout.setTintColorResource(R.color.colorPrimary);

        mTv_video_title_control = (TextView) playerView.findViewById(R.id.tv_video_title_control);
        mTv_video_title_control.setText(mVideoDetail.title);




        seekBar.addOnSeekBarChangeListener(this);
        exoPlayerManager = new ExoPlayerManager(playerView, previewPlayerView, seekBarLayout,
                proxyUrl, this);

        //set like counter
        mTv_like_count.setText(String.valueOf(mVideoDetail.likeCount));

        //Set dislikecount
        mTv_dislike_count.setText(String.valueOf(mVideoDetail.disLikeCount));

        //set video title
        mTv_video_title.setText(mVideoDetail.title);

        //set uplader name
        mTv_uplader_name.setText(mVideoDetail.author);

        mTv_video_description.setText(mVideoDetail.body);

        topView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    ImageView arrow = (ImageView) findViewById(R.id.toggle_description_view);
                    View extra = findViewById(R.id.detailExtraView);
                    if (extra.getVisibility() == View.VISIBLE) {
                        extra.setVisibility(View.GONE);
                        arrow.setImageResource(R.drawable.arrow_down);
                    } else {
                        extra.setVisibility(View.VISIBLE);
                        arrow.setImageResource(R.drawable.arrow_up);
                    }
                }
                return true;
            }
        });

        //Set uploader image
        Glide.with(this)
                .load(mVideoDetail.profileImage)
                .into(mCi_uploader_image);

        //Detect full screen
        requestFullScreenIfLandscape();

        //init listener
        initExoFullScreenListener();

        initDatabaseReferences();

        initLikeListener();


        if (mAuth.getCurrentUser() != null) {
            Uri photoUri = mAuth.getCurrentUser().getPhotoUrl();
            Glide.with(this)
                    .load(photoUri)
                    .into(mCi_user_image);

            if (mVideoDetail.likes.containsKey(getUid())) {
                Log.d(TAG, "liked");
                mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
            } else {
                mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
            }

            if (mVideoDetail.disLikes.containsKey(getUid())) {
                Log.d(TAG, "liked");
                mIv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
            } else {
                mIv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
            }
        }

        MySimpleOnGestureListener listener = new MySimpleOnGestureListener();
        gestureDetector = new GestureDetector(this, listener);
        gestureDetector.setIsLongpressEnabled(false);
        playerView.setOnTouchListener(listener);

        mAdapter = new CommentAdapter(this, mCommentsRef);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        mRv_comments.setLayoutManager(linearLayoutManager);
        mRv_comments.setAdapter(mAdapter);

        //get totla duration
        mTv_view_count.setText(getString(R.string.views_string, String.valueOf(mVideoDetail.views)));
    }

    /*private void videoComment() {
        if (mAuth.getCurrentUser() != null) {

            FirebaseDatabase.getInstance().getReference().child("users").child(getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user information

                            // Create new comment object
                            String author = mAuth.getCurrentUser().getDisplayName();
                            String profileImage = null;
                            if (mAuth.getCurrentUser().getPhotoUrl() != null) {

                                profileImage = mAuth.getCurrentUser().getPhotoUrl().toString();
                            }

                            String commentText = mEt_comment.getText().toString();

                            Comment comment = new Comment(getUid(), author, commentText, profileImage);

                            // Push the comment, it will appear in the list
                            mCommentsRef.push().setValue(comment);

                            // Clear the field
                            mEt_comment.setText(null);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }*/

    private void initLikeListener() {

        mIv_like.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                if (mAuth.getCurrentUser() != null) {
                    onLikeClicked(mGlobalVideoRef);
                    onLikeClicked(mUserVideoRef);
                    onLikeClicked(mCategoryRef);

                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN_LIKE
                    );
                }
            }
        });

        mIv_dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() != null) {
                    onDisLikeClicked(mGlobalVideoRef);
                    onDisLikeClicked(mUserVideoRef);
                    onDisLikeClicked(mCategoryRef);

                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN_DISLIKE
                    );
                }
            }
        });
    }

    private void initExoFullScreenListener() {
        mExoFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    isFullScreen = true;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    isFullScreen = false;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                        }
                    }, 3000);

                }
            }
        });
    }

    private void initDatabaseReferences() {
        mAuth = FirebaseAuth.getInstance();
        mGlobalVideoRef = mDatabase.child("videos").child(mPostKey);
        mUserVideoRef = mDatabase.child("user-videos").child(mVideoDetail.uid).child(mPostKey);
        mCategoryRef = mDatabase.child("categories").child(mVideoDetail.category).child(mPostKey);
        mCommentsRef = mDatabase.child("video-comments").child(mPostKey);
    }

    public void viewsCount(DatabaseReference videoRef) {
        videoRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final VideoDetail videoDetail = mutableData.getValue(VideoDetail.class);

                if (videoDetail == null) {
                    return Transaction.success(mutableData);
                }

                videoDetail.views = videoDetail.views + 1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //set view counter
                        mTv_view_count.setText(getString(R.string.views_string, String.valueOf(videoDetail.views)));
                    }
                });

                mutableData.setValue(videoDetail);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void onLikeClicked(DatabaseReference videoRef) {
        videoRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final VideoDetail videoDetail = mutableData.getValue(VideoDetail.class);

                if (videoDetail == null) {
                    return Transaction.success(mutableData);
                }

                if (videoDetail.likes.containsKey(getUid())) {

                    videoDetail.likeCount = videoDetail.likeCount - 1;
                    videoDetail.likes.remove(getUid());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set like counter
                            mTv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            mTv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                        }
                    });
                } else {

                    if (videoDetail.disLikes.containsKey(getUid())) {
                        videoDetail.disLikeCount = videoDetail.disLikeCount - 1;
                        videoDetail.disLikes.remove(getUid());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //set like counter
                                mTv_like_count.setText(String.valueOf(videoDetail.likeCount));
                                //Set dislikecount
                                mTv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                                mIv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                            }
                        });
                    }

                    videoDetail.likeCount = videoDetail.likeCount + 1;
                    videoDetail.likes.put(getUid(), true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set like counter
                            mTv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            mTv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
                        }
                    });
                }

                mutableData.setValue(videoDetail);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void onDisLikeClicked(DatabaseReference videoRef) {
        videoRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final VideoDetail videoDetail = mutableData.getValue(VideoDetail.class);

                if (videoDetail == null) {
                    return Transaction.success(mutableData);
                }

                if (videoDetail.disLikes.containsKey(getUid())) {
                    videoDetail.disLikeCount = videoDetail.disLikeCount - 1;
                    videoDetail.disLikes.remove(getUid());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set like counter
                            mTv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            mTv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            mIv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                        }
                    });
                } else {
                    if (videoDetail.likes.containsKey(getUid())) {
                        videoDetail.likeCount = videoDetail.likeCount - 1;
                        videoDetail.likes.remove(getUid());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //set like counter
                                mTv_like_count.setText(String.valueOf(videoDetail.likeCount));
                                //Set dislikecount
                                mTv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                                mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                            }
                        });
                    }
                    videoDetail.disLikeCount = videoDetail.likeCount + 1;
                    videoDetail.disLikes.put(getUid(), true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set like counter
                            mTv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            mTv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            mIv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
                        }
                    });
                }

                mutableData.setValue(videoDetail);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            exoPlayerManager.play(data.getData());
        }*/

        if (resultCode == RESULT_OK) {
            if (requestCode == RC_SIGN_IN_LIKE) {
                mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
                onLikeClicked(mGlobalVideoRef);
                onLikeClicked(mUserVideoRef);
                onLikeClicked(mCategoryRef);
            } else if (requestCode == RC_SIGN_IN_DISLIKE) {
                mIv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                onDisLikeClicked(mGlobalVideoRef);
                onDisLikeClicked(mUserVideoRef);
                onDisLikeClicked(mCategoryRef);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        exoPlayerManager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        exoPlayerManager.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        exoPlayerManager.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        exoPlayerManager.onStop();
        mAdapter.cleanupListener();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // load video progress
        if (fromUser) {

            exoPlayerManager.preview((float) progress / seekBar.getMax());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        exoPlayerManager.stopPreview();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        /*if (item.getItemId() == R.id.action_local) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video*//*.mp4");
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
        } else if (item.getItemId() == R.id.action_toggle) {
            if (seekBarLayout.isShowingPreview()) {
                seekBarLayout.hidePreview();
                exoPlayerManager.stopPreview();
            } else {
                seekBarLayout.showPreview();
                exoPlayerManager.preview((float) seekBar.getProgress() / seekBar.getMax());
            }
        }*/
        return true;
    }

    private void requestFullScreenIfLandscape() {
        if (getResources().getBoolean(R.bool.landscape)) {
            mCoordinatorLayout.setFitsSystemWindows(false);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            isFullScreen = true;
            getSupportActionBar().hide();
            mNs_nestedView.setVisibility(View.GONE);
            playerView.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT));
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else {
            getSupportActionBar().show();
            isFullScreen = false;
            getWindow().getDecorView().setSystemUiVisibility(0);
            mCoordinatorLayout.setFitsSystemWindows(true);
            mNs_nestedView.setVisibility(View.VISIBLE);
            CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    (int) getResources().getDimension(R.dimen.playerView_default));

            layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            playerView.setLayoutParams(layoutParams);
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestFullScreenIfLandscape();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requestFullScreenIfLandscape();
        }

    }

    @Override
    public void runHandler(long total_duration) {
        Log.d(TAG, "" + total_duration);
        VIEW_DURATION = (total_duration * 30) / 100;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewsCount(mCategoryRef);
                viewsCount(mGlobalVideoRef);
                viewsCount(mUserVideoRef);
            }
        }, VIEW_DURATION);
    }

    @Override
    public void onBackPressed() {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }, 3000);

        } else {
            Log.d("videoDetail", "back");
            super.onBackPressed();
        }
    }

    private class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
        private boolean isMoving;
        private boolean isExoControlShow = false;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (e.getX() > playerView.getWidth() / 2)
                exoPlayerManager.onFastForward();
            else exoPlayerManager.onFastRewind();
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isExoControlShow) {
                exoPlayerManager.hideexo();
                isExoControlShow = false;

                return true;
            } else {
                exoPlayerManager.showExo();
                isExoControlShow = true;
                return true;
            }
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            gestureDetector.onTouchEvent(motionEvent);

            return true;


        }
    }


}
