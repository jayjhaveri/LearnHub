package com.jayjhaveri.learnhub;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
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
import com.github.rubensousa.previewseekbar.PreviewSeekBar;
import com.github.rubensousa.previewseekbar.PreviewSeekBarLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.Fragments.CommentFragment;
import com.jayjhaveri.learnhub.Utilities.Utilities;
import com.jayjhaveri.learnhub.adapter.CommentAdapter;
import com.jayjhaveri.learnhub.exoplayer.ExoPlayerManager;
import com.jayjhaveri.learnhub.model.User;
import com.jayjhaveri.learnhub.model.VideoDetail;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoDetailActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        Toolbar.OnMenuItemClickListener,
        CommentAdapter.EmptyCommentListListener,
        GoogleApiClient.OnConnectionFailedListener, EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_VIDEO_DETAIL = "videoDetail_extra";
    public static final int RC_STORAGE = 110;
    private static final int RC_SIGN_IN_LIKE = 102;
    private static final int RC_SIGN_IN_DISLIKE = 103;
    private static final int RC_SIGN_IN_COMMENT = 104;
    private static final int RC_SIGN_IN_FILE_DOWNLOAD = 105;
    private static final int RC_SIGN_IN_BOOKMARK = 106;
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private static final String TAG = VideoDetail.class.getSimpleName();
    public FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
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

    //Uploader info button
    @BindView(R.id.bt_uploader_info)
    Button bt_uploader_info;

    //Bookmark
    @BindView(R.id.iv_bookmark)
    ImageView iv_bookmark;

    //File download
    @BindView(R.id.iv_file_download)
    ImageView iv_file_download;

    //Toolbar
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    //Text view upload date
    @BindView(R.id.tv_upload_date)
    TextView tv_upload_date;

    TextView tv_video_title_control;
    ImageButton ib_exo_fullscreen;
    //perms
    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    //Notification progress
    NotificationManager mNotifyManager;
    int notificationId = 2;
    private GestureDetector gestureDetector;
    //Database references
    private DatabaseReference mGlobalVideoRef;
    private DatabaseReference userVideoRef;
    private DatabaseReference categoryRef;
    private DatabaseReference userRef;
    private NotificationCompat.Builder notificationBuilder;
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

    //ValueEventListener
    private ValueEventListener valueEventListener;

    @OnClick(R.id.tv_comment_here)
    public void onCommentClick() {
        if (firebaseAuth.getCurrentUser() != null) {
            CommentFragment fragment = CommentFragment.newInstance(firebaseAuth.getCurrentUser().getPhotoUrl().toString());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment.show(transaction, "dialog");
        } else {
            Utilities.starAuthActivity(VideoDetailActivity.this, RC_SIGN_IN_COMMENT);
        }
    }

    @OnClick(R.id.bt_category)
    public void onCategoryClick() {
        Intent intent = new Intent(this, CategoryActivity.class);
        intent.putExtra("name", videoDetail.category);
        startActivity(intent);
    }

    @OnClick(R.id.bt_uploader_info)
    public void onUploaderButtonClick() {
        Intent intent = new Intent(this, UserVideosActivity.class);
        intent.putExtra(UserVideosActivity.EXTRA_VIDEO_UID, videoDetail.uid);
        startActivity(intent);
    }

    @OnClick(R.id.iv_bookmark)
    public void onBookmarkClick() {
        Log.d("OnBookmark", "CLick lcikc");
        if (firebaseAuth.getCurrentUser() != null) {
            userRef = databaseReference.child("users").child(getUid());
            onBookmark();
        } else {
            Utilities.starAuthActivity(VideoDetailActivity.this, RC_SIGN_IN_BOOKMARK);
        }
    }

    @OnClick(R.id.iv_file_download)
    public void onDownloadClick() {

        if (firebaseAuth.getCurrentUser() == null) {
            Utilities.starAuthActivity(VideoDetailActivity.this, RC_SIGN_IN_FILE_DOWNLOAD);
        } else {

            if (!EasyPermissions.hasPermissions(this, perms)) {
                EasyPermissions.requestPermissions(this, "need permissions", RC_STORAGE, perms);
                return;
            }

            afterPermission();
        }

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
//        videoDetail = (VideoDetail) getIntent().getSerializableExtra(EXTRA_VIDEO_DETAIL);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VideoDetailActivity.this);
        linearLayoutManager.setReverseLayout(true);
        rv_comments.setLayoutManager(linearLayoutManager);

        commentsRef = databaseReference.child("video-comments").child(postKey);
        commentAdapter = new CommentAdapter(VideoDetailActivity.this, commentsRef, VideoDetailActivity.this);
        rv_comments.setAdapter(commentAdapter);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                videoDetail = dataSnapshot.getValue(VideoDetail.class);


                HttpProxyCacheServer proxy = BaseApplication.getProxy(VideoDetailActivity.this);
                String proxyUrl = proxy.getProxyUrl(videoDetail.videoUrl);
                playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
                SimpleExoPlayerView previewPlayerView
                        = (SimpleExoPlayerView) findViewById(R.id.previewPlayerView);
                seekBar = (PreviewSeekBar) playerView.findViewById(R.id.exo_progress);
                seekBarLayout = (PreviewSeekBarLayout) findViewById(R.id.previewSeekBarLayout);
                ib_exo_fullscreen = (ImageButton) findViewById(R.id.exo_fullscreen);
                ib_exo_fullscreen.setImageDrawable(
                        new IconicsDrawable(VideoDetailActivity.this)
                                .icon(GoogleMaterial.Icon.gmd_fullscreen)
                                .color(ContextCompat.getColor(VideoDetailActivity.this, R.color.player_text_color))
                                .sizeDp(24)
                );
                seekBarLayout.setTintColorResource(R.color.colorPrimary);


                tv_video_title_control = (TextView) playerView.findViewById(R.id.tv_video_title_control);
                tv_video_title_control.setText(videoDetail.title);


                seekBar.addOnSeekBarChangeListener(VideoDetailActivity.this);
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
                Glide.with(VideoDetailActivity.this)
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
                    Glide.with(VideoDetailActivity.this)
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
                gestureDetector = new GestureDetector(VideoDetailActivity.this, listener);
                gestureDetector.setIsLongpressEnabled(false);
                playerView.setOnTouchListener(listener);

                //get total duration
                tv_view_count.setText(getString(R.string.views_string, String.valueOf(videoDetail.views)));

                tv_video_description.setMovementMethod(LinkMovementMethod.getInstance());

                if (videoDetail.fileUrl != null) {

                    iv_file_download.setImageDrawable(
                            new IconicsDrawable(VideoDetailActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_attach_file)
                                    .sizeRes(R.dimen.detail_item_bookmark)
                    );
                } else {
                    iv_file_download.setVisibility(View.GONE);
                }
                String formattedDate = getFormattedDate();
                tv_upload_date.setText(formattedDate);

                onStart();
                onResume();

                //bookmark
                iv_bookmark.setImageDrawable(
                        new IconicsDrawable(VideoDetailActivity.this)
                                .icon(GoogleMaterial.Icon.gmd_bookmark_border)
                                .sizeRes(R.dimen.detail_item_bookmark)
                );

                Handler handlerView = new Handler();
                handlerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        viewsCount(mGlobalVideoRef);
                        viewsCount(categoryRef);
                        viewsCount(userVideoRef);
                    }
                }, 3000);

                if (firebaseAuth.getCurrentUser() != null) {

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            if (user == null) {
                                return;
                            }

                            if (firebaseAuth.getCurrentUser() != null) {
                                if (user.bookmarks.containsKey(postKey)) {
                                    iv_bookmark.setImageDrawable(
                                            new IconicsDrawable(VideoDetailActivity.this)
                                                    .icon(GoogleMaterial.Icon.gmd_bookmark)
                                                    .sizeRes(R.dimen.detail_item_bookmark)
                                    );
                                } else {
                                    iv_bookmark.setImageDrawable(
                                            new IconicsDrawable(VideoDetailActivity.this)
                                                    .icon(GoogleMaterial.Icon.gmd_bookmark_border)
                                                    .sizeRes(R.dimen.detail_item_bookmark)
                                    );
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        FirebaseDatabase.getInstance().getReference().child("videos").
                child(postKey).addListenerForSingleValueEvent(valueEventListener);

    }

    private String getFormattedDate() {
        Date date = new Date(videoDetail.getTimestamp());
        String pattern = "yyyy/MM/dd";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
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
                    updateUserLike(userRef);

                } else {
                    Utilities.starAuthActivity(VideoDetailActivity.this, RC_SIGN_IN_LIKE);
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
                    updateUserDisLike(userRef);
                } else {
                    Utilities.starAuthActivity(VideoDetailActivity.this, RC_SIGN_IN_DISLIKE);
                }
            }
        });
    }

    private void updateUserLike(DatabaseReference userRef) {
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final User user = mutableData.getValue(User.class);

                if (user == null) {
                    return Transaction.success(mutableData);
                }

                if (user.likes.containsKey(postKey)) {

                    user.likes.remove(postKey);
                } else {
                    if (user.dislikes.containsKey(postKey)) {
                        user.dislikes.remove(postKey);
                    }
                    user.likes.put(postKey, -(System.currentTimeMillis()));
                }

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void updateUserDisLike(DatabaseReference userRef) {
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final User user = mutableData.getValue(User.class);

                if (user == null) {
                    return Transaction.success(mutableData);
                }

                if (user.dislikes.containsKey(postKey)) {

                    user.dislikes.remove(postKey);
                } else {
                    if (user.likes.containsKey(postKey)) {
                        user.likes.remove(postKey);
                    }
                    user.dislikes.put(postKey, -(System.currentTimeMillis()));
                }

                mutableData.setValue(user);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void initExoFullScreenListener() {
        ib_exo_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    ib_exo_fullscreen.setImageDrawable(
                            new IconicsDrawable(VideoDetailActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_fullscreen_exit)
                                    .color(ContextCompat.getColor(VideoDetailActivity.this, R.color.player_text_color))
                                    .sizeDp(20)
                    );
                    isFullScreen = true;
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    isFullScreen = false;

                    ib_exo_fullscreen.setImageDrawable(
                            new IconicsDrawable(VideoDetailActivity.this)
                                    .icon(GoogleMaterial.Icon.gmd_fullscreen)
                                    .color(ContextCompat.getColor(VideoDetailActivity.this, R.color.player_text_color))
                                    .sizeDp(20)
                    );

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
        if (firebaseAuth.getCurrentUser() != null) {
            userRef = databaseReference.child("users").child(getUid());
        }
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

    private void onBookmark() {
        userRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                final User user = mutableData.getValue(User.class);

                if (user == null) {
                    return Transaction.success(mutableData);
                }

                if (user.bookmarks.containsKey(postKey)) {

                    user.bookmarks.remove(postKey);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set bookmark
                            iv_bookmark.setImageDrawable(
                                    new IconicsDrawable(VideoDetailActivity.this)
                                            .icon(GoogleMaterial.Icon.gmd_bookmark_border)
                                            .sizeRes(R.dimen.detail_item_bookmark)
                            );
                        }
                    });
                } else {
                    user.bookmarks.put(postKey, (-System.currentTimeMillis()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set bookmark
                            iv_bookmark.setImageDrawable(
                                    new IconicsDrawable(VideoDetailActivity.this)
                                            .icon(GoogleMaterial.Icon.gmd_bookmark)
                                            .sizeRes(R.dimen.detail_item_bookmark)
                            );
                        }
                    });
                }

                mutableData.setValue(user);
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
                    videoDetail.disLikeCount = videoDetail.disLikeCount + 1;
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
                Utilities.putDataToUsers(Utilities.getLoginUser(firebaseAuth));
                userRef = databaseReference.child("users").child(getUid());
                onLikeClicked(mGlobalVideoRef);
                onLikeClicked(userVideoRef);
                onLikeClicked(categoryRef);
                updateUserLike(userRef);
            } else if (requestCode == RC_SIGN_IN_DISLIKE) {
                iv_like.setColorFilter(ContextCompat.getColor(VideoDetailActivity.this, R.color.darkColor));
                userRef = databaseReference.child("users").child(getUid());
                Utilities.putDataToUsers(Utilities.getLoginUser(firebaseAuth));
                onDisLikeClicked(mGlobalVideoRef);
                onDisLikeClicked(userVideoRef);
                onDisLikeClicked(categoryRef);
                updateUserDisLike(userRef);
            } else if (requestCode == RC_SIGN_IN_COMMENT) {
                Utilities.putDataToUsers(Utilities.getLoginUser(firebaseAuth));
                onCommentClick();
            } else if (requestCode == RC_SIGN_IN_FILE_DOWNLOAD) {
                Utilities.putDataToUsers(Utilities.getLoginUser(firebaseAuth));

                if (!EasyPermissions.hasPermissions(this, perms)) {
                    EasyPermissions.requestPermissions(this, "need permissions", RC_STORAGE, perms);
                    return;
                }
                afterPermission();
            } else if (requestCode == RC_SIGN_IN_BOOKMARK) {
                Utilities.putDataToUsers(Utilities.getLoginUser(firebaseAuth));
                userRef = databaseReference.child("users").child(getUid());
                onBookmark();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (exoPlayerManager != null) {
            exoPlayerManager.onStart();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayerManager != null) {
            exoPlayerManager.onResume();
        }
//        exoPlayerManager.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayerManager != null) {

            exoPlayerManager.onPause();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (exoPlayerManager != null) {

            exoPlayerManager.onStop();
        }
        commentAdapter.cleanupListener();

        FirebaseDatabase.getInstance().getReference().
                child("videos").child(postKey).removeEventListener(valueEventListener);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // load video progress
        if (fromUser) {
            if (exoPlayerManager != null) {

                exoPlayerManager.preview((float) progress / seekBar.getMax());
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LOW_PROFILE
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LOW_PROFILE
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
            isFullScreen = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            nv_nestedView.setVisibility(View.GONE);

            ib_exo_fullscreen.setImageDrawable(
                    new IconicsDrawable(VideoDetailActivity.this)
                            .icon(GoogleMaterial.Icon.gmd_fullscreen_exit)
                            .color(ContextCompat.getColor(VideoDetailActivity.this, R.color.player_text_color))
                            .sizeRes(R.dimen.detail_item_bookmark)
            );

            playerView.setLayoutParams(new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT));
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            isFullScreen = false;
            getWindow().getDecorView().setSystemUiVisibility(0);
            coordinatorLayout.setFitsSystemWindows(true);
            nv_nestedView.setVisibility(View.VISIBLE);
            CoordinatorLayout.LayoutParams layoutParams = new CoordinatorLayout.LayoutParams
                    (CoordinatorLayout.LayoutParams.MATCH_PARENT,
                            (int) getResources().getDimension(R.dimen.playerView_default));

            ib_exo_fullscreen.setImageDrawable(
                    new IconicsDrawable(VideoDetailActivity.this)
                            .icon(GoogleMaterial.Icon.gmd_fullscreen)
                            .color(ContextCompat.getColor(VideoDetailActivity.this, R.color.player_text_color))
                            .sizeRes(R.dimen.detail_item_bookmark)
            );

            layoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            playerView.setLayoutParams(layoutParams);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_STORAGE)
    public void afterPermission() {

        if (EasyPermissions.hasPermissions(this, perms)) {

            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationBuilder = new NotificationCompat.Builder(this);

            notificationBuilder.setContentTitle("Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(android.R.drawable.stat_sys_download);

            Toast.makeText(VideoDetailActivity.this,
                    R.string.detail_file_download,
                    Toast.LENGTH_SHORT).show();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReferenceFromUrl(videoDetail.fileUrl);
            final File rootPath = new File(Environment.getExternalStorageDirectory(), "/LearnHub");
            Log.d("meta data", "" + rootPath);
            storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    final String type = storageMetadata.getContentType();
                    String name = storageMetadata.getName();
                    Log.d("meta data", type);
                    final String extension = type.substring(type.lastIndexOf("/") + 1);

                    if (!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    final File localFile = new File(rootPath, name + "." + extension);


                    mNotifyManager.notify(notificationId, notificationBuilder.build());

                    storageReference.getFile(localFile).addOnSuccessListener(
                            new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    notificationBuilder.setContentText("Download complete");
                                    // Removes the progress bar
                                    notificationBuilder.setProgress(0, 0, false);
                                    notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                                    mNotifyManager.notify(notificationId, notificationBuilder.build());
                                    mNotifyManager.cancel(notificationId);

                                    openFileSnackBar(rootPath, localFile, type);
                                }
                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            notificationBuilder.setProgress(100, (int) progress, false);
                            mNotifyManager.notify(notificationId, notificationBuilder.build());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            });
        }
    }

    private void openFileSnackBar(File rootPath, final File localFile, final String type) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout,
                "File is stored in " + rootPath, Snackbar.LENGTH_INDEFINITE)
                .setAction("OPEN", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent openIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri =
                                FileProvider.getUriForFile(VideoDetailActivity.this,
                                        getApplicationContext().getPackageName() + ".provider",
                                        localFile);
                        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        openIntent.setDataAndType(fileUri, type);
                        try {
                            startActivity(openIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(VideoDetailActivity.this, R.string.can_not_open,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        snackbar.show();
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
