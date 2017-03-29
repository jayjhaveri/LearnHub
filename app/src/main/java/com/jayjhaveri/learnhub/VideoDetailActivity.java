package com.jayjhaveri.learnhub;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;
import com.firebase.ui.auth.AuthUI;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
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
import com.jayjhaveri.learnhub.Fragments.VideoListFragment;
import com.jayjhaveri.learnhub.exoplayer.ExoPlayerManager;
import com.jayjhaveri.learnhub.model.VideoDetail;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoDetailActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_VIDEO_URL = "video_url";
    private static final int RC_SIGN_IN_LIKE = 102;
    private static final int RC_SIGN_IN_DISLIKE = 103;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private static final String TAG = VideoDetail.class.getSimpleName();

    //    Toolbar toolbar;
    String url;

    //Database Reference
    DatabaseReference mDatabase;


    //Boolean for fullscreen
    boolean isFullScreen = false;

    @BindView(R.id.coordinator)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.ns_nestedView)
    NestedScrollView mNs_nestedView;

    @BindView(R.id.bt_like)
    LikeButton mBt_like;

    @BindView(R.id.bt_dislike)
    LikeButton mBt_dislike;

    ImageButton mExoFullscreen;
    ExpandableRelativeLayout expandableLayout1;
    //
    private String mPostKey;
    private String mVideoUid;
    private ExoPlayerManager exoPlayerManager;
    private PreviewSeekBarLayout seekBarLayout;
    private PreviewSeekBar seekBar;
    private SimpleExoPlayerView playerView;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        decorView = getWindow().getDecorView();

        setContentView(R.layout.activity_video_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ButterKnife.bind(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        url = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        mVideoUid = getIntent().getStringExtra(VideoListFragment.EXTRA_VIDEO_UID);

        HttpProxyCacheServer proxy = BaseApplication.getProxy(this);
        String proxyUrl = proxy.getProxyUrl(url);


//        mVideoRef = FirebaseDatabase.getInstance().getReference().child("videos").child(mPostKey);

        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        SimpleExoPlayerView previewPlayerView
                = (SimpleExoPlayerView) findViewById(R.id.previewPlayerView);
        seekBar = (PreviewSeekBar) playerView.findViewById(R.id.exo_progress);
        seekBarLayout = (PreviewSeekBarLayout) findViewById(R.id.previewSeekBarLayout);
        mExoFullscreen = (ImageButton) findViewById(R.id.exo_fullscreen);
        seekBarLayout.setTintColorResource(R.color.colorPrimary);

        seekBar.addOnSeekBarChangeListener(this);
        exoPlayerManager = new ExoPlayerManager(playerView, previewPlayerView, seekBarLayout,
                proxyUrl);


        requestFullScreenIfLandscape();

        mExoFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    isFullScreen = true;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    isFullScreen = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
        });


        mBt_like.setOnLikeListener(new OnLikeListener() {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            DatabaseReference globalVideoRef = mDatabase.child("videos").child(mPostKey);
            DatabaseReference userVideRef = mDatabase.child("user-videos").child(mVideoUid).child(mPostKey);

            @Override
            public void liked(LikeButton likeButton) {

                if (auth.getCurrentUser() != null) {
                    onStartClicked(globalVideoRef);
                    onStartClicked(userVideRef);

                } else {
                    likeButton.setLiked(false);
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN_LIKE
                    );
                }


            }

            @Override
            public void unLiked(LikeButton likeButton) {
                onStartClicked(globalVideoRef);
                onStartClicked(userVideRef);
            }
        });
    }

    private void onStartClicked(DatabaseReference videoRef) {
        videoRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                VideoDetail videoDetail = mutableData.getValue(VideoDetail.class);

                if (videoDetail == null) {
                    return Transaction.success(mutableData);
                }

                if (videoDetail.likes.containsKey(getUid())) {
                    videoDetail.likeCount = videoDetail.likeCount - 1;
                    videoDetail.likes.remove(getUid());
                    mBt_like.setLiked(false);
                } else {
                    videoDetail.likeCount = videoDetail.likeCount + 1;
                    videoDetail.likes.put(getUid(), true);
                    mBt_like.setLiked(true);
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
                DatabaseReference globalVideoRef = mDatabase.child("videos").child(mPostKey);
                DatabaseReference userVideRef = mDatabase.child("user-videos").child(mVideoUid).child(mPostKey);

                onStartClicked(globalVideoRef);
                onStartClicked(userVideRef);
                mBt_like.setLiked(true);
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

//            getSupportActionBar().hide();
            mNs_nestedView.setVisibility(View.GONE);
            playerView.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT));
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else {
//            getSupportActionBar().show();
            getWindow().getDecorView().setSystemUiVisibility(0);
            mCoordinatorLayout.setFitsSystemWindows(true);
            mNs_nestedView.setVisibility(View.VISIBLE);
            playerView.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.playerView_default)));
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

    public void expandableButton1(View view) {
        expandableLayout1 = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout1);
        expandableLayout1.toggle(); // toggle expand and collapse
    }
}
