package com.example.musicplayer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
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

class PlayerManager implements SensorEventListener,  AudioManager.OnAudioFocusChangeListener  {

    private static PlayerManager mInstance = null;
    private static SimpleExoPlayer mPlayer;
    private PlayerView mPlayerView;
    private DefaultDataSourceFactory dataSourceFactory;
    private String uriString = "";
    private PlayerCallBack listener;
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private Context mContext;
    private AudioManager mAudioManager;


    private PlayerManager(Context context) {
        mContext = context;
        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        TrackSelection.Factory trackSelection = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(trackSelection);
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        mPlayerView = new PlayerView(context);
        mPlayerView.setUseController(true);
        mPlayerView.requestFocus();
        mPlayerView.setPlayer(mPlayer);
        Uri uri = Uri.parse(uriString);
        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)), BANDWIDTH_METER);
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
        setSensor();
        setAudioManager();
    }


    static PlayerManager getSharedInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new PlayerManager(mContext);
        }
        return mInstance;
    }


    private void setSensor() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setAudioManager() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null) {
            mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }


    private void setPlayerType(String state) {
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Play audio from earpiece with using Sensor PROXIMITY
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < mProximity.getMaximumRange()) {
                //near
                if (mAudioManager != null) {
                    PlayerManager.getSharedInstance(mContext).setPlayerType("Ear");
                    mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    mAudioManager.setSpeakerphoneOn(false);
                }

            } else {
                //far
                if (mAudioManager != null) {
                    PlayerManager.getSharedInstance(mContext).setPlayerType("Speaker");
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    mAudioManager.setSpeakerphoneOn(true);
                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // control Play and Pause incoming or outgoing call
    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange <= 0) {
            //LOSS -> PAUSE
            if (listener!=null) {
                listener.onPausePlayer();
            }
        } else {
            //GAIN -> PLAY
            if (listener!=null) {
                listener.onPlayPlayer();
            }
        }
    }
    interface PlayerCallBack {
        void onPlayPlayer();

        void onPausePlayer();

        void onEndPlayer();
    }

}
