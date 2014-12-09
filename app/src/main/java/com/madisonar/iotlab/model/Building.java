package com.madisonar.iotlab.model;

import android.graphics.drawable.Drawable;
import android.view.SurfaceView;
import android.view.View;

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
    private final String mCardRes;

    public Building(double latL, double longL,
                    double latR, double longR,
                    float headR, float headL,
                    String name, String cardRes)
    {
        mLatitudeLeft = latL;
        mLongitudeLeft = longL;
        mLatitudeRight = latR;
        mLongitudeRight = longR;
        mHeadingLeft = headL;
        mHeadingRight = headR;
        mName = name;
        mCardRes = cardRes;
    }

    public double[] getCoordsRight(){ double[] toReturn = {mLatitudeRight, mLongitudeRight}; return toReturn; }
    public double[] getCoordsLeft() { double[] toReturn = {mLatitudeLeft, mLongitudeLeft}; return toReturn; }
    public float getHeadingLeft()  { return mHeadingLeft; }
    public float getHeadingRight() { return mHeadingRight; }
    public String getName()         { return mName; }
    public String getCardRes()      { return mCardRes; }
}
