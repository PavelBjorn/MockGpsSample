package com.room.pavelfedor.mockgpssample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MockGoogleLocationActivity extends AppCompatActivity {

    private Timer timer;
    private FusedLocationProviderClient client;
    private LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (callback != null) {
                MockGoogleLocationActivity.this.<TextView>findViewById(R.id.result).setText(
                        "Provider = " + locationResult.getLastLocation().getProvider() + "\n"
                                + "Lat = " + locationResult.getLastLocation().getLatitude() + "\n"
                                + "Lng = " + locationResult.getLastLocation().getLongitude() + "\n"
                                + "Time = " + locationResult.getLastLocation().getTime()
                );
            }
        }
    };

    public static final String MOCK_PROVIDER = "MyMockProvider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = LocationServices.getFusedLocationProviderClient(this);
    }

    public void onClick(View view) {
        Button btn = (Button) view;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    1
            );
            return;
        }

        if (client != null && btn.getText().equals("Stop Mock location")) {
            stopMockLocation();
            btn.setText("Start Mock location");
        } else if (client != null) {
            startMockLocation();
            btn.setText("Stop Mock location");
        }
    }

    public void startMockLocation() throws SecurityException {
        client.setMockMode(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(
                        MockGoogleLocationActivity.this,
                        "Mock mode enable",
                        Toast.LENGTH_LONG
                ).show();

                LocationRequest request = new LocationRequest();
                request.setInterval(1000);
                request.setFastestInterval(1000);
                client.requestLocationUpdates(request, callback, null);
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
                                try {
                                    client.setMockLocation(location).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MockGoogleLocationActivity.this, "setMockLocation", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (SecurityException e) {
                                    Toast.makeText(MockGoogleLocationActivity.this, "Mock Location Disable", Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        0L,
                        1000 * 60L
                );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(
                        MockGoogleLocationActivity.this,
                        e.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    public void stopMockLocation() throws SecurityException {
        client.setMockMode(false);
        client.removeLocationUpdates(callback);
        if (timer != null) timer.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            startMockLocation();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
