package com.madisonar.iotlab;

import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.madisonar.iotlab.model.Landmarks;

/**
 * Created by sielicki on 12/1/14.
 */
public class MadisonarRenderer implements DirectRenderingCallback {

    private SurfaceHolder mHolder;
    private RenderThread mRenderThread;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private final FrameLayout mLayout;
    private final CompassView mCompassView;
    private final RelativeLayout mTipsContainer;
    private final TextView mTipsView;
    private final OrientationManager mOrientationManager;
    private final ResponseManager mResponseManager;
    private final Landmarks mLandmarks;

    @Override
    public void renderingPaused(SurfaceHolder surfaceHolder, boolean b) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
