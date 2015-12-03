package com.morgadesoft.darknotes.model;

import com.morgadesoft.darknotes.R;

public enum CaptureMethod {
    CAPTURE_PER_GESTURE(R.string.capture_per_gesture),
    CAPTURE_PER_WHITESPACE_LINEBREAK(R.string.capture_per_ws_lb_gesture),
    MULTITOUCH_CAPTURE(R.string.multi_touch_guided_capture);
    
    public final int description;

	private CaptureMethod(int description) {
    	this.description = description;
	}
}
