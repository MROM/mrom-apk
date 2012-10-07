package com.mmontuori.atrix_mrom;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class Settings extends PreferenceActivity {
	public boolean checkOnBoot;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		loadPrefs(this);
	}

	public void loadPrefs(Context ctx) {
		SharedPreferences prefs = null;
		PreferenceScreen screen = null;
		try {
			screen = getPreferenceScreen();
		}
		catch (Exception ex) {
			screen = null;
		}
		if (screen == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		} else {
			prefs = screen.getSharedPreferences();	
		}
		if (prefs != null) {
			checkOnBoot = prefs.getBoolean("check_on_boot", true);
		} else {
			checkOnBoot = true;
		}
	}
}
