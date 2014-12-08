package com.madisonar.iotlab;

import com.google.android.glass.widget.CardScrollView;
import com.madisonar.madisonar.R;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MadisonarActivity extends Activity {

    private OrientationManager mOrientationManager;
    private ResponseManager mResponseManager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        SensorManager sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mOrientationManager = new OrientationManager(sensorManager, locationManager);
        mResponseManager = new ResponseManager(this.getApplicationContext(), locationManager, mOrientationManager);
        mResponseManager.forceUpdateCurrentRespTask();
    }

    @Override
    protected void onStart(){
        super.onStart();

        setContentView(R.layout.compass);

        MadisonarView mMadisonarView = (MadisonarView) findViewById(R.id.compass);

        MadisonarRenderer madisonarRenderer =
                new MadisonarRenderer(this, mOrientationManager, mResponseManager, mMadisonarView);

        mMadisonarView.setWillNotDraw(false);
        mMadisonarView.setKeepScreenOn(true);
        mMadisonarView.getHolder().addCallback(madisonarRenderer);
    }

    @Override
    protected void onPause() {
        super.onDestroy();
    }
}
