package com.example.musicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Random;


/**
 * read music in device and add to a list
 * create a player notification for controlling music (play-pause-)
 * control Play and Pause incoming or outgoing call
 * Play audio from earpiece with using  Sensor PROXIMITY
 */

public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_PERMISSIONS = 101;
    private static int[] mTitleTabLayout = {R.string.songs, R.string.artist, R.string.album, R.string.playlist};
    private DisplayMetrics mDisplayMetrics;
    private ArrayList<MusicModel> mListMusic;
    private String mFilePath;
    private int mCurrentTag;
    private String mArtist;
    private String mTitle;
    private PlayerView player_view;
    private RecyclerView rv_list_music;
    private SlidingUpPanelLayout slide_up_panel;
    private ImageView iv_thumbnail;
    private ImageView iv_thumbnail_blur;
    private ImageView iv_play_pause_collapse;
    private ImageView exo_next_my;
    private ImageView exo_prev_my;
    private ImageView exo_shuffle_my;
    private ImageView exo_repeat_toggle_my;
    private TextView tv_artist_expand;
    private TextView tv_song_name_expand;
    private TextView tv_music_name_collapse;
    private TextView tv_artist_collapse;
    private ConstraintLayout cl_expand;
    private ConstraintLayout cl_collapse;
    private NotificationManagerCompat mNotificationManagerCompat;
    private MediaSessionCompat mMediaSessionCompat;
    private Bitmap mThumbnailBitmap;
    private String mPlayAndPauseState = "Pause";
    private String mRepeatState = "Off";
    private String mShuffleState = "Off";
    private TabLayout tab_layout;
    private ViewPager view_pager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        
        setContentView(R.layout.activity_main);
        findView();
        setOthers();
        setAdapters();
        setInterfaces();
        setSlideListener();
        setClickListeners();
        setTabLayout();

    }

    private void findView() {
        rv_list_music = findViewById(R.id.rv_list_music);
        slide_up_panel = findViewById(R.id.slide_up_panel);
        player_view = findViewById(R.id.player_view);
        iv_thumbnail = findViewById(R.id.iv_thumbnail);
        iv_thumbnail_blur = findViewById(R.id.iv_thumbnail_blur);
        iv_play_pause_collapse = findViewById(R.id.iv_play_pause_collapse);
        exo_prev_my = findViewById(R.id.exo_prev_my);
        exo_next_my = findViewById(R.id.exo_next_my);
        exo_repeat_toggle_my = findViewById(R.id.exo_repeat_toggle_my);
        exo_shuffle_my = findViewById(R.id.exo_shuffle_my);
        tv_artist_expand = findViewById(R.id.tv_artist_expand);
        tv_song_name_expand = findViewById(R.id.tv_song_name_expand);
        tv_music_name_collapse = findViewById(R.id.tv_music_name_collapse);
        tv_artist_collapse = findViewById(R.id.tv_artist_collapse);
        cl_collapse = findViewById(R.id.cl_collapse);
        cl_expand = findViewById(R.id.cl_expand);
        tab_layout = findViewById(R.id.tab_layout);
        view_pager = findViewById(R.id.view_pager);
    }


    private void setOthers() {
        mListMusic = new ArrayList<>();
        mDisplayMetrics = getResources().getDisplayMetrics();
    }

    private void setAdapters() {
        rv_list_music.setLayoutManager(new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false));
        checkPermission();
    }

    private void setInterfaces() {
        AdapterListMusic.setGoToPlayActivityInterface(new AdapterListMusic.GoToPlayActivityInterface() {
            @Override
            public void sendPlayInformation(int tag) {
                setDetailsMusic(tag);
                setPlayerView();
                setNotification();
            }
        });

        // when play and pause button selected this part is called
        NotificationBroadCastReceiver.setNotifyPlayAndPauseChange(new NotificationBroadCastReceiver.NotifyPlayAndPauseChange() {
            @Override
            public void notifyChange(String state) {
                if (state.equals("PlayAndPause")) {
                    if (mPlayAndPauseState.equals("Play")) {
                        pauseMedia();
                    } else {
                        playMedia();
                    }
                }
                else if (state.equals("Previous")) {
                    previousMedia();
                }
                else if (state.equals("Next")) {
                    nextMedia();
                }
                else {
                    PlayerManager.getSharedInstance(MainActivity.this).pausePlayer();
                    if (slide_up_panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slide_up_panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        slide_up_panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    }
                    else if (slide_up_panel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                        slide_up_panel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    }
                }

            }
        });
    }

    private void setSlideListener() {
        slide_up_panel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                cl_collapse.setAlpha((1.0f - slideOffset));
                cl_expand.setAlpha((slideOffset));
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });
    }

    private void setClickListeners() {

        //pause or play music
        iv_play_pause_collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayAndPauseState.equals("Play")) {
                    pauseMedia();
                } else {
                    playMedia();
                }
            }
        });

        //go next music
        exo_next_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMedia();

            }
        });

        //go previous music
        exo_prev_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousMedia();
            }
        });

        // repeat current played song
        exo_repeat_toggle_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRepeatState.equals("Off")) {
                    mRepeatState = "Once";
                    exo_repeat_toggle_my.setImageResource(R.drawable.ic_repeat_one);
                    exo_repeat_toggle_my.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    mRepeatState = "Off";
                    exo_repeat_toggle_my.setImageResource(R.drawable.ic_repeat);
                    exo_repeat_toggle_my.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        //if on than that select random music
        exo_shuffle_my.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mShuffleState.equals("On")) {
                    mShuffleState = "Off";
                    exo_shuffle_my.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    mShuffleState = "On";
                    exo_shuffle_my.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);
                }
            }
        });
    }

    private void setTabLayout() {
        view_pager.setOffscreenPageLimit(4);
        view_pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        tab_layout.setupWithViewPager(view_pager);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                // permission granted
                getMusicFromDevices();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        } else {
            // permission granted
            getMusicFromDevices();
        }
    }

    //read music in device and add to a list
    public void getMusicFromDevices() {
        Uri uri;
        Cursor mCursor;
        int COLUMN_INDEX_DATA, COLUMN_INDEX_ARTIST, COLUMN_INDEX_TITLE;
        String absolutePathOfFile;
        int tags = -1;
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM_ID};
        mCursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
        if (mCursor != null) {
            COLUMN_INDEX_DATA = mCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            COLUMN_INDEX_ARTIST = mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            COLUMN_INDEX_TITLE = mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            while (mCursor.moveToNext()) {
                tags++;
                absolutePathOfFile = mCursor.getString(COLUMN_INDEX_DATA);
                MusicModel musicModel = new MusicModel();
                musicModel.setFilePath(absolutePathOfFile);
                musicModel.setArtist(mCursor.getString(COLUMN_INDEX_ARTIST));
                musicModel.setTitle(mCursor.getString(COLUMN_INDEX_TITLE));
                musicModel.setTag(tags);
                mListMusic.add(musicModel);
            }
            AdapterListMusic mAdapterListMusic = new AdapterListMusic(MainActivity.this, mListMusic);
            rv_list_music.setAdapter(mAdapterListMusic);
            mCursor.close();
        }
    }

    private void setPlayerView() {
        if (mFilePath != null) {
            slide_up_panel.setPanelHeight((int) (((float) mDisplayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT) * 64));
            if (slide_up_panel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                slide_up_panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
            player_view.setPlayer(PlayerManager.getSharedInstance(MainActivity.this).getPlayerView().getPlayer());
            player_view.setUseArtwork(false);
            player_view.setShutterBackgroundColor(Color.TRANSPARENT);
            player_view.setControllerHideOnTouch(false);
            PlayerManager.getSharedInstance(MainActivity.this).playStream(mListMusic.get(mCurrentTag).getFilePath());
            PlayerManager.getSharedInstance(this).setPlayerListener(new PlayerManager.PlayerCallBack() {

                @Override
                public void onPlayPlayer() {
                    playMedia();
                }

                @Override
                public void onPausePlayer() {
                    pauseMedia();
                }

                @Override
                public void onEndPlayer() {
                    if (mRepeatState.equals("Once")) {
                        PlayerManager.getSharedInstance(MainActivity.this).playStream(mListMusic.get(mCurrentTag).getFilePath());
                    } else {
                        if (mShuffleState.equals("On")){
                            Random rand = new Random();
                            mCurrentTag = rand.nextInt(mListMusic.size());
                            setDetailsMusic(mCurrentTag);
                            setPlayerView();
                            setNotification();
                        }
                        else {
                            mPlayAndPauseState = "End";
                            iv_play_pause_collapse.setImageResource(R.drawable.ic_play);
                            notifyNotification(R.drawable.ic_play, "pause", true, false);
                        }

                    }
                }
            });
        } else {
            finish();
            Toast.makeText(MainActivity.this, getString(R.string.public_error_player), Toast.LENGTH_LONG).show();
        }
    }

    // set sliding up panel details music for every time music played
    private void setDetailsMusic(int tag) {
        mCurrentTag = tag;
        mFilePath = mListMusic.get(mCurrentTag).getFilePath();
        if (mListMusic.get(mCurrentTag).getData() == null) {
            mListMusic.get(mCurrentTag).setData(new MediaMetadataRetriever());
            mListMusic.get(mCurrentTag).getData().setDataSource(mListMusic.get(mCurrentTag).getFilePath());
        }
        byte[] mThumbnail = mListMusic.get(mCurrentTag).getData().getEmbeddedPicture();
        mArtist = mListMusic.get(mCurrentTag).getArtist();
        mTitle = mListMusic.get(mCurrentTag).getTitle();
        tv_artist_expand.setText(mArtist);
        tv_song_name_expand.setText(mTitle);
        tv_artist_collapse.setText(mArtist);
        tv_music_name_collapse.setText(mTitle);
        if (mThumbnail == null) {
            mThumbnailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_thumbnail);
        } else {
            mThumbnailBitmap = BitmapFactory.decodeByteArray(mThumbnail, 0, mThumbnail.length);
        }

        if (mThumbnailBitmap == null) {
            mThumbnailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_thumbnail);
        }
        Glide.with(MainActivity.this)
                .load(BlurEffect.blur(MainActivity.this, mThumbnailBitmap))
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(iv_thumbnail_blur);

        Glide.with(MainActivity.this)
                .load(mThumbnailBitmap)
                .apply(new RequestOptions().transform(new RoundedCorners((int) (((float) mDisplayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT) * 8))))
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(iv_thumbnail);
    }


    private void setNotification() {
        mNotificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
        mMediaSessionCompat = new MediaSessionCompat(MainActivity.this, "Music Player");
        notifyNotification(R.drawable.exo_icon_pause, "play", false, true);
    }

    // create a player notification for controlling music (play-pause-)
    private void notifyNotification(int sourceDrawable, String state, boolean autoCancel, boolean onGoing) {
        Intent thisActivityIntent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent thisActivityPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, thisActivityIntent, 0);

        Intent broadcastIntentPlayPause = new Intent(MainActivity.this, NotificationBroadCastReceiver.class);
        broadcastIntentPlayPause.setAction("PlayAndPause");

        Intent broadcastIntentPlayPrevious = new Intent(MainActivity.this, NotificationBroadCastReceiver.class);
        broadcastIntentPlayPrevious.setAction("Previous");

        Intent broadcastIntentPlayNext = new Intent(MainActivity.this, NotificationBroadCastReceiver.class);
        broadcastIntentPlayNext.setAction("Next");

        PendingIntent broadcastPendingIntentPlayPause = PendingIntent.getBroadcast(MainActivity.this, 0, broadcastIntentPlayPause, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent broadcastPendingIntentPrevious = PendingIntent.getBroadcast(MainActivity.this, 1, broadcastIntentPlayPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent broadcastPendingIntentNext = PendingIntent.getBroadcast(MainActivity.this, 2, broadcastIntentPlayNext, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent broadcastClearIntent = new Intent(MainActivity.this, NotificationBroadCastReceiver.class);
        broadcastClearIntent.setAction("Clear");
        PendingIntent broadcastClearPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, broadcastClearIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(MainActivity.this, AppInitial.CHANNEL_MUSIC_ID)
                .setSmallIcon(R.drawable.ic_notification_app_icon)
                .setLargeIcon(mThumbnailBitmap)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentTitle(mTitle)
                .setContentText(mArtist)
                .setLargeIcon(mThumbnailBitmap)
                .addAction(R.drawable.ic_skip_previous, "previous", broadcastPendingIntentPrevious)
                .addAction(sourceDrawable, state, broadcastPendingIntentPlayPause)
                .addAction(R.drawable.ic_skip_next, "next", broadcastPendingIntentNext)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSessionCompat.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setDeleteIntent(broadcastClearPendingIntent)
                .setContentIntent(thisActivityPendingIntent)
                .setAutoCancel(autoCancel)
                .setOngoing(onGoing)
                .build();
        mNotificationManagerCompat.notify(10, notification);
    }

    private void playMedia() {
        if (mPlayAndPauseState.equals("End")) {
            PlayerManager.getSharedInstance(MainActivity.this).playStream(mFilePath);
        } else {
            PlayerManager.getSharedInstance(MainActivity.this).resumePlayer();
        }
        iv_play_pause_collapse.setImageResource(R.drawable.ic_pause);
        notifyNotification(R.drawable.ic_pause, "play", false, true);
        mPlayAndPauseState = "Play";
    }

    private void pauseMedia() {
        mPlayAndPauseState = "Pause";
        iv_play_pause_collapse.setImageResource(R.drawable.ic_play);
        notifyNotification(R.drawable.ic_play, "pause", true, false);
        PlayerManager.getSharedInstance(MainActivity.this).pausePlayer();
    }

    private void nextMedia(){
        if (mCurrentTag < mListMusic.size() - 1) {
            mCurrentTag++;
            setDetailsMusic(mCurrentTag);
            setPlayerView();
            setNotification();
        }
    }

    private void previousMedia(){
        if (mCurrentTag > 0) {
            mCurrentTag--;
            setDetailsMusic(mCurrentTag);
            setPlayerView();
            setNotification();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    getMusicFromDevices();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.public_permission_fail_message), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (slide_up_panel != null &&
                (slide_up_panel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slide_up_panel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            slide_up_panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //these fragment are using for ViewPager in 4 tab (TabLayout) 
    class ViewPagerAdapter extends FragmentPagerAdapter {

        ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return new ArtistFragment();
                case 2:
                    return new AlbumFragment();
                case 3:
                    return new PlayListFragment();
                default:
                    return new SongsFragment();
            }
        }

        @Override
        public int getCount() {
            return mTitleTabLayout.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return getString(mTitleTabLayout[position]);
        }
    }
}
