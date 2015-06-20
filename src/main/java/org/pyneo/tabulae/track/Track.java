package org.pyneo.tabulae.track;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.gui.Base;

import java.io.File;

public class Track extends Base implements Constants {
	private boolean visible = true;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
	}

	@Override
	public void onAttach(Activity activity) {
		if (DEBUG) {
			Log.d(TAG, "Track.onAttach");
		}
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle bundle) {
		if (DEBUG) {
			Log.d(TAG, "Track.onCreate");
		}
		super.onCreate(bundle);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) {
			Log.d(TAG, "Track.onActivityCreated");
		}
		// add a parsedTrack overlay
		try {
			final MapView mapView = ((Tabulae)getActivity()).getMapView();
			final TrackGpxParser track = new TrackGpxParser(new File("/sdcard/tabulae/export/track46.gpx"));
			Overlay mPathOverlay = new TrackOverlay(getActivity(), track);
			mapView.getOverlayManager().add(mPathOverlay);
		}
		catch (Exception e) {
			Log.e(TAG, "e=" + e, e);
		}
	}
}
