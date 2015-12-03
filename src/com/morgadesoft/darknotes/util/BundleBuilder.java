package com.morgadesoft.darknotes.util;

import java.io.Serializable;

import android.os.Bundle;

public class BundleBuilder {
	
	private Bundle bundle;

	private BundleBuilder() {
		bundle = new Bundle();
	}

	public static Bundle bundle(String key, Serializable value) {
		return builder().object(key, value).bundle;
	}

	public static Bundle bundle(String key, String value) {
		return builder().string(key, value).bundle;
	}
	
	public static BundleBuilder builder() {
		return new BundleBuilder();
	}
	
	
	public BundleBuilder integer(String key, Integer value) {
		bundle.putInt(key, value);
		return this;
	}
	
	
	public BundleBuilder string(String key, String value) {
		bundle.putString(key, value);
		return this;
	}
	
	public BundleBuilder object(String key, Serializable value) {
		bundle.putSerializable(key, value);
		return this;
	}
	
	public Bundle bundle() {
		return bundle;
	}
}
