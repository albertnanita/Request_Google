package com.glancy.googlerequester.bus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class MainThreadBus extends Bus
{
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            super.post(event);
        }
        else
        {
            mMainThreadHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    post(event);
                }
            });
        }
    }
}