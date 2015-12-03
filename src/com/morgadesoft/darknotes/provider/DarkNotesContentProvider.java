package com.morgadesoft.darknotes.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.morgadesoft.darknotes.data.DarkNotesSQLiteOpenHelper;
import com.morgadesoft.darknotes.data.DarkNotesContract.Note;

public class DarkNotesContentProvider extends ContentProvider {
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int NOTES_MATCH = 10;
	
	public static final String AUTHORITY = "com.morgadesoft.darknotes.contentprovider";
	public static final String BASE_PATH = "darknotes";
	public static final String PATH_URI_NOTES = "/notes";
	
	public static final Uri CONTENT_URI_NOTES = Uri.parse("content://" + AUTHORITY+ "/" + BASE_PATH + PATH_URI_NOTES);
	public static final String CONTENT_TYPE_NOTES = ContentResolver.CURSOR_DIR_BASE_TYPE + "/notes";
	
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + PATH_URI_NOTES, NOTES_MATCH);
	}
	
	private DarkNotesSQLiteOpenHelper database;
	
	@Override
	public boolean onCreate() {
		database = new DarkNotesSQLiteOpenHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder q = new SQLiteQueryBuilder();
		String group = null;
		
		switch (sURIMatcher.match(uri)) {
			case NOTES_MATCH:
				q.setTables(Note.TABLE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		Cursor cursor = q.query(database.getWritableDatabase(), projection, selection, selectionArgs, group, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
		
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionValues) {
		switch (sURIMatcher.match(uri)) {
		case NOTES_MATCH:
			return database.getWritableDatabase().delete(Note.TABLE_NAME, selection, selectionValues);
		}
		throw new IllegalArgumentException("Invalid delete uri: " + uri);
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
			case NOTES_MATCH:
				return CONTENT_TYPE_NOTES;
		}
		
		throw new IllegalArgumentException("Unknown URI: " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sURIMatcher.match(uri)) {
			case NOTES_MATCH:
				long id = database.getWritableDatabase().insert(Note.TABLE_NAME, null, values);
				return getIdUri(CONTENT_URI_NOTES, id);
			default:
				throw new IllegalArgumentException("Invalid insert URI: " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (sURIMatcher.match(uri)) {
		case NOTES_MATCH:
			return  database.getWritableDatabase().update(Note.TABLE_NAME, values, selection, selectionArgs);
		default:
			throw new IllegalArgumentException("Invalid update URI: " + uri);
	}
	}
	
	public static Integer getIdFromUri(Uri uri) {
		return Integer.parseInt(uri.getLastPathSegment());
	}

	public static Uri getIdUri(Uri baseUri, long id) {
		return baseUri.buildUpon()
				.appendPath(String.valueOf(id))
				.build();
	}
	
}
