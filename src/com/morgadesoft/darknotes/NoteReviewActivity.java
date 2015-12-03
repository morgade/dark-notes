package com.morgadesoft.darknotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.morgadesoft.darknotes.NoteReviewFragment.Listener;
import com.morgadesoft.darknotes.util.BundleBuilder;

public class NoteReviewActivity extends ActionBarActivity implements Listener  {
	public static final String EXTRA_NOTE = "NoteReviewActivity.EXTRA_NOTE";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_review);

		Fragment frag = getSupportFragmentManager().findFragmentById(R.id.activity_note_review_frame_A);
		if (frag==null) {
			frag = new NoteReviewFragment();
			frag.setArguments(BundleBuilder.bundle(NoteReviewFragment.ARG_NOTE, getIntent().getSerializableExtra(EXTRA_NOTE)));
			
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.activity_note_review_frame_A, frag, null)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();
		}
	}

	@Override
	public void sendTextRequested(String text) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(intent, getString(R.string.share_reviewed_text)));
	}


}
