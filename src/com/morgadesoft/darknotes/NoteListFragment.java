package com.morgadesoft.darknotes;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.morgadesoft.darknotes.data.DarkNotesContract;
import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.provider.DarkNotesContentProvider;

public class NoteListFragment extends Fragment implements LoaderCallbacks<Cursor>, OnItemClickListener, OnItemLongClickListener, OnClickListener {
	private static final int LOADER_LIST = 1;
	
	private Note selectedNote;
	private ListView listView;
	
	interface Listener {
		void createNoteRequested();
		void noteClicked(Note note);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_note_list, null);
		this.listView = (ListView) view.findViewById(R.id.fragment_node_list_list_view);
		this.listView.setEmptyView(view.findViewById(R.id.fragment_node_list_list_empty_view));
		this.listView.setOnItemClickListener(this);
		this.listView.setOnItemLongClickListener(this);
		this.listView.setAdapter(new SimpleCursorAdapter(getActivity(), R.layout.list_item_note, null, new String[]{DarkNotesContract.Note.COLUMN_NAME}, new int[]{R.id.list_item_note_text}, SimpleCursorAdapter.NO_SELECTION));
		
		TextView textView = (TextView) view.findViewById(R.id.fragment_node_list_text_view_empty);
		textView.setOnClickListener(this);
		
		registerForContextMenu(this.listView);
		return view;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.activity_home_context, menu);
	}	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.activity_home_context_view:
			((Listener)getActivity()).noteClicked(selectedNote);
			break;
		case R.id.activity_home_context_delete:
			((NoteDetailFragment.Listener)getActivity()).deleteNoteRequested(selectedNote);
			break;
		case R.id.activity_home_context_render:
			((NoteDetailFragment.Listener)getActivity()).renderNoteRequested(selectedNote);
			break;
		case R.id.activity_home_context_review:
			((NoteDetailFragment.Listener)getActivity()).reviewNoteRequested(selectedNote);
			break;
		}
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		reload();
	}
	
	public void reload() {
		getLoaderManager().restartLoader(LOADER_LIST, null, this);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_note_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new_note:
				((Listener)getActivity()).createNoteRequested();
				return true;
		}
		return false;
	}
	
	@Override
	public void onClick(View view) {
		((Listener)getActivity()).createNoteRequested();
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
		Note note = DarkNotesContract.Note.loadNote(cursor);
		
		((Listener)getActivity()).noteClicked(note);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
		Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
		selectedNote = DarkNotesContract.Note.loadNote(cursor);
		return false;
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(getActivity(), 
								DarkNotesContentProvider.CONTENT_URI_NOTES, 
								DarkNotesContract.Note.PROJECTION_ALL, 
								null, 
								null, 
								DarkNotesContract.Note.COLUMN_NAME);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		((CursorAdapter)this.listView.getAdapter()).swapCursor(cursor);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}



}
