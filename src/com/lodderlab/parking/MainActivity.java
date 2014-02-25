package com.lodderlab.parking;


import android.support.v4.app.Fragment;
import com.lodderlab.parking.RunFragment;

public class MainActivity extends SingleFragmentActivity {
	// receives a value from ...parking.run_id
	// this value controls whether or not the RunFragment is created and runs
    public static final String EXTRA_RUN_ID	= "com.lodder.parking.run_id";
	
    // something is broken what exactly I am not sure...... some things are missing must look at book
    @Override
    protected Fragment createFragment() {
        long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
        if (runId != -1){
        	return RunFragment.newInstances(runId);
        } else {
        	return new RunFragment();
        }
    }
}