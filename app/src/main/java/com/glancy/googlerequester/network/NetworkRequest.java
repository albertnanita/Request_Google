package com.glancy.googlerequester.network;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.util.Map;

public class NetworkRequest extends Request<NetworkResponse>
{
    private final Response.Listener<NetworkResponse> mListener;

    public NetworkRequest(final String url, final Response.Listener<NetworkResponse> listener, final Response.ErrorListener errorListener)
    {
        super(Method.GET, url, errorListener);

        mListener = listener;
    }

    @Override
    protected void deliverResponse(final NetworkResponse response)
    {
        mListener.onResponse(response);
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(final com.android.volley.NetworkResponse response)
    {
        final long responseTimeMs = System.currentTimeMillis();
        final long roundtripDurationMs = response.networkTimeMs;
        final int statusCode = response.statusCode;
        final Map<String, String> mHeaders = response.headers;

        final NetworkResponse networkResponse = new NetworkResponse(responseTimeMs,
                roundtripDurationMs,
                statusCode,
                mHeaders);

        return Response.success(networkResponse, HttpHeaderParser.parseCacheHeaders(response));
    }
}