package com.jayjhaveri.learnhub;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.SeekBar;

import com.github.rubensousa.previewseekbar.PreviewSeekBar;
import com.github.rubensousa.previewseekbar.PreviewSeekBarLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.jayjhaveri.learnhub.exoplayer.ExoPlayerManager;

public class VideoDetailActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        Toolbar.OnMenuItemClickListener {

    private static final int PICK_FILE_REQUEST_CODE = 2;

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_VIDEO_URL = "video_url";

    private String mPostKey;
//    private DatabaseReference mVideoRef;
//    private ValueEventListener mVideoListener;

    private ExoPlayerManager exoPlayerManager;
    private PreviewSeekBarLayout seekBarLayout;
    private PreviewSeekBar seekBar;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        url = getIntent().getStringExtra(EXTRA_VIDEO_URL);
//        mVideoRef = FirebaseDatabase.getInstance().getReference().child("videos").child(mPostKey);

        SimpleExoPlayerView playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        SimpleExoPlayerView previewPlayerView
                = (SimpleExoPlayerView) findViewById(R.id.previewPlayerView);
        seekBar = (PreviewSeekBar) playerView.findViewById(R.id.exo_progress);
        seekBarLayout = (PreviewSeekBarLayout) findViewById(R.id.previewSeekBarLayout);

        seekBarLayout.setTintColorResource(R.color.colorPrimary);

        seekBar.addOnSeekBarChangeListener(this);
        exoPlayerManager = new ExoPlayerManager(playerView, previewPlayerView, seekBarLayout,url);

//        requestFullScreenIfLandscape();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            exoPlayerManager.play(data.getData());
        }*/
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

    /*private void requestFullScreenIfLandscape() {
        if (getResources().getBoolean(R.bool.landscape)) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.inflateMenu(R.menu.main);
            toolbar.setOnMenuItemClickListener(this);
        }
    }*/
}
