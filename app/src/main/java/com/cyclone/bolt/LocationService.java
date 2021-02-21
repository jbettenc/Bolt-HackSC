package com.cyclone.bolt;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.ProviderError;

public class LocationService extends Service {

    LocationTracker locTracker;
    Location prevLocation;
    float metersRun = 0;

    int startMode;       // indicates how to behave if the service is killed
    IBinder binder;      // interface for clients that bind
    boolean allowRebind; // indicates whether onRebind should be used

    public void test() {

    }
    @Override
    public void onCreate() {
        // The service is being created
        locTracker = new LocationTracker(5000, 20f, true, true, true);
        locTracker.addListener(new LocationTracker.Listener() {
            @Override
            public void onLocationFound(@NonNull Location location) {
                // Increment our meters count
                if(prevLocation != null)
                    metersRun += prevLocation.distanceTo(location);

                FirebaseCalls.pushDistance(FirebaseAuth.getInstance().getUid(), CurrentMatch.currentMatch.getMatchId(), metersRun);

                // Update our previous location to the newest location
                prevLocation = location;

                if(metersRun >= CurrentMatch.currentMatch.distance) {
                    // Make sure we send our completion time to Firebase. Also need to notify our
                    // main UI that we have completed the task, and then update it.
                    CurrentMatch.matchComplete();

                    stopSelf();
                } else {
                    CurrentMatch.b_status.setText(metersRun + "/" + CurrentMatch.currentMatch.distance);
                }
            }

            @Override
            public void onProviderError(@NonNull ProviderError providerError) {
                // Do something to tell the user that updates are failing.
                System.out.println("Provider Error: " + providerError.getMessage());
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        // This will fail if we do not get access to the user's permissions.
        locTracker.startListening(getApplicationContext());
        return startMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return allowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        locTracker.stopListening(true);
    }
}
