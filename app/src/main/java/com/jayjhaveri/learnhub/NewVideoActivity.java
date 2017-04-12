package com.jayjhaveri.learnhub;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
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
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewVideoActivity extends BaseActivity {


    private static final int RC_FILE = 213;
    final int RC_IMAGE = 201;
    @BindView(R.id.vv_new)
    SimpleExoPlayerView playerView;
    //EditText for VideoTitle
    @BindView(R.id.et_title)
    TextInputEditText et_title;

    @BindView(R.id.et_desc)
    TextInputEditText et_desc;

    //ImageButton for upload thumbnail
    @BindView(R.id.iv_upload_image)
    ImageView iv_upload_image;

    @BindView(R.id.bt_upload_file)
    Button bt_upload_file;

    @BindView(R.id.spinner_category)
    AppCompatSpinner spinnerCategory;

    @BindView(R.id.tv_file_info)
    TextView tv_file_info;
    FirebaseAuth firebaseAuth;
    //Uri from Upload activity
    Uri videoUri;
    //Uri for image
    Uri imageUri;
    //Download ImageUrl
    Uri downloadImageUrl;
    //Download VideoUrl
    Uri downloadVideoUrl;
    //Uri for file download
    Uri downloadFileUrl;
    //Selected category
    String mSelectedCategory;
    //Notification progress
    NotificationManager mNotifyManager;
    int notificationId = 1;
    //duration of video
    String duration;
    private NotificationCompat.Builder notificationBuilder;
    //Firebase Reference
    private FirebaseStorage firebaseStorage;
    private StorageReference storageRef;
    private StorageReference videoUploadRef;
    private StorageReference imageUploadRef;
    private StorageReference fileRef;
    private SimpleExoPlayer exoPlayer;
    private DatabaseReference database;

    private boolean saved = false;
    private Uri fileUri;


    @OnClick(R.id.bt_upload_file)
    void onUploadClick() {
        openFileIntent();
    }


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        if (getIntent() != null) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            videoUri = Uri.parse(getIntent().getStringExtra(MainActivity.EXTRA_URI));
            retriever.setDataSource(this, videoUri);

            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(duration)),
                    (TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(duration)) % 60)
            );

            Log.d("NewVideo", "VideoUri" + videoUri);
            Log.d("NewVideo", "VideoDuration " + duration);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadVideoPreview();

        firebaseStorage = FirebaseStorage.getInstance();
        storageRef = firebaseStorage.getReference();
        videoUploadRef = storageRef.child("videos/" + videoUri.getLastPathSegment());

        //Database Reference
        database = FirebaseDatabase.getInstance().getReference();

        //Auth reference
        firebaseAuth = FirebaseAuth.getInstance();

        iv_upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageIntent();
            }
        });

        initSpinner();

    }

    private void initSpinner() {
        SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.list_spinner,
                R.id.tv_category_name,
                BaseApplication.categoryList);
// Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Category category = BaseApplication.categoryList.get(i);
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
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the exoPlayer
        exoPlayer =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

        // Bind the exoPlayer to the view.
        playerView.setPlayer(exoPlayer);

        // Measures bandwidth during playback. Can be null if not required.

// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "LearnHub"), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(videoUri,
                dataSourceFactory, extractorsFactory, null, null);
