package org.pyneo.tabulae.traffic;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Tabulae;
import org.pyneo.tabulae.track.TrackItem;
import org.pyneo.tabulae.track.TrackPointItem;
import static org.pyneo.tabulae.traffic.Constants.*;

public class Traffic extends Base {
/*
// see https://developer.android.com/reference/android/os/Handler.html
	Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override public void run() {
				Intent intent = new Intent(ActivityName.this, ThirdQuestion.class);
				startActivity(intent);
			}
		}, 2000);
// see https://developer.android.com/reference/android/app/AlarmManager.html
*/
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "inform");
		}
	});
	protected boolean enabled = false;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_request_traffic: {
				if (getActivity() != null) {
					Bundle b = new Bundle();
					b.putBoolean("enabled", enabled);
					((Tabulae)getActivity()).inform(R.id.event_notify_traffic, b);
				}
			}
			break;
			case R.id.event_do_traffic: {
				if (!enabled) {
					enabled = true;
					try {
						mThreadPool.execute(new Runnable() {
							public void run() {
								try {
									final File cache_dir = new File(((Tabulae) getActivity()).getBaseDir(), "cache");
									//noinspection ResultOfMethodCallIgnored
									cache_dir.mkdirs();
									try (final SQLiteDatabase db = ((Tabulae)getActivity()).getWritableDatabase()) {
										// remove old incidents
										int count = TrackItem.deleteCategory(db, TrackItem.CATEGORY_TRAFFIC);
										if (DEBUG) Log.d(TAG, "deleteCategory=" + count);
									}
									((Tabulae)getActivity()).asyncInform(R.id.event_do_track_list, null);
									// retrieve new report
									ReportRetriever.Incidents incidents = ReportRetriever.go(cache_dir, null);
									try (final SQLiteDatabase db = ((Tabulae)getActivity()).getWritableDatabase()) {
										for (ReportRetriever.Incident incident: incidents) {
											Log.d(TAG, "incident=" + incident);
											TrackItem trackItem = new TrackItem(incident.getName(), incident.getDescription());
											trackItem.setCategoryid(TrackItem.CATEGORY_TRAFFIC);
											for (Location position: incident.getPosition()) {
												trackItem.add(null, new TrackPointItem(position.getLatitude(), position.getLongitude()));
											}
											trackItem.insert(db);
										}
									}
									enabled = false;
									//Bundle b = new Bundle();
									//b.putBoolean("enabled", enabled);
									//((Tabulae)getActivity()).asyncInform(R.id.event_notify_traffic, b);
									((Tabulae)getActivity()).asyncInform(R.id.event_do_track_list, null);
								}
								catch (Exception e) {
									Log.d(TAG, "traffic load e=" + e, e);
									//Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
								}
							}
						});
					}
					catch (Exception e) {
						Log.d(TAG, "traffic load e=" + e);
					}
				}
			}
			break;
		}
	}
}
