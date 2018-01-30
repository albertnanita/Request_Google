package com.glancy.googlerequester.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.glancy.googlerequester.R;
import com.glancy.googlerequester.bus.BusProvider;
import com.glancy.googlerequester.bus.events.DatabaseClearedEvent;
import com.glancy.googlerequester.bus.events.NetworkResponseAddedEvent;
import com.glancy.googlerequester.network.NetworkResponse;
import com.glancy.googlerequester.views.NetworkResponseMarkerView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import timber.log.Timber;

import static io.realm.Realm.getDefaultInstance;

public class GraphFragment extends Fragment
{
    public static final String TAG = GraphFragment.class.getSimpleName();
    public static final int ANIMATION_DURATION_MS = 1000;

    private Realm mRealm;
    private LineChart mLineChart;

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
        final View view = inflater.inflate(R.layout.fragment_graph, container, false);

        mLineChart = (LineChart) view.findViewById(R.id.fragment_graph_line_chart);
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.getXAxis().setTextSize(14f);
        mLineChart.getXAxis().setTextColor(getResources().getColor(android.R.color.black));
        mLineChart.getXAxis().setGranularityEnabled(true);
        mLineChart.getXAxis().setGranularity(1f);
        mLineChart.getXAxis().setDrawGridLines(false);

        // TODO: Why doesn't this work? With only a few data points, -1 shows up on the x-axis.
        mLineChart.getXAxis().setAxisMinValue(0f);

        mLineChart.getAxisLeft().setTextSize(14f);
        mLineChart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.black));
        mLineChart.getAxisLeft().setAxisMinValue(0f);
        mLineChart.getAxisLeft().setGranularityEnabled(true);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.setNoDataTextColor(getResources().getColor(android.R.color.black));
        mLineChart.setDescription(null);
        mLineChart.setPinchZoom(true);
        mLineChart.setDragEnabled(true);
        mLineChart.setDoubleTapToZoomEnabled(true);

        final NetworkResponseMarkerView marker = new NetworkResponseMarkerView(getContext(), R.layout.item_network_response);
        mLineChart.setMarkerView(marker);

        mLineChart.invalidate();

        return view;
    }

    @Override
    public void onHiddenChanged(final boolean hidden)
    {
        super.onHiddenChanged(hidden);

        if (!hidden)
        {
            refreshItems(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        BusProvider.getInstance().register(this);
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
        mLineChart.clear();
    }

    @Subscribe
    public void onNetworkResponseAddedEvent(final NetworkResponseAddedEvent networkResponseAddedEvent)
    {
        Timber.d("Received NetworkResponseAddedEvent. Refreshing items.");

        refreshItems(false);
    }

    private void refreshItems(final boolean animate)
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

                final List<Entry> entries = new ArrayList<>();

                for (int i = 0; i < results.size(); i++)
                {
                    final NetworkResponse networkResponse = results.get(i);

                    final Entry entry = new Entry(i, networkResponse.getRoundtripDurationMs());
                    entry.setData(networkResponse);

                    entries.add(entry);
                }

                final LineDataSet dataSet = new LineDataSet(entries, getResources().getString(R.string.roundtrip_time_ms));
                dataSet.setColor(getResources().getColor(R.color.colorAccent));
                dataSet.setValueTextColor(getResources().getColor(android.R.color.black));
                dataSet.setLineWidth(2f);
                dataSet.setCircleColor(getResources().getColor(R.color.colorAccent));
                dataSet.setCircleRadius(4f);
                dataSet.setCircleHoleRadius(2f);
                dataSet.setDrawValues(false);
                final LineData lineData = new LineData(dataSet);

                mLineChart.setData(lineData);

                if (animate)
                {
                    mLineChart.animateXY(ANIMATION_DURATION_MS, ANIMATION_DURATION_MS);
                }

                mLineChart.invalidate();

                results.removeChangeListener(this);
            }
        };

        results.addChangeListener(realmChangeListener);
    }
}