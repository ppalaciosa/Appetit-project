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

// Se requiere control de:
// 1. Camara
// 2. Sensor de orientacion
// 3. Acelerometro


public class CameraAccess extends Activity {

    // Camera variables...
    SurfaceView cameraPreview; // Maneja la referencia al XML correspondiente
    SurfaceHolder previewHolder; // Para controlar el SurfaceView
    Camera camera;
    boolean inPreview; // Indica si el Preview esta activo para tomarlo en cuenta

    // Orientation Sensor variables...
    final static String TAG = "PAAR"; // Tag para los log statements
    SensorManager sensorManager; // Para obtener sensor data y administrar sensores

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
    double latitude;
    double longitude;
    double altitude;

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

// Definiendo objeto sensorEventListener (que es usado en onCreate, onResume, etc)
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

// Definiendo objeto locationListener...
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

// Location Manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // LocationManager es un servicio del sistema al cual llamamos
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2,locationListener);
        // Solicitamos updates. Proveedor / 2s sampling / min_distance(m) to register / donde notificar

// Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // SensorManager es un servicio del sistema al cual llamamos

        // Orientation Sensor
        orientationSensor = Sensor.TYPE_ORIENTATION; // Asignando la constante del orientation sensor
        // Acelerometer Sensor
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;

        // Sensor Listener para Orientacion
        sensorManager.registerListener(sensorEventListener,  // Registramos nuestro SensorEventListener
                sensorManager.getDefaultSensor(orientationSensor), // para el orientation sensor por default
                SensorManager.SENSOR_DELAY_NORMAL); // con el delay normal, ideal para cambios de UI
                // indica que tan seguido se debe actualizar el sensor. Influye en el consumo de energia y CPU

        // Sensor Listener para Acelerometro
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);

// Camera
        inPreview = false; // No display right now

        cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview); // Asociando al XML con id cameraPreview
        previewHolder = cameraPreview.getHolder(); // Asociando Holder
        previewHolder.addCallback(surfaceCallback); // Registrando Callback para recibir
        // notificaciones de SurfaceView
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // para que algo en el sistema
        // provea bitmaps que actualicen el SurfaceView

    }

    @Override
    public void onResume() {
        super.onResume();
        // recuperar GPS
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
                2, locationListener);

        // recuperar orientation sensor
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);

        // recuperar acelerometro
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);

        // recuperar camara
        camera=Camera.open();

    }


    @Override
    public void onPause() {
        // Pausa camara
        if (inPreview) {
            camera.stopPreview();
        }
        camera.release();
        camera = null;
        inPreview = false;

        // Desactivar GPS
        locationManager.removeUpdates(locationListener);

        // Desactivar sensorManager
        sensorManager.unregisterListener(sensorEventListener);

        super.onPause();
    }


    // Surface callback. Hay que esperar que el surfaceCallback tenga su metodo surfaceCreated() llamado antes de
    // registrar la camara
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
}
