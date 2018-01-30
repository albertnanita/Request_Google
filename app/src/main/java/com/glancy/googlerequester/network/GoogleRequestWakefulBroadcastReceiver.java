package com.glancy.googlerequester.network;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * This BroadcastReceiver is used to guarantee that the CPU is woken up and stays away until the
 * corresponding IntentService has completed its work. This class leverages wakelocks in order to
 * keep the device awake. This BroadcastReceiver in concert with its corresponding Service will
 * continue to schedule alarms to execute the Service indefinitely until the application is
 * force stopped.
 */
public class GoogleRequestWakefulBroadcastReceiver extends WakefulBroadcastReceiver
{
    private static final int ALARM_PENDING_INTENT_REQUEST_CODE = 777;
    private static final long ALARM_REPEAT_INTERVAL_S = 10;

    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        Timber.d("onReceive() - Starting wakeful service at: " + new Date());

        final Intent service = new Intent(context, GoogleRequestService.class);
        startWakefulService(context, service);
    }

    /**
     * Ideally, we could just set a repeating alarm once and never have to worry about scheduling
     * alarms again. Unfortunately, it seems that when a repeating alarm is set, it takes at least a
     * minute in between events even if the interval is set to just a few seconds. To work around
     * this limitation to get smaller intervals, we instead only set one alarm at a time. The alarm
     * is reset at the end of each service execution.
     * @param context
     */
    public static void scheduleAlarms(final Context context)
    {
        final Intent intent = new Intent(context, GoogleRequestWakefulBroadcastReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_PENDING_INTENT_REQUEST_CODE, intent, 0);
        final long intervalMs = TimeUnit.MILLISECONDS.convert(ALARM_REPEAT_INTERVAL_S, TimeUnit.SECONDS);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalMs,
                intervalMs,
                pendingIntent);
    }

    /**
     * See comments on {@link GoogleRequestWakefulBroadcastReceiver#scheduleAlarms(Context)}. This
     * is a workaround that allows us to achieve smaller intervals between alarms. While this works,
     * the interval that is actually used is still no less than 5 seconds no matter what times are
     * specified. It isn't perfect, but it's close enough.
     * @param context
     */
    public static void scheduleNextAlarm(final Context context)
    {
        final Intent intent = new Intent(context, GoogleRequestWakefulBroadcastReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_PENDING_INTENT_REQUEST_CODE, intent, 0);

        final long wakeupTimeMs = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(ALARM_REPEAT_INTERVAL_S, TimeUnit.SECONDS);
        Timber.d("scheduleNextAlarm() - Scheduling next broadcast for: " + new Date(wakeupTimeMs));

        // We don't have to worry about double scheduling the alarms if the app is re-launched
        // multiple times because according to the official documentation, "When you set a second
        // alarm that uses the same pending intent, it replaces the original alarm."
        // If we didn't need to be exact with when the next time this service is launched, we could
        // use set() instead of setExact(). When we don't use an exact alarm, the operating system
        // can be smart about bundling alarms and batch execute a set of alarms. We could even use
        // yet another approach if we wanted to and use setWindow().
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeupTimeMs, pendingIntent);
    }
}