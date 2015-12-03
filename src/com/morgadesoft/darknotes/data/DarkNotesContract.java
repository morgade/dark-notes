package com.morgadesoft.darknotes.data;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

public abstract class DarkNotesContract {
	
	 public static abstract class Note implements BaseColumns {
	        public static final String TABLE_NAME = "notes";
	        public static final String COLUMN_NAME = "name";
	        public static final String COLUMN_PIECE_COUNT = "piece_count";
	        public static final String COLUMN_DATE = "creation_date";
	        
	        public static final String[] PROJECTION_ALL = {_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_PIECE_COUNT };
	        
	        public static ContentValues createContentValues(com.morgadesoft.darknotes.model.Note note) {
	        	ContentValues values = new ContentValues();
	    		values.put(DarkNotesContract.Note.COLUMN_NAME, note.getName());
	    		values.put(DarkNotesContract.Note.COLUMN_DATE, note.getCreationDate().getTime());
	    		values.put(DarkNotesContract.Note.COLUMN_PIECE_COUNT, note.getPieceCount());
	    		
	    		return values;
	        }
	        
	        public static com.morgadesoft.darknotes.model.Note loadNote(Cursor cursor) {
	    		com.morgadesoft.darknotes.model.Note note = new com.morgadesoft.darknotes.model.Note();
	    		note.setId(cursor.getInt(cursor.getColumnIndex(DarkNotesContract.Note._ID)));
	    		note.setName(cursor.getString(cursor.getColumnIndex(DarkNotesContract.Note.COLUMN_NAME)));
	    		note.setCreationDate(new Date(cursor.getLong(cursor.getColumnIndex(DarkNotesContract.Note.COLUMN_DATE))));
	    		note.setPieceCount(cursor.getInt(cursor.getColumnIndex(DarkNotesContract.Note.COLUMN_PIECE_COUNT)));
	    		
	    		return note;
	        }
	 }
	 
}
