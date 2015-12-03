package com.morgadesoft.darknotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.preferences.PreferencesConstants;
import com.morgadesoft.darknotes.util.BundleBuilder;

public class NoteCaptureActivity extends ActionBarActivity  { 
	public static final String EXTRA_NOTE = "NoteCaptureActivity.EXTRA_NOTE";

	private Note note;
	private boolean theatherMode;
	private boolean keepScreenOn;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		note = (Note) getIntent().getSerializableExtra(EXTRA_NOTE);
		
		if (note==null) {
			Log.w(NoteCaptureActivity.class.getSimpleName(), "Note not present in intent extra");
			setResult(RESULT_CANCELED);
			finish();
		}
		
		setContentView(R.layout.activity_note_capture);

		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
		
		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.activity_note_capture_frame);
		if (frag==null) {
			frag = new NoteCaptureFragment();
			frag.setArguments(BundleBuilder.bundle(NoteCaptureFragment.ARG_NOTE, note));
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.activity_note_capture_frame, frag)
				.commit();
		}
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		theatherMode = pref.getBoolean(PreferencesConstants.PREFERENCE_ENABLE_THEATER_MODE, false);
		keepScreenOn = pref.getBoolean(PreferencesConstants.PREFERENCE_KEEP_SCREEN_ON, false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (theatherMode || keepScreenOn) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		if (theatherMode) {
			LayoutParams lp = getWindow().getAttributes();
			lp.screenBrightness = 0.01f;
			lp.buttonBrightness = 0.01f;
			getWindow().setAttributes(lp);
		}
	}
	
	@Override
	public void onBackPressed() {
		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.activity_note_capture_frame);
		((NoteCaptureFragment)frag).onBackPressed();
	}
	
	
}
