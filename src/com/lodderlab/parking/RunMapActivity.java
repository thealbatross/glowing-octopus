package com.lodderlab.parking;

import android.support.v4.app.Fragment;

public class RunMapActivity extends SingleFragmentActivity {
	// a  key for passing a run ID as a long
	public static final String EXTRA_RUN_ID = "com.lodderlab.parking.runid";
	
	@Override
	protected Fragment createFragment(){ 
		long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
		if(runId != -1){
			return RunMapFragment.newInstance(runId);
		} else {
			return new RunMapFragment();
		}
	}

}
