package com.example.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationBroadCastReceiverPrevious extends BroadcastReceiver {

    private static NotifyPreviousChange mNotifyPreviousChange;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (mNotifyPreviousChange != null){
            mNotifyPreviousChange.notifyChange();
        }

    }
    interface NotifyPreviousChange{
        void notifyChange();
    }

    static void setNotifyPrevoiusChange(NotifyPreviousChange notifyPrevoiusChange){
        mNotifyPreviousChange = notifyPrevoiusChange;
    }
}
