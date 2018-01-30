package com.glancy.googlerequester.network;

import java.util.Map;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class NetworkResponse extends RealmObject
{
    private String mId;
    private long mRequestTimeMs;
    private long mResponseTimeMs;
    private long mRoundtripDurationMs;
    private int mStatusCode;
    private String mNetworkType;
    private float mBatteryLevel;
    private boolean mIsBatteryCharging;

    // TODO: I don't think maps are supported by realm. Break these into individual attributes? Or just store as a JSON String to be parsed into UI later.
    @Ignore
    private Map<String, String> mHeaders;

    public NetworkResponse()
    {
        mId = UUID.randomUUID().toString();
    }

    public NetworkResponse(final long responseTimeMs,
                           final long roundtripDurationMs,
                           final int statusCode,
                           final Map<String, String> headers)
    {
        mId = UUID.randomUUID().toString();
        mResponseTimeMs = responseTimeMs;
        mRoundtripDurationMs = roundtripDurationMs;
        mStatusCode = statusCode;
        mHeaders = headers;
    }

    public void setRequestTimeMs(final long requestTimeMs)
    {
        mRequestTimeMs = requestTimeMs;
    }

    public void setNetworkType(final String networkType)
    {
        mNetworkType = networkType;
    }

    public void setBatteryLevel(final float batteryLevel)
    {
        mBatteryLevel = batteryLevel;
    }

    public void setIsBatteryCharging(final boolean isBatteryCharging)
    {
        mIsBatteryCharging = isBatteryCharging;
    }

    public String getId()
    {
        return mId;
    }

    public long getRequestTimeMs()
    {
        return mRequestTimeMs;
    }

    public long getResponseTimeMs()
    {
        return mResponseTimeMs;
    }

    public long getRoundtripDurationMs()
    {
        return mRoundtripDurationMs;
    }

    public int getStatusCode()
    {
        return mStatusCode;
    }

    public boolean isSuccess()
    {
        return ((mStatusCode <= 299) && (mStatusCode >= 200));
    }

    public Map<String, String> getHeaders()
    {
        return mHeaders;
    }

    public String getNetworkType()
    {
        return mNetworkType;
    }

    public float getBatteryLevel()
    {
        return mBatteryLevel;
    }

    public boolean isBatteryCharging()
    {
        return mIsBatteryCharging;
    }
}