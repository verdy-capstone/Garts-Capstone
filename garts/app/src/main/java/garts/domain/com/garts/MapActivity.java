package garts.domain.com.garts;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

import garts.domain.com.garts.utils.Configs;
import garts.domain.com.garts.R;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String CHOSEN_LOCATION_EXTRA_KEY = "CHOSEN_LOCATION_EXTRA_KEY";

    /* Views */
    private GoogleMap mapView;

    /* Variables */
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Init Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.msMapView);
        mapFragment.getMapAsync(this);
        MapsInitializer.initialize(this);

        // MARK: - CLOSE BUTTON ------------------------------------
        Button closeButt = findViewById(R.id.msCloseButt);
        closeButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // MARK: - OK BUTTON ------------------------------------
        Button okButt = findViewById(R.id.msOkButt);
        okButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latitude != 0) {
                    // Set chosenLocation's coordinates
                    Location chosenLocation = new Location("provider");
                    chosenLocation.setLatitude(latitude);
                    chosenLocation.setLongitude(longitude);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(CHOSEN_LOCATION_EXTRA_KEY, chosenLocation);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    // No Location set
                    Configs.simpleAlert(getString(R.string.map_description), MapActivity.this);
                }
            }
        });
    }

    // MARK: - ON MAP READY ----------------------------------------------------------------
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mapView = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // MapView settings
        mapView.setMyLocationEnabled(false);
        mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Zoom the Google Map to Deafult Location
        mapView.moveCamera(CameraUpdateFactory.newLatLng(Configs.DEFAULT_LOCATION));
        mapView.animateCamera(CameraUpdateFactory.zoomTo(20));


        // Move Map to change Location's coordinates
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                CameraPosition cp = googleMap.getCameraPosition();
                Log.i("log-", "NEW LATITUDE: " + String.valueOf(cp.target.latitude));
                Log.i("log-", "NEW LONGITUDE: " + String.valueOf(cp.target.longitude));

                latitude = cp.target.latitude;
                longitude = cp.target.longitude;
            }
        });
    }
}
