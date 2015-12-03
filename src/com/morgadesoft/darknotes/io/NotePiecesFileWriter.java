package com.morgadesoft.darknotes.io;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.content.Context;

import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.EOFNotePiece;

public class NotePiecesFileWriter implements Closeable {
	private int writeCount = 0;
	private ObjectOutputStream out;
	
	public NotePiecesFileWriter(Context context, Note note) throws IOException {
		out = new ObjectOutputStream(new FileOutputStream(NotePiecesFileTools.createNoteDataFile(context, note)));
	}
	
	public void writePiece(NotePiece piece) throws IOException {
		writeCount++;
		out.writeObject(piece);
	};
	
	public int getWriteCount() {
		return writeCount;
	}
	
	public void close() {
		try { 
			writeCount++;
			writePiece(new EOFNotePiece());
			out.close(); 
		} catch (Throwable t) {
			
		}
	}
}
