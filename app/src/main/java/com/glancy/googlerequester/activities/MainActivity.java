package com.glancy.googlerequester.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.glancy.googlerequester.R;
import com.glancy.googlerequester.bus.BusProvider;
import com.glancy.googlerequester.bus.events.DatabaseClearedEvent;
import com.glancy.googlerequester.fragments.GraphFragment;
import com.glancy.googlerequester.fragments.ListFragment;
import com.glancy.googlerequester.network.GoogleRequestWakefulBroadcastReceiver;
import com.glancy.googlerequester.network.NetworkResponse;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import io.realm.Realm;
import io.realm.RealmResults;

import static io.realm.Realm.getDefaultInstance;

public class MainActivity extends AppCompatActivity
{
    private Realm mRealm;
    private Fragment mListFragment;
    private Fragment mGraphFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null)
        {
            mListFragment = new ListFragment();
            mGraphFragment = new GraphFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_main_fragment_container, mListFragment, ListFragment.TAG)
                    .add(R.id.activity_main_fragment_container, mGraphFragment, GraphFragment.TAG)
                    .commit();
        }
        else
        {
            mListFragment = getSupportFragmentManager().findFragmentByTag(ListFragment.TAG);
            mGraphFragment = getSupportFragmentManager().findFragmentByTag(GraphFragment.TAG);
        }

        final BottomBar bottomBar = (BottomBar) findViewById(R.id.activity_main_bottom_bar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener()
        {
            @Override
            public void onTabSelected(final int tabId)
            {
                if (tabId == R.id.tab_list)
                {
                    showFragment(mListFragment);
                }
                else if (tabId == R.id.tab_graph)
                {
                    showFragment(mGraphFragment);
                }
            }
        });

        // Ensure that the requests are being sent in case the user has force stopped the app since
        // it was last launched.
        GoogleRequestWakefulBroadcastReceiver.scheduleNextAlarm(this);

        mRealm = getDefaultInstance();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mRealm.removeAllChangeListeners();
        mRealm.close();
    }

    private void showFragment(final Fragment fragment)
    {
        final Fragment fragmentToHide;
        final Fragment fragmentToShow;
        final int enterAnimation;
        final int exitAnimation;

        if (fragment == mListFragment)
        {
            fragmentToHide = mGraphFragment;
            fragmentToShow = mListFragment;

            enterAnimation = R.anim.slide_in_left;
            exitAnimation = R.anim.slide_out_right;
        }
        else
        {
            fragmentToHide = mListFragment;
            fragmentToShow = mGraphFragment;

            enterAnimation = R.anim.slide_in_right;
            exitAnimation = R.anim.slide_out_left;
        }

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(enterAnimation, exitAnimation);
        fragmentTransaction.hide(fragmentToHide);
        fragmentTransaction.show(fragmentToShow);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        if (item.getItemId() == R.id.menu_activity_main_action_clear)
        {
            mRealm.executeTransactionAsync(new Realm.Transaction()
            {
                @Override
                public void execute(final Realm bgRealm)
                {
                    if (isFinishing())
                    {
                        return;
                    }

                    final RealmResults<NetworkResponse> results = bgRealm.where(NetworkResponse.class).findAll();
                    results.deleteAllFromRealm();
                }
            }, new Realm.Transaction.OnSuccess()
            {
                @Override
                public void onSuccess()
                {
                    BusProvider.getInstance().post(new DatabaseClearedEvent());
                }
            });

            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }
}