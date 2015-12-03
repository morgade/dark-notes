package com.morgadesoft.darknotes.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.morgadesoft.darknotes.R;
import com.morgadesoft.darknotes.model.CaptureMethod;
import com.morgadesoft.darknotes.model.CaptureVisualStyle;

public class UserPreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener, PreferencesConstants {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configure();
        
        ListPreference pref = getCaptureMethodPreference();
        pref.setOnPreferenceChangeListener(this);
        onPreferenceChange(pref, pref.getValue());
        
        pref = getCaptureVisualStylePreference();
        pref.setOnPreferenceChangeListener(this);
        onPreferenceChange(pref, pref.getValue());
        
        CheckBoxPreference checkPref = getVibrationFeedbackPreference();
        if (noVibrator()) {
        	checkPref.setSummary(R.string.this_device_doesn_t_have_a_vibrator);
        	checkPref.setChecked(false);
        	checkPref.setEnabled(false);
        }
    }
	
	@SuppressWarnings("deprecation")
	public void configure() {
		addPreferencesFromResource(R.xml.preferences);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(PREFERENCE_CAPTURE_METHOD)) {
			CaptureMethod method = CaptureMethod.valueOf(newValue.toString());
			if (method == CaptureMethod.CAPTURE_PER_WHITESPACE_LINEBREAK) {
				getEnableCapturebyWordPreference().setEnabled(true);
			} else {
				getEnableCapturebyWordPreference().setEnabled(false);
			}
			getCaptureMethodPreference().setSummary( method.description);
		} else if (preference.getKey().equals(PREFERENCE_CAPTURE_VISUAL_STYLE)) {
			CaptureVisualStyle style = CaptureVisualStyle.valueOf(newValue.toString());
			getCaptureVisualStylePreference().setSummary(style.description);
		}
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public ListPreference getCaptureMethodPreference() {
		return (ListPreference) findPreference(PREFERENCE_CAPTURE_METHOD);
	}
	
	@SuppressWarnings("deprecation")
	public ListPreference getCaptureVisualStylePreference() {
		return (ListPreference) findPreference(PREFERENCE_CAPTURE_VISUAL_STYLE);
	}
	
	@SuppressWarnings("deprecation")
	public CheckBoxPreference getEnableCapturebyWordPreference() {
		return (CheckBoxPreference) findPreference(PREFERENCE_CAPTURE_BY_WORD_DETECTION);
	}
	
	@SuppressWarnings("deprecation")
	public CheckBoxPreference getVibrationFeedbackPreference() {
		return (CheckBoxPreference) findPreference(PREFEREFNCE_VIBRATION_FEEDBACK);
	}
	
	@SuppressWarnings("deprecation")
	public CheckBoxPreference getKeepScreenOnPreference() {
		return (CheckBoxPreference) findPreference(PREFERENCE_KEEP_SCREEN_ON);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private boolean noVibrator() {
		try {
			if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				if (vibrator==null || !vibrator.hasVibrator()) {
					return true;
				}
			}
		} catch (Throwable t) {
			
		}
		return false;
	}
}
