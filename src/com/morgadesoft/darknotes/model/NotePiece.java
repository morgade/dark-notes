package com.morgadesoft.darknotes.model;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public interface NotePiece extends Serializable {
	public static final int CAPTURE_GRID_SIZE = 500;
	
	int getHeight();
	int getWidth();
	void drawCropped(Canvas canvas, Paint drawPaint, Matrix scaleMatrix, float x, float y);

	long getTimeStamp();
	boolean isWhiteSpace();
	boolean isLineBreak();
}
