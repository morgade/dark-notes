package com.morgadesoft.darknotes.ui;

import java.util.Iterator;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.View;

import com.morgadesoft.darknotes.engine.NotePieceBitmapBuilder;
import com.morgadesoft.darknotes.model.piece.PathNotePiece;

public class PathNoteAnimationTask extends AsyncTask<Void, Void, Void> {
	private Canvas canvas;
	private Paint paint;
	private Matrix matrix;
	private PathNotePiece notePiece;
	private View view;
	private float x;
	private float y;
	
	public PathNoteAnimationTask(Canvas canvas, Paint paint, Matrix matrix, float x, float y, PathNotePiece notePiece, View viewToInvalidate) {
		this.canvas = canvas;
		this.paint = paint;
		this.matrix = matrix;
		this.notePiece = notePiece;
		this.view = viewToInvalidate;
		this.x =x;
		this.y = y;
	}

	@Override
	protected Void doInBackground(Void... params) {
		final float[] mappedMin = new float[2];
		final float[] mappedPoint = new float[2];
		Iterator<short[]> itPoints = null;
		Iterator<List<short[]>> itPaths = notePiece.getPaths().iterator();
		NotePieceBitmapBuilder croppedBuilder = new NotePieceBitmapBuilder(0, 0, canvas, paint);
		matrix.mapPoints(mappedMin, new float[]{notePiece.getMinX(),notePiece.getMinY()});
		
		while (itPaths.hasNext()) {
			croppedBuilder.nextPath();
			if (itPoints==null || !itPoints.hasNext()) {
				itPoints = itPaths.next().iterator();
				while (itPoints.hasNext()) {
					try { Thread.sleep(25); } catch (InterruptedException e) { return null; }
					if (this.isCancelled()) {
						return null;
					}
					short[] point = itPoints.next();
					mappedPoint[0] = point[0];
					mappedPoint[1] = point[1];
					matrix.mapPoints(mappedPoint);
					croppedBuilder.nextPoint(mappedPoint[0] - mappedMin[0] + x,  mappedPoint[1] - mappedMin[1] + y);
					view.postInvalidate();
				}
			}
		}
		return null;
	}
}
