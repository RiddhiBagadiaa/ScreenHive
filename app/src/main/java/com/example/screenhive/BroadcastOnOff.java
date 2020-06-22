package com.example.screenhive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.time.Instant;
import java.util.Date;

public class BroadcastOnOff extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            Date now = Date.from(Instant.now());
            long dayEndLong = ChatHeadService.prefs.getLong("DayEnd", 0);

            if(now.getTime() > dayEndLong) {
                TimeService.resetChronometer();
                SharedPreferences.Editor editor = ChatHeadService.prefs.edit();
                editor.putLong("DayEnd", TimeService.endOfDay(now));
                editor.apply();
            }

            TimeService.startChronometer();


        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) ||
                Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            TimeService.pauseChronometer();
        }

    }
}
