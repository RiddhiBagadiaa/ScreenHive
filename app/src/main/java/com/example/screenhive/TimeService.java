package com.example.screenhive;

import android.content.SharedPreferences;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.Date;

public class TimeService {

    private static long pausedTime;

    public static long endOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static  void startChronometer() {
        ChatHeadService.mChronometer.setBase(SystemClock.elapsedRealtime() - ChatHeadService.prefs.getLong("time", 0));
        ChatHeadService.mChronometer.start();
    }

    public static void pauseChronometer() {
        ChatHeadService.mChronometer.stop();
        pausedTime = SystemClock.elapsedRealtime() - ChatHeadService.mChronometer.getBase();
        prepareSharedData(pausedTime);
    }

    public static void resetChronometer() {
        ChatHeadService.mChronometer.setBase(SystemClock.elapsedRealtime());
        pausedTime = 0;
        prepareSharedData(pausedTime);
    }

    public static void prepareSharedData(long pauseTime) {
        SharedPreferences.Editor editor = ChatHeadService.prefs.edit();
        editor.putLong("time", pauseTime);
        editor.apply();
    }
}
