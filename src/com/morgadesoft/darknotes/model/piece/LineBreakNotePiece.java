package com.morgadesoft.darknotes.model.piece;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class LineBreakNotePiece extends BaseNotePiece implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public LineBreakNotePiece() {
	}

	@Override
	public int getWidth() {
		return 0;
	}
	
	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public void drawCropped(Canvas canvas, Paint drawPaint, Matrix scaleMatrix, float x, float y) {
		
	}

	@Override
	public boolean isWhiteSpace() {
		return false;
	}

	@Override
	public boolean isLineBreak() {
		return true;
	}

}
