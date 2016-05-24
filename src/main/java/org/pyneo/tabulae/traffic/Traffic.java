package org.pyneo.tabulae.traffic;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
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
import static org.pyneo.tabulae.traffic.Constants.TAG;

public class Traffic extends Base {
	protected ExecutorService mThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(@NonNull Runnable r) {
			return new Thread(r, "inform");
		}
	});
	protected boolean enabled = false;

	public void inform(int event, Bundle extra) {
		//if (DEBUG) Log.d(TAG, "Track.inform event=" + event + ", extra=" + extra);
		switch (event) {
			case R.id.event_request_traffic: {
				Bundle b = new Bundle();
				b.putBoolean("enabled", enabled);
				((Tabulae)getActivity()).inform(R.id.event_notify_traffic, b);
			}
			break;
			case R.id.event_do_traffic: {
				try {
					final File cache_dir = new File(((Tabulae) getActivity()).getBaseDir(), "cache");
					//noinspection ResultOfMethodCallIgnored
					cache_dir.mkdirs();
					mThreadPool.execute(new Runnable() {
						public void run() {
							try {
								SQLiteDatabase db = null; // TODO
								TrackItem.deleteCategory(db, TrackItem.CATEGORY_TRAFFIC);
								ReportRetriever.Incidents incidents = ReportRetriever.go(cache_dir, null);
								for (ReportRetriever.Incident incident: incidents) {
									TrackItem trackItem = new TrackItem(incident.getName(), incident.getDescription());
									trackItem.setCategoryid(TrackItem.CATEGORY_TRAFFIC);
									Log.d(TAG, "incident=" + incident);
									for (Location position: incident.getPosition()) {
										trackItem.add(null, new TrackPointItem(position.getLatitude(), position.getLongitude()));
									}
									trackItem.insert(db);
								}
								enabled = true;
								Bundle b = new Bundle();
								b.putBoolean("enabled", enabled);
								((Tabulae)getActivity()).inform(R.id.event_notify_traffic, b);
							}
							catch (Exception e) {
								Toast.makeText(getActivity().getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
								Log.d(TAG, "traffic load e=" + e, e);
							}
						}
					});
				}
				catch (Exception e) {
					Log.d(TAG, "traffic load e=" + e);
				}
			}
			break;
		}
	}
}
