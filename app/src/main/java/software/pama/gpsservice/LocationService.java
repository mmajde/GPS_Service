package software.pama.gpsservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service implements LocationListener
{
    public static final String BROADCAST_ACTION = "Hello World";
    private static final int TWO_MINUTES = 100;
    public LocationManager locationManager;
    public Location previousBestLocation = null;
    public Location mCurrentLocation = null;
    private Handler handler = new Handler();
    private Toast mToast;

    Intent intent;

    @Override
    public void onCreate()
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    public void runLocationService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        //getLastKnownLocation();
        mToast= Toast.makeText(getApplicationContext(), " ", Toast.LENGTH_LONG);
        showLocation.run();
    }

    private void getLastKnownLocation() {
        if(mCurrentLocation == null) {
            String list[] = {
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER };
            for (String s : list) {
                Location tmp = locationManager.getLastKnownLocation(s);
                if (mCurrentLocation == null || tmp.getTime() > mCurrentLocation.getTime()) {
                    mCurrentLocation = tmp;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(this);
        handler.removeCallbacksAndMessages(showLocation);
        stopSelf();
        super.onDestroy();
    }

    private Runnable showLocation = new Runnable() {
        @Override
        public void run() {
            if(mCurrentLocation != null) {
                mToast.setText(mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
                mToast.show();
            }
            handler.postDelayed(this, 1000);
        }
    };

    public void onLocationChanged(final Location loc)
    {
        Log.i("**************************************", "Location changed");
        mCurrentLocation = loc;
    }

    public void onProviderDisabled(String provider)
    {
        Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
    }


    public void onProviderEnabled(String provider)
    {
        Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
    }


    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.d(SyncStateContract.Constants.DATA, provider + " \nSTATUS: " + status);
    }

    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public LocationService getLocationService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Location getmCurrentLocation() {
        return mCurrentLocation;
    }
//    }
}

// --------------------------------- old version -------------------------------------------------

//
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.Intent;
//import android.location.Location;
//import android.os.Binder;
//import android.os.Handler;
//import android.os.IBinder;
//import android.provider.SyncStateContract;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//import android.widget.Toast;
//
//import com.google.android.gms.maps.GoogleMap;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
//public class LocationService extends Service  implements GoogleMap.OnMyLocationChangeListener {
//
//    private IBinder mBinder = new LocalBinder();
//    private int distance = 0;
//    private Toast myToast;
//    private GoogleMap mMap;
//    private Location location;
//    private Timer updatingTimer;
//    private Handler handler;
//    private TimerTask notify = new TimerTask() {
//        @Override
//        public void run() {
//            myToast.setText("Usługa ciągle działa");
//            myToast.show();
//        }
//    };
//
//    public LocationService() {
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        updatingTimer = new Timer();
//        myToast = Toast.makeText(getApplicationContext(), "Usługa została uruchomiona", Toast.LENGTH_SHORT);
//        myToast.show();
//    }
//
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
////        Toast.makeText(this, "Service ruszył", Toast.LENGTH_LONG).show();
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
//        builder.setTicker("Gps service ruszył");
//        builder.setContentIntent(pi);
//        builder.setContentTitle("GpsService");
//        builder.setContentText("Tracking");
//        builder.setOngoing(true);
//        startForeground(1, builder.build());
//
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        updatingTimer.cancel();
//        Toast.makeText(this, "Service zatrzymany w onDestroy", Toast.LENGTH_SHORT).show();
//        super.onDestroy();
//    }
//
//    @Override
//    public void onMyLocationChange(Location location) {
//        distance++;
//        Log.i(SyncStateContract.Constants.DATA, distance + "");
//    }
//
//    public void setOnLocationChangeListener(final GoogleMap mMap) {
//        this.mMap = mMap;
//        //updatingTimer.scheduleAtFixedRate(notify, 3*1000, 3*1000);
////        Log.i(SyncStateContract.Constants.DATA, "Ustawiony listener");
////        this.mMap.setOnMyLocationChangeListener(LocationService.this);
//
//        handler=new Handler();
//        final Runnable r = new Runnable()
//        {
//            public void run()
//            {
//                myToast.setText(mMap.getMyLocation().getLatitude() + "");
//                myToast.show();
//                handler.postDelayed(this, 3000);
//            }
//        };
//        handler.postDelayed(r, 3000);
//
//    }
//
//    public class LocalBinder extends Binder {
//        public LocationService getService() {
//            return LocationService.this;
//        }
//    }
//}
