/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.madisonar.iotlab;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.animation.LinearInterpolator;

import com.madisonar.iotlab.model.Building;
import com.madisonar.iotlab.util.MathUtils;
import com.madisonar.madisonar.R;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Draws a stylized compass, with text labels at the cardinal and ordinal directions, and tick
 * marks at the half-winds. The red "needles" in the display mark the current heading.
 */
public class MadisonarView extends SurfaceView {

    /** Various dimensions and other drawing-related constants. */
    private static final float NEEDLE_WIDTH = 6;
    private static final float NEEDLE_HEIGHT = 60;
    private static final int   NEEDLE_COLOR = Color.WHITE;


    private static final float TICK_WIDTH = 2;
    private static final float TICK_HEIGHT = 10;
    private static final float DIRECTION_TEXT_HEIGHT = 36.0f;
    private static final float BUILDING_TEXT_HEIGHT = 22.0f;

    private static final float BUILDING_BOX_TOP = -180.0f + DIRECTION_TEXT_HEIGHT * 1.5f;
    private static final float BUILDING_BOX_BOT = 90.0f;
    private static final float BUILDING_BOX_THICK = 12;
    private static final int[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA, Color.LTGRAY, Color.WHITE};

    /**
     * If the difference between two consecutive headings is less than this value, the canvas will
     * be redrawn immediately rather than animated.
     */
    private static final float MIN_DISTANCE_TO_ANIMATE = 15.0f;

    /** The actual heading that represents the direction that the user is facing. */
    private float mHeading;

    /**
     * Represents the heading that is currently being displayed when the view is drawn. This is
     * used during animations, to keep track of the heading that should be drawn on the current
     * frame, which may be different than the desired end point.
     */
    private float mAnimatedHeading;

    private OrientationManager mOrientation;
    private ResponseManager mResponseManager;
    private ArrayList<Building> mBuildings;

    private Building infoBuilding;
    private boolean viewInfo = false;
    private BitmapFactory mBitmapFactory;
    private Bitmap currentSlide;
    private Paint mBitmapPaint;

    private final Paint mDirectionPaint;
    private final Paint mTickPaint;
    private final Paint mBoxPaint;
    private final Paint mBoxFillPaint;
    private final Paint mBuildingLabelPaint;
    private final Paint mBuildingPointerLinePaint;


    private final Path mPath;
    private final Rect mTextBounds;
    private final NumberFormat mDistanceFormat;
    private final String[] mDirections;
    private final ValueAnimator mAnimator;

    public MadisonarView(Context context) {
        this(context, null, 0);
    }

