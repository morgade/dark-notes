package com.morgadesoft.darknotes.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.Handler;

import com.morgadesoft.darknotes.R;
import com.morgadesoft.darknotes.exception.DarkNotesExceptionHandler;
import com.morgadesoft.darknotes.io.NotePiecesFileReader;
import com.morgadesoft.darknotes.model.Note;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.LineBreakNotePiece;
import com.morgadesoft.darknotes.model.piece.PathNotePiece;
import com.morgadesoft.darknotes.model.piece.WhiteSpaceNotePiece;

public class NoteRenderer {
	
	public static final String BUNDLE_PARAM_WIDTH = "width";
	public static final String BUNDLE_PARAM_HEIGHT = "height";
	public static final String BUNDLE_PARAM_LINE_HEIGHT = "lineHeight";
	public static final String BUNDLE_PARAM_CAPTURE_WIDTH = "captureWidth";
	
	private static final int DEFAULT_WIDTH = 764;
	private static final int DEFAULT_HEIGHT = 1024;
	
	public interface Listener {
		void onProgress(int progress, String message);
	}
	
	public File render(Context context, Note note, Bundle params, final File outFile, final Listener listener) {
		NotePiecesFileReader reader = new NotePiecesFileReader(context, note);
		return render(context, note, params, reader, outFile, listener);
	}
	
