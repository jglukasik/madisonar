package com.madisonar.iotlab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.madisonar.iotlab.model.Building;

import java.util.ArrayList;

public class NearbyBuilder {

    private static final String TAG = NearbyBuilder.class.getSimpleName();

    private final ArrayList<Building> mBuildings;

    public class ResponseReceiver extends BroadcastReceiver{
        public static final String ACTION_RESP =
                "com.madisonar.iotlab.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context c, Intent i)
        {

        }
    }

    public NearbyBuilder(Context context)
    {
        mBuildings = new ArrayList<Building>();
        Intent getJSON = new Intent(context, JsonPullService.class);
        context.startService(getJSON);
    }
}
