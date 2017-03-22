package com.jayjhaveri.learnhub;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jayjhaveri.learnhub.Fragments.CategoriesFragment;
import com.jayjhaveri.learnhub.Fragments.HomeFragment;
import com.jayjhaveri.learnhub.Fragments.MostPopularFragment;
import com.jayjhaveri.learnhub.Fragments.MostRecentFragment;
import com.jayjhaveri.learnhub.Utilities.Utilities;
import com.jayjhaveri.learnhub.model.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    //Categories ArrayList
    public static List<Category> categoryList = new ArrayList<>();

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 101;
    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;

    @BindView(R.id.main_tabs)
    TabLayout mTabLayout;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private int currentItemForViewpager = 1;

    private Uri outputFileUri;

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    private boolean saved = false;

    private void openImageIntent() {

/*// Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = System.currentTimeMillis()+".img";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        Log.d("Output",""+outputFileUri);*/

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("video/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, REQUEST_TAKE_GALLERY_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();


                Uri selectedVideoUri = data.getData();
                Log.d("Output", "" + selectedVideoUri);

                retriever.setDataSource(this, selectedVideoUri);

                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                long timeInMillisec = Long.parseLong(time );

                if (timeInMillisec > 600000){
                    Toast.makeText(this, "can not upload",Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(MainActivity.this, NewVideoActivity.class);
                    intent.putExtra("uri", selectedVideoUri.toString());
                    startActivity(intent);
                }



            } else if (requestCode == RC_SIGN_IN) {
                openImageIntent();
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        final FirebaseAuth auth = FirebaseAuth.getInstance();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser() != null) {

                    openImageIntent();
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN
                    );
                }
            }
        });

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(currentItemForViewpager);
        loadCategoryList();


    }

    public void loadCategoryList() {
        categoryList.add(new Category(R.mipmap.ic_launcher, "Cooking"));
        categoryList.add(new Category(R.mipmap.ic_launcher, "Computer"));
        categoryList.add(new Category(R.mipmap.ic_launcher, "Health"));
        categoryList.add(new Category(R.mipmap.ic_launcher, "Medical"));
        categoryList.add(new Category(R.mipmap.ic_launcher, "Science"));
    }

    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CategoriesFragment(), getString(R.string.categories_fragment_title));
        adapter.addFragment(new HomeFragment(), getString(R.string.home_fragment_title));
        adapter.addFragment(new MostPopularFragment(), getString(R.string.most_popular_fragment_title));
        adapter.addFragment(new MostRecentFragment(), getString(R.string.most_recent_fragment_title));
        viewPager.setAdapter(adapter);
    }


    public  static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkPendingUploads() {
        Uri sessionUri = Uri.parse(Utilities.readStringPreference(this,
                getString(R.string.session_uri)));

        Uri videoUri = Uri.parse(Utilities.readStringPreference(this,
                getString(R.string.video_uri)));

        if (!sessionUri.toString().equals("no_uri") && !videoUri.toString().equals("no_uri")) {
            //Firebase Reference


            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference videoRef = storageRef.child("videos/" + videoUri.getLastPathSegment());
            UploadTask uploadTask = videoRef.putFile(videoUri, new StorageMetadata.Builder().build(), sessionUri);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    @SuppressWarnings("VisibleForTests")
                    Uri sessionUri = taskSnapshot.getUploadSessionUri();
                    if (sessionUri != null && !saved) {
                        saved = true;
                        Utilities.writeStringPreference(MainActivity.this,
                                getString(R.string.session_uri),
                                sessionUri.toString());
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                    Utilities.deleteStringPreference(MainActivity.this, getString(R.string.session_uri));
                    Utilities.deleteStringPreference(MainActivity.this, getString(R.string.video_uri));
                }
            });
        }
    }
}
