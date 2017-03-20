package de.emdete.tabulae;

import android.content.Context;
import android.app.Fragment;
import android.os.Bundle;
import android.content.SharedPreferences;

abstract public class Base extends Fragment {
	private SharedPreferences preferences;

	abstract public void inform(int event, Bundle extra);

	public void notice(int event, String key, String value) {
		if (getActivity() != null) {
			Bundle extra = new Bundle();
			extra.putString(key, value);
			((Tabulae)getActivity()).inform(event, extra);
		}
	}

	public void notice(int event, String key, boolean value) {
		if (getActivity() != null) {
			Bundle extra = new Bundle();
			extra.putBoolean(key, value);
			((Tabulae)getActivity()).inform(event, extra);
		}
	}

	public void notice(int event, String key, int value) {
		if (getActivity() != null) {
			Bundle extra = new Bundle();
			extra.putInt(key, value);
			((Tabulae)getActivity()).inform(event, extra);
		}
	}

	public SharedPreferences getPreferences() {
		if (preferences == null) {
			preferences = getActivity().getSharedPreferences("map", Context.MODE_PRIVATE);
		}
		return preferences;
	}
}
