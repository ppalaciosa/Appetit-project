package com.appetit.camera;

import android.app.Activity;
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


public abstract class ARdependencies extends Activity {

// VARIABLES

// Camera variables...
    SurfaceView cameraPreview; // Maneja la referencia al XML correspondiente
    SurfaceHolder previewHolder; // Para controlar el SurfaceView
    Camera camera;
    boolean inPreview; // Indica si el Preview esta activo para tomarlo en cuenta

// Orientation Sensor variables...
    final static String TAG = "PAAR"; // Tag para los log statements
    SensorManager sensorManager; // Para obtener sensor data y administrar sensores
    // (Este sera usado tanto por OrientationSensor como por Acelerometer)

    int orientationSensor;
    float headingAngle; // angulo en sentido X
    float pitchAngle; // angulo en sentido Y
    float rollAngle; // angulo en sentido Z

// Acelerometer variables...
    int accelerometerSensor; // constante del acelerometro
    float xAxis; // aceleracion en X
    float yAxis; // aceleracion en Y
    float zAxis; // aceleracion en Z

// GPS variables...
    LocationManager locationManager;
    // Se emplea LocationManager de manera analoga a SensorManager
    double latitude;
    double longitude;
    double altitude;


// METHODS and CONSTRUCTORS

// Camera - Metodo para obtener el mejor PreviewSize
    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                }
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return (result);
    }

// Camera - Surface callback. Hay que esperar que el surfaceCallback tenga su metodo
// surfaceCreated() llamado antes de registrar la camara
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Log.e("CameraAccess", "Exception in setPreviewDisplay()", t);
            }
        }

        // Pasamos la configuration data a la camara para que esta sepa que tan grande debe dibujarse el preview
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int
                height) {
            Camera.Parameters parameters = camera.getParameters(); // Parametros de Camara
            Camera.Size size = getBestPreviewSize(width, height, parameters); // Parametros de equipo

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height); // Asignando...
                camera.setParameters(parameters); // ...
                camera.startPreview(); // Show preview
                inPreview = true; // Cambiamos nuestro indicador booleano a ON
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // not used
        }
    };


// (OrientationSensor + Acelerometer) - Definiendo objeto sensorEventListener
// (que es usado en onCreate, onResume, etc)
    final SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {  // Recibir updates del sensor

            // Eventos de orientation sensor
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) { // filtrar solo orientation sensor
                // almacenar sus valores
                headingAngle = sensorEvent.values[0];
                pitchAngle = sensorEvent.values[1];
                rollAngle = sensorEvent.values[2];

                // Console log
                Log.d(TAG, "Heading: " + String.valueOf(headingAngle));
                Log.d(TAG, "Pitch: " + String.valueOf(pitchAngle));
                Log.d(TAG, "Roll: " + String.valueOf(rollAngle));
            }

            // Eventos de acelerometro
            else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                xAxis = sensorEvent.values[0];
                yAxis = sensorEvent.values[1];
                zAxis = sensorEvent.values[2];
                Log.d(TAG, "X Axis: " + String.valueOf(xAxis));
                Log.d(TAG, "Y Axis: " + String.valueOf(yAxis));
                Log.d(TAG, "Z Axis: " + String.valueOf(zAxis));
            }
        }

        public void onAccuracyChanged(Sensor senor, int accuracy) {
            //Not used
        }
    };

// GPS - Definiendo objeto locationListener
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Este metodo es invocado con frecuencia dada por el sampling rate (2s)

            // GPS parameters...
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            altitude = location.getAltitude();
            // Console Log
            Log.d(TAG, "Latitude: " + String.valueOf(latitude));
            Log.d(TAG, "Longitude: " + String.valueOf(longitude));
            Log.d(TAG, "Altitude: " + String.valueOf(altitude));
        }
        public void onProviderDisabled(String arg0) {
            // TOD Auto-generated method stub
        }
        public void onProviderEnabled(String arg0) {
            // TOD Auto-generated method stub
        }
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TOD Auto-generated method stub
        }
    };

}


