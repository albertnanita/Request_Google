package com.glancy.googlerequester.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton
{
    private static VolleySingleton sInstance;
    private static Context sContext;

    private RequestQueue mRequestQueue;

    public static synchronized VolleySingleton getInstance(final Context context)
    {
        if (sInstance == null)
        {
            sInstance = new VolleySingleton(context);
        }

        return sInstance;
    }

    private VolleySingleton(final Context context)
    {
        sContext = context;

        mRequestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue()
    {
        if (mRequestQueue == null)
        {
            // According to the documentation, "getApplicationContext() is key, it keeps you from
            // leaking the Activity or BroadcastReceiver if someone passes one in."
            mRequestQueue = Volley.newRequestQueue(sContext.getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(final Request<T> req)
    {
        getRequestQueue().add(req);
    }
}