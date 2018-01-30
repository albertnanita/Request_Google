package com.glancy.googlerequester;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class Application extends android.app.Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        setUpLogging();

        setUpRealm();
    }

    public void setUpLogging()
    {
        // Only plant the Timber tree in debug builds so we don't log in production.
        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public void setUpRealm()
    {
        // Don't use in-memory database because we need to persist data even after the app is
        // closed.
        Realm.init(this);
        final RealmConfiguration config =   new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
    }
}