package com.room.pavelfedor.mockgpssample;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MockLocationActivity extends AppCompatActivity implements LocationListener {

    private Timer timer;
    private LocationManager manager;

    public static final String MOCK_PROVIDER = "MyMockProvider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void onClick(View view) {
        try {
            Button btn = (Button) view;
            if (manager != null && manager.isProviderEnabled(MOCK_PROVIDER)) {
                stopMockLocation();
                btn.setText("Start Mock location");
            } else if (manager != null) {
                startMockLocation();
                btn.setText("Stop Mock location");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Mock Location Disable", Toast.LENGTH_LONG).show();
        }
    }

    public void startMockLocation() throws SecurityException {
        manager.addTestProvider(MOCK_PROVIDER, false, false,
                false, false, true, false, false, 0, 5);
        manager.setTestProviderEnabled(MOCK_PROVIDER, true);
        manager.requestLocationUpdates(MOCK_PROVIDER, 0, 0, this);
        timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        Random random = new Random();
                        Location location = new Location(MOCK_PROVIDER);
                        location.setLatitude(random.nextInt(90) + random.nextDouble());
                        location.setLongitude(random.nextInt(90) + random.nextDouble());
                        location.setAccuracy(5);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            location.setElapsedRealtimeNanos(System.nanoTime());
                        } else {
                            try {
                                location.getClass().getField("mElapsedRealtimeNanos").set(
                                        location,
                                        System.nanoTime()
                                );
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                        location.setTime(System.currentTimeMillis());
                        manager.setTestProviderLocation(MOCK_PROVIDER, location);
                    }
                },
                0L,
                1000 * 60L
        );
    }

    public void stopMockLocation() throws SecurityException {
        manager.setTestProviderEnabled(MOCK_PROVIDER, false);
        manager.removeTestProvider(MOCK_PROVIDER);
        if (timer != null) timer.cancel();
    }


    @Override
    public void onLocationChanged(Location location) {
        this.<TextView>findViewById(R.id.result).setText(
                "Provider = " + location.getProvider() + "\n"
                        + "Lat = " + location.getLatitude() + "\n"
                        + "Lng = " + location.getLongitude() + "\n"
                        + "Time = " + location.getTime()
        );
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStop() {
        if (timer != null) timer.cancel();
        try {
            stopMockLocation();
        } catch (Throwable e) {
            Log.d("Tag", e.toString());
        }
        super.onStop();
    }
}