	private File render(final Context context, final Note note, Bundle params,  final NotePiecesFileReader reader, final File outFile, final Listener listener) {
		Handler handler = new Handler(context.getMainLooper());
		List<File> files = new LinkedList<File>();
		
		// Set rendering constants
		final int width = params.getInt(BUNDLE_PARAM_WIDTH, DEFAULT_WIDTH);
		final int height = params.getInt(BUNDLE_PARAM_HEIGHT, DEFAULT_HEIGHT);
		final int lineHeight = params.getInt(BUNDLE_PARAM_LINE_HEIGHT);
		final int captureWidth = params.getInt(BUNDLE_PARAM_CAPTURE_WIDTH);
		
		final int lineSpace = 10;
		final float whitespace = lineHeight;
		final float marginSpace = whitespace / 2f;
		final float pageTopMargin = lineHeight / 2;
		final float pageLeftMargin = width / 15;
		
		// Processed pieces count
		int processed = 0;
		
		// Render tools
		final Matrix scaleMatrix = new Matrix();
		final int bgNote1 = context.getResources().getColor(R.color.bgNote1);
		final int bgNote2 = context.getResources().getColor(R.color.bgNote2);
		final LinearGradient gradient = new LinearGradient(0, 0, 0, height, bgNote1 , bgNote2, TileMode.CLAMP);
		final Paint marginPaint = new Paint();
		final Paint linePaint = new Paint();
		final Paint drawPaint = new Paint();
		final Paint textPaint = new Paint();
		marginPaint.setStrokeWidth(3);
		marginPaint.setColor(Color.RED);
		linePaint.setColor(Color.GRAY);
		linePaint.setStrokeWidth(1);
		drawPaint.setColor(Color.BLACK);
		drawPaint.setStyle(Style.STROKE);
		drawPaint.setAntiAlias(true);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(lineHeight/2f);
		
		// Control variables
		Bitmap pageBitmap = null;
		Canvas canvas = null;
		float x = 0;
		float y = 0; 
		NotePiece lastPiece = null; 
		
		NotePiece piece = reader.next();
		while (piece != null) {
			// Prepare new page if needed
			if (pageBitmap==null) {
				pageBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
				canvas = new Canvas(pageBitmap);
				Paint bgPaint = new Paint(bgNote1);
				// Draw solid background
				canvas.drawRect(0, 0, width, height, bgPaint);
				// Draw background gradient
				bgPaint.setShader(gradient);
				canvas.drawRect(2, 2, width-2, height-2, bgPaint);
				
				// Draw margin line
				canvas.drawLine(pageLeftMargin - 5, 0, pageLeftMargin - 5, height, marginPaint);
				// Draw write lines
				for (int i = (int) pageTopMargin+lineHeight+lineSpace; i < height; i+=lineHeight+lineSpace) {
					canvas.drawLines(new float[]{0, i, width, i}, linePaint);
				}
				
				String s = context.getString(R.string.page_s, files.size()+1);
				canvas.drawText(s, width - textPaint.measureText(s) - 5, height - textPaint.getTextSize()  - 3, textPaint);
				// Init x and y
				x = pageLeftMargin + marginSpace;
				y = pageTopMargin + 5;
			}
			
			if (piece instanceof WhiteSpaceNotePiece) {
				if (!(lastPiece instanceof WhiteSpaceNotePiece) && !(lastPiece instanceof LineBreakNotePiece)) {
					x += whitespace; 
				}
			} else if (piece instanceof LineBreakNotePiece) {
				if (!(lastPiece instanceof LineBreakNotePiece)) {
					y += lineHeight + lineSpace;
					x = pageLeftMargin + marginSpace;
					if (y + lineHeight >height) { // Page break // Page break
						File f = getOutFile(outFile, files.size());
						handler.post(new ProgressUpdater(listener, processed, context.getResources().getString(R.string.writing_param_, f.getName())));
						files.add(writeBitmap(context, pageBitmap, f));
						pageBitmap = null;
						if (!outFile.isDirectory()) {
							return files.get(0);
						}
					}
				}
			} else if (piece instanceof PathNotePiece) {
				if (piece.getHeight()>0 && piece.getWidth()>0) {
					float pieceHeight = piece.getHeight();
					// Impede fator de escala que exploda o width
					if ((float)piece.getHeight()/(float)piece.getWidth() < 0.10) {
						pieceHeight = piece.getWidth();
					}
					
					final float pieceDesiredWidth = piece.getWidth()/(float)NotePiece.CAPTURE_GRID_SIZE * captureWidth;
					
					final float heightScaleFactor = (float)lineHeight / pieceHeight ;
					final float widthScaleFactor = pieceDesiredWidth / piece.getWidth();
					final float pieceAdjustedWidth = piece.getWidth() * widthScaleFactor;
					
//					float pieceAdjustedHeight = piece.getHeight() * scaleFactor;
					scaleMatrix.setScale(widthScaleFactor, heightScaleFactor, piece.getWidth()/2f, pieceHeight/2f);
					
					// Checa se vai passar do limite da página
					if (x+pieceAdjustedWidth > width) {
						y += lineHeight + lineSpace;
						x = pageLeftMargin + marginSpace;
						if (y + lineHeight >height) { // Page break
							File f = getOutFile(outFile, files.size());
							handler.post(new ProgressUpdater(listener, processed, context.getResources().getString(R.string.writing_param_, f.getName())));
							files.add(writeBitmap(context, pageBitmap, f));
							pageBitmap = null;
							if (!outFile.isDirectory()) {
								return files.get(0);
							} else {
								continue;
							}
						}
					}
					
					piece.drawCropped(canvas, drawPaint, scaleMatrix, x, y);
					x += pieceAdjustedWidth;
				}
			}
			
			lastPiece = piece;
			piece = reader.next();
			
			processed++;
			handler.post(new ProgressUpdater(listener, processed, context.getResources().getString(R.string.rendering_)));
		}
		
		try { reader.close(); } catch (Exception e) { }
		
		File f = getOutFile(outFile, files.size());
		handler.post(new ProgressUpdater(listener, processed, context.getResources().getString(R.string.writing_param_, f.getName()) ));
		files.add(writeBitmap(context, pageBitmap, f));
		
		return files.get(0);
	}
	
	private File getOutFile(File outFile, int index) {
		if (outFile.isDirectory()) {
			return new File(outFile, String.format("%04d.jpg", index));
		} else {
			return outFile;
		}
	}
	
	private File writeBitmap(Context context, Bitmap bitmap, File f) {
		try {
			if (bitmap==null) {
				return null;
			}
			FileOutputStream out = new FileOutputStream(f); 
			bitmap.compress(CompressFormat.JPEG, 87, out);
			out.close();
			return f;
		} catch (IOException e) {
			DarkNotesExceptionHandler.handleRecoverableException(context, e);
			return null;
		}
	}
	
	class ProgressUpdater implements Runnable {
		private Listener listener;
		private int processed;
		private String message;
		
		public ProgressUpdater(Listener listener, int processed, String message) {
			this.listener = listener;
			this.processed = processed;
			this.message = message;
		}

		@Override
		public void run() {
			if (listener!=null) {
				listener.onProgress(processed, message);
			}
		}
		
	}
}