// Prepare the exoPlayer with the source.
        exoPlayer.prepare(videoSource);

        exoPlayer.setPlayWhenReady(true);

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

            int isSuccess = uploadVideo();
            if (isSuccess == 0) {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_IMAGE) {
                imageUri = data.getData();
                Glide.with(this)
                        .load(imageUri)
                        .into(iv_upload_image);
            } else if (requestCode == RC_FILE) {
                fileUri = data.getData();
                Log.d("New video", "" + fileUri);
                tv_file_info.setVisibility(View.VISIBLE);
                tv_file_info.setText(getString(R.string.selected_file_desc) + getFileName(fileUri));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        exoPlayer.setPlayWhenReady(true);
    }

    private int uploadVideo() {

        final String title = et_title.getText().toString();
        final String desc = et_desc.getText().toString();

        if (TextUtils.isEmpty(title)) {
            et_title.setError("Required");
            return 1;
        }

        if (TextUtils.isEmpty(desc)) {
            et_desc.setError("Required");
            return 1;
        }

        if (imageUri == null) {
            Toast.makeText(NewVideoActivity.this, "Image is required", Toast.LENGTH_SHORT).show();
            return 1;
        }

        firebaseStorage = FirebaseStorage.getInstance();
        Long time = System.currentTimeMillis();
        storageRef = firebaseStorage.getReferenceFromUrl("gs://" + getString(R.string.google_storage_bucket));
        videoUploadRef = storageRef.child(getUid()).child("videos").child(time.toString()).child(videoUri.getLastPathSegment());
        imageUploadRef = storageRef.child(getUid()).child("images").child(time.toString()).child(videoUri.getLastPathSegment());
        fileRef = storageRef.child(getUid()).child("files").child(time.toString()).child(videoUri.getLastPathSegment());
//        imageUploadRef = storageRef.child("images/" + imageUri.getLastPathSegment());
//        fileRef = storageRef.child("files/" + fileUri.getLastPathSegment());

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this);


        notificationBuilder.setContentTitle("Upload")
                .setContentText("Upload in progress")
                .setSmallIcon(android.R.drawable.stat_sys_upload);

        Utilities.writeStringPreference(this, getString(R.string.video_uri), videoUri.toString());





        final String userId = getUid();

        Toast.makeText(this, "Upload in progress.....", Toast.LENGTH_LONG).show();


        new Thread(
                new Runnable() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void run() {


                        final UploadTask uploadTaskImage = imageUploadRef.putFile(imageUri);


                        uploadTaskImage.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                downloadImageUrl = taskSnapshot.getDownloadUrl();
                            }
                        });


                        if (fileUri != null) {
                            UploadTask uploadTaskFile = fileRef.putFile(fileUri);
                            uploadTaskFile.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    downloadFileUrl = taskSnapshot.getDownloadUrl();
                                }
                            });
                        }

                        final UploadTask uploadTaskVideo = videoUploadRef.putFile(videoUri);

                        mNotifyManager.notify(notificationId, notificationBuilder.build());
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
                                notificationBuilder.setContentText("Upload complete");
                                // Removes the progress bar
                                notificationBuilder.setProgress(0, 0, false);
                                notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
                                mNotifyManager.notify(notificationId, notificationBuilder.build());
                                mNotifyManager.cancel(notificationId);
                                //Firebase user
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                String profileImage = user.getPhotoUrl().toString();

                                VideoDetail videoDetail;
                                String key = database.child("videos").push().getKey();
                                if (profileImage != null) {
                                    videoDetail = new VideoDetail(getUid(),
                                            user.getDisplayName(),
                                            mSelectedCategory,
                                            title,
                                            desc,
                                            profileImage,
                                            downloadVideoUrl.toString(),
                                            downloadImageUrl.toString(),
                                            downloadFileUrl != null ? downloadFileUrl.toString() : null,
                                            duration);
                                } else {
                                    videoDetail = new VideoDetail(getUid(),
                                            user.getDisplayName(),
                                            mSelectedCategory,
                                            title,
                                            desc,
                                            null,
                                            downloadVideoUrl.toString(),
                                            downloadImageUrl.toString(),
                                            downloadFileUrl != null ? downloadFileUrl.toString() : null,
                                            duration);
                                }


                                Map<String, Object> videoValues = videoDetail.toMap();

                                Map<String, Object> childUpdates = new HashMap<>();

                                childUpdates.put("/videos/" + key, videoValues);
                                childUpdates.put("/user-videos/" + getUid() + "/" + key, videoValues);
                                childUpdates.put("/categories/" + mSelectedCategory + "/" + key, videoValues);
                                database.updateChildren(childUpdates);

                                Utilities.deleteStringPreference(NewVideoActivity.this, getString(R.string.video_uri));
                                Utilities.deleteStringPreference(NewVideoActivity.this, getString(R.string.session_uri));

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

                                    Utilities.writeStringPreference(NewVideoActivity.this,
                                            getString(R.string.video_uri),
                                            videoUri.toString());
                                }
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                notificationBuilder.setProgress(100, (int) progress, false);
                                mNotifyManager.notify(notificationId, notificationBuilder.build());
                            }
                        });

                    }
                }
        ).start();

        return 0;
    }

    //Open image chooser intent
    private void openImageIntent() {
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageIntent.setType("image/*");
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        Intent chooserIntent = Intent.createChooser(imageIntent, "Select Image");
        startActivityForResult(chooserIntent, RC_IMAGE);
    }

    private void openFileIntent() {
        Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        Intent chooserIntent = Intent.createChooser(fileIntent, "Select file");
        startActivityForResult(chooserIntent, RC_FILE);
    }

    public String getFileName(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("NewFile", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("NewFile", "Size: " + size);
                return displayName;
            }

        } finally {
            cursor.close();
        }

        return null;
    }



}
