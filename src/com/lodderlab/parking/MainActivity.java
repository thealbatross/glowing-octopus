package com.lodderlab.parking;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MainActivity extends SingleFragmentActivity {
	public static final String EXTRA_RUN_ID = "com.lodderlab.parking.ACTION_LOCATION";
	
    @Override
    protected Fragment createFragment() 
    {
    	long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
    	if (runId != -1){
    		return RunFragment.newInstances(runId);
    	}
    	else {
    		 return new RunFragment(); //Changed from RunFragment(); to solve a problem
    	}       
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
    	// the id argument will be the Run ID; CursorAdapter gives us this for free
    	Intent i = new Intent(getActivity(), MainActivity.class);
    	i.putExtra(MainActivity.EXTRA_RUN_ID, id);
    	startActivity(i); 
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
