package com.morgadesoft.darknotes.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.morgadesoft.darknotes.R;
import com.morgadesoft.darknotes.model.NotePiece;
import com.morgadesoft.darknotes.model.piece.PathNotePiece;

public class NotePieceViewer extends View {
	private NotePiece notePiece;
	
	private Paint shadowPaint;
	private Matrix noteMatrix;
	private Rect viewBounds;

	private Bitmap animatedBitmap;
	private PathNoteAnimationTask animationTask;
	private Paint backgroundPaint;
	private Paint textPaint;
	
	private boolean animated = true;
	
	public NotePieceViewer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint.setColor(0x22000000);
		shadowPaint.setStrokeWidth(3);
		noteMatrix = new Matrix();
	}

	public void setAnimated(boolean animated) {
		this.animated = animated;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			playAnimation();
		}
		return true;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (notePiece!=null) {
			playAnimation();
		}
	}
	
	public void setNotePiece(NotePiece notePiece) {
		this.notePiece = notePiece;
		invalidate();
		playAnimation();
	}
	
	public NotePiece getNotePiece() {
		return notePiece;
	}

	private void playAnimation() {
		if (!animated) {
			return;
		}
		
		this.animatedBitmap = Bitmap.createBitmap(Math.max(getWidth(), 1) , Math.max(getHeight(), 1), Config.ARGB_8888);
		
		if (notePiece instanceof PathNotePiece) {
			Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			linePaint.setColor(0xFF000000);
			linePaint.setStrokeWidth(3);
			
			if (animationTask != null) {
				animationTask.cancel(true);
			}
			
			float scale = Math.min(this.animatedBitmap.getWidth() / (float)notePiece.getWidth(), this.animatedBitmap.getHeight() / (float)notePiece.getHeight());
			Matrix matrix = new Matrix();
			matrix.setScale(scale,scale);
			float x =  this.animatedBitmap.getWidth()/2f-(scale*notePiece.getWidth()/2f);
			float y =  this.animatedBitmap.getHeight()/2f-(scale*notePiece.getHeight()/2f);
			
			animationTask = new PathNoteAnimationTask(new Canvas(animatedBitmap), linePaint, matrix, x, y, (PathNotePiece) notePiece, this);
			animationTask.execute();
		}
	}

	public NotePieceViewer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NotePieceViewer(Context context) {
		this(context, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// allocate text paint
		if (textPaint==null) {
			textPaint = new Paint();
			textPaint.setAntiAlias(true);
			textPaint.setTextSize(getHeight()/10f);
		}
		// allocate background paint
		if (backgroundPaint==null) {
			backgroundPaint = new Paint();
			backgroundPaint.setDither(true);
			backgroundPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), getContext().getResources().getColor(R.color.bgNote1), getContext().getResources().getColor(R.color.bgNote2), TileMode.CLAMP));
		}
		// Allocate viewBounds
		if (viewBounds==null) {
			viewBounds = new Rect(0, 0, getWidth(), getHeight());
		}
		
		// Draw background
		canvas.drawRect(viewBounds, backgroundPaint);
		
		if (notePiece!=null) {
			float scale = Math.min(getWidth() / (float)notePiece.getWidth(), getHeight() / (float)notePiece.getHeight());
			// Set scale matrix
			noteMatrix.setScale(scale , scale);
			// Draw piece
			notePiece.drawCropped(canvas, shadowPaint,  noteMatrix, getWidth()/2f-(scale*notePiece.getWidth()/2f), getHeight()/2f-(scale*notePiece.getHeight()/2f));
			
			canvas.drawBitmap(animatedBitmap, 0, 0, null);
			
			String s = DateUtils.formatDateTime(getContext(), notePiece.getTimeStamp(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME);
			canvas.drawText(s, getWidth() - textPaint.measureText(s) - 2, getHeight() -2, textPaint);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (animationTask!=null) {
			animationTask.cancel(true);
		}
	}
}
