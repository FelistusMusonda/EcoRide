package com.example.ecoride;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class NavigationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 3001;

    private WebView navigationMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean mapPageLoaded = false;
    private boolean routeStartedFromLiveLocation = false;
    private boolean fallbackRouteShown = false;

    private double fromLat;
    private double fromLon;
    private double toLat;
    private double toLon;
    private String fromName;
    private String toName;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        navigationMap = findViewById(R.id.navigation_map);
        Button btnEndNavigation = findViewById(R.id.btn_end_navigation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        readRouteExtras();
        setupLocationCallback();
        setupMap();

        btnEndNavigation.setOnClickListener(v -> finish());
    }

    private void readRouteExtras() {
        fromLat = getIntent().getDoubleExtra("from_lat", 0);
        fromLon = getIntent().getDoubleExtra("from_lon", 0);
        toLat = getIntent().getDoubleExtra("to_lat", 0);
        toLon = getIntent().getDoubleExtra("to_lon", 0);
        fromName = getIntent().getStringExtra("from_name");
        toName = getIntent().getStringExtra("to_name");
        mode = getIntent().getStringExtra("mode");

        if (fromName == null) {
            fromName = "Start";
        }
        if (toName == null) {
            toName = "Destination";
        }
        if (mode == null) {
            mode = "walking";
        }
    }

    private void setupMap() {
        WebSettings settings = navigationMap.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        navigationMap.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mapPageLoaded = true;
                startNavigationMode();
                enableLiveLocation();
            }
        });
        navigationMap.setWebChromeClient(new WebChromeClient());
        navigationMap.loadUrl("file:///android_asset/map.html");
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateCurrentLocationMarker(location);
                    showRouteFromLiveLocation(location);
                }
            }
        };
    }

    private void showRoute() {
        if (!mapPageLoaded || fromLat == 0 || toLat == 0) {
            return;
        }

        String js = String.format(java.util.Locale.US,
                "setRoute(%f,%f,%f,%f,%s,%s,%s)",
                fromLat,
                fromLon,
                toLat,
                toLon,
                quoteForJavaScript(fromName),
                quoteForJavaScript(toName),
                quoteForJavaScript(mode));
        navigationMap.evaluateJavascript(js, null);
    }

    private void showRouteFromLiveLocation(Location location) {
        if (!mapPageLoaded || routeStartedFromLiveLocation || toLat == 0) {
            return;
        }

        routeStartedFromLiveLocation = true;
        String js = String.format(java.util.Locale.US,
                "setRoute(%f,%f,%f,%f,%s,%s,%s)",
                location.getLatitude(),
                location.getLongitude(),
                toLat,
                toLon,
                quoteForJavaScript("Your location"),
                quoteForJavaScript(toName),
                quoteForJavaScript(mode));
        navigationMap.evaluateJavascript(js, null);
    }

    private void showFallbackRoute() {
        if (!routeStartedFromLiveLocation && !fallbackRouteShown) {
            fallbackRouteShown = true;
            showRoute();
        }
    }

    private void startNavigationMode() {
        if (mapPageLoaded && navigationMap != null) {
            navigationMap.evaluateJavascript("startNavigationMode()", null);
        }
    }

    private void enableLiveLocation() {
        if (!hasLocationPermission()) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    updateCurrentLocationMarker(location);
                    showRouteFromLiveLocation(location);
                } else {
                    showFallbackRoute();
                }
            });
            startLiveLocationUpdates();
        } catch (SecurityException ignored) {
        }
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLiveLocationUpdates() {
        if (!hasLocationPermission()) {
            return;
        }

        LocationRequest request = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException ignored) {
        }
    }

    private void updateCurrentLocationMarker(Location location) {
        if (!mapPageLoaded || navigationMap == null) {
            return;
        }

        double speedKmh = location.hasSpeed() ? location.getSpeed() * 3.6 : 0;
        double bearing = location.hasBearing() ? location.getBearing() : 0;
        String js = String.format(java.util.Locale.US,
                "updateLocationFromAndroid(%f,%f,%f,'%.1f',%f)",
                location.getLatitude(),
                location.getLongitude(),
                location.hasAccuracy() ? location.getAccuracy() : 20,
                speedKmh,
                bearing);
        navigationMap.evaluateJavascript(js, null);
    }

    private String quoteForJavaScript(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            if (granted) {
                enableLiveLocation();
            } else {
                showFallbackRoute();
                Toast.makeText(this, "Location permission is needed for live navigation.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLocationPermission()) {
            startLiveLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        if (navigationMap != null) {
            navigationMap.destroy();
            navigationMap = null;
        }
        super.onDestroy();
    }
}
