package com.glancy.googlerequester.views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.glancy.googlerequester.R;
import com.glancy.googlerequester.network.NetworkResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NetworkResponseMarkerView extends MarkerView
{
    private ImageView mResponseStatusImageView;
    private TextView mResponseRoundtripTimeTextView;
    private TextView mRequestTimeTextView;
    private TextView mRequestDateTextView;

    public NetworkResponseMarkerView(final Context context, final int layoutResource)
    {
        super(context, layoutResource);

        mResponseStatusImageView = (ImageView) findViewById(R.id.item_network_response_status_image_view);
        mResponseRoundtripTimeTextView = (TextView) findViewById(R.id.item_network_response_roundtrip_time_text_view);
        mRequestTimeTextView = (TextView) findViewById(R.id.item_network_response_request_time_text_view);
        mRequestDateTextView = (TextView) findViewById(R.id.item_network_response_request_date_text_view);
    }

    @Override
    public void refreshContent(final Entry entry, final Highlight highlight)
    {
        final NetworkResponse networkResponse = (NetworkResponse) entry.getData();

        if (networkResponse.isSuccess())
        {
            mResponseStatusImageView.setImageResource(R.drawable.green_circle);
        }
        else
        {
            mResponseStatusImageView.setImageResource(R.drawable.red_circle);
        }

        mResponseRoundtripTimeTextView.setText(networkResponse.getRoundtripDurationMs() + " ms");

        // From: http://stackoverflow.com/a/10317196/1425004
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
        final String day = simpleDateFormat.format(new Date(networkResponse.getRequestTimeMs()));
        if (day.endsWith("1") && !day.endsWith("11"))
        {
            simpleDateFormat = new SimpleDateFormat("MMM d'st' yyyy");
        }
        else if (day.endsWith("2") && !day.endsWith("12"))
        {
            simpleDateFormat = new SimpleDateFormat("MMM d'nd' yyyy");
        }
        else if (day.endsWith("3") && !day.endsWith("13"))
        {
            simpleDateFormat = new SimpleDateFormat("MMM d'rd' yyyy");
        }
        else
        {
            simpleDateFormat = new SimpleDateFormat("MMM d'th' yyyy");
        }

        final String requestDate = simpleDateFormat.format(new Date(networkResponse.getRequestTimeMs()));
        mRequestDateTextView.setText(requestDate);

        simpleDateFormat = new SimpleDateFormat("h:mm:ss a");
        final String requestTime = simpleDateFormat.format(new Date(networkResponse.getRequestTimeMs()));
        mRequestTimeTextView.setText(requestTime);
    }

    /**
     * Taken from: http://stackoverflow.com/a/37286641/1425004
     */
    @Override
    public int getXOffset(final float xpos)
    {
        // This will center the marker-view horizontally.
        final int min_offset = 50;
        if (xpos < min_offset)
        {
            return 0;
        }

        final WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        if ((displayMetrics.widthPixels - xpos) < min_offset)
        {
            // For right hand side.
            return -getWidth();
        }
        else if ((displayMetrics.widthPixels - xpos) < 0)
        {
            // For left hand side.
            return -getWidth();
        }

        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(final float ypos)
    {
        return 0;
    }
}