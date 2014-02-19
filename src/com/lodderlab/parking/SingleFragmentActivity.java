package com.lodderlab.parking;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.FrameLayout;

public abstract class SingleFragmentActivity extends FragmentActivity {
    protected static final String FRAGMENT_TAG = "SingleFragmentActivity.Fragment";
    
    //Class abstraction, causes conflict with MainActivity.java...
    //Changed Fragment -> RunFragment so as to resolve that.
    protected abstract Fragment createFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) { //Used to be public, caused conflict with MainActivity.java
        super.onCreate(savedInstanceState);
        FrameLayout fl = new FrameLayout(this);
        fl.setId(R.id.container);
        setContentView(fl);
        
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.container);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction()
                .add(R.id.container, fragment)
                .commit();
        }
    }
}
