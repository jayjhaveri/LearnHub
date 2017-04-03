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
import android.widget.Button;
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
        Toolbar.OnMenuItemClickListener, CommentAdapter.EmptyCommentListListener {

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_VIDEO_DETAIL = "videoDetail_extra";
    private static final int RC_SIGN_IN_LIKE = 102;
    private static final int RC_SIGN_IN_DISLIKE = 103;
    private static final int RC_SIGN_IN_COMMENT = 104;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private static final String TAG = VideoDetail.class.getSimpleName();
    public FirebaseAuth firebaseAuth;
    public DatabaseReference commentsRef;
    //Database Reference
    DatabaseReference databaseReference;
    //VideoDetail Instance from VideoListFragment
    VideoDetail videoDetail;
    //Boolean for fullscreen
    boolean isFullScreen = false;
    //
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.ns_nestedView)
    NestedScrollView nv_nestedView;
    @BindView(R.id.iv_like)
    ImageView iv_like;
    @BindView(R.id.tv_like_count)
    TextView tv_like_count;
    @BindView(R.id.tv_dislike_count)
    TextView tv_dislike_count;
    @BindView(R.id.iv_dislike)
    ImageView iv_dislike;
    @BindView(R.id.ci_uploader_image)
    CircleImageView ci_uploader_image;
    @BindView(R.id.tv_video_title)
    TextView tv_video_title;
    @BindView(R.id.tv_uploader_name)
    TextView tv_uploader_name;
    @BindView(R.id.detailTopView)
    View topView;
    @BindView(R.id.tv_video_description)
    TextView tv_video_description;
    @BindView(R.id.ci_user_image)
    CircleImageView ci_user_image;
    @BindView(R.id.rv_comments)
    RecyclerView rv_comments;
    @BindView(R.id.tv_comment_here)
    TextView tv_comment_here;
    @BindView(R.id.detail_text_content_layout)
    RelativeLayout detail_text;
    //view count
    @BindView(R.id.tv_view_count)
    TextView tv_view_count;
    //Category Button
    @BindView(R.id.bt_category)
    Button bt_category;
    //TextView no comment
    @BindView(R.id.tv_no_comments)
    TextView tv_no_comments;

    //Toolbar
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    TextView tv_video_title_control;
    ImageButton ib_exo_fullscreen;
    private GestureDetector gestureDetector;
    //Database references
    private DatabaseReference mGlobalVideoRef;
    private DatabaseReference userVideoRef;
    private DatabaseReference categoryRef;
    //
    private String postKey;

    //duration for update views
    private long VIEW_DURATION = 0;

    private ExoPlayerManager exoPlayerManager;
    private PreviewSeekBarLayout seekBarLayout;
    private PreviewSeekBar seekBar;
    private SimpleExoPlayerView playerView;
    private View decorView;

    //Adapter
    private CommentAdapter commentAdapter;


    @OnClick(R.id.tv_comment_here)
    public void onCommentClick() {
        if (firebaseAuth.getCurrentUser() != null) {
            CommentFragment fragment = CommentFragment.newInstance(firebaseAuth.getCurrentUser().getPhotoUrl().toString());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment.show(transaction, "dialog");
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                            .build(),
                    RC_SIGN_IN_COMMENT
            );
        }
    }

    @OnClick(R.id.bt_category)
    public void onCategoryClick() {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("name", videoDetail.category);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();

        setContentView(R.layout.activity_video_detail);

        ButterKnife.bind(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get post key from intent
        postKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        videoDetail = (VideoDetail) getIntent().getSerializableExtra(EXTRA_VIDEO_DETAIL);

        HttpProxyCacheServer proxy = BaseApplication.getProxy(this);
        String proxyUrl = proxy.getProxyUrl(videoDetail.videoUrl);
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        SimpleExoPlayerView previewPlayerView
                = (SimpleExoPlayerView) findViewById(R.id.previewPlayerView);
        seekBar = (PreviewSeekBar) playerView.findViewById(R.id.exo_progress);
        seekBarLayout = (PreviewSeekBarLayout) findViewById(R.id.previewSeekBarLayout);
        ib_exo_fullscreen = (ImageButton) findViewById(R.id.exo_fullscreen);
        seekBarLayout.setTintColorResource(R.color.colorPrimary);

        tv_video_title_control = (TextView) playerView.findViewById(R.id.tv_video_title_control);
        tv_video_title_control.setText(videoDetail.title);


        seekBar.addOnSeekBarChangeListener(this);
        exoPlayerManager = new ExoPlayerManager(playerView, previewPlayerView, seekBarLayout,
                proxyUrl);

        //set like counter
        tv_like_count.setText(String.valueOf(videoDetail.likeCount));

        //Set dislikecount
        tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));

        //set video title
        tv_video_title.setText(videoDetail.title);

        //set uplader name
        tv_uploader_name.setText(videoDetail.author);

        tv_video_description.setText(videoDetail.body);

        //set category name
        bt_category.setText(videoDetail.category);

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
                .load(videoDetail.profileImage)
                .into(ci_uploader_image);

        //Detect full screen
        requestFullScreenIfLandscape();

        //init listener
        initExoFullScreenListener();

        initDatabaseReferences();

        initLikeListener();


        if (firebaseAuth.getCurrentUser() != null) {
            Uri photoUri = firebaseAuth.getCurrentUser().getPhotoUrl();
            Glide.with(this)
                    .load(photoUri)
                    .into(ci_user_image);

            if (videoDetail.likes.containsKey(getUid())) {
                Log.d(TAG, "liked");
                iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
            } else {
                iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
            }

            if (videoDetail.disLikes.containsKey(getUid())) {
                Log.d(TAG, "liked");
                iv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
            } else {
                iv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
            }
        }

        MySimpleOnGestureListener listener = new MySimpleOnGestureListener();
        gestureDetector = new GestureDetector(this, listener);
        gestureDetector.setIsLongpressEnabled(false);
        playerView.setOnTouchListener(listener);

        commentAdapter = new CommentAdapter(VideoDetailActivity.this, commentsRef, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        rv_comments.setLayoutManager(linearLayoutManager);
        rv_comments.setAdapter(commentAdapter);
        //get totla duration
        tv_view_count.setText(getString(R.string.views_string, String.valueOf(videoDetail.views)));
    }

    /*private void videoComment() {
        if (firebaseAuth.getCurrentUser() != null) {

            FirebaseDatabase.getInstance().getReference().child("users").child(getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user information

                            // Create new comment object
                            String author = firebaseAuth.getCurrentUser().getDisplayName();
                            String profileImage = null;
                            if (firebaseAuth.getCurrentUser().getPhotoUrl() != null) {

                                profileImage = firebaseAuth.getCurrentUser().getPhotoUrl().toString();
                            }

                            String commentText = mEt_comment.getText().toString();

                            Comment comment = new Comment(getUid(), author, commentText, profileImage);

                            // Push the comment, it will appear in the list
                            commentsRef.push().setValue(comment);

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

        iv_like.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                if (firebaseAuth.getCurrentUser() != null) {
                    onLikeClicked(mGlobalVideoRef);
                    onLikeClicked(userVideoRef);
                    onLikeClicked(categoryRef);

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

        iv_dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() != null) {
                    onDisLikeClicked(mGlobalVideoRef);
                    onDisLikeClicked(userVideoRef);
                    onDisLikeClicked(categoryRef);

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
        ib_exo_fullscreen.setOnClickListener(new View.OnClickListener() {
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
        firebaseAuth = FirebaseAuth.getInstance();
        mGlobalVideoRef = databaseReference.child("videos").child(postKey);
        userVideoRef = databaseReference.child("user-videos").child(videoDetail.uid).child(postKey);
        categoryRef = databaseReference.child("categories").child(videoDetail.category).child(postKey);
        commentsRef = databaseReference.child("video-comments").child(postKey);
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
                        tv_view_count.setText(getString(R.string.views_string, String.valueOf(videoDetail.views)));
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
                            tv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
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
                                tv_like_count.setText(String.valueOf(videoDetail.likeCount));
                                //Set dislikecount
                                tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                                iv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                            }
                        });
                    }

                    videoDetail.likeCount = videoDetail.likeCount + 1;
                    videoDetail.likes.put(getUid(), true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set like counter
                            tv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
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
                            tv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            iv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
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
                                tv_like_count.setText(String.valueOf(videoDetail.likeCount));
                                //Set dislikecount
                                tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                                iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                            }
                        });
                    }
                    videoDetail.disLikeCount = videoDetail.likeCount + 1;
                    videoDetail.disLikes.put(getUid(), true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set like counter
                            tv_like_count.setText(String.valueOf(videoDetail.likeCount));
                            //Set dislikecount
                            tv_dislike_count.setText(String.valueOf(videoDetail.disLikeCount));
                            iv_dislike.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
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
                iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.blue_like));
                onLikeClicked(mGlobalVideoRef);
                onLikeClicked(userVideoRef);
                onLikeClicked(categoryRef);
            } else if (requestCode == RC_SIGN_IN_DISLIKE) {
                iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                onDisLikeClicked(mGlobalVideoRef);
                onDisLikeClicked(userVideoRef);
                onDisLikeClicked(categoryRef);
            } else if (requestCode == RC_SIGN_IN_COMMENT) {
                onCommentClick();
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
        commentAdapter.cleanupListener();
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
            coordinatorLayout.setFitsSystemWindows(false);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            isFullScreen = true;
            getSupportActionBar().hide();
            nv_nestedView.setVisibility(View.GONE);
            playerView.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT));
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else {
            getSupportActionBar().show();
            isFullScreen = false;
            getWindow().getDecorView().setSystemUiVisibility(0);
            coordinatorLayout.setFitsSystemWindows(true);
            nv_nestedView.setVisibility(View.VISIBLE);
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

    @Override
    public void getCommentListSize(int size) {
        if (size <= 0) {
            rv_comments.setVisibility(View.INVISIBLE);
            tv_no_comments.setVisibility(View.VISIBLE);
        } else {
            rv_comments.setVisibility(View.VISIBLE);
            tv_no_comments.setVisibility(View.GONE);
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
