package software.pama.gpsservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    // Connection
    private LocationService mService;
    private boolean mBound = false;
    private Location mCurrentLocation;
    // Linia rysowana na mapie
    private PolylineOptions line;
    // Przebyty dystans
    private float distance = 0;
    //private LocationBroadcastReceiver locationBroadcastReceiver = new LocationBroadcastReceiver();

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(mService == null) {
                mBound = true;
                LocationService.LocalBinder binder = (LocationService.LocalBinder) iBinder;
                mService = binder.getLocationService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
//            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        line = new PolylineOptions();

        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        final Button runService = (Button) findViewById(R.id.btnRunService);
        runService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mBound) {
                    mService.runLocationService();
                }
                //getApplicationContext().registerReceiver(locationBroadcastReceiver, new IntentFilter("Hello World"));
            }
        });

        final Button drawTrack = (Button) findViewById(R.id.btnDrawTrack);
        drawTrack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBound)
                    countAndDrawDistance(mService.getmCurrentLocation());
            }
        });

//        final Button getDistance = (Button) findViewById(R.id.btnGetDistance);
//        getDistance.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mCurrentLocation =
//                        mService.getmCurrentLocation() != null ?  mService.getmCurrentLocation() : mCurrentLocation;
//                if(mCurrentLocation != null) {
//                    Toast.makeText(
//                            getApplicationContext(),
//                            mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude(),
//                            Toast.LENGTH_LONG).show();
//                }
////                if(mService == null)
////                    Log.i(SyncStateContract.Constants.DATA, "service nie dziala");
////                if(mMap == null)
////                    Log.i(SyncStateContract.Constants.DATA, "mapa nie dziala");
////                mService.setOnLocationChangeListener(mMap);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        //getApplicationContext().unregisterReceiver(locationBroadcastReceiver);
        super.onDestroy();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
    }
//
//    public class LocationBroadcastReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            double latitude = intent.getDoubleExtra("Latitude", 0);
//            double longitude = intent.getDoubleExtra("Longitude", 0);
//            Toast.makeText(context, "Received intent. " + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
//        }
//    }

    /**
     * Wylicza dystans oraz rysuje linię pomiędzy punktami
     *
     * @param location
     */
    private void countAndDrawDistance(Location location) {
        if(mCurrentLocation == null) {
            mCurrentLocation = location;
            return;
        }
        if(location == null) {
            return;
        }
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        float dist = mCurrentLocation.distanceTo(location);
        // Jeśli wiekszy niż 4m
        if(dist > 0.0) {
            drawLine(latlng);
            distance += dist;
            TextView txtDistance = (TextView) findViewById(R.id.txtDistance);
            txtDistance.setText(Float.toString(distance) + " m");
            mCurrentLocation = location;
        }
    }

    /**
     * Rysuje linię na podstawie parametru LatLng
     *
     * @param latlng - odcinek pomiędzy dwoma lokalizacjami
     */
    public void drawLine(LatLng latlng) {
        // Czyścimy mapę
        mMap.clear();
        // Rysujemy calą linię z dodanym nowym punktem
        mMap.addPolyline(line.add(latlng));
    }
}
