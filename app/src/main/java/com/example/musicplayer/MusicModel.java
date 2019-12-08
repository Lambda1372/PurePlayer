package com.example.musicplayer;

import android.media.MediaMetadataRetriever;

class MusicModel {
    private String filePath, artist, title;
    private MediaMetadataRetriever data = null;
    private int tag = -1;

    String getFilePath() {
        return filePath;
    }

    MediaMetadataRetriever getData() {
        return data;
    }

    void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    void setData(MediaMetadataRetriever data) {
        this.data = data;
    }

    String getArtist() {
        return artist;
    }

    String getTitle() {
        return title;
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    void setTitle(String title) {
        this.title = title;
    }

    int getTag() {
        return tag;
    }

    void setTag(int tag) {
        this.tag = tag;
    }
}
