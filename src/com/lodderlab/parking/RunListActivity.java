package com.lodderlab.parking;

import android.support.v4.app.Fragment;

// check the book for more info about RunListFragment

public class RunListActivity extends SingleFragmentActivity {

	@Override 
	protected Fragment createFragment(){
		return new RunListFragment();
	}
}
