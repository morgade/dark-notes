package com.morgadesoft.darknotes;

import java.io.File;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.morgadesoft.darknotes.engine.NoteRenderer;
import com.morgadesoft.darknotes.io.NotePiecesFileTools;
import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.util.BundleBuilder;

public class NoteDetailFragment extends Fragment implements LoaderCallbacks<Bitmap>, OnClickListener {
	public static final String ARGUMENT_NOTE = "NoteDetailFragment.ARGUMENT_NOTE";
	private static final int LOADER_PREVIEW = 0;
	
	private ImageView imageViewPreview;
	private ProgressBar progressBarPreview;
	private TextView textViewName;
	private TextView textCreated;
	private TextView textViewEmpty;
	private ViewGroup viewEmptyPreview;

	private Note note;

	interface Listener {
		void startNoteRequested(Note note);
		void deleteNoteRequested(Note note);
		void renderNoteRequested(Note note);
		void notifyNoteChanged(Note note);
		void reviewNoteRequested(Note note);
		void changeTitleRequested(Note note);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		note = (Note) getArguments().getSerializable(ARGUMENT_NOTE);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_note_detail, null);
		this.textViewName = (TextView) view.findViewById(R.id.fragment_note_detail_text_title);
		this.imageViewPreview = (ImageView) view.findViewById(R.id.fragment_note_detail_image_preview);
		this.progressBarPreview = (ProgressBar) view.findViewById(R.id.fragment_note_detail_progress_preview);
		this.textCreated = (TextView) view.findViewById(R.id.fragment_note_detail_text_created);
		this.viewEmptyPreview = (ViewGroup) view.findViewById(R.id.fragment_note_detail_layout_empty_preview);
		this.textViewEmpty = (TextView) view.findViewById(R.id.fragment_note_detail_text_empty_preview);
		
		this.textViewEmpty.setOnClickListener(this);
		this.textViewName.setOnClickListener(this);
		this.imageViewPreview.setOnClickListener(this);
		
		reload(note);
		
		return view;
	}
	
	public void reload(Note note) {
		this.note = note;
		this.textViewName.setText(note.getName());
		String date = DateUtils.formatDateTime(getActivity(), note.getCreationDate().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME);
		this.textCreated.setText(getResources().getString(R.string.created_p, date ));
		
		this.viewEmptyPreview.setVisibility(View.GONE);
		this.progressBarPreview.setVisibility(View.VISIBLE);
		getLoaderManager().restartLoader(LOADER_PREVIEW, null, this).forceLoad();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_note_details, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.fragment_note_details_action_delete:
			((Listener)getActivity()).deleteNoteRequested(note);
			return true;
		case R.id.fragment_note_details_action_render:
			((Listener)getActivity()).renderNoteRequested(note);
			return true;
		case R.id.fragment_note_details_action_capture:
			((Listener)getActivity()).startNoteRequested(note);
			return true;
		case R.id.fragment_note_details_action_review:
			((Listener)getActivity()).reviewNoteRequested(note);
			return true;	
			
		}
		
		return false;
	}
	
	@Override
	public Loader<Bitmap> onCreateLoader(int id, Bundle bundle) {
		return new AsyncTaskLoader<Bitmap>(getActivity()) {
			@Override public Bitmap loadInBackground() {
				boolean landscape = getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
				File preview = NotePiecesFileTools.getPreviewFile(getActivity(), note, landscape);
				Bitmap bitmap = null;
				if (preview.exists() && preview.canRead()) {
					bitmap = BitmapFactory.decodeFile(preview.getAbsolutePath());
					if (bitmap==null) {
						preview.delete();
					} else {
						return bitmap;
					}
				}
				
				if (NotePiecesFileTools.getNoteDataFiles(getActivity(), note).length > 0) {
					NoteRenderer renderer = new NoteRenderer();
					renderer.render(getActivity(), 
							note, 
							BundleBuilder.builder()
							.integer(NoteRenderer.BUNDLE_PARAM_WIDTH, landscape?640:480)
							.integer(NoteRenderer.BUNDLE_PARAM_HEIGHT, landscape?300:640)
							.integer(NoteRenderer.BUNDLE_PARAM_LINE_HEIGHT, landscape?48:30)
							.integer(NoteRenderer.BUNDLE_PARAM_CAPTURE_WIDTH, landscape?85:80)
							.bundle(),
							preview,
							null);
					if (preview.exists() && preview.canRead()) {
						return BitmapFactory.decodeFile(preview.getAbsolutePath());
					}
				}
				return null;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
		this.progressBarPreview.setVisibility(View.GONE);
		if (bitmap==null) {
			this.viewEmptyPreview.setVisibility(View.VISIBLE);
			this.imageViewPreview.setVisibility(View.GONE);
		} else {
			this.viewEmptyPreview.setVisibility(View.GONE);
			this.imageViewPreview.setVisibility(View.VISIBLE);
			this.imageViewPreview.setImageBitmap(bitmap);
		}
	}

	@Override
	public void onLoaderReset(Loader<Bitmap> arg0) {
		this.progressBarPreview.setVisibility(View.GONE);
	}
	
	@Override
	public void onClick(View v) {
		if (v==textViewName) {
			((Listener)getActivity()).changeTitleRequested(note);
		} else if (v==imageViewPreview) {
			((Listener)getActivity()).renderNoteRequested(note);
		} else if (v==textViewEmpty) {
			((Listener)getActivity()).startNoteRequested(note);
		}
	}
	
}
