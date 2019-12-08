package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadCastReceiverPlayPause extends BroadcastReceiver {

    private static NotifyPlayAndPauseChange mNotifyPlayAndPauseChange;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mNotifyPlayAndPauseChange != null){
            mNotifyPlayAndPauseChange.notifyChange();
        }

    }
    interface NotifyPlayAndPauseChange{
        void notifyChange();
    }

    static void setNotifyPlayAndPauseChange(NotifyPlayAndPauseChange notifyPlayAndPauseChange){
        mNotifyPlayAndPauseChange = notifyPlayAndPauseChange;
    }
}
