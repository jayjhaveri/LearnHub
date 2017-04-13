package com.jayjhaveri.learnhub;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jayjhaveri.learnhub.Fragments.CategoriesFragment;
import com.jayjhaveri.learnhub.Fragments.MostPopularFragment;
import com.jayjhaveri.learnhub.Fragments.MostRecentFragment;
import com.jayjhaveri.learnhub.Utilities.Utilities;
import com.jayjhaveri.learnhub.adapter.ViewPagerAdapter;
import com.jayjhaveri.learnhub.model.User;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity {

    // Choose an arbitrary request code value
    public static final int RC_SIGN_IN_OPEN_IMAGE_INTENT = 123;
    public static final int RC_SIGN_IN = 124;
    public static final int RC_MANAGE_DOCUMENTS = 201;
    public static final String EXTRA_URI = "uri";
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 101;
    private static final String[] cameraPerms = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @BindView(R.id.main_view_pager)
    ViewPager viewPagerMain;
    @BindView(R.id.main_tabs)
    TabLayout tabLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private int currentItemForViewpager = 1;
    private Uri outputFileUri;
    private boolean saved = false;
    private FirebaseAuth auth;

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
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));

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

                retriever.setDataSource(this, selectedVideoUri);

                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String extension = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

                Log.d("Output", "" + extension.substring(extension.lastIndexOf("/") + 1));
                long timeInMillisec = Long.parseLong(time);

                if (timeInMillisec > 600000) {
                    Toast.makeText(this, R.string.file_duration_can_not_upload, Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, NewVideoActivity.class);
                    intent.putExtra(EXTRA_URI, selectedVideoUri.toString());
                    startActivity(intent);
                }


            } else if (requestCode == RC_SIGN_IN_OPEN_IMAGE_INTENT) {
                User user = Utilities.getLoginUser(auth);
                Utilities.putDataToUsers(user);
                openImageIntent();
            } else if (requestCode == RC_SIGN_IN) {
                User user = Utilities.getLoginUser(auth);
                Utilities.putDataToUsers(user);
                loadAuthNavigationDrawer(toolbar);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        auth = FirebaseAuth.getInstance();

        IconicsDrawable icon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_videocam)
                .color(Color.WHITE)
                .sizeDp(24);

        fab.setImageDrawable(icon);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (auth.getCurrentUser() != null) {

                    openImageIntent();
                } else {
                    Utilities.starAuthActivity(MainActivity.this, RC_SIGN_IN_OPEN_IMAGE_INTENT);
                }
            }
        });

        setupViewPager(viewPagerMain);
        tabLayout.setupWithViewPager(viewPagerMain);
        viewPagerMain.setCurrentItem(currentItemForViewpager);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() != null) {
            loadAuthNavigationDrawer(toolbar);
        } else {
            loadWithoutAuthNavigationDrawer(toolbar);
        }
    }

    protected void loadAuthNavigationDrawer(final Toolbar toolbar) {

        final FirebaseUser user = auth.getCurrentUser();
        IProfile profile = new ProfileDrawerItem().withName(user.getDisplayName()).
                withEmail(user.getEmail()).
                withIcon(user.getPhotoUrl()).withIdentifier(100);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        profile
                )
                .build();


        PrimaryDrawerItem home = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.home_navigation).withIcon(GoogleMaterial.Icon.gmd_home)
                .withIconColorRes(R.color.colorPrimaryDark)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        PrimaryDrawerItem mydVideos = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.my_videos_navigation)
                .withIcon(GoogleMaterial.Icon.gmd_personal_video)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        PrimaryDrawerItem likedVideos = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.liked_videos_navigation)
                .withIcon(GoogleMaterial.Icon.gmd_thumb_up)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        PrimaryDrawerItem bookmarkVideos = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.bookmar_videos_navigation)
                .withIcon(GoogleMaterial.Icon.gmd_bookmark)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        PrimaryDrawerItem logout = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.logout_navigation)
                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        PrimaryDrawerItem shareApp = new PrimaryDrawerItem().withIdentifier(6).withName(R.string.share_app_navigation)
                .withIcon(GoogleMaterial.Icon.gmd_share)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);


        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withDrawerWidthDp(250)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult, true)
                .addDrawerItems(
                        home,
                        mydVideos,
                        likedVideos,
                        bookmarkVideos,
                        new DividerDrawerItem(),
                        logout
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 0:
                                break;
                            case 1:
                                Intent intent = new Intent(MainActivity.this, UserVideosActivity.class);
                                intent.putExtra(UserVideosActivity.EXTRA_VIDEO_UID, auth.getCurrentUser().getUid());
                                Bundle bundle = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    startActivity(intent, bundle);
                                } else {
                                    startActivity(intent);
                                }
                                break;
                            case 2:

                                Intent likedVideoIntent = new Intent(MainActivity.this, LikeVideosActivity.class);
                                likedVideoIntent.putExtra(LikeVideosActivity.EXTRA_IS_LIKE, getString(R.string.extra_likedVideoIntent_likes));
                                Bundle bundleLikeVideo = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    bundleLikeVideo = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    startActivity(likedVideoIntent, bundleLikeVideo);
                                } else {
                                    startActivity(likedVideoIntent);
                                }
                                break;

                            case 3:
                                Intent bookmarkIntent = new Intent(MainActivity.this, BookmarkActivity.class);
                                Bundle bundleBookmark = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    bundleBookmark = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle();
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    startActivity(bookmarkIntent, bundleBookmark);
                                } else {
                                    startActivity(bookmarkIntent);
                                }
                                break;

                            case 5:
                                AuthUI.getInstance()
                                        .signOut(MainActivity.this)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            public void onComplete(@NonNull Task<Void> task) {
                                                // user is now signed out
                                                loadWithoutAuthNavigationDrawer(toolbar);
                                            }
                                        });
                                break;
                            case 6:
                                break;
                        }
                        return false;
                    }
                })
                .build();
    }

    protected void loadWithoutAuthNavigationDrawer(Toolbar toolbar) {

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .build();


        PrimaryDrawerItem home = new PrimaryDrawerItem().withIdentifier(1)
                .withName(getString(R.string.drawer_item_home)).withIcon(GoogleMaterial.Icon.gmd_home)
                .withIconColorRes(R.color.colorPrimaryDark)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        PrimaryDrawerItem logIn = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.login_drawer_navigation).withIcon(GoogleMaterial.Icon.gmd_account_circle)
                .withIconColorRes(R.color.colorPrimaryDark)
                .withSelectedTextColorRes(R.color.colorPrimaryDark);

        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withDrawerWidthDp(250)
                .withAccountHeader(headerResult, true)
                .withToolbar(toolbar)
                .addDrawerItems(
                        home,
                        logIn
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case 0:
                                break;
                            case 1:
                                startActivityForResult(
                                        AuthUI.getInstance()
                                                .createSignInIntentBuilder()
                                                .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                                .build(),
                                        RC_SIGN_IN
                                );
                                break;
                            case 2:
                                break;
                            case 5:
                                break;
                        }
                        return false;
                    }
                })
                .build();
    }



    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CategoriesFragment(), getString(R.string.categories_fragment_title));
        adapter.addFragment(new MostRecentFragment(), getString(R.string.most_recent_fragment_title));
        adapter.addFragment(new MostPopularFragment(), getString(R.string.most_popular_fragment_title));
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.action_search).getActionView();

        ComponentName cn = new ComponentName(this, SearchResultsActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));

//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setSubmitButtonEnabled(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkPendingUploads() {
        Uri sessionUri = Uri.parse(Utilities.readStringPreference(this,
                getString(R.string.session_uri)));

        Uri videoUri = Uri.parse(Utilities.readStringPreference(this,
                getString(R.string.video_uri)));

        if (!sessionUri.toString().equals(getString(R.string.no_uri)) &&
                !videoUri.toString().equals(getString(R.string.no_uri))) {
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
                    Toast.makeText(MainActivity.this, R.string.success_upload, Toast.LENGTH_LONG).show();
                    Utilities.deleteStringPreference(MainActivity.this, getString(R.string.session_uri));
                    Utilities.deleteStringPreference(MainActivity.this, getString(R.string.video_uri));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_MANAGE_DOCUMENTS)
    private void uploadPendings() {
        if (!EasyPermissions.hasPermissions(this, cameraPerms)) {
            return;
        }

        if (!Utilities.readStringPreference(this, getString(R.string.session_uri)).equals(getString(R.string.no_uri))) {
            Uri videoUri = Uri.parse(Utilities.readStringPreference(this, getString(R.string.video_uri)));

            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference().
                    child("videos/" + videoUri.getLastPathSegment());
            UploadTask uploadTask = mStorageRef.putFile(Uri.parse(Utilities.readStringPreference(this, getString(R.string.video_uri))),
                    new StorageMetadata.Builder().build()
                    , Uri.parse(Utilities.readStringPreference(this, getString(R.string.session_uri))));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Utilities.deleteStringPreference(MainActivity.this, getString(R.string.video_uri));
                    Utilities.deleteStringPreference(MainActivity.this, getString(R.string.session_uri));
                }
            });
        }
    }
}
