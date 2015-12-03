package com.morgadesoft.darknotes;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.morgadesoft.darknotes.exception.DarkNotesExceptionHandler;
import com.morgadesoft.darknotes.io.NotePiecesFileWriter;
import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.PathNotePiece;
import com.morgadesoft.darknotes.preferences.PreferencesConstants;
import com.morgadesoft.darknotes.ui.NotePieceCaptureView;
import com.morgadesoft.darknotes.ui.OnNoteCaptureListener;
import com.morgadesoft.darknotes.util.ExecutorTask;

public class NoteCaptureFragment extends Fragment implements OnNoteCaptureListener {
	public static final String ARG_NOTE = "ARG_NOTE";

	private Note note;
	private NotePieceCaptureView captureView;
	private NotePiecesFileWriter notePiecesWriter;
	private ProgressBar progressBar;

	private int pendingTasks;
	private boolean backPressed;
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		note = (Note) getArguments().getSerializable(ARG_NOTE);
		
		try {
			this.notePiecesWriter = new NotePiecesFileWriter(getActivity(), note);
		} catch (IOException e) {
			DarkNotesExceptionHandler.handleRecoverableException(getActivity(), e);
			getActivity().setResult(Activity.RESULT_CANCELED);
			getActivity().finish();
		}
		
		this.backPressed = false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_note_capture, null);
		
		this.captureView = (NotePieceCaptureView) view.findViewById(R.id.activity_note_capture_capture_view);
		this.captureView.setOnNotePieceCaptureListener(this);
		
		this.progressBar = (ProgressBar) view.findViewById(R.id.activity_note_capture_progress_bar);
		goFullScreen();
		
		return view;
	}

	@Override
	public void onStop() {
		if (!backPressed) {
			captureView.forceCapture();
		}
		super.onStop();
	}
	
	@TargetApi(19)
	private void goFullScreen() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean dim = prefs.getBoolean(PreferencesConstants.PREFERENCE_DIM_NAVIGATION_BUTTONS, false);
		dim = dim | prefs.getBoolean(PreferencesConstants.PREFERENCE_ENABLE_THEATER_MODE, true);
		
		int DIM_NAV = dim ? View.SYSTEM_UI_FLAG_LOW_PROFILE : 0;
		
		if (Build.VERSION.SDK_INT >= 19) {
			View decorView = getActivity().getWindow().getDecorView();
//			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
			decorView.setSystemUiVisibility(DIM_NAV | View.SYSTEM_UI_FLAG_FULLSCREEN);
		} else if (Build.VERSION.SDK_INT >= 16) {
			View decorView = getActivity().getWindow().getDecorView();
			decorView.setSystemUiVisibility(DIM_NAV | View.SYSTEM_UI_FLAG_FULLSCREEN);
		} else if (Build.VERSION.SDK_INT >= 14) {
			View decorView = getActivity().getWindow().getDecorView();
			decorView.setSystemUiVisibility(DIM_NAV | WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else if (Build.VERSION.SDK_INT >= 11) {
			View decorView = getActivity().getWindow().getDecorView();
			decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
	}
	
	@Override
	public void pieceCaptured(NotePiece notePiece) {
		if ((notePiece instanceof PathNotePiece) && notePiece.getWidth()==0 && notePiece.getHeight()==0) {
			return;
		}

		pendingTasks++;
		new ExecutorTask<NotePiece, Object, Object>() {
			@Override protected Object doInBackground(NotePiece... pieces) {
				try {
					notePiecesWriter.writePiece(pieces[0]);
					return pieces[0];
				} catch (Throwable e) {
					return e;
				}
			}
			
			@Override protected void onPostExecute(Object result) {
				pendingTasks--;
				if (backPressed) {
					if (pendingTasks==0) {
						niceFinish();
					} else {
						progressBar.setProgress(progressBar.getProgress()+1);
					}
				}
			}
		}.executeOnExecutor(executor, notePiece);
	}

	public void onBackPressed() {
		this.captureView.finishCapture();
		
		if (backPressed) {
			Toast.makeText(getActivity(), R.string.saving_note_, Toast.LENGTH_LONG).show();
			return;
		}
		
		backPressed = true;
		executor.shutdown();
		
		if (pendingTasks==0) {
			niceFinish();
		} else {
			// Esperar até o fim das pending tasks
			this.progressBar.setVisibility(View.VISIBLE);
			this.progressBar.setMax(pendingTasks);
			this.progressBar.setProgress(1);
		}
	}

	private void niceFinish() {
		note.setPieceCount(note.getPieceCount()+this.notePiecesWriter.getWriteCount());
		Intent result = new Intent();
		result.putExtra(NoteCaptureActivity.EXTRA_NOTE, note);
		getActivity().setResult(Activity.RESULT_OK, result);
		
		this.notePiecesWriter.close();
		getActivity().finish();
		
	}
}
