package com.morgadesoft.darknotes.exception;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.morgadesoft.darknotes.R;

public class DarkNotesExceptionHandler implements UncaughtExceptionHandler {
	private Context context;
	
	public DarkNotesExceptionHandler(Context context) {
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		handleRecoverableException(context, ex);
		System.exit(2);
	}
	
	public static void handleRecoverableException(Context context, Throwable ex) {
		Log.wtf(context.getPackageName(), "Uncaught exception on FILMBUFF", ex);
		
		AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
	    messageBox.setTitle("Ops !");
	    messageBox.setMessage(ex.getMessage());
	    messageBox.setCancelable(false);
	    messageBox.setNeutralButton("OK", null);
	    messageBox.show();
	}
	
	public static void handleRecoverableException(Context context, String msg, Throwable ex) {
		Log.wtf(context.getPackageName(), "Uncaught exception on FILMBUFF", ex);
		
		AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
	    messageBox.setTitle(context.getResources().getString(R.string.app_name));
	    messageBox.setMessage(msg+": "+ex.getMessage());
	    messageBox.setCancelable(false);
	    messageBox.setNeutralButton("OK", null);
	    messageBox.show();
	}

}
