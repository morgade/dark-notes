package com.morgadesoft.darknotes.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;

import com.morgadesoft.darknotes.R;

public class DialogUtils {
	public static final String PROMPT_TAG = "prompt";
	public static final String CONFIRM_TAG = "confirm";
	
	public interface PromptListener {
		void handlePromptResponse(String value, int id);
	}
	
	public interface ConfirmListener {
		void handleResponse(boolean response, int id);
	}

	
	public static void showPrompt(FragmentManager manager, final int messageResourceId, final String defaultValue, int promptId) {
		PromptDialogFragment d = PromptDialogFragment.create(messageResourceId, defaultValue, promptId);
		d.show(manager, PROMPT_TAG);
	}
	
	public static void showConfirm(FragmentManager manager, int confirmId, int titleResourceId, String message) {
		showConfirm(manager, confirmId, titleResourceId, message, android.R.string.yes, android.R.string.no);
	}
	
	public static void showConfirm(FragmentManager manager, int confirmId, int titleResourceId, String message, int positiveButton, int negativeButton) {
	    ConfirmDialogFragment frag = ConfirmDialogFragment.create(message, confirmId, titleResourceId, negativeButton, positiveButton);
	    frag.show(manager, CONFIRM_TAG);
	}
	
	public static AlertDialog showMessage(Context context, int title, int message) {
		return showMessage(context, context.getResources().getString(title), context.getResources().getString(message));
	}
	
	public static AlertDialog showMessage(Context context, String title, String message) {
		return new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(android.R.string.ok, null)
		.show();
	}

	public static AlertDialog about(final Context context) {
		String versionName = "";
		try {
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) { }
		
		return new AlertDialog.Builder(context)
		.setTitle(context.getResources().getString(R.string.app_name)+" v"+versionName)
		.setMessage(context.getResources().getString(R.string.aboutMessage))
		.setPositiveButton(android.R.string.ok, null)
		.setNegativeButton(R.string.rate, new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+ context.getPackageName())), context.getResources().getString(R.string.rate)) );
			}
		})
		.show();
	}
	
	
	
	public static class ConfirmDialogFragment extends DialogFragment {
		
		public static ConfirmDialogFragment create(String message, int confirmId, int titleResourceId, int negativeButton, int positiveButton) {
			ConfirmDialogFragment d = new ConfirmDialogFragment();
			d.setArguments(BundleBuilder.builder().integer("confirmId", confirmId)
												  .string("message", message)
												  .integer("positiveButton", positiveButton)
												  .integer("negativeButton", negativeButton)
												  .integer("titleResourceId", titleResourceId).bundle());
			return d;
		}

		public android.app.Dialog onCreateDialog(android.os.Bundle savedInstanceState) {
			final String message = getArguments().getString("message");
			final int positiveButton = getArguments().getInt("positiveButton");
			final int negativeButton = getArguments().getInt("negativeButton");
			final int titleResourceId = getArguments().getInt("titleResourceId");
			final int confirmId = getArguments().getInt("confirmId");
			
			AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setTitle(titleResourceId).setMessage(message)
		    .setPositiveButton(getActivity().getResources().getString(positiveButton), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ConfirmListener)getActivity()).handleResponse(true, confirmId);
				}
			})
		    .setNegativeButton(getActivity().getResources().getString(negativeButton), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((ConfirmListener)getActivity()).handleResponse(false, confirmId);
				}
			}).create();
			return dialog;
		}

		
	}
	
	public static class PromptDialogFragment extends DialogFragment {
		private EditText input;
		
		public static PromptDialogFragment create(int messageResourceId, String defaultValue, int promptId) {
			PromptDialogFragment d = new PromptDialogFragment();
			d.setArguments(BundleBuilder.builder().string("defaultValue", defaultValue)
												  .integer("promptId", promptId)
												  .integer("messageResourceId", messageResourceId).bundle());
			return d;
		}

		@Override
		public void onSaveInstanceState(Bundle b) {
			b.putParcelable("value", input.onSaveInstanceState());
		}
		
		public android.app.Dialog onCreateDialog(android.os.Bundle savedInstanceState) {
			final String defaultValue = getArguments().getString("defaultValue");
			final int messageResourceId = getArguments().getInt("messageResourceId");
			final int promptId = getArguments().getInt("promptId");
			input = new EditText(getActivity());
			input.setMaxLines(1);
			input.setHint(defaultValue);
			
			if (savedInstanceState!=null) {
				input.onRestoreInstanceState(savedInstanceState.getParcelable("value"));
			}
			
			AlertDialog dialog = new AlertDialog.Builder(getActivity())
		    .setTitle(messageResourceId).setView(input)
		    .setPositiveButton(getActivity().getResources().getString(android.R.string.ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (input.getText().length()==0) {
						((PromptListener)getActivity()).handlePromptResponse(defaultValue, promptId);
					} else {
						((PromptListener)getActivity()).handlePromptResponse(input.getText().toString(), promptId);
					}
				}
			})
		    .setNegativeButton(getActivity().getResources().getString(android.R.string.cancel), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					((PromptListener)getActivity()).handlePromptResponse(null, promptId);
				}
			}).create();
			return dialog;
		}

		@Override
		 public void onDestroyView() {
		     if (getDialog() != null && getRetainInstance()) {
		    	 getDialog().setDismissMessage(null);
		     }
	         super.onDestroyView();
		 }
	}
}

