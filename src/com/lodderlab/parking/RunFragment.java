package com.lodderlab.parking;

// find the getLoaderManager method 
// find the isTrackingRun method

import android.app.Fragment;
import android.app.LoaderManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

/**
 * Created by Joshua on 1/27/14.
 */

public class RunFragment extends Fragment
{
	private static final String ARG_RUN_ID = "RUN_ID";
	private static final String TAG = "RunFragment";
	private static final int LOAD_RUN = 0;
    private static final int LOAD_LOCATION = 1;
	private Button mStartButton, mStopButton, mMapButton;
    private TextView mStartedTextView, mLatitudeTextView, mLongitudeTextView, mAltitudeTextView, mDurationTextView;
    
    // this method is successfully implemented
    public static RunFragment newInstance(long runId){
    	// create Bundle object called args
    	Bundle args = new Bundle();
    	// pass arguments to args
    	args.putLong(ARG_RUN_ID, runId);
    	// create RunFragment object
    	RunFragment rf = new RunFragment();
    	// pass data in the args object to the rf object 
    	rf.setArguments(args);
    	// set the return to rf
    	return rf;
    }
    
    private BroadcastReceiver mLocationReceiver = new LocationReceiver(){

        @Override
        protected void onLocationReceived(Context context, Location loc){
        	if(!mRunManager.isTrackingRun(mRun)) return; 
        mLastLocation = loc;
            if (isVisible())
                updateUI();
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled){
            int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    };

    private RunManager mRunManager;

    private Run mRun;
    private Location mLastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    mRunManager = RunManager.get(getActivity());
    // check for a Run ID as an argument, and find the run
    Bundle args = getArguments();
    if (args != null){
    	// sets run id data
    	long runId = args.getLong(ARG_RUN_ID, -1);
    	// nested if
    		if (runId != -1)
    		{
    			// for now I am depreciating this setup
    		//LoaderManager lm = getLoaderManager();
    		//lm.initLoader(LOAD_RUN, args, new RunLoaderCallbacks());
    		//lm.initLoader(LOAD_LOCATION, args, new LocationLoaderCallbacks());
    			mRun = mRunManager.getRun(runId);
    			mLastLocation = mRunManager.getLastLocationForRun(runId);
    		}
    	}
    }
    // creates the UI
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mStartedTextView = (TextView)view.findViewById(R.id.run_startedTextView);
        mLatitudeTextView = (TextView)view.findViewById(R.id.run_latitudeTextView);
        mLongitudeTextView = (TextView)view.findViewById(R.id.run_longitudeTextView);
        mAltitudeTextView = (TextView)view.findViewById(R.id.run_altitudeTextView);
        mDurationTextView = (TextView)view.findViewById(R.id.run_durationTextView);

        mStartButton = (Button)view.findViewById(R.id.run_startButton);
        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mRun = mRunManager.startNewRun();
                updateUI();
            }
        });

        mStopButton = (Button)view.findViewById(R.id.run_stopButton);
        mStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick(View v){
                mRunManager.stopRun();
                updateUI();
            }
        });
        
        mMapButton = (Button)view.findViewById(R.id.run_mapButton);
        mMapButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), RunMapActivity.class);
				i.putExtra(RunMapActivity.EXTRA_RUN_ID, mRun.getId());
				startActivity(i);
			}
		});
        
        updateUI();

        return view;
    }

	public class RunLoader extends DataLoader<Run>{
		private long mRunId;
		
		public RunLoader(Context context, long runId){
			super(context);
			mRunId = runId;
		}
		
		@Override
		public Run loadInBackground(){
			return RunManager.get(getContext()).getRun(mRunId);
		}
	}
    
	private class LocationLoaderCallbacks implements LoaderCallbacks<Location>{
		@Override
		public Loader<Location> onCreateLoader(int id, Bundle args){
			return new LastLocationLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}
		@Override
		public void onLoadFinished(Loader<Location> loader, Location location){
			mLastLocation = location;
			updateUI();
		}
		@Override
		public void onLoaderReset(Loader<Location> loader){
		// do nothing
		}
	}
	
	private class RunLoaderCallbacks implements LoaderCallbacks<Run>{
		
		@Override
		public Loader<Run> onCreateLoader(int id, Bundle args){
		return new RunLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}
		
		@Override 
		public void onLoadFinished(Loader<Run> loader, Run run){
			mRun = run;
			updateUI();
		}
		
		@Override
		public void onLoaderReset(Loader<Run> loader){
			// Do nothing
		}
	}
	
    @Override
    public void onStart(){
        super.onStart();
        getActivity().registerReceiver(mLocationReceiver, new IntentFilter(RunManager.ACTION_LOCATION));
    }

    @Override
    public void onStop(){
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }

    private void updateUI(){
        boolean started = mRunManager.isTrackingRun();
        // is tracking method is inherited by the RunManager
        boolean trackingThisRun = mRunManager.isTrackingRun(mRun);
        
        if (mRun != null)
            mStartedTextView.setText(mRun.getStartDate().toString());

        int durationSeconds = 0;

        if(mRun != null && mLastLocation != null)
        {
            durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
            mLatitudeTextView.setText(Double.toString(mLastLocation.getLatitude()));
            mLongitudeTextView.setText(Double.toString(mLastLocation.getLongitude()));
            mAltitudeTextView.setText(Double.toString(mLastLocation.getAltitude()));
            mMapButton.setEnabled(true);
        }else{
        	mMapButton.setEnabled(false);
        }
        mDurationTextView.setText(Run.formatDuration(durationSeconds));

        mStartButton.setEnabled(!started);
        mStopButton.setEnabled(started && trackingThisRun);
    }
}
