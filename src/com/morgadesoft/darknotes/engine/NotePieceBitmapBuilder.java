package com.morgadesoft.darknotes.engine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class NotePieceBitmapBuilder {
	private Canvas bitmapCanvas;
	private Paint bitmapPaint;
	private float originX = 0;
	private float originY = 0;
	private float lastX = -1;
	private float lastY = -1;
	
	public NotePieceBitmapBuilder(float originX, float originY, Canvas canvas, Paint paint) {
		if (paint==null) {
			this.bitmapPaint = new Paint();
			this.bitmapPaint.setColor(Color.BLACK);
			this.bitmapPaint.setStrokeWidth(2);
		} else {
			this.bitmapPaint = paint;
		}
		this.bitmapCanvas = canvas;
		this.originX = originX;
		this.originY = originY;
	}
	
	public void nextPath() {
		this.lastX = -1;
		this.lastY = -1;
	}
	
	public void nextPoint(float x, float y) {
		if (this.lastX==-1) {
			this.bitmapCanvas.drawPoint(originX + x, originY + y, bitmapPaint);
		} else {
			this.bitmapCanvas.drawLine(originX + this.lastX, originY + this.lastY, originX + x, originY + y, bitmapPaint);
		}
	
		this.lastX = x;
		this.lastY = y;
	}
	
}
