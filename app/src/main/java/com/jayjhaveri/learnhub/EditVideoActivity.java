package com.jayjhaveri.learnhub;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.Fragments.VideoListFragment;
import com.jayjhaveri.learnhub.Utilities.Utilities;
import com.jayjhaveri.learnhub.model.VideoDetail;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditVideoActivity extends BaseActivity {

    //EditText for VideoTitle
    @BindView(R.id.et_title)
    TextInputEditText et_title;
    @BindView(R.id.et_desc)
    TextInputEditText et_desc;
    @BindView(R.id.iv_video_image)
    ImageView iv_video_image;
    private String videoKey;
    private DatabaseReference databaseReference;
    private DatabaseReference videoRef;
    private DatabaseReference userVideoRef;
    private DatabaseReference categoryRef;
    private String category;
    private String mSelectedCategory;
    private VideoDetail videoDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (getIntent().getStringExtra(VideoListFragment.EXTRA_VIDEO_KEY) != null) {
            videoKey = getIntent().getStringExtra(VideoListFragment.EXTRA_VIDEO_KEY);
            category = getIntent().getStringExtra(VideoListFragment.EXTRA_VIDEO_CATEGORY);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        videoRef = Utilities.getVideosRef().child(videoKey);
        userVideoRef = Utilities.getUserVideosRef().child(getUid()).child(videoKey);
        categoryRef = Utilities.getCategoryVideosRef().child(category);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                videoDetail = dataSnapshot.getValue(VideoDetail.class);
                et_title.setText(videoDetail.title);
                et_desc.setText(videoDetail.body);
                StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoDetail.imageUrl);
                Glide.with(EditVideoActivity.this)
                        .using(new FirebaseImageLoader())
                        .load(imageRef)
                        .placeholder(R.drawable.dummy_thumbnail)
                        .into(iv_video_image);
            }

            int getSelectedPosition(String videoCategory) {
                for (int i = 0; i < BaseApplication.categoryList.size(); i++) {
                    String category = BaseApplication.categoryList.get(i).getCategoryName();
                    if (videoCategory.equals(category)) {
                        return i;
                    }
                }

                return 0;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        videoRef.addListenerForSingleValueEvent(valueEventListener);

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

            editVideo();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void editVideo() {

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/videos/" + videoKey + "/title", et_title.getText().toString());
        childUpdates.put("/user-videos/" + getUid() + "/" + videoKey + "/title", et_title.getText().toString());
        childUpdates.put("/categories/" + videoDetail.category + "/" + videoKey + "/title", et_title.getText().toString());

        childUpdates.put("/videos/" + videoKey + "/body", et_desc.getText().toString());
        childUpdates.put("/user-videos/" + getUid() + "/" + videoKey + "/body", et_desc.getText().toString());
        childUpdates.put("/categories/" + videoDetail.category + "/" + videoKey + "/body", et_desc.getText().toString());


        databaseReference.updateChildren(childUpdates);
    }


}
