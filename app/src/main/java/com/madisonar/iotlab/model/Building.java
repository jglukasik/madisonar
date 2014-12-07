package com.madisonar.iotlab.model;

/**
 * Created by sielicki on 11/22/14.
 */
public class Building {
    private final double mLatitudeLeft;
    private final double mLongitudeLeft;
    private final double mLatitudeRight;
    private final double mLongitudeRight;
    private final float mHeadingRight;
    private final float mHeadingLeft;
    private final String mName;

    public Building(double latL, double longL,
                    double latR, double longR,
                    float headR, float headL,
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
    public float getHeadingLeft()  { return mHeadingLeft; }
    public float getHeadingRight() { return mHeadingRight; }
    public String getName()         { return mName; }
}