    public MadisonarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MadisonarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Paints for directions and ticks
        mDirectionPaint = new Paint();
        mDirectionPaint.setStyle(Paint.Style.FILL);
        mDirectionPaint.setAntiAlias(true);
        mDirectionPaint.setTextSize(DIRECTION_TEXT_HEIGHT);
        mDirectionPaint.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));

        mTickPaint = new Paint();
        mTickPaint.setStyle(Paint.Style.STROKE);
        mTickPaint.setStrokeWidth(TICK_WIDTH);
        mTickPaint.setAntiAlias(true);
        mTickPaint.setColor(Color.WHITE);


        // Paints for our Buildings
        mBoxPaint = new Paint();
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BUILDING_BOX_THICK);
        mBoxPaint.setColor(colors[0]);

        mBoxFillPaint = new Paint();
        mBoxFillPaint.setStyle(Paint.Style.FILL);
        mBoxFillPaint.setStrokeWidth(BUILDING_BOX_THICK);
        mBoxFillPaint.setColor(Color.DKGRAY);

        mBuildingLabelPaint = new TextPaint();
        mBuildingLabelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBuildingLabelPaint.setAntiAlias(true);
        mBuildingLabelPaint.setColor(Color.WHITE);
        mBuildingLabelPaint.setTextSize(BUILDING_TEXT_HEIGHT);
        mBuildingLabelPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));

        mBuildingPointerLinePaint = new Paint();
        mBuildingPointerLinePaint.setStyle(Paint.Style.STROKE);
        mBuildingPointerLinePaint.setStrokeWidth(BUILDING_BOX_THICK / 2);
        mBuildingPointerLinePaint.setAntiAlias(true);
        mBuildingPointerLinePaint.setColor(Color.BLACK);


        mBitmapPaint = new Paint();
        mBitmapFactory = new BitmapFactory();

        mPath = new Path();
        mTextBounds = new Rect();

        mDistanceFormat = NumberFormat.getNumberInstance();
        mDistanceFormat.setMinimumFractionDigits(0);
        mDistanceFormat.setMaximumFractionDigits(1);

        // We use NaN to indicate that the compass is being drawn for the first
        // time, so that we can jump directly to the starting orientation
        // instead of spinning from a default value of 0.
        mAnimatedHeading = Float.NaN;

        mDirections = context.getResources().getStringArray(R.array.direction_abbreviations);

        mAnimator = new ValueAnimator();
        setupAnimator();
    }

    /**
     * Sets the instance of {@link OrientationManager} that this view will use to get the current
     * heading and location.
     *
     * @param orientationManager the instance of {@code OrientationManager} that this view will use
     */
    public void setOrientationManager(OrientationManager orientationManager) {
        mOrientation = orientationManager;
    }

    public void setResponseManager(ResponseManager responseManager) {
        mResponseManager = responseManager;
    }

    /**
     * Gets the current heading in degrees.
     *
     * @return the current heading.
     */
    public float getHeading() {
        return mHeading;
    }

    /**
     * Sets the current heading in degrees and redraws the compass. If the angle is not between 0
     * and 360, it is shifted to be in that range.
     *
     * @param degrees the current heading
     */
    public void setHeading(float degrees) {
        mHeading = com.madisonar.iotlab.util.MathUtils.mod(degrees, 360.0f);
        animateTo(mHeading);
    }


    public void setBuildingsList(ArrayList<Building> bs){
        mBuildings = bs;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!viewInfo){
            currentSlide = null;
            // The view displays 90 degrees across its width so that one 90 degree head rotation is
            // equal to one full view cycle.
            float pixelsPerDegree = getWidth() / 90.0f;
            float centerX = getWidth() / 2.0f;
            float centerY = getHeight() / 2.0f;

            canvas.save();
            canvas.translate(-mAnimatedHeading * pixelsPerDegree + centerX, centerY);

            // In order to ensure that places on a boundary close to 0 or 360 get drawn correctly, we
            // draw them three times; once to the left, once at the "true" bearing, and once to the
            // right.
            for (int i = -1; i <= 1; i++) {
                drawBuildings(canvas, pixelsPerDegree, i * pixelsPerDegree * 360);
            }

            drawCompassDirections(canvas, pixelsPerDegree);

            canvas.restore();

            mDirectionPaint.setColor(NEEDLE_COLOR);
            drawNeedle(canvas);
        }
        else{
            if (currentSlide == null){
                int resourceID = this.getResources().getIdentifier(infoBuilding.getCardRes(), "drawable", "com.madisonar.madisonar");
                currentSlide = mBitmapFactory.decodeResource(this.getResources(), resourceID);
            }
            canvas.drawBitmap(currentSlide, 0, 0, mBitmapPaint);
        }

    }

    /**
     * Draws the compass direction strings (N, NW, W, etc.).
     *
     * @param canvas the {@link android.graphics.Canvas} upon which to draw
     * @param pixelsPerDegree the size, in pixels, of one degree step
     */
    private void drawCompassDirections(Canvas canvas, float pixelsPerDegree) {
        float degreesPerTick = 360.0f / mDirections.length;

        mDirectionPaint.setColor(Color.WHITE);

        // We draw two extra ticks/labels on each side of the view so that the
        // full range is visible even when the heading is approximately 0.
        for (int i = -2; i <= mDirections.length + 2; i++) {
            if (MathUtils.mod(i, 2) == 0) {
                // Draw a text label for the even indices.
                String direction = mDirections[MathUtils.mod(i, mDirections.length)];
                mDirectionPaint.getTextBounds(direction, 0, direction.length(), mTextBounds);

                canvas.drawText(direction,
                        i * degreesPerTick * pixelsPerDegree - mTextBounds.width() / 2,
                        (canvas.getHeight() / -2) + DIRECTION_TEXT_HEIGHT, mDirectionPaint);
            } else {
                // Draw a tick mark for the odd indices.
                canvas.drawLine(i * degreesPerTick * pixelsPerDegree,
                        (canvas.getHeight() / -2) + DIRECTION_TEXT_HEIGHT / 2 - (TICK_HEIGHT / 2),
                        i * degreesPerTick * pixelsPerDegree,
                        (canvas.getHeight() / -2) + DIRECTION_TEXT_HEIGHT / 2 + (TICK_HEIGHT / 2),
                        mTickPaint);
            }
        }
    }


    private void drawBuildings(Canvas canvas, float pixelsPerDegree, float offset){
        try{
            mBuildings = mResponseManager.getCurrentResp().getBuildings();
            int colorIndex = 0;
            if (mBuildings != null){
                synchronized (mBuildings){
                    Location userLocation = mOrientation.getLocation();
                    for (Building b : mBuildings){
                        RectF toDraw;
                        //get the bounds
                        if (b.getHeadingRight() < b.getHeadingLeft()){
                            //draw the rectangles
                            toDraw = new RectF(
                                    offset + b.getHeadingLeft() * pixelsPerDegree,
                                    BUILDING_BOX_TOP,
                                    offset + (360.0f + b.getHeadingRight()) * pixelsPerDegree,
                                    BUILDING_BOX_BOT);
                        }
                        else{
                            toDraw = new RectF(
                                    offset + b.getHeadingLeft() * pixelsPerDegree,
                                    BUILDING_BOX_TOP,
                                    offset + b.getHeadingRight() * pixelsPerDegree,
                                    BUILDING_BOX_BOT);
                        }
                        //draw the boxes
                        canvas.drawRect(toDraw, mBoxPaint);
                        canvas.drawRect(toDraw, mBoxFillPaint);
                        //draw the text
                        Rect textBounds = new Rect();
                        mBuildingLabelPaint.getTextBounds(b.getName(),0, b.getName().length(), textBounds);
                        if (textBounds.width() < toDraw.width()){
                            //draw it centered.
                            canvas.drawText(b.getName(),
                                    toDraw.centerX() - ( textBounds.width() / 2 ),
                                    toDraw.centerY() + ( textBounds.height() / 2),
                                    mBuildingLabelPaint);
                        }
                        else{
                            mBuildingPointerLinePaint.setColor(colors[colorIndex]);
                            canvas.drawLine(toDraw.centerX(), toDraw.centerY(), toDraw.centerX(), toDraw.bottom + toDraw.height() / 8, mBuildingPointerLinePaint);
                            canvas.drawText(b.getName(),
                                    toDraw.centerX() - ( textBounds.width() / 2 ),
                                    toDraw.bottom + (toDraw.height() / 4) + toDraw.centerY() + ( textBounds.height() / 2),
                                    mBuildingLabelPaint);
                        }
                        colorIndex++;
                        if (colorIndex >= colors.length ){ colorIndex = 0;}
                        mBoxPaint.setColor(colors[colorIndex]);
                    }
                }
            }
        }
        catch( NullPointerException e ){
            //This happens sometimes when it launches and isn't ready. Don't worry about it, it gets fixed.
        }
    }

    /**
     * Draws a needle that is centered at the top or bottom of the compass.
     *
     * @param canvas the {@link android.graphics.Canvas} upon which to draw
     */
    private void drawNeedle(Canvas canvas) {
        float centerX = getWidth() / 2.0f;
        float origin = getHeight();
        float sign = -1;
        float needleHalfWidth = NEEDLE_WIDTH / 2;

        mPath.reset();
        mPath.moveTo(centerX - needleHalfWidth, origin);
        mPath.lineTo(centerX - needleHalfWidth, origin + sign * (NEEDLE_HEIGHT - 4));
        mPath.lineTo(centerX, origin + sign * NEEDLE_HEIGHT);
        mPath.lineTo(centerX + needleHalfWidth, origin + sign * (NEEDLE_HEIGHT - 4));
        mPath.lineTo(centerX + needleHalfWidth, origin);
        mPath.close();

        canvas.drawPath(mPath, mDirectionPaint);
    }

    /**
     * Sets up a {@link android.animation.ValueAnimator} that will be used to animate the compass
     * when the distance between two sensor events is large.
     */
    private void setupAnimator() {
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(250);

        // Notifies us at each frame of the animation so we can redraw the view.
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mAnimatedHeading = MathUtils.mod((Float) mAnimator.getAnimatedValue(), 360.0f);
                invalidate();
            }
        });

        // Notifies us when the animation is over. During an animation, the user's head may have
        // continued to move to a different orientation than the original destination angle of the
        // animation. Since we can't easily change the animation goal while it is running, we call
        // animateTo() again, which will either redraw at the new orientation (if the difference is
        // small enough), or start another animation to the new heading. This seems to produce
        // fluid results.
        mAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animator) {
                animateTo(mHeading);
            }
        });
    }

    /**
     * Animates the view to the specified heading, or simply redraws it immediately if the
     * difference between the current heading and new heading are small enough that it wouldn't be
     * noticeable.
     *
     * @param end the desired heading
     */
    private void animateTo(float end) {
        // Only act if the animator is not currently running. If the user's orientation changes
        // while the animator is running, we wait until the end of the animation to update the
        // display again, to prevent jerkiness.
        if (!mAnimator.isRunning()) {
            float start = mAnimatedHeading;
            float distance = Math.abs(end - start);
            float reverseDistance = 360.0f - distance;
            float shortest = Math.min(distance, reverseDistance);

            if (Float.isNaN(mAnimatedHeading) || shortest < MIN_DISTANCE_TO_ANIMATE) {
                // If the distance to the destination angle is small enough (or if this is the
                // first time the compass is being displayed), it will be more fluid to just redraw
                // immediately instead of doing an animation.
                mAnimatedHeading = end;
                invalidate();
            } else {
                // For larger distances (i.e., if the compass "jumps" because of sensor calibration
                // issues), we animate the effect to provide a more fluid user experience. The
                // calculation below finds the shortest distance between the two angles, which may
                // involve crossing 0/360 degrees.
                float goal;

                if (distance < reverseDistance) {
                    goal = end;
                } else if (end < start) {
                    goal = end + 360.0f;
                } else {
                    goal = end - 360.0f;
                }

                mAnimator.setFloatValues(start, goal);
                mAnimator.start();
            }
        }
    }

    public boolean viewInfo()
    {
        infoBuilding = mResponseManager.getBuildingViaHeading(mOrientation.getHeading());
        if (infoBuilding != null){
            viewInfo = true;
        }
        else {
            viewInfo = false;
        }
        return viewInfo;
    }
    public void stopViewInfo()
    {
        viewInfo = false;
        infoBuilding = null;
    }
}
