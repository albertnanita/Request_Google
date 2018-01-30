package com.glancy.googlerequester.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.glancy.googlerequester.R;
import com.glancy.googlerequester.adapters.NetworkResponseAdapter;
import com.glancy.googlerequester.bus.BusProvider;
import com.glancy.googlerequester.bus.events.DatabaseClearedEvent;
import com.glancy.googlerequester.bus.events.NetworkResponseAddedEvent;
import com.glancy.googlerequester.network.NetworkResponse;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import timber.log.Timber;

import static io.realm.Realm.getDefaultInstance;

public class ListFragment extends Fragment
{
    public static final String TAG = ListFragment.class.getSimpleName();

    private Realm mRealm;
    private NetworkResponseAdapter mNetworkResponseAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mRealm = getDefaultInstance();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mRealm.removeAllChangeListeners();
        mRealm.close();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_list_recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mNetworkResponseAdapter = new NetworkResponseAdapter();
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mNetworkResponseAdapter);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        BusProvider.getInstance().register(this);

        refreshItems();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onDatabaseClearedEvent(final DatabaseClearedEvent databaseClearedEvent)
    {
        mNetworkResponseAdapter.clearItems();
    }

    @Subscribe
    public void onNetworkResponseAddedEvent(final NetworkResponseAddedEvent networkResponseAddedEvent)
    {
        Timber.d("Received NetworkResponseAddedEvent. Refreshing items.");

        refreshItems();
    }

    private void refreshItems()
    {
        // For some reason, the change listener stops working after three changes. People all over
        // GitHub and StackOverflow are having the same issue. Instead of keeping one listener
        // registered at all times, I just register one at a time. Changes will be communicated via
        // the event bus instead.
        final RealmResults<NetworkResponse> results = mRealm.where(NetworkResponse.class).findAllSortedAsync("mRequestTimeMs");
        final RealmChangeListener<RealmResults<NetworkResponse>> realmChangeListener = new RealmChangeListener<RealmResults<NetworkResponse>>()
        {
            @Override
            public void onChange(final RealmResults<NetworkResponse> results)
            {
                if (!isAdded())
                {
                    results.removeChangeListener(this);

                    return;
                }

                // Don't add the network responses we already have. This way we can invalidate a
                // range and animate it.
                // RealmResults doesn't support iterator for some reason. Make a copy.
                final List<NetworkResponse> resultsCopy = new ArrayList<>(results);
                final Set<String> currentItemsIds = mNetworkResponseAdapter.getItemIds();
                final Iterator<NetworkResponse> networkResponseIterator = resultsCopy.iterator();
                while (networkResponseIterator.hasNext())
                {
                    final NetworkResponse networkResponse = networkResponseIterator.next();
                    if (currentItemsIds.contains(networkResponse.getId()))
                    {
                        networkResponseIterator.remove();
                    }
                }

                Timber.d("Adding " + resultsCopy.size() + " items to the adapter.");

                if (resultsCopy.size() > 0)
                {
                    mNetworkResponseAdapter.addItems(resultsCopy);

                    mRecyclerView.smoothScrollToPosition(mNetworkResponseAdapter.getItemCount() - 1);
                }

                results.removeChangeListener(this);
            }
        };

        results.addChangeListener(realmChangeListener);
    }
}