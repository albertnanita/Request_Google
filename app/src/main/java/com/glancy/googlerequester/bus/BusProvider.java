package com.glancy.googlerequester.bus;

import com.squareup.otto.Bus;

/**
 * Based off sample project:
 * https://github.com/square/otto/blob/master/otto-sample/src/main/java/com/squareup/otto/sample/BusProvider.java
 */
public final class BusProvider
{
    private static final MainThreadBus sBus = new MainThreadBus();

    public static Bus getInstance()
    {
        return sBus;
    }

    private BusProvider()
    {
        // No instances.
    }
}