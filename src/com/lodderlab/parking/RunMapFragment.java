package com.lodderlab.parking;

import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.lodderlab.parking.RunDatabaseHelper.LocationCursor;

public class RunMapFragment extends SupportMapFragment implements LoaderCallbacks<Cursor>{
	private static final String ARG_RUN_ID = "ID_RUN";
	private static final int LOAD_LOCATIONS = 0;
	private LocationCursor mLocationCursor;
	private GoogleMap mGoogleMap;
	
	public static RunMapFragment newInstance(long runId){
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunMapFragment rf = new RunMapFragment();
		rf.setArguments(args);
		return rf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// check for a run id as an argument, and find the run
		Bundle args = getArguments();
		if (args != null){
			long runId = args.getLong(ARG_RUN_ID, -1);
			if (runId != -1){
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_LOCATIONS, args, this);
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle SavedInstanceState){
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		// stash a reference to the GoogleMap
		mGoogleMap = getMap();
		// show the user's location
		mGoogleMap.setMyLocationEnabled(true);
		return v;
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args){
		long runId = args.getLong(ARG_RUN_ID, -1);
		return new LocationListCursorLoader(getActivity(), runId);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
		mLocationCursor = (LocationCursor)cursor;
		updateUI();
	}
	
	@Override
	public void onLoadReset(Loader<Cursor> loader){
		mLocationCursor = (LocationCursor)cursor;
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader){
		//stop using the data
		mLocationCursor.close();
		mLocationCursor = null;
	}
	
	private void updateUI(){
		if (mGoogleMap == null || mLocationCursor == null)
			return;
			
			// setup an overly on the map for this run's location
			// create a polyline with all the points
			PolylineOptions line = new PolyLineOptions();
			// also create a LatLngBounds so that you can zoom to fit
			LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
			//iterate over the Locations
			mLocationCursor.moveToFirst();
			while(!mLocationCursor.isAfterLast()){
				Location loc = mLocationCursor.getLocation();
				LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
			
				Resources r = getResources();
				
				//if this is the location, add a marker for it
				if (mLocationCursor.isFirst()){
				String startDate = new Date(loc.getTime()).toString();
				MarkerOptions startMarkerOptions = new MarkerOptions().position(latLng).title(r.getString(R.string.run_start)).snippet(r.getString(R.string.runStarted_at_format, startDate));
				mGoogleMap.addMarker(startMarkerOptions);
				} else if (mLocationCursor.isLast()){
					// if this is the last location, and not also the first, add a marker
					String endDate = new Date(loc.getTime()).toString();
					MakerOptions finishMakerOptions = new MarkerOptions()
					.postion(latLng)
					.title(r.getString(R.string.run_finished))
					.snippet(r.getString(R.string.run_finished_at_format, endDate));
					mGoogleMap.addMarker(finishMarkerOptions);
				
				line.add(latLng);
				latLngBuilder.include(latLng);
				mLocationCursor.moveToNext();
			}
		// make the map zoom to show the track, with some padding
		// use the size of the current display in pixels as a boudning box
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		// construct a movement instruction  for the map camera
		LatLngBounds latLngBounds = latLngBuilder.build();
		CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBounds, display.getWidth(), display.getHeight(), 15);
		mGoogleMap.moveCamera(movement);
		}
	}
}
