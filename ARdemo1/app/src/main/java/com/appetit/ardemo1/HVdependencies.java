package com.appetit.ardemo1;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;


public abstract class HVdependencies extends Activity {

    SurfaceView cameraPreview;
    SurfaceHolder previewHolder;
    Camera camera;
    boolean inPreview;

    float[] aValues = new float[3];
    float[] mValues = new float[3];
    HorizonView horizonView;
    SensorManager sensorManager;
    LocationManager locationManager;

    Button updateAltitudeButton;
    TextView altitudeValue;

    final static String TAG = "PAAR";

    double currentAltitude;
    double pitch;
    double newAltitude;
    double changeInAltitude;
    double thetaTan;

    // Para configuracion de Camara - Heredado del CameraPreview
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    // SurfaceHolder Callback - Heredado del CameraPreview
    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview = true;
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // not used
        }
    };



    // LocationListener
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            currentAltitude = location.getAltitude();
        }

        public void onProviderDisabled(String arg0) {
            //Not Used
        }

        public void onProviderEnabled(String arg0) {
            //Not Used
        }

        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            //Not Used
        }
    };

    // updateAltitude()
    public void updateAltitude() {
        int time = 300;
        float speed = 4.5f;

        double distanceMoved = (speed*time)*0.3048;
        if(pitch != 0 && currentAltitude != 0)
        {
            thetaTan = Math.tan(pitch);
            changeInAltitude = thetaTan * distanceMoved;
            newAltitude = currentAltitude + changeInAltitude;
            altitudeValue.setText(String.valueOf(newAltitude));
        }
        else
        {
            altitudeValue.setText("Try Again");
        }
    }


    // UpdateOrientation
    public void updateOrientation(float[] values) {
        if (horizonView!= null) {
            horizonView.setBearing(values[0]);
            horizonView.setPitch(values[1]);
            horizonView.setRoll(-values[2]);
            horizonView.invalidate();
        }
    }


    // CalculateOrientation
    public float[] calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        float[] outR = new float[9];


        SensorManager.getRotationMatrix(R, null, aValues, mValues);
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X,
                SensorManager.AXIS_Z, outR);

        SensorManager.getOrientation(outR, values);
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        // pitch variable isn't empty
        pitch = values[1];
        return values;
    }

}
