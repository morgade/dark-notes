package com.morgadesoft.darknotes.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import com.morgadesoft.darknotes.R;
import com.morgadesoft.darknotes.model.CaptureVisualStyle;

public class MultiTouchCapturer extends BaseTouchCapturer {
	
	private int pressPointerIndex;
	private int writePointerIndex;
	private int maxPointerCount;
	private boolean hasWritten;
	
	public MultiTouchCapturer(View view, CaptureVisualStyle style, boolean useVibration) {
		super(view, style, useVibration);
		this.writePointerIndex = -1;
		this.pressPointerIndex = -1;
		this.hasWritten = false;
		this.view = view;
	}
	

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// Primeiro ponteiro
		if (event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			pressPointerIndex = event.getActionIndex();
			writePointerIndex = -1;
			hasWritten = false;
		}
		
		// Segundo ponteiro pressionado
		if (event.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN) {
			maxPointerCount = Math.max(maxPointerCount, event.getPointerCount());

			if (maxPointerCount<3) {
				writePointerIndex = event.getActionIndex();
				hasWritten = true;
				currentPiece.startNewPath();
				currentBitmapBuilder.nextPath();
				addPoint(new float[]{event.getX(writePointerIndex), event.getY(writePointerIndex)}, view.getWidth(), view.getHeight());
			}
		}
		
		// Segundo ponteiro solto
		if (event.getActionMasked()==MotionEvent.ACTION_POINTER_UP) {
			if (event.getActionIndex()==writePointerIndex) {
				addPoint(new float[]{event.getX(writePointerIndex), event.getY(writePointerIndex)}, view.getWidth(), view.getHeight());
				writePointerIndex = -1;
			}
		}
		
		// Movimento em progresso
		if (event.getActionMasked()==MotionEvent.ACTION_MOVE) {
			if (writePointerIndex>-1 && event.getPointerCount()>1) {
				addEventHistoryPoints(event, view.getWidth(), view.getHeight(), writePointerIndex);
			}
		}
		
		// Terminando movimento
		if (event.getActionMasked()==MotionEvent.ACTION_UP) {
			if (maxPointerCount>2) {
				capturedLineBreak();
			} else if (hasWritten) {
				capturedCurrentPiece();
				capturedWhitespace(view.getWidth(), view.getHeight());
			}
			
			pressPointerIndex = -1;
			writePointerIndex = -1;
			maxPointerCount = 0;
		}
		
		view.invalidate();
		return true;
	}

	public void paintView(Context context, Canvas canvas) {
		if (style!=CaptureVisualStyle.NONE) {
			canvas.drawBitmap(getCurrentBitmap(), getCurrentBitmapMatrix(view.getWidth(), view.getHeight()), getDrawPaint());
		
			if (maxPointerCount>2) {
				String text = context.getString(R.string.release_all_touchs_to_register_the_linebreak);
				canvas.drawText(text,  view.getWidth()/2f - getDrawPaint().measureText(text)/2f, getDrawPaint().getTextSize()+1, getDrawPaint());
			} else if (pressPointerIndex>-1) {
				if (hasWritten) {
					drawText(canvas, context.getString(R.string.release_both_touchs_to_register_the_word), 1);
				} else {
					drawText(canvas, context.getString(R.string.keep_first_touch_pressed_and_use_a_second_touch_to_write), 1);
					drawText(canvas, context.getString(R.string.touch_three_points_at_same_time_to_register_a_linebreak), 2);
				}
			} else {
				drawText(canvas, context.getString(R.string.press_and_hold_a_point_on_screen_to_start_writing_a_word), 1);
			}
		}
		
	}
	
	private void drawText(Canvas canvas, String text, int line) {
		canvas.drawText(text,  view.getWidth()/2f - getDrawPaint().measureText(text)/2f, getDrawPaint().getTextSize()*line+1+line, getDrawPaint());
	}
}
