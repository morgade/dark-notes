package com.morgadesoft.darknotes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.morgadesoft.darknotes.R;
import com.morgadesoft.darknotes.engine.NotePieceBitmapBuilder;
import com.morgadesoft.darknotes.model.CaptureVisualStyle;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.LineBreakNotePiece;
import com.morgadesoft.darknotes.model.piece.PathNotePiece;
import com.morgadesoft.darknotes.model.piece.WhiteSpaceNotePiece;

public abstract class BaseTouchCapturer implements OnTouchListener {
	protected PathNotePiece currentPiece;
	protected NotePiece lastPiece;
	
	protected NotePieceBitmapBuilder currentBitmapBuilder;
	
	protected Bitmap currentBitmap;
	
	protected View view;
	protected CaptureVisualStyle style;
	
	private Paint drawPaint;
	private Matrix currentBitmapMatrix;
	private Matrix currentBitmapInvertedMatrix;
	private Vibrator vibrator;
	private OnNoteCaptureListener listener;

	
	public BaseTouchCapturer(View view, CaptureVisualStyle style, boolean useVibration) {
		this.view = view;
		this.style = style;
		this.drawPaint = new Paint();
		this.drawPaint.setTextSize(view.getContext().getResources().getDimension(R.dimen.capture_text_help_size));
		if (style==CaptureVisualStyle.DARK) {
			this.drawPaint.setColor(Color.WHITE);
		} else {
			this.drawPaint.setColor(Color.BLACK);
		}
		this.drawPaint.setAntiAlias(true);
		
		this.currentBitmap = Bitmap.createBitmap(NotePiece.CAPTURE_GRID_SIZE, NotePiece.CAPTURE_GRID_SIZE, Config.ARGB_8888);
		this.currentBitmapBuilder =  new NotePieceBitmapBuilder(0, 0, new Canvas(this.currentBitmap), this.drawPaint);
		this.currentPiece = new PathNotePiece();
		
		if (useVibration) {
			try {
				this.vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
			} catch (Throwable t) {
				
			}
		}
	}

	protected void vibrate(long ... milli) {
		if (vibrator!=null) {
			try { vibrator.vibrate(milli ,-1); } catch (Throwable t) {	}
		}
	}
	
	protected void capturedWhitespace(int width, int height) {
		if (listener!=null && !(lastPiece instanceof WhiteSpaceNotePiece) && !(lastPiece instanceof LineBreakNotePiece)) {
			listener.pieceCaptured(new WhiteSpaceNotePiece());
			vibrate(0, 100);
		}
		resetCapture();
	}

	
	protected void capturedLineBreak() {
		if (listener!=null) {
			listener.pieceCaptured(new LineBreakNotePiece());
			vibrate(0, 100, 100, 100);
		}
		resetCapture();
	}
	
	protected void capturedCurrentPiece() {
		if (listener!=null) {
			listener.pieceCaptured(this.currentPiece);
		}
		resetCapture();
	}
	
	protected void resetCapture() {
		this.currentBitmap.eraseColor(0x00000000);
		this.currentBitmapBuilder =  new NotePieceBitmapBuilder(0, 0, new Canvas(this.currentBitmap), this.drawPaint);
		
		this.lastPiece = this.currentPiece;
		this.currentPiece = new PathNotePiece();
	}
	
	
	@Override
	public abstract boolean onTouch(View v, MotionEvent event);
	
	protected Matrix getCurrentBitmapInvertedMatrix(int width, int height) {
		if (this.currentBitmapInvertedMatrix==null) {
			this.currentBitmapInvertedMatrix = new Matrix();
			getCurrentBitmapMatrix(width, height).invert(this.currentBitmapInvertedMatrix);
		}
		
		return this.currentBitmapInvertedMatrix;
	}
	
	public void setListener(OnNoteCaptureListener listener) {
		this.listener = listener;
	}

	
	public Matrix getCurrentBitmapMatrix(int viewWidth, int viewHeight) {
		if (this.currentBitmapMatrix==null) {
			this.currentBitmapMatrix = new Matrix();
			this.currentBitmapMatrix.setScale(((float)viewWidth)/((float)this.currentBitmap.getWidth()), ((float)viewHeight)/((float)this.currentBitmap.getHeight()));
		}
		
		return this.currentBitmapMatrix;
	}
	
	public Bitmap getCurrentBitmap() {
		return currentBitmap;
	}
	
	public Paint getDrawPaint() {
		return drawPaint;
	}

	public void forceWhitespace() {
		capturedCurrentPiece();
		capturedWhitespace(20, 20);
	}

	protected void addPoint(float[] point, int viewWidth, int viewHeight) {
		getCurrentBitmapInvertedMatrix(viewWidth, viewHeight).mapPoints(point);
		this.currentPiece.addPoint((short)point[0], (short)point[1]);
		if (style!=CaptureVisualStyle.NONE) {
			this.currentBitmapBuilder.nextPoint((int)point[0], (int)point[1]);
		}
	}
	
	protected void addEventHistoryPoints(MotionEvent event, int viewWidth, int viewHeight) {
		addEventHistoryPoints(event, viewWidth, viewHeight, 0);
	}
	
	protected void addEventHistoryPoints(MotionEvent event, int viewWidth, int viewHeight, int index) {
		try {
			float[] point = new float[2];
			for (int i = 0; i < event.getHistorySize(); i++) {
				point[0] = event.getHistoricalX(index, i);
				point[1] = event.getHistoricalY(index, i);
				addPoint(point, viewWidth, viewHeight);
			}
			// Registra o ponto que originou o evento
			point[0] = event.getX(index);
			point[1] = event.getY(index);
			addPoint(point, viewWidth, viewHeight);
		} catch (Throwable t) {
//			Log.i("XX", "P:"+ index, t);
		}
	}

	public abstract void paintView(Context context, Canvas canvas);
}
