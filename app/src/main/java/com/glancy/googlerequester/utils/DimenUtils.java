package com.glancy.googlerequester.utils;

import android.content.res.Resources;

public class DimenUtils
{
    public static int dpToPx(final Resources resources, final int dp)
    {
        final float scale = resources.getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}