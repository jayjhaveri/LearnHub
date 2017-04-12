package com.jayjhaveri.learnhub;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jayjhaveri.learnhub.Fragments.MostPopularFragment;
import com.jayjhaveri.learnhub.Fragments.MostRecentFragment;
import com.jayjhaveri.learnhub.Utilities.Utilities;
import com.jayjhaveri.learnhub.adapter.ViewPagerAdapter;
import com.jayjhaveri.learnhub.model.User;
import com.jayjhaveri.learnhub.model.VideoDetail;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserVideosActivity extends BaseActivity {

    public static final String EXTRA_VIDEO_UID = "video_uid";
    public static String uid = null;
    @BindView(R.id.user_view_pager)
    ViewPager viewPagerMain;
    @BindView(R.id.main_tabs)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ci_user_image_userVideos)
    CircleImageView ci_user_image;
    @BindView(R.id.ci_user_name)
    TextView ci_user_name;
    @BindView(R.id.ct_user_video)
    CollapsingToolbarLayout ct_user_video;
    private int currentItemForViewpager = 1;
    private DatabaseReference userDatabaseRef;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_videos);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().getStringExtra(EXTRA_VIDEO_UID) != null) {
            uid = getIntent().getStringExtra(EXTRA_VIDEO_UID);
        }
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(getUid());

        setupViewPager(viewPagerMain);
        tabLayout.setupWithViewPager(viewPagerMain);
        viewPagerMain.setCurrentItem(currentItemForViewpager);
        ct_user_video.setExpandedTitleTextAppearance(R.style.ExpandedTitle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get user object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                Glide.with(UserVideosActivity.this)
                        .load(user.profileImage)
                        .into(ci_user_image);
                ct_user_video.setTitle(user.name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        };

        userDatabaseRef.addValueEventListener(userListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (userListener != null) {
            userDatabaseRef.removeEventListener(userListener);
        }
    }

    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MostPopularFragment(), getString(R.string.most_popular_fragment_title));
        adapter.addFragment(new MostRecentFragment(), getString(R.string.most_recent_fragment_title));
        viewPager.setAdapter(adapter);
    }

    public void doPositiveClick(String videoKey, VideoDetail videoDetail) {
        Utilities.getUserVideosRef().child(getUid()).child(videoKey).removeValue();
        Utilities.getVideosRef().child(videoKey).removeValue();
        Utilities.getCategoryVideosRef().child(videoDetail.category).child(videoKey).removeValue();
        Utilities.getCommentRef().child(videoKey).removeValue();
        // Create a storage reference from our app


        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference videoRef = storage.getReferenceFromUrl(videoDetail.videoUrl);
        StorageReference imageRef = storage.getReferenceFromUrl(videoDetail.imageUrl);

        videoRef.delete();
        imageRef.delete();
    }

    public void doNegativeClick(String videoKey) {

    }
}
