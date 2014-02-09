package com.lodderlab.parking;

import android.content.Context;
import android.location.Location;

/**
 * Created by Joshua on 2/6/14.
 */
public class TrackingLocationReceiver extends LocationReceiver {
    @Override
    protected void onLocationReceived(Context c, Location loc)
    {
        RunManager.get(c).insertLocation(loc);
    }
}
