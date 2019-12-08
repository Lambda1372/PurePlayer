package com.example.musicplayer;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

class PlayerManager {

    private static PlayerManager mInstance = null;
    private static SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;
    private DefaultDataSourceFactory dataSourceFactory;
    private String uriString = "";
    private PlayerCallBack listener;

    private PlayerManager(Context mContext) {

        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelection = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelection);
        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);
        mPlayerView = new PlayerView(mContext);
        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();
        mPlayerView.setPlayer(mPlayer);
        Uri uri = Uri.parse(uriString);
        dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, mContext.getString(R.string.app_name)), BANDWIDTH_METER);
        MediaSource src = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        mPlayer.prepare(src);
        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    if (listener != null) {
                        listener.onPlayPlayer();
                    }
                } else if (playWhenReady) {
                    // or ended (plays when seek away from end)
                    if (playbackState == Player.STATE_ENDED) {
                        if (listener != null) {
                            listener.onEndPlayer();
                        }
                    }
                } else {
                    // player paused in any state
                    if (listener != null) {
                        listener.onPausePlayer();
                    }
                }
            }
        });
    }


    static PlayerManager getSharedInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new PlayerManager(mContext);
        }
        return mInstance;
    }

    void setPlayerType(String state) {
        if (state.equals("Ear")) {
            mPlayer.setAudioStreamType(C.STREAM_TYPE_VOICE_CALL);
        } else {
            mPlayer.setAudioStreamType(C.STREAM_TYPE_MUSIC);
        }
    }

    void setPlayerListener(PlayerCallBack mPlayerCallBack) {
        listener = mPlayerCallBack;
    }

    PlayerView getPlayerView() {
        return mPlayerView;
    }

    void playStream(String urlToPlay) {
        uriString = urlToPlay;
        Uri uri = Uri.parse(uriString);
        MediaSource source;
        source = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(new DefaultExtractorsFactory()).createMediaSource((uri));

        // Prepare the player with the source.
        if (mPlayer != null) {
            mPlayer.prepare(source);
            mPlayer.setPlayWhenReady(true);
        }
    }

    void pausePlayer() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
            mPlayer.getPlaybackState();
        }
    }

    void resumePlayer() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
            mPlayer.getPlaybackState();
        }
    }

    void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    interface PlayerCallBack {
        void onPlayPlayer();

        void onPausePlayer();

        void onEndPlayer();
    }

}