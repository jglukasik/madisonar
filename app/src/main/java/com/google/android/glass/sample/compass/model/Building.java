package com.google.android.glass.sample.compass.model;

import java.util.ArrayDeque;

/**
 * Created by sielicki on 11/22/14.
 */
public class Building {
    private final double mLatitudeLeft;
    private final double mLongitudeLeft;
    private final double mLatitudeRight;
    private final double mLongitudeRight;
    private final double mHeadingRight;
    private final double mHeadingLeft;
    private final String mName;

    public Building(double latL, double longL,
                    double latR, double longR,
                    double headR, double headL,
                    String name)
    {
        mLatitudeLeft = latL;
        mLongitudeLeft = longL;
        mLatitudeRight = latR;
        mLongitudeRight = longR;
        mHeadingLeft = headL;
        mHeadingRight = headR;
        mName = name;
    }

    public double[] getCoordsRight(){ double[] toReturn = {mLatitudeRight, mLongitudeRight}; return toReturn; }
    public double[] getCoordsLeft() { double[] toReturn = {mLatitudeLeft, mLongitudeLeft}; return toReturn; }
    public double getHeadingLeft()  { return mHeadingLeft; }
    public double getHeadingRight() { return mHeadingRight; }
    public String getName()         { return mName; }
}
