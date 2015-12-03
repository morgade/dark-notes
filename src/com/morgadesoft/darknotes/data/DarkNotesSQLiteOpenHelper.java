package com.morgadesoft.darknotes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.morgadesoft.darknotes.data.DarkNotesContract.Note;

public class DarkNotesSQLiteOpenHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "DarkNotes.db";

	public DarkNotesSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+Note.TABLE_NAME+" ( " +
				Note._ID+" integer primary key autoincrement, "+
				Note.COLUMN_NAME+" text not null, " +
				Note.COLUMN_DATE+" long not null, " +
				Note.COLUMN_PIECE_COUNT+" long not null " +
				");");
	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
