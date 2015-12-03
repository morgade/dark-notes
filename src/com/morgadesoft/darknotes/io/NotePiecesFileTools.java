package com.morgadesoft.darknotes.io;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.morgadesoft.darknotes.model.Note;

public class NotePiecesFileTools {
	private static final String SETTINGS_WARNING_SHOWN_FILE = "setwarn_shown";
	private static final String DATA_FOLDER = "notes_data";
	private static final String PREVIEW_FOLDER = "preview";

	public static File getNoteDataDir(Context context, Note note) {
		File f = new File(context.getFilesDir(), DATA_FOLDER);
		f = new File(f, String.valueOf(note.getId()));
		if (!f.isDirectory()) {
			f.delete();
		}
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	
	public static File[] getNoteDataFiles(Context context, Note note) {
		File[] files = getNoteDataDir(context, note).listFiles();
		if (files==null) {
			return new File[0];
		} else {
			Arrays.sort(files, new Comparator<File>() {
				@Override public int compare(File lhs, File rhs) {
					return lhs.getName().compareTo(rhs.getName());
				}
			});
			return  files;
		}
	}
	
	public static File createNoteDataFile(Context context, Note note) {
		File f = getNoteDataDir(context, note);
		return new File(getNoteDataDir(context, note), String.format(Locale.US, "%04d", f.list().length));
	}
	
	public static File getPreviewFile(Context context, Note note, boolean landscape) {
		File f = new File(context.getCacheDir(), PREVIEW_FOLDER);
		if (!f.exists()) {
			f.mkdir();
		}
		return new File(f, note.getId()+ (landscape?"land":"port") +".jpg");
	}
	
	public static File getRenderDir(Context context, Note note) {
		File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), note.getName().replaceAll("\\W+", "_"));
		if (!f.exists()) {
			f.mkdirs();
		}
		return f;
	}
	
	public static void deletePreviewFiles(Context context, Note note) {
		try {
			getPreviewFile(context, note, false).delete();
			getPreviewFile(context, note, true).delete();
		} catch (Throwable t) {
			Log.wtf(NotePiecesFileTools.class.getSimpleName(), t);
		}
	}
	
	public static void deleteNoteDataFiles(Context context, Note note) {
		try {
			File[] files = getNoteDataFiles(context, note);
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		} catch (Throwable t) {
			Log.wtf(NotePiecesFileTools.class.getSimpleName(), t);
		}
	}

	public static boolean hasSettingsWarningShownFile(Context context) {
		return new File(context.getFilesDir(), SETTINGS_WARNING_SHOWN_FILE).exists();
	}
	
	public static void createSettingsWarningShownFile(Context context) {
		File f=  new File(context.getFilesDir(), SETTINGS_WARNING_SHOWN_FILE);
		try {
			new FileOutputStream(f).close();
		} catch (Exception e) {
			Log.wtf(NotePiecesFileTools.class.getSimpleName(), e);
		}
	}
}
