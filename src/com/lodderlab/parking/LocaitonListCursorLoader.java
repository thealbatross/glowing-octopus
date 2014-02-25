package com.lodderlab.parking;

import android.content.Context;
import android.database.Cursor;

public class LocaitonListCursorLoader extends SQLiteCursorLoader{
	private long mRunId;
	
	public LocaitonListCursorLoader(Context c, long runId){
		super(c);
		mRunId = runId;
	}

	@Override
	protected Cursor loadCursor(){
		return RunManager.get(getContext()).queryLocationsForRun(mRunId);
	}
}
