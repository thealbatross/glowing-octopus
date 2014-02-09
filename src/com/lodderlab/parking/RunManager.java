package com.lodderlab.parking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.Location;
import android.util.Log;

/**
 * Created by Joshua on 1/27/14.
 */
public class RunManager {
    private static final String TAG = "RunManager";

    private static final String PREFS_FILE = "runs";
    private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";

    public static final String ACTION_LOCATION = "com.bignerdranch.android.runtracker.ACTION_LOCATION";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;
    private RunDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentRunId;

    private RunManager(Context appContext){
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
        mHelper = new RunDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentRunId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
    }

    public Run startNewRun()
    {
        Run run = insertRun();
        startTrackingRun(run);
        return run;
    }

    public void startTrackingRun(Run run)
    {
        mCurrentRunId = run.getId();
        mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).commit();
        startLocationUpdates();
    }

    public void stopRun()
    {
        stopLocationUpdates();
        mCurrentRunId = -1;
        mPrefs.edit().remove(PREF_CURRENT_RUN_ID).commit();
    }

    private Run insertRun()
    {
        Run run = new Run();
        run.setId(mHelper.insertRun(run));
        return run;
    }

    public void insertLocation(Location loc)
    {
        if (mCurrentRunId != -1)
        {
            mHelper.insertLocation(mCurrentRunId, loc);
        }
        else
        {
            Log.e(TAG, "Location received with no tracking run; ignoring.");
        }
    }

    public static RunManager get(Context c){
        if (sRunManager == null){
            sRunManager = new RunManager(c.getApplicationContext());
        }
        return sRunManager;
    }
    private PendingIntent getLocationPendingIntent(boolean shouldCreate){
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }


    public void startLocationUpdates(){
        String provider = LocationManager.GPS_PROVIDER;

        //Start updates from the LocationManager
        Location lastKnow = mLocationManager.getLastKnownLocation((provider));
        if (lastKnow != null)
        {
            //Reset the time to know
            lastKnow.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnow);
        }

        PendingIntent pi = getLocationPendingIntent(true);
        mLocationManager.requestLocationUpdates(provider,0, 0, pi);
        }

    private void broadcastLocation(Location location)
    {
        Intent broadcasts = new Intent(ACTION_LOCATION);
        broadcasts.putExtra(LocationManager.KEY_LOCATION_CHANGED,location);
        mAppContext.sendBroadcast(broadcasts);
    }

    public void stopLocationUpdates(){
        String provider = LocationManager.GPS_PROVIDER;

        //Start updates from the LocationManager
        PendingIntent pi = getLocationPendingIntent(false);
        if(pi != null){
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingRun(){
        return getLocationPendingIntent(false) != null;
    }
}
