package com.appetit.ardemo1;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class AHactivity extends HVdependencies {


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_ahactivity);

        inPreview = false;

        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
        previewHolder = cameraPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        altitudeValue = (TextView) findViewById(R.id.altitudeValue);

        updateAltitudeButton = (Button) findViewById(R.id.altitudeUpdateButton);
        updateAltitudeButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                updateAltitude();
            }

        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2,
                locationListener);


        horizonView = (HorizonView) this.findViewById(R.id.horizonView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        updateOrientation(new float[] {0, 0, 0});
    };



    private final SensorEventListener sensorEventListener = new
            SensorEventListener() {
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                        aValues = event.values;
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                        mValues = event.values;

                    updateOrientation(calculateOrientation());
                }

                public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            };

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener, accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, magField,
                SensorManager.SENSOR_DELAY_FASTEST);

        camera=Camera.open();

    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onStop();
    }


    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }
        sensorManager.unregisterListener(sensorEventListener);
        camera.release();
        camera=null;
        inPreview=false;

        super.onPause();
    }
}


