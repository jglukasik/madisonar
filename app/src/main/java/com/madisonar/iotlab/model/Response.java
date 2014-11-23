package com.madisonar.iotlab.model;

import java.util.ArrayList;

public class Response
{
    private ArrayList<Building> mBuildings;
    private double mUnixTime = 0;
    private double mRequestLat;
    private double mRequestLng;

    public Response(ArrayList<Building> mBuildings,
                    double mUnixTime, double mRequestLat,
                    double mRequestLng)
    {
        this.mBuildings = mBuildings;
        this.mUnixTime = mUnixTime;
        this.mRequestLat = mRequestLat;
        this.mRequestLng = mRequestLng;
    }

    public double getRequestLng() {
        return mRequestLng;
    }

    public ArrayList<Building> getBuildings() {
        return mBuildings;
    }

    public double getUnixTime() {
        return mUnixTime;
    }

    public double getRequestLat() {
        return mRequestLat;
    }
}
