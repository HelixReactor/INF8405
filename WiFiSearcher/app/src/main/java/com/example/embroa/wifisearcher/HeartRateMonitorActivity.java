package com.example.embroa.wifisearcher;

/**
 * Created by Emilie on 2018-04-13.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HeartRateMonitorActivity extends Activity {
    private SensorManager sensorManager;
    private Sensor heartRateSensor;
    private TextView textView;
    private int timesChanged = 0;
    private String heartRateValue;
    private boolean isDone = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_monitor);
        textView = (TextView) findViewById(R.id.text_heartRate);
        textView.setText("");
        startMonitor();
        findViewById(R.id.okBtn).setVisibility(View.INVISIBLE);

        final IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BroadcastReceiver batteryLevelReceiver;

        BatteryHistory.initHistory("HeartRateActivity");
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BatteryHistory.callbackOnReceive(intent, getProjectDB());
            }
        };

        registerReceiver(batteryLevelReceiver, batteryLevelFilter);

        findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMonitor();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        BatteryHistory.initHistory("HeartRateActivity");
    }

    protected void startMonitor() {
        textView.setText("");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if(heartRateSensor != null)
            textView.setText("Appuyer votre doigt sur le capteur près de la caméra arrière.");
        else
            textView.setText("Erreur: capteur indisponible.");

        sensorManager.registerListener(sensorEventListener, heartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }
    protected void stopMonitor() {
        sensorManager.unregisterListener(sensorEventListener, heartRateSensor);
        Intent nextIntent = null;
        Intent previousIntent = getIntent();

        Boolean isLast = previousIntent.getBooleanExtra("isLast", false);
        Intent returnIntent = new Intent();
        if (isLast){
            // Return last measure
            returnIntent.putExtra("lastHeartRate",heartRateValue);
        } else {
            // Return first measure
            returnIntent.putExtra("firstHeartRate",heartRateValue);
        }
        setResult(Activity.RESULT_OK,returnIntent);
        finish();

    }

    public SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                float heartRate = event.values[0];
                if(!isDone) {
                    if (heartRate == 0) {
                        textView.setTextSize(24);
                        textView.setText("Calcul en cours.\nGardez votre doigt sur le capteur");
                        timesChanged = 0;
                    } else {
                        textView.setText(""+(int) heartRate);
                        textView.setTextSize(64);
                        timesChanged++;

                        if (timesChanged > 10) {
                            heartRateValue = Float.toString(heartRate);
                            isDone = true;
                            findViewById(R.id.okBtn).setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
    };

    public SQLiteDatabase getProjectDB() {
        return openOrCreateDatabase("INF8405", MODE_PRIVATE, null);
    }
}
