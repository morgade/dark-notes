package com.morgadesoft.darknotes.model;

import com.morgadesoft.darknotes.R;

public enum CaptureVisualStyle {
	NONE(R.string.light_visual),
    LIGHT(R.string.light_visual),
    DARK(R.string.dark_visual);
    
    public final int description;

	private CaptureVisualStyle(int description) {
    	this.description = description;
	}
}
