package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadCastReceiver extends BroadcastReceiver {

    private static NotifyPlayAndPauseChange mNotifyPlayAndPauseChange;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction()!=null && mNotifyPlayAndPauseChange != null) {
                mNotifyPlayAndPauseChange.notifyChange(intent.getAction());

        }


    }
    interface NotifyPlayAndPauseChange{
        void notifyChange(String state);
    }

    static void setNotifyPlayAndPauseChange(NotifyPlayAndPauseChange notifyPlayAndPauseChange){
        mNotifyPlayAndPauseChange = notifyPlayAndPauseChange;
    }
}
