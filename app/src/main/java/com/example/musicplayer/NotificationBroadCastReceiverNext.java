package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadCastReceiverNext extends BroadcastReceiver {

    private static NotifyNextChange mNotifyNextChange;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mNotifyNextChange != null){
            mNotifyNextChange.notifyChange();
        }

    }
    interface NotifyNextChange{
        void notifyChange();
    }

    static void setNotifyNextChange(NotifyNextChange notifyNextChange){
        mNotifyNextChange = notifyNextChange;
    }
}
