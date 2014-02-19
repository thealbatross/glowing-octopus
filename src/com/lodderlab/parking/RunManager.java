package com.lodderlab.parking;

import com.lodderlab.parking.RunDatabaseHelper.LocationCursor;
import com.lodderlab.parking.RunDatabaseHelper.RunCursor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.location.Location;
import android.util.Log;

/**
 * Created by Joshua on 1/27/14.
 * 
 * Purpose: the RunManger is in charge of queuing runs and receiveing information from the database
 * 
 */

public class RunManager {
	// to be found
	private static final String TAG = "RunManager";
	// tag string for runs
    private static final String PREFS_FILE = "runs";
    // type that stores the current id of the run
    private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";
    // type that stores the location of the runner
    public static final String ACTION_LOCATION = "com.lodderlab.parking.ACTION_LOCATION";
    // to be found
    private static RunManager sRunManager;
    // field that stores the context method, still unsure about purpose....
    private Context mAppContext;
    // to be found
    private LocationManager mLocationManager;
    // to be found
    private RunDatabaseHelper mHelper;
    // to be found
    private SharedPreferences mPrefs;
    // type long associated with the method mCurrentRunId; "m" prefix denotes generation of both getter and setter for the assigned object, i think....
    private long mCurrentRunId;

    /**
     * 
     * @param appContext
     * 
     * receives field Context, Context is inherited
     * 
     * to my knowledge this method sets up all the data types and the data that they receive 
     *
     */
    private RunManager(Context appContext){
        mAppContext = appContext;
        mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
        mHelper = new RunDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mCurrentRunId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
    }

    /**
     * 
     * @param id
     * @return 
     */
    public Run getRun(long id){
    	Run run = null; 
    	RunCursor cursor = mHelper.queryRun(id);
    	cursor.moveToFirst();
    	// if you got a row, get a run
    	if (!cursor.isAfterLast())
    		run = cursor.getRun();
    	cursor.close();
    	return run;
    }
    
    public Location getLastLocationForRun(long runId){
    	Location location = null;
    	LocationCursor cursor = mHelper.queryLastLocationForRun(runId);
    	cursor.moveToFirst();
    	// if you got a row, get a location
    	if (!cursor.isAfterLast())
    		location = cursor.getLocation();
    	cursor.close();
    	return location;
    }
    
    public RunCursor queryRuns()
    {
    	return mHelper.queryRuns();
    }
    
    public Run startNewRun()
    {
        Run run = insertRun();
        startTrackingRun(run);
        return run;
    }

    public LocationCursor queryLocationsForRun(long runId){
    	return mHelper.queryLocationForRun(runId);
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
