package com.glancy.googlerequester.network;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import com.android.volley.toolbox.RequestFuture;
import com.glancy.googlerequester.bus.BusProvider;
import com.glancy.googlerequester.bus.events.NetworkResponseAddedEvent;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;
import timber.log.Timber;

public class GoogleRequestService extends IntentService
{
    private static final String URL = "https://www.google.com";

    private Realm mRealm;

    public GoogleRequestService()
    {
        super(GoogleRequestService.class.getName());
    }

    @Override
    protected void onHandleIntent(final Intent intent)
    {
        Timber.d("onHandleIntent() - Handling service Intent at: " + new Date());

        // A future is used here in order to convert Volley's asynchronous behavior into synchronous
        // behavior. It may not be necessary, but I worry about calling completeWakefulIntent() from
        // an asynchronous callback. I could see there being issues if callbacks from back-to-back
        // network requests get out of order for some reason. I could not find any documentation on
        // the behavior of the wake lock in such situations. The synchronous route is a much safer
        // approach.
        final RequestFuture<NetworkResponse> requestFuture = RequestFuture.newFuture();

        final NetworkRequest request = new NetworkRequest(URL, requestFuture, requestFuture);

        final long requestTimeMs = System.currentTimeMillis();

        // Since battery information is sticky, we can read the Intent immediately without
        // registering a BroadcastReceiver and waiting.
        final Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        final float batteryLevel;
        final boolean isCharging;

        if (batteryIntent == null)
        {
            batteryLevel = -1;
            isCharging = false;
        }
        else
        {
            final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            final int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            final int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            batteryLevel = ((level / (float) scale) * 100.0f);

            // I wouldn't think that BATTERY_STATUS_FULL would mean it is charging, but this line is
            // taken directly from the official Android documentation.
            isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);
        }

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        NetworkResponse networkResponse = null;
        String networkTypeReadable = null;

        if (networkInfo == null)
        {
            networkResponse = new NetworkResponse(0, 0, 0, null);
        }
        else
        {
            final int networkType = networkInfo.getType();
            if (networkType == ConnectivityManager.TYPE_MOBILE)
            {
                // Break the mobile type down into more granular types like LTE.
                networkTypeReadable = networkInfo.getSubtypeName();
            }
            else
            {
                networkTypeReadable = networkInfo.getTypeName();
            }

            VolleySingleton.getInstance(this).addToRequestQueue(request);

            try
            {
                networkResponse = requestFuture.get();
            }
            catch (final InterruptedException e)
            {
                Timber.e(e, "onHandleIntent() - Error during network request.");
            }
            catch (final ExecutionException e)
            {
                Timber.e(e, "onHandleIntent() - Error during network request.");
            }
        }

        if (networkResponse != null)
        {
            networkResponse.setRequestTimeMs(requestTimeMs);
            networkResponse.setBatteryLevel(batteryLevel);
            networkResponse.setIsBatteryCharging(isCharging);
            networkResponse.setNetworkType(networkTypeReadable);

            Timber.d("onHandleIntent() ----------------------------------------");
            Timber.d("onHandleIntent() - Successfully parsed network response.");
            Timber.d("onHandleIntent() - Status Code: " + networkResponse.getStatusCode());
            Timber.d("onHandleIntent() - Is success: " + networkResponse.isSuccess());
            Timber.d("onHandleIntent() - Header Count: " + (networkResponse.getHeaders() == null ? null : networkResponse.getHeaders().size()));
            Timber.d("onHandleIntent() - Network Type: " + networkResponse.getNetworkType());
            Timber.d("onHandleIntent() - Battery Level: " + networkResponse.getBatteryLevel());
            Timber.d("onHandleIntent() - Battery Is Charging: " + networkResponse.isBatteryCharging());
            Timber.d("onHandleIntent() - Request Time (ms): " + networkResponse.getRequestTimeMs());
            Timber.d("onHandleIntent() - Response Time (ms): " + networkResponse.getResponseTimeMs());
            Timber.d("onHandleIntent() - Round Trip Duration (ms): " + networkResponse.getRoundtripDurationMs());

            // The request time I save is not exact. Volley doesn't return one so I have to estimate
            // it. Check to see how far off the response roundtrip time Volley returns is from my
            // calculated roundtrip time.
            final long calculatedRoundtripMs = (networkResponse.getResponseTimeMs() - networkResponse.getRequestTimeMs());
            final long roundtripErrorMs = Math.abs(calculatedRoundtripMs - networkResponse.getRoundtripDurationMs());
            Timber.d("onHandleIntent() - Round Trip Error (ms): " + roundtripErrorMs);

            mRealm = Realm.getDefaultInstance();

            mRealm.beginTransaction();

            mRealm.copyToRealm(networkResponse);

            mRealm.commitTransaction();

            mRealm.close();

            BusProvider.getInstance().post(new NetworkResponseAddedEvent());
        }

        GoogleRequestWakefulBroadcastReceiver.scheduleNextAlarm(this);

        GoogleRequestWakefulBroadcastReceiver.completeWakefulIntent(batteryIntent);

        Timber.d("onHandleIntent() - Completed handling service Intent at: " + new Date());
    }
}