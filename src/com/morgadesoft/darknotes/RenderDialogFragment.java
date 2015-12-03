package com.morgadesoft.darknotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.morgadesoft.darknotes.engine.NoteRenderer;
import com.morgadesoft.darknotes.preferences.PreferencesConstants;
import com.morgadesoft.darknotes.util.BundleBuilder;

public class RenderDialogFragment extends DialogFragment implements OnClickListener, TextWatcher, OnSeekBarChangeListener {
	private static final int DEFAULT_WIDTH = 768;
	private static final int DEFAULT_HEIGHT = 1024;
	private static final int DEFAULT_LINE_HEIGHT = 25;
	private static final int DEFAULT_CAPTURE_WIDTH = 75;
	
	public static final int MAX_WIDTH = 3000;
	public static final int MAX_HEIGHT = 3000;
	
	private EditText editWidth;
	private EditText editHeight;
	private SeekBar seekLineHeight;
	private SeekBar seekCaptureWidth;
	private TextView textLabelWidth;
	private TextView textLabelHeight;
	private TextView textLabelLineHeight;
	private TextView textLabelCaptureWidth;
	
	private ColorStateList defaultTextColor;

	public interface Listener {
		void renderClicked(Bundle renderParameters);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.dialog_render_note, null);
		
		editWidth = (EditText) view.findViewById(R.id.dialog_render_note_edit_width);
		editHeight = (EditText) view.findViewById(R.id.dialog_render_note_edit_height);
		seekLineHeight = (SeekBar) view.findViewById(R.id.dialog_render_note_seek_line_height);
		seekCaptureWidth = (SeekBar) view.findViewById(R.id.dialog_render_note_seek_capture_width);
		textLabelWidth = (TextView) view.findViewById(R.id.dialog_render_note_text_label_width);
		textLabelHeight = (TextView) view.findViewById(R.id.dialog_render_note_text_label_height);
		textLabelLineHeight = (TextView) view.findViewById(R.id.dialog_render_note_text_label_line_height);
		textLabelCaptureWidth = (TextView) view.findViewById(R.id.dialog_render_note_text_label_capture_width);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		final int width = pref.getInt(PreferencesConstants.PREFERENCE_RENDER_WIDTH, DEFAULT_WIDTH);
		final int height = pref.getInt(PreferencesConstants.PREFERENCE_RENDER_HEIGHT, DEFAULT_HEIGHT);
		editWidth.setText(String.valueOf(width));
		editHeight.setText(String.valueOf(height));
		
		int cw = pref.getInt(PreferencesConstants.PREFERENCE_RENDER_CAPTURE_WIDTH, DEFAULT_CAPTURE_WIDTH);
		seekCaptureWidth.setProgress(cw - getMinCW(width));
		int lh = pref.getInt(PreferencesConstants.PREFERENCE_RENDER_LINE_HEIGHT, DEFAULT_LINE_HEIGHT);
		seekLineHeight.setProgress(lh - getMinLH(height));
		
		editWidth.addTextChangedListener(this);
		editHeight.addTextChangedListener(this);
		seekCaptureWidth.setOnSeekBarChangeListener(this);
		seekLineHeight.setOnSeekBarChangeListener(this);
		
		defaultTextColor = textLabelWidth.getTextColors();
		
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.render_note)
        	   .setView(view)
               .setPositiveButton(android.R.string.ok, this)
               .setNegativeButton(android.R.string.cancel, this);
               
        
        return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int button) {
		if (button == DialogInterface.BUTTON_POSITIVE) {
			Bundle bundle = viewToBundle();
			Editor pref = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
			pref.putInt(PreferencesConstants.PREFERENCE_RENDER_WIDTH, bundle.getInt(NoteRenderer.BUNDLE_PARAM_WIDTH));
			pref.putInt(PreferencesConstants.PREFERENCE_RENDER_HEIGHT, bundle.getInt(NoteRenderer.BUNDLE_PARAM_HEIGHT));
			pref.putInt(PreferencesConstants.PREFERENCE_RENDER_LINE_HEIGHT, bundle.getInt(NoteRenderer.BUNDLE_PARAM_LINE_HEIGHT));
			pref.putInt(PreferencesConstants.PREFERENCE_RENDER_CAPTURE_WIDTH, bundle.getInt(NoteRenderer.BUNDLE_PARAM_CAPTURE_WIDTH));
			pref.commit();
			((Listener)getActivity()).renderClicked(bundle);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		viewToBundle();
	}
	
	@Override
	public void afterTextChanged(Editable arg0) {
		viewToBundle();
	}
	
	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {	}
	
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		viewToBundle();
	}
	
	private Bundle viewToBundle() {
		try {
			final int width = Integer.parseInt(editWidth.getText().toString());
			final int height = Integer.parseInt(editHeight.getText().toString());
			
			final int maxLH = getMaxLH(height);
			final int minLH = getMinLH(height);
			final int maxCW = getMaxCW(width);
			final int minCW = getMinCW(width);
			
			final int captureWidth = seekCaptureWidth.getProgress() + minCW;
			final int lineHeight = seekLineHeight.getProgress() + minLH;
			
			textLabelLineHeight.setText(getResources().getString(R.string.line_height)+" ("+lineHeight+")");
			textLabelCaptureWidth.setText(getResources().getString(R.string.width_per_capture)+" ("+captureWidth+")");
			
			seekCaptureWidth.setMax(maxCW);
			seekLineHeight.setMax(maxLH);
			
			boolean error = false;
			
			error |= swapErrorColor(textLabelWidth, width, MAX_WIDTH, 200, R.string.width);
			error |= swapErrorColor(textLabelHeight, height, MAX_HEIGHT, 200, R.string.height);
			//error |= swapErrorColor(textLabelLineHeight, lineHeight, maxLH, minLH, R.string.line_height);
			//error |= swapErrorColor(textLabelCaptureWidth, captureWidth, maxCW, minCW, R.string.width_per_capture);
			
			((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!error);
			
			if (error) {
				return null;
			} else {
				return BundleBuilder.builder()
						.integer(NoteRenderer.BUNDLE_PARAM_WIDTH, width)
						.integer(NoteRenderer.BUNDLE_PARAM_HEIGHT, height)
						.integer(NoteRenderer.BUNDLE_PARAM_LINE_HEIGHT, lineHeight)
						.integer(NoteRenderer.BUNDLE_PARAM_CAPTURE_WIDTH, captureWidth)
						.bundle();
			}
			
		} catch (NumberFormatException nfe) {
			((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			return null;
		}
	}
	
	private int getMinCW(int width) {
		return width/20;
	}


	private int getMaxCW(int width) {
		return width/5;
	}


	private int getMinLH(int height) {
		return height/50;
	}


	private int getMaxLH(int height) {
		return height/10;
	}


	private boolean swapErrorColor(TextView textView, int value, int max, int min, int originalLabel) {
		if (value>max) {
			textView.setText("max. "+ max);
			textView.setTextColor(Color.RED);
			return true;
		} else if (value < min){
			textView.setText("min. "+ min);
			textView.setTextColor(Color.RED);
			return true;
		} else {
			textView.setText(originalLabel);
			textView.setTextColor(defaultTextColor);
			return false;
		}
		
	}


}
