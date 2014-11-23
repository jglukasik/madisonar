package com.google.android.glass.sample.compass;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.glass.sample.compass.model.Building;

import java.util.ArrayList;

public class NearbyBuilder {

    private static final String TAG = NearbyBuilder.class.getSimpleName();

    private final ArrayList<Building> mBuildings;

    public Buildings(Context context)
    {
        mBuildings = new ArrayList<Building>();
        Intent getJSON = new Intent(context, JsonPullService.class);
    }
}
