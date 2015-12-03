package com.morgadesoft.darknotes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.morgadesoft.darknotes.io.NotePiecesFileReader;
import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.PathNotePiece;
import com.morgadesoft.darknotes.ui.NotePieceViewer;

public class NoteReviewFragment extends Fragment implements LoaderCallbacks<PathNotePiece>, OnClickListener, OnEditorActionListener {
public static final String ARG_NOTE = "NoteReviewActivity.ARG_NOTE";
	
	private NotePieceViewer notePieceViewer;

	private Note note;
	private NotePiece notePiece;
	private NotePiecesFileReader reader;
	private int piecesRead;
	private StringBuilder textFinalBuilder;
	
	private ProgressBar progressLoading;
	private ProgressBar progressNote;
	private ImageButton buttonNext;
	private TextView textFinal;
	private EditText editText;
	private TextView textViewDone;

	
	interface Listener {
		void sendTextRequested(String text);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		note = (Note)getArguments().getSerializable(ARG_NOTE);
		reader = new NotePiecesFileReader(getActivity(), note);
		textFinalBuilder = new StringBuilder();
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_note_review, null);

		notePieceViewer = (NotePieceViewer) view.findViewById(R.id.fragment_note_review_note_piece_viewer);
		progressLoading = (ProgressBar) view.findViewById(R.id.fragment_note_review_note_progress_loading);
		progressNote = (ProgressBar) view.findViewById(R.id.fragment_note_review_note_image_progress_note);
		buttonNext = (ImageButton) view.findViewById(R.id.fragment_note_review_note_image_button_next);
		textFinal = (TextView) view.findViewById(R.id.fragment_note_review_note_text_final);
		editText = (EditText) view.findViewById(R.id.fragment_note_review_note_edit_text);
		textViewDone = (TextView) view.findViewById(R.id.fragment_note_review_note_text_done);
		
		progressNote.setMax(note.getPieceCount());
		progressNote.setProgress(0);
		
		
		editText.setOnEditorActionListener(this);
		
		buttonNext.setOnClickListener(this);
		
		if (notePiece!=null) {
			notePieceViewer.setNotePiece(notePiece);
		} else {
			progressLoading.setVisibility(View.VISIBLE);
			getLoaderManager().restartLoader(0, null, this).forceLoad();
		}
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_note_review, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==R.id.fragment_note_review_menu_share) {
			if (textFinalBuilder.length()==0) {
				Toast toast = Toast.makeText(getActivity(), R.string.no_text_to_share_was_typed, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} else {
				((Listener)getActivity()).sendTextRequested( textFinalBuilder.toString());
			}
		}
		return true;
	}
	
	public void seekNextPath() {
		textFinalBuilder.append(editText.getText().toString());
		textFinal.setText(textFinalBuilder);
		editText.setText("");
		
		progressLoading.setVisibility(View.VISIBLE);
		getLoaderManager().initLoader(0, null, this).forceLoad();
	}
	
	@Override
	public void onClick(View view) {
		if (view==buttonNext) {
			seekNextPath();
		}
	}
	
	@Override
	public boolean onEditorAction(TextView tv, int actionId, KeyEvent ke) {
		if (actionId == EditorInfo.IME_NULL  && ke.getAction() == KeyEvent.ACTION_DOWN) {
			seekNextPath();
		}
		return true;
	}
	
	@Override
	public Loader<PathNotePiece> onCreateLoader(int id, Bundle bundle) {
		return new AsyncTaskLoader<PathNotePiece>(getActivity()) {
			@Override public PathNotePiece loadInBackground() {
				NotePiece piece = reader.next();
				piecesRead++;
				PathNotePiece buildedPiece = null;
				while (piece!=null) {
					if (piece.isLineBreak()) {
						textFinalBuilder.append("\r\n");
						if (buildedPiece!=null) {
							return buildedPiece; 
						}
					} else if (piece.isWhiteSpace()) {
						textFinalBuilder.append(" ");
						if (buildedPiece!=null) {
							return buildedPiece; 
						}
					} else  if ( (piece instanceof PathNotePiece) && piece.getWidth()>0  && piece.getHeight()>0) {
						if (buildedPiece==null) {
							buildedPiece = new PathNotePiece();
						}
						buildedPiece.concat((PathNotePiece) piece);
					}
					piece = reader.next();
					piecesRead++;
				}
				return buildedPiece;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<PathNotePiece> loader, PathNotePiece notePiece) {
		this.notePiece = notePiece;
		progressLoading.setVisibility(View.GONE);
		progressNote.setProgress(piecesRead);
		notePieceViewer.setNotePiece(notePiece);
		if (notePiece==null) {
			textViewDone.setVisibility(View.VISIBLE);
			buttonNext.setEnabled(false);
			editText.setEnabled(false);
		}
	}

	@Override
	public void onLoaderReset(Loader<PathNotePiece> loader) {
		
	}

}
