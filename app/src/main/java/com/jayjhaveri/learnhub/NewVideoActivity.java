package com.jayjhaveri.learnhub;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jayjhaveri.learnhub.Utilities.Utilities;
import com.jayjhaveri.learnhub.adapter.SpinnerAdapter;
import com.jayjhaveri.learnhub.model.Category;
import com.jayjhaveri.learnhub.model.VideoDetail;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewVideoActivity extends BaseActivity {


    final int RC_IMAGE = 201;
    @BindView(R.id.vv_new)
    SimpleExoPlayerView mVv_new;
    //EditText for VideoTitle
    @BindView(R.id.et_title)
    TextInputEditText mEt_title;

    @BindView(R.id.et_desc)
    TextInputEditText mEt_desc;

    //ImageButton for upload thumbnail
    @BindView(R.id.ib_upload_image)
    AppCompatImageButton mIbUploadImageButton;

    @BindView(R.id.spinner_category)
    AppCompatSpinner mSpinner_category;

    //Firebase Reference
    FirebaseStorage mStorage;
    StorageReference mStorageRef;
    StorageReference mVideoRef;
    StorageReference mImageRef;
    FirebaseAuth mAuth;
    //Uri from Upload activity
    Uri videoUri;
    //Uri for image
    Uri imageUri;
    //Download ImageUrl
    Uri downloadImageUrl;
    //Download VideoUrl
    Uri downloadVideoUrl;
    //Selected category
    String mSelectedCategory;
    //Notification progress
    NotificationManager mNotifyManager;
    int mId = 1;
    private SimpleExoPlayer mPlayer;
    private DatabaseReference mDatabase;
    private NotificationCompat.Builder mBuilder;
    private boolean saved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        if (getIntent() != null) {
            videoUri = Uri.parse(getIntent().getStringExtra("uri"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadVideoPreview();

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();
        mVideoRef = mStorageRef.child("videos/" + videoUri.getLastPathSegment());

        //Database Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Auth reference
        mAuth = FirebaseAuth.getInstance();

        mIbUploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageIntent();
            }
        });

        initSpinner();

    }

    private void initSpinner() {
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.list_category,
                R.id.tv_category_name,
                MainActivity.categoryList);
// Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        mSpinner_category.setAdapter(adapter);

        mSpinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Category category = MainActivity.categoryList.get(i);
                mSelectedCategory = category.getCategoryName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSelectedCategory = null;
            }
        });
    }

    private void loadVideoPreview() {
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the mPlayer
        mPlayer =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        // Bind the mPlayer to the view.
        mVv_new.setPlayer(mPlayer);

        // Measures bandwidth during playback. Can be null if not required.

// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "LearnHub"), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(videoUri,
                dataSourceFactory, extractorsFactory, null, null);
// Prepare the mPlayer with the source.
        mPlayer.prepare(videoSource);

        mPlayer.setPlayWhenReady(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_new_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_upload) {

            uploadVideo();
            finish();
        } else if (id == R.id.action_file_upload) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_IMAGE) {
                imageUri = data.getData();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayer.setPlayWhenReady(true);
    }

    private void uploadVideo() {
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference();

        mVideoRef = mStorageRef.child("videos/" + videoUri.getLastPathSegment());
        mImageRef = mStorageRef.child("images/" + imageUri.getLastPathSegment());

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);


        mBuilder.setContentTitle("Upload")
                .setContentText("Upload in progress")
                .setSmallIcon(android.R.drawable.stat_sys_upload);

        Utilities.writeStringPreference(this, getString(R.string.video_uri), videoUri.toString());

        final String title = mEt_title.getText().toString();
        final String desc = mEt_desc.getText().toString();

        if (TextUtils.isEmpty(title)) {
            mEt_title.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(desc)) {
            mEt_desc.setError("Required");
            return;
        }

        final String userId = getUid();

        Toast.makeText(this, "Upload in progress.....", Toast.LENGTH_LONG).show();


        new Thread(
                new Runnable() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void run() {


                        final UploadTask uploadTaskImage = mImageRef.putFile(imageUri);


                        uploadTaskImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                downloadImageUrl = taskSnapshot.getDownloadUrl();
                            }
                        });

                        final UploadTask uploadTaskVideo = mVideoRef.putFile(videoUri);
                        ;

                        mNotifyManager.notify(mId, mBuilder.build());
                        uploadTaskVideo.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                downloadVideoUrl = taskSnapshot.getDownloadUrl();
                                Utilities.deleteStringPreference(NewVideoActivity.this, getString(R.string.session_uri));
                                Utilities.deleteStringPreference(NewVideoActivity.this, getString(R.string.video_uri));
                                mBuilder.setContentText("Upload complete");
                                // Removes the progress bar
                                mBuilder.setProgress(0, 0, false);
                                mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                                mNotifyManager.notify(mId, mBuilder.build());

                                //Firebase user
                                FirebaseUser user = mAuth.getCurrentUser();
                                String profileImage = user.getPhotoUrl().toString();

                                VideoDetail videoDetail;
                                String key = mDatabase.child("videos").push().getKey();
                                if (profileImage != null) {
                                     videoDetail = new VideoDetail(getUid(),
                                            user.getDisplayName(),
                                             mSelectedCategory,
                                            title,
                                            desc,
                                            profileImage,
                                            downloadVideoUrl.toString(),
                                            downloadImageUrl.toString(),
                                            null);
                                } else {
                                     videoDetail = new VideoDetail(getUid(),
                                            user.getDisplayName(),
                                             mSelectedCategory,
                                            title,
                                            desc,
                                            null,
                                            downloadVideoUrl.toString(),
                                            downloadImageUrl.toString(),
                                            null);
                                }


                                Map<String, Object> videoValues = videoDetail.toMap();

                                Map<String, Object> childUpdates = new HashMap<>();

                                childUpdates.put("/videos/" + key, videoValues);
                                childUpdates.put("/user-videos/" + userId + "/" + key, videoValues);
                                childUpdates.put("/categories/" + mSelectedCategory + "/" + key, videoValues);
                                mDatabase.updateChildren(childUpdates);

                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                            @SuppressWarnings("VisibleForTests")
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri sessionUri = taskSnapshot.getUploadSessionUri();
                                if (sessionUri != null && !saved) {
                                    saved = true;
                                    Utilities.writeStringPreference(NewVideoActivity.this,
                                            getString(R.string.session_uri),
                                            sessionUri.toString());
                                }
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mBuilder.setProgress(100, (int) progress, false);
                                mNotifyManager.notify(mId, mBuilder.build());
                            }
                        });

                    }
                }
        ).start();
    }

    //Open image chooser intent
    private void openImageIntent() {
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        Intent chooserIntent = Intent.createChooser(imageIntent, "Select Image");
        startActivityForResult(chooserIntent, RC_IMAGE);
    }


}
