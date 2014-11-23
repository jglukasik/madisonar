package com.madisonar.iotlab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.madisonar.iotlab.model.Building;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NearbyBuilder
{
    //private static final String TAG = NearbyBuilder.class.getSimpleName();

    private ArrayList<Building> mBuildings;
    private double unixTime = 0;


    private double requestLat;
    private double requestLng;
    private int status = 1;

    public class ResponseReceiver extends BroadcastReceiver{
        public static final String ACTION_RESP =
                "com.madisonar.iotlab.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context receivedContext, Intent receivedIntent)
        {
            String rawJSON = receivedIntent.getStringExtra(JsonPullService.PARAM_OUT_MSG);
            try
            {
                ArrayList<Building> newBuildings = new ArrayList<Building>();
                JSONObject container = new JSONObject(rawJSON);

                JSONObject response = container.getJSONObject("response");
                double newTime = response.getDouble("unixTime");
                double newRequestLat = response.getDouble("requestLat");
                double newRequestLng = response.getDouble("requestLng");
                JSONArray buildingJSONArray = response.getJSONArray("buildings");
                for (int i = 0; i < buildingJSONArray.length(); i++)
                {
                    JSONObject currentBuilding = buildingJSONArray.getJSONObject(i);
                    Building toAdd = new Building(
                            currentBuilding.getDouble("lLat"),
                            currentBuilding.getDouble("lLng"),
                            currentBuilding.getDouble("rLat"),
                            currentBuilding.getDouble("rLng"),
                            currentBuilding.getDouble("rHeading"),
                            currentBuilding.getDouble("lHeading"),
                            currentBuilding.getString("bName"));
                    newBuildings.add(toAdd);
                }
                mBuildings = newBuildings;
                unixTime = newTime;
                requestLat = newRequestLat;
                requestLng = newRequestLng;
            }
            catch (JSONException e)
            {
                status = 1;
            }
        }
    }

    public NearbyBuilder(Context context)
    {
        mBuildings = new ArrayList<Building>();
        Intent inputIntent = new Intent(context, JsonPullService.class);
        context.startService(inputIntent);
    }



    public int getStatus() {
        return status;
    }

    public double getUnixTime() {
        return unixTime;
    }

    public double getRequestLat() {
        return requestLat;
    }

    public double getRequestLng() {
        return requestLng;
    }

    public ArrayList<Building> getmBuildings() {
        return mBuildings;
    }
}
