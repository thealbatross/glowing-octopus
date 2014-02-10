package com.lodderlab.parking;

public class RunListActivity extends SingleFragmentActivity {

	@Override 
	protected Fragment createFragment(){
		return new RunListFragment();
	}
}
