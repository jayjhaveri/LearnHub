/*
 * Copyright 2016 The Android Open Source Project
 * Copyright 2017 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jayjhaveri.learnhub.exoplayer;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.github.rubensousa.previewseekbar.PreviewSeekBarLayout;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.jayjhaveri.learnhub.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExoPlayerManager implements ExoPlayer.EventListener {

    // 1 minute
    private static final int ROUND_DECIMALS_THRESHOLD = 1 * 60 * 1000;
    //    public AfterGetDuration mAfterGetDuration;
    @BindView(R.id.play_pause)
    FrameLayout mPlayPauseFrame;
    @BindView(R.id.exo_repeat)
    ImageButton exo_repeat;
    private ExoPlayerMediaSourceBuilder mediaSourceBuilder;
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayerView previewPlayerView;
    private SimpleExoPlayer player;
    private SimpleExoPlayer previewPlayer;
    private PreviewSeekBarLayout seekBarLayout;
    private ProgressBar progressBar;
    private long total_duration;

    public ExoPlayerManager(SimpleExoPlayerView playerView, SimpleExoPlayerView previewPlayerView,
                            PreviewSeekBarLayout seekBarLayout, String url) {
        this.playerView = playerView;
        this.previewPlayerView = previewPlayerView;
        this.seekBarLayout = seekBarLayout;
        this.mediaSourceBuilder = new ExoPlayerMediaSourceBuilder(playerView.getContext(), url);
        ButterKnife.bind(this, playerView);
        progressBar = (ProgressBar) playerView.findViewById(R.id.pb_video);

//        mAfterGetDuration = afterGetDuration;
    }

    @OnClick(R.id.exo_repeat)
    public void repeatVideo() {
        player.seekTo(0);
        player.setPlayWhenReady(true);
    }

    public void preview(float offset) {
        total_duration = previewPlayer.getDuration();
        int scale = player.getDuration() >= ROUND_DECIMALS_THRESHOLD ? 2 : 1;
        float offsetRounded = roundOffset(offset, scale);
        player.setPlayWhenReady(false);
        previewPlayer.seekTo((long) (offsetRounded * total_duration));
        previewPlayer.setPlayWhenReady(false);
    }

    public void play(Uri uri) {
        mediaSourceBuilder.setUri(uri);
        if (player != null) {
            player.release();
        }
        if (previewPlayer != null) {
            previewPlayer.release();
        }
        createPlayers();
    }

    public void onStart() {
        if (Util.SDK_INT > 23) {
            createPlayers();
        }
    }

    public void onResume() {
        if ((Util.SDK_INT <= 23 || player == null || previewPlayer == null)) {
            createPlayers();
        }
    }

    public void onPause() {
        if (Util.SDK_INT <= 23) {
            releasePlayers();
        }
    }

    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayers();
        }
    }

    public void stopPreview() {
        player.setPlayWhenReady(true);
    }

    private float roundOffset(float offset, int scale) {
        return (float) (Math.round(offset * Math.pow(10, scale)) / Math.pow(10, scale));
    }

    private void releasePlayers() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (previewPlayer != null) {
            previewPlayer.release();
            previewPlayer = null;
        }
    }

    private void createPlayers() {
        player = createFullPlayer();
        playerView.setPlayer(player);
        previewPlayer = createPreviewPlayer();
        previewPlayerView.setPlayer(previewPlayer);
        total_duration = previewPlayer.getDuration();
    }

    private SimpleExoPlayer createFullPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory
                = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(playerView.getContext(),
                trackSelector, loadControl);
        player.setPlayWhenReady(true);
        player.prepare(mediaSourceBuilder.getMediaSource(false));
        player.addListener(this);
        return player;
    }

    private SimpleExoPlayer createPreviewPlayer() {
        TrackSelection.Factory videoTrackSelectionFactory = new WorstVideoTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new PreviewLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(previewPlayerView.getContext(),
                trackSelector, loadControl);
        player.setPlayWhenReady(false);
        player.prepare(mediaSourceBuilder.getMediaSource(true));
        return player;
    }

    private void seekBy(int milliSeconds) {
        if (player == null) return;
        int progress = (int) (player.getCurrentPosition() + milliSeconds);
        player.seekTo(progress);
    }

    public void onFastRewind() {
        seekBy(-10000);
    }

    public void onFastForward() {
        seekBy(10000);
    }

    public void hideexo() {
        playerView.hideController();
    }

    public void showExo() {
        playerView.showController();
    }

    public long getTotalDuration() {
        return total_duration;
    }


    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_READY && playWhenReady) {
            seekBarLayout.hidePreview();
            /*mPauseButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.VISIBLE);*/
            total_duration = player.getDuration();
            Log.d("manaegr", "" + total_duration);
//            mAfterGetDuration.runHandler(total_duration);
            mPlayPauseFrame.setVisibility(View.VISIBLE);
            exo_repeat.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else if (playbackState == ExoPlayer.STATE_ENDED) {
            mPlayPauseFrame.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            exo_repeat.setVisibility(View.VISIBLE);

        } else if (playbackState == ExoPlayer.STATE_BUFFERING) {
            mPlayPauseFrame.setVisibility(View.GONE);
            exo_repeat.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            mPlayPauseFrame.setVisibility(View.VISIBLE);
            exo_repeat.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

   /* public interface AfterGetDuration {
        void runHandler(long total_duration);
    }*/
}
