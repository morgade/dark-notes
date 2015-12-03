package com.morgadesoft.darknotes.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Iterator;

import android.content.Context;
import android.util.Log;

import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.EOFNotePiece;

public class NotePiecesFileReader implements Closeable {
	private ObjectInputStream currentInput;
	private boolean eof = false;
	private Iterator<File> fileIterator;
	
	public NotePiecesFileReader(Context context, Note note) {
		fileIterator = Arrays.asList(NotePiecesFileTools.getNoteDataFiles(context, note)).iterator();
		openNextFile();
	}
	
	private boolean openNextFile() {
		try {
			if (currentInput!=null) {
				try { currentInput.close(); } catch (Exception e) { }
			}
			
			if (fileIterator.hasNext()) {
				File f = fileIterator.next();
//				Log.i(getClass().getSimpleName(), "opening next piece file "+ f);
				currentInput = new ObjectInputStream(new FileInputStream(f));
				return true;
			}
			return false;
		} catch (Throwable e) {
			Log.wtf(NotePiecesFileReader.class.getSimpleName(), e);
			return false;
		}
	}
	
	public NotePiece next() {
		try {
			if (eof) {
				return null;
			}
			NotePiece notePiece = (NotePiece) currentInput.readObject();
//			Log.i(getClass().getSimpleName(), "Reading: "+ notePiece.getClass().getSimpleName());
			if (notePiece instanceof EOFNotePiece) {
				eof = !openNextFile();
				return this.next();
			} else  {
				return notePiece;
			}
		} catch (Throwable e) {
			Log.wtf(NotePiecesFileReader.class.getSimpleName(), e);
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		try { currentInput.close(); } catch (Exception e) { }
		eof = true;
	}
	
}
