package com.morgadesoft.darknotes.model.piece;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class WhiteSpaceNotePiece extends BaseNotePiece implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public WhiteSpaceNotePiece() {
		
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
		return true;
	}

	@Override
	public boolean isLineBreak() {
		return false;
	}

}
