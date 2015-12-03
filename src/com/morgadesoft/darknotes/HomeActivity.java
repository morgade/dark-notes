package com.morgadesoft.darknotes;

import java.io.File;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.morgadesoft.darknotes.data.DarkNotesContract;
import com.morgadesoft.darknotes.engine.NoteRenderer;
import com.morgadesoft.darknotes.io.NotePiecesFileTools;
import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.preferences.UserPreferencesActivity;
import com.morgadesoft.darknotes.provider.DarkNotesContentProvider;
import com.morgadesoft.darknotes.util.BundleBuilder;
import com.morgadesoft.darknotes.util.DialogUtils;

public class HomeActivity  extends ActionBarActivity implements NoteListFragment.Listener, 
															   NoteDetailFragment.Listener, 
															   RenderDialogFragment.Listener,
															   DialogUtils.PromptListener,
															   DialogUtils.ConfirmListener {
	private static final int PROMPT_CREATE = 0;
	private static final int PROMPT_CHANGE_TITLE = 1;
	private static final int CONFIRM_SHOW_PREFERENCES_BEFORE_START = 0;
	private static final int CONFIRM_RESTART_OR_APPEND = 1;
	private static final int CONFIRM_DELETE = 2;
	
	private boolean hasFrameB;
	private Note currentNote;
	private Fragment listFragment;
	private Fragment detailFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		if (savedInstanceState!=null) {
			currentNote = (Note) savedInstanceState.get("currentNote");
		}
		
		hasFrameB = findViewById(R.id.activity_home_frame_B)!=null;
		
		FragmentManager fm = getSupportFragmentManager();  
		listFragment = (Fragment) fm.findFragmentById(R.id.activity_home_frame_A);
		
		if (listFragment==null) {
			listFragment = new NoteListFragment();
			
			fm.beginTransaction()
			  .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
			  .replace(R.id.activity_home_frame_A, listFragment)
			  .commit();
		}
		
		
		if (hasFrameB) {
			detailFragment = (Fragment) fm.findFragmentById(R.id.activity_home_frame_B);
			if (detailFragment==null) {
				detailFragment = new NoteNotSelectedFragment();
				fm.beginTransaction()
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.replace(R.id.activity_home_frame_B, detailFragment)
					.commit();
			}
		}
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("currentNote", currentNote);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.activity_home_preferences:
				startActivity(new Intent(this, UserPreferencesActivity.class));
				return true;
		case R.id.activity_home_about:
				DialogUtils.about(this);
				return true;
		}
		return false;
	}
	
	@Override
	public void createNoteRequested() {
		DialogUtils.showPrompt(getSupportFragmentManager(), R.string.new_note, getResources().getString(R.string.untitled_note), PROMPT_CREATE);
	}
	
	@Override
	public void changeTitleRequested(Note note) { 
		currentNote = note;
		DialogUtils.showPrompt(getSupportFragmentManager(), R.string.title, note.getName(), PROMPT_CHANGE_TITLE);
	}
	
	@Override 
	public void handlePromptResponse(String value, int promptId) {
		if (value!=null) {
			if (promptId==PROMPT_CREATE) {
				Note note = new Note();
				note.setCreationDate(new Date());
				note.setName(value);
				
				Uri uri = getContentResolver().insert(DarkNotesContentProvider.CONTENT_URI_NOTES, DarkNotesContract.Note.createContentValues(note));
				note.setId(DarkNotesContentProvider.getIdFromUri(uri));
				
				if (hasFrameB) {
					((NoteListFragment)getSupportFragmentManager().findFragmentById(R.id.activity_home_frame_A)).reload();
				} else {
					noteClicked(note);
				}
			} else if (promptId==PROMPT_CHANGE_TITLE) {
				currentNote.setName(value);
				
				getContentResolver().update(DarkNotesContentProvider.CONTENT_URI_NOTES, 
						DarkNotesContract.Note.createContentValues(currentNote),
						DarkNotesContract.Note._ID+"=?", 
						new String[]{String.valueOf(currentNote.getId())});
				
				notifyNoteChanged(currentNote);
			}
		}
	}
	
	@Override
	public void noteClicked(Note note) {
		NoteDetailFragment fragment = new NoteDetailFragment();
		fragment.setArguments(BundleBuilder.bundle(NoteDetailFragment.ARGUMENT_NOTE, note));
		
		currentNote = note;
		
		if (hasFrameB) {
			getSupportFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.activity_home_frame_B, fragment)
				.commit();
		} else {
			getSupportFragmentManager().beginTransaction()
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.activity_home_frame_A, fragment)
				.addToBackStack(null)
				.commit();
		}
		
	}
	
	@Override
	public void startNoteRequested(final Note note) {
		currentNote = note;
		// Mostra mensagem de aviso para configuração de captura na primeira nota
		if (!NotePiecesFileTools.hasSettingsWarningShownFile(this)) {
			DialogUtils.showConfirm(getSupportFragmentManager(), CONFIRM_SHOW_PREFERENCES_BEFORE_START, R.string.capture_settings, getResources().getString(R.string.capture_warning));
			
			NotePiecesFileTools.createSettingsWarningShownFile(this);
			return;
		}
		
		NotePiecesFileTools.deletePreviewFiles(this, note);
		
		
		if (NotePiecesFileTools.getNoteDataFiles(this, note).length>0) {
			DialogUtils.showConfirm(getSupportFragmentManager(), CONFIRM_RESTART_OR_APPEND, R.string.start_note, getResources().getString(R.string.append_or_restart), R.string.append, R.string.restart);
		} else {
			NotePiecesFileTools.deleteNoteDataFiles(this, note);
			startActivityForResult(buildCaptureActivity(), 0);
		}
	}
	
	@Override
	public void handleResponse(boolean response, int id) {
		if (id==CONFIRM_SHOW_PREFERENCES_BEFORE_START) {
			if (response) {
				startActivity(new Intent(HomeActivity.this, UserPreferencesActivity.class));
			} else {
				startNoteRequested(currentNote);
			}
		} else if (id==CONFIRM_RESTART_OR_APPEND) {
			if (response) {
				startActivityForResult(buildCaptureActivity(), 0);
			} else {
				NotePiecesFileTools.deleteNoteDataFiles(this, currentNote);
				currentNote.setPieceCount(0);
				startActivityForResult(buildCaptureActivity(), 0);
			}			
		} else if (id==CONFIRM_DELETE) {
			if (response) { 
				NotePiecesFileTools.deletePreviewFiles(this, currentNote);
				NotePiecesFileTools.deleteNoteDataFiles(this, currentNote);
				getContentResolver().delete(DarkNotesContentProvider.CONTENT_URI_NOTES, DarkNotesContract.Note._ID+"=?", new String[]{String.valueOf(currentNote.getId())});
				
				
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_home_frame_A);
				// Recarrega lista se ela esta no frame A
				if (fragment instanceof NoteListFragment) { 
					((NoteListFragment)fragment).reload();
				}
				if (hasFrameB) {
					// Limpa o frame B se este existir
					getSupportFragmentManager().beginTransaction()
					.replace(R.id.activity_home_frame_B, new NoteNotSelectedFragment())
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
				} else if (fragment instanceof NoteDetailFragment){
					// Volta na pilha se estiver exibindo o detail
					getSupportFragmentManager().popBackStack();
				}
			}
		}
		
	}
	
	private Intent buildCaptureActivity() {
		final Intent intent = new Intent(this, NoteCaptureActivity.class);
		intent.putExtra(NoteCaptureActivity.EXTRA_NOTE, currentNote);
		return intent;
	}

	@Override
	protected void onActivityResult(int request, int result, Intent data) {
		if (result==RESULT_OK) {
			Toast.makeText(this, "Note capture succeful !", Toast.LENGTH_LONG).show();
			Note note = (Note) data.getExtras().getSerializable(NoteCaptureActivity.EXTRA_NOTE);
			getContentResolver().update(DarkNotesContentProvider.CONTENT_URI_NOTES, DarkNotesContract.Note.createContentValues(note),DarkNotesContract.Note._ID+"=?", new String[]{String.valueOf(note.getId())});
			notifyNoteChanged(note);
		}
	}

	@Override
	public void deleteNoteRequested(Note note) {
		currentNote = note;
		DialogUtils.showConfirm(getSupportFragmentManager(), CONFIRM_DELETE, R.string.delete, getResources().getString(R.string.delete_note_, currentNote.getName())  );
	}
	
	@Override
	public void reviewNoteRequested(Note note) {
		if (NotePiecesFileTools.getNoteDataFiles(this, currentNote).length==0) {
			DialogUtils.showMessage(this, R.string.this_note_is_empty, R.string.this_action_can_t_be_performed_using_an_empty_note);
		} else {
			Intent intent = new Intent(this, NoteReviewActivity.class);
			intent.putExtra(NoteReviewActivity.EXTRA_NOTE, note);
			startActivity(intent);
		}
	}
	
	@Override
	public void renderNoteRequested(Note note) {
		currentNote = note;
		
		if (NotePiecesFileTools.getNoteDataFiles(this, currentNote).length==0) {
			DialogUtils.showMessage(this, R.string.this_note_is_empty, R.string.this_action_can_t_be_performed_using_an_empty_note);
		} else {
			RenderDialogFragment dialog = new RenderDialogFragment();
			dialog.show(getSupportFragmentManager(), "render");
		}
	}
	
	@Override
	public void renderClicked(final Bundle renderParams) {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Rendering note ...");
		progressDialog.setTitle("Please wait");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMax(currentNote.getPieceCount());
		progressDialog.show();
		
		new AsyncTask<Void, String, File>() {
			@Override protected File doInBackground(Void ... params) {
				NoteRenderer renderer = new NoteRenderer();
				File file = renderer.render(HomeActivity.this, currentNote, renderParams, 
											NotePiecesFileTools.getRenderDir(HomeActivity.this, currentNote), 
											new NoteRenderer.Listener() {
					@Override public void onProgress(int progress, String message) {
						progressDialog.setMessage(message);
						progressDialog.setProgress(progress);
					}
				});
				
				return file;
			}
			
			@Override protected void onPostExecute(final File file) {
				File[] files = file.getParentFile().listFiles();
				progressDialog.setMessage("Sending "+files.length+" page images to '"+file.getParentFile().getName()+"' gallery folder ...");
				
				String[] fileNames = new String[files.length];
				for (int i = 0; i < files.length; i++) {
					fileNames[i] = files[i].getAbsolutePath();
				}
				
				MediaScannerConnection.scanFile(HomeActivity.this, fileNames, null, new MediaScannerConnection.OnScanCompletedListener() {
					boolean shown = false;
					@Override public void onScanCompleted(String path, Uri uri) {
						if (!shown) {
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
							
							progressDialog.dismiss();
							startActivity(intent);
							shown = true;
						}
					}
				});

			}
		}.execute();
	}

	@Override
	public void notifyNoteChanged(Note note) {
		currentNote = note;
		Fragment fragA = getSupportFragmentManager().findFragmentById(R.id.activity_home_frame_A);
		Fragment fragB = getSupportFragmentManager().findFragmentById(R.id.activity_home_frame_B);
		if (fragA instanceof NoteListFragment) {
			((NoteListFragment)fragA).reload();
		}
		if (fragA instanceof NoteDetailFragment) {
			((NoteDetailFragment)fragA).reload(currentNote);
		}
		if (fragB instanceof NoteListFragment) {
			((NoteListFragment)fragB).reload();
		}
		if (fragB instanceof NoteDetailFragment) {
			((NoteDetailFragment)fragB).reload(currentNote);
		}
	}

}

