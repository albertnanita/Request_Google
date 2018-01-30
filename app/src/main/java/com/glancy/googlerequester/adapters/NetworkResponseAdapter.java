package com.glancy.googlerequester.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.glancy.googlerequester.R;
import com.glancy.googlerequester.network.NetworkResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkResponseAdapter extends RecyclerView.Adapter<NetworkResponseAdapter.ViewHolder>
{
    final Set<String> mItemIds = new HashSet<>();
    final List<NetworkResponse> mItems = new ArrayList<>();
    final Set<String> mExpandedCardIds = new HashSet<>();

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType)
    {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.item_network_response, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    public Set<String> getItemIds()
    {
        return mItemIds;
    }

    public void clearItems()
    {
        mItemIds.clear();
        mItems.clear();
        mExpandedCardIds.clear();

        notifyDataSetChanged();
    }

    /**
     * This assumes that all network responses added here are for a time after the current latest
     * network response.
     *
     * @param items
     */
    public void addItems(final List<NetworkResponse> items)
    {
        final int oldSize = mItems.size();

        mItems.addAll(items);

        for (final NetworkResponse networkResponse : items)
        {
            mItemIds.add(networkResponse.getId());
        }

        notifyItemRangeInserted(oldSize, items.size());
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position)
    {
        final NetworkResponse networkResponse = mItems.get(position);

        if (networkResponse.isSuccess())
        {
            viewHolder.responseStatusImageView.setImageResource(R.drawable.green_circle);
        }
        else
        {
            viewHolder.responseStatusImageView.setImageResource(R.drawable.red_circle);
        }

        viewHolder.responseRoundtripTimeTextView.setText(networkResponse.getRoundtripDurationMs() + " ms");

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
        viewHolder.requestDateTextView.setText(requestDate);

        simpleDateFormat = new SimpleDateFormat("h:mm:ss a");
        final String requestTime = simpleDateFormat.format(new Date(networkResponse.getRequestTimeMs()));
        viewHolder.requestTimeTextView.setText(requestTime);

        viewHolder.responseCodeTextView.setText(String.valueOf(networkResponse.getStatusCode()));
        viewHolder.batteryStateTextView.setText(networkResponse.isBatteryCharging() ? R.string.charging : R.string.not_charging);
        viewHolder.connectionTypeTextView.setText(networkResponse.getNetworkType());
        viewHolder.batteryLevelTextView.setText(networkResponse.getBatteryLevel() + "%");

        viewHolder.cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                if (viewHolder.detailsSection.getVisibility() == View.VISIBLE)
                {
                    mExpandedCardIds.remove(networkResponse.getId());
                }
                else
                {
                    mExpandedCardIds.add(networkResponse.getId());
                }

                notifyItemChanged(position);
            }
        });

        if (mExpandedCardIds.contains(networkResponse.getId()))
        {
            viewHolder.detailsSection.setVisibility(View.VISIBLE);
        }
        else
        {
            viewHolder.detailsSection.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems.size();
    }

    @Override
    public void onViewRecycled(final ViewHolder viewHolder)
    {
        super.onViewRecycled(viewHolder);

        viewHolder.detailsSection.setVisibility(View.GONE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        ImageView responseStatusImageView;
        TextView responseRoundtripTimeTextView;
        TextView requestTimeTextView;
        TextView requestDateTextView;
        TextView responseCodeTextView;
        TextView batteryStateTextView;
        TextView connectionTypeTextView;
        TextView batteryLevelTextView;
        View detailsSection;

        public ViewHolder(final View itemView)
        {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.item_network_response_card_view);
            responseStatusImageView = (ImageView) itemView.findViewById(R.id.item_network_response_status_image_view);
            responseRoundtripTimeTextView = (TextView) itemView.findViewById(R.id.item_network_response_roundtrip_time_text_view);
            requestTimeTextView = (TextView) itemView.findViewById(R.id.item_network_response_request_time_text_view);
            requestDateTextView = (TextView) itemView.findViewById(R.id.item_network_response_request_date_text_view);
            responseCodeTextView = (TextView) itemView.findViewById(R.id.item_network_response_response_code_text_view);
            batteryStateTextView = (TextView) itemView.findViewById(R.id.item_network_response_battery_state_text_view);
            connectionTypeTextView = (TextView) itemView.findViewById(R.id.item_network_response_connection_type_text_view);
            batteryLevelTextView = (TextView) itemView.findViewById(R.id.item_network_response_battery_level_text_view);
            detailsSection = itemView.findViewById(R.id.item_network_response_details_section);
        }
    }
}