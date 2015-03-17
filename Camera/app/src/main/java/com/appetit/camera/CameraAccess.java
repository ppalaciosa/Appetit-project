package com.appetit.camera;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

// Se requiere control de:
// 1. Camara
// 2. Sensor de orientacion
// 3. Acelerometro
// 4. GPS

public class CameraAccess extends ARdependencies {

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
    };

}
