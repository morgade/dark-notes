package com.morgadesoft.darknotes.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.morgadesoft.darknotes.R;
import com.morgadesoft.darknotes.model.CaptureMethod;
import com.morgadesoft.darknotes.model.CaptureVisualStyle;
import com.morgadesoft.darknotes.preferences.PreferencesConstants;

public class NotePieceCaptureView extends View  {
	private BaseTouchCapturer capturer;
	
	public NotePieceCaptureView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public NotePieceCaptureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NotePieceCaptureView(Context context) {
		super(context);
		init();
	}

	
	private void init() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		CaptureVisualStyle style = CaptureVisualStyle.valueOf(prefs.getString(PreferencesConstants.PREFERENCE_CAPTURE_VISUAL_STYLE, CaptureVisualStyle.DARK.name()));
		boolean useVibration = prefs.getBoolean(PreferencesConstants.PREFEREFNCE_VIBRATION_FEEDBACK, true);
		boolean useWordDetection = prefs.getBoolean(PreferencesConstants.PREFERENCE_CAPTURE_BY_WORD_DETECTION, true);
		boolean wordDetectFeedback = prefs.getBoolean(PreferencesConstants.PREFERENCE_ENABLE_WORD_DETECT_FEEDBACK, true); 
		CaptureMethod method = CaptureMethod.valueOf(prefs.getString(PreferencesConstants.PREFERENCE_CAPTURE_METHOD, CaptureMethod.CAPTURE_PER_GESTURE.toString()));
		boolean theatherMode = prefs.getBoolean(PreferencesConstants.PREFERENCE_ENABLE_THEATER_MODE, false);
		// Force dark style on theather mode
		if (theatherMode) {
			style = CaptureVisualStyle.NONE;
		}
		
		if (style == CaptureVisualStyle.DARK || style == CaptureVisualStyle.NONE) {
			this.setBackgroundResource(R.drawable.note_capture_view_bg_dark);
		} else {
			this.setBackgroundResource(R.drawable.note_capture_view_bg_light);
		}
		
		if (method==CaptureMethod.MULTITOUCH_CAPTURE) {
			capturer = new MultiTouchCapturer(this, style, useVibration);
		} else {
			capturer = new TouchCapturer(this, method, style, useVibration, useWordDetection, wordDetectFeedback);
		}
		this.setOnTouchListener(capturer);
	}

	public void forceCapture() {
		capturer.forceWhitespace();
	}
	
	public void finishCapture() {
		this.capturer.capturedCurrentPiece();
		setOnTouchListener(null);
	}
	
	public void setOnNotePieceCaptureListener(OnNoteCaptureListener listener) {
		this.capturer.setListener(listener);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.capturer.paintView(getContext(), canvas);
	}

}
