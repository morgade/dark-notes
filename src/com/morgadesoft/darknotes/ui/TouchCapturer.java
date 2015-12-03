package com.morgadesoft.darknotes.ui;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.morgadesoft.darknotes.model.CaptureMethod;
import com.morgadesoft.darknotes.model.CaptureVisualStyle;

public class TouchCapturer extends BaseTouchCapturer {
	private static final int GESTURE_GENERIC = 0;
	private static final int GESTURE_WHITE_SPACE = 1;
	private static final int GESTURE_WHITE_LINE_BREAK = 2;
	
	private float maxUpX = 0;
	private boolean wordDetection;
	private boolean wordDetectionFeedback;
	private CaptureMethod captureMethod;
	
	private Paint hollowPaint;
	
	public TouchCapturer(View view, CaptureMethod captureMethod, CaptureVisualStyle style, boolean useVibration, boolean useWordDetection, boolean wordDetectFeedback) {
		super(view, style, useVibration);
		this.wordDetection = useWordDetection;
		this.wordDetectionFeedback = wordDetectFeedback;
		this.captureMethod = captureMethod;
		this.view = view;
		
		hollowPaint = new Paint();
		if (style==CaptureVisualStyle.DARK) {
			hollowPaint.setColor(0x22FFFFFF);
		} else if (style==CaptureVisualStyle.LIGHT) {
			hollowPaint.setColor(0x11000000);	
		} else {
			this.wordDetectionFeedback = false;
		}
	}
	

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// Iniciando movimento
		if (event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			
			// WORD DETECTION !
			if (captureMethod==CaptureMethod.CAPTURE_PER_WHITESPACE_LINEBREAK && wordDetection) {
				// Calcula o terço da tela
				int screenThird = view.getWidth() / 3;
				// Verifica se registriou ACTION_UP no terço final e ACTION_DOWN no terço inicial para gerar whitespace
				if ( (maxUpX > screenThird*2)  && (event.getX() < screenThird) ) {
					capturedCurrentPiece();
					capturedWhitespace(view.getWidth(), view.getHeight());
					maxUpX = 0;
				}
				// Inicializa um novo path
				currentPiece.startNewPath();
				currentBitmapBuilder.nextPath();
			}
		}
		
		// Movimento em progresso
		if (event.getActionMasked()==MotionEvent.ACTION_DOWN || event.getActionMasked()==MotionEvent.ACTION_MOVE) {
			// Registra o ponto do ACTION_UP
			maxUpX = Math.max(maxUpX, event.getX());
			addEventHistoryPoints(event, view.getWidth(), view.getHeight());
		}
		
		
	    if (event.getActionMasked()==MotionEvent.ACTION_UP) {
	    	addEventHistoryPoints(event, view.getWidth(), view.getHeight());
	    	
			final int gestureType = getGestureType();
			switch (gestureType) {
			case GESTURE_GENERIC:
				if (captureMethod==CaptureMethod.CAPTURE_PER_GESTURE) {
					capturedCurrentPiece();
				} else {
					currentPiece.startNewPath();
					currentBitmapBuilder.nextPath();
				}
				break;
			case GESTURE_WHITE_SPACE:
				this.currentPiece.dropCurrentPath();
				this.capturedCurrentPiece();
				this.capturedWhitespace(view.getWidth(), view.getHeight());
				break;
			case GESTURE_WHITE_LINE_BREAK:
				this.currentPiece.dropCurrentPath();
				this.capturedCurrentPiece();
				this.capturedLineBreak();
				break;
			}
		} 
		
		
		view.invalidate();
		return true;
	}

	private int getGestureType() {
		// Detecta movimento de whitespace e linebreak
		float bitmapThird = this.currentBitmap.getWidth() / 3;
		List<short[]> currentPath = this.currentPiece.getCurrentPath();
		if (currentPath!=null && currentPath.size()>1) {
			short minY = Short.MAX_VALUE;
			short maxY = Short.MIN_VALUE;
			float pointDropLimit = this.currentBitmap.getWidth()/20.f;
			float yThreshold = this.currentBitmap.getHeight() / 4.0f;
			boolean whitespace = true, linebreak = true;
			short[] lastPoint = null;
			for (short[] point : currentPath) {
				// Verifica ponto inicial;
				if (lastPoint==null) {
					if (point[0] < bitmapThird*2) {
						linebreak = false;
					}
					if (point[0] > bitmapThird) {
						whitespace = false;
					}
				}
				// Verifica se deve continuar
				if (!whitespace && !linebreak) {
					break;
				}
				// Verifica se o ponto está distante significativamente
				if (lastPoint!=null && Math.abs(point[0]-lastPoint[0])<pointDropLimit && Math.abs(point[1]-lastPoint[1])<pointDropLimit) {
					continue;
				}
				
				// Verifica o limite de deslocamento do y
				minY = (short) Math.min(minY, point[1]);
				maxY = (short) Math.max(maxY, point[1]);
				if (maxY-minY>yThreshold) {
//					Log.i(getClass().getSimpleName(), "Y theshold passed:"+ yThreshold+" < "+(maxY-minY));
					whitespace = false;
					linebreak = false;
					break;
				}

				if (lastPoint!=null) {
					// direita -> esquerda
					if (point[0] < lastPoint[0]) {
						whitespace = false;
					} else  {
						// esquerda -> depois de já ter voltado
						linebreak = false;
					}
				}
				
				lastPoint = point;
			}
			
			if (lastPoint!=null) {
//				Log.i(getClass().getSimpleName(), "Check: WS "+ whitespace +" - LB "+linebreak +" lx "+ lastPoint[0]); 
				if (whitespace && lastPoint[0] > bitmapThird*2) {
					return GESTURE_WHITE_SPACE;
				} else if (linebreak && lastPoint[0]<bitmapThird) {
					return GESTURE_WHITE_LINE_BREAK;
				}
			}
		}
		
		return GESTURE_GENERIC;
	}
	
	
	public void paintView(Context context, Canvas canvas) {
		if (style!=CaptureVisualStyle.NONE) {
			canvas.drawBitmap(getCurrentBitmap(), getCurrentBitmapMatrix(view.getWidth(), view.getHeight()), getDrawPaint());
		}
		
		if (wordDetection && wordDetectionFeedback && captureMethod==CaptureMethod.CAPTURE_PER_WHITESPACE_LINEBREAK) {
			float screenThird = view.getWidth() / 3f;
			if (maxUpX < screenThird * 2) {
				canvas.drawRect(screenThird*2, 0, view.getWidth(), view.getHeight(), hollowPaint);
				String text = "Finish word here";
				canvas.drawText(text, (screenThird*2) + (getDrawPaint().measureText(text)/2f), view.getHeight()-getDrawPaint().getTextSize() - 2, getDrawPaint());
			} else if (maxUpX > screenThird * 2) {
				canvas.drawRect(0, 0, screenThird, view.getHeight(), hollowPaint);
				String text = "Start word here";
				canvas.drawText(text,  (getDrawPaint().measureText(text)/2f), view.getHeight()-getDrawPaint().getTextSize() - 2, getDrawPaint());
			}
		}
	}
	
}
