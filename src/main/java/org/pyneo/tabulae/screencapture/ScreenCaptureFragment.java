package org.pyneo.tabulae.screencapture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import org.pyneo.tabulae.R;
import org.pyneo.tabulae.Base;
import org.pyneo.tabulae.Tabulae;
import java.io.File;
import java.util.Random;

public class ScreenCaptureFragment extends Base implements Constants {
	private static final String STATE_ENABLED = "screencapture_enabled";
	private static final String STATE_RESULT_CODE = "screencapture_result_code";
	private static final String STATE_RESULT_DATA = "screencapture_result_data";
	protected boolean enabled;
	protected int mScreenWidth;
	protected int mScreenHeight;
	protected int mScreenDensity;
	protected MediaProjectionManager mMediaProjectionManager;
	protected MediaProjection mMediaProjection;
	private int mResultCode;
	private Intent mResultData;
	protected VirtualDisplay mVirtualDisplay;
	protected MediaRecorder mMediaRecorder;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onCreate");
		if (savedInstanceState != null) {
			enabled = savedInstanceState.getBoolean(STATE_ENABLED);
			mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
			mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
		}
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onActivityCreated enabled=" + enabled + ", mVirtualDisplay=" + mVirtualDisplay);
		Activity activity = getActivity();
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		mScreenDensity = mDisplayMetrics.densityDpi;
		mScreenWidth = mDisplayMetrics.widthPixels;
		mScreenHeight = mDisplayMetrics.heightPixels;
		mMediaProjectionManager = (MediaProjectionManager)activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
		mMediaRecorder = new MediaRecorder();
		if (enabled && mVirtualDisplay == null) {
			go();
		}
	}

	@Override public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onSaveInstanceState mResultData=" + mResultData + ", mResultCode=" + mResultCode);
		outState.putBoolean(STATE_ENABLED, enabled);
		if (mResultData != null) {
			outState.putInt(STATE_RESULT_CODE, mResultCode);
			outState.putParcelable(STATE_RESULT_DATA, mResultData);
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onActivityResult resultCode=" + resultCode + ", requestCode=" + requestCode + ", resultData=" + resultData);
		switch (requestCode) {
			case R.id.allow_screen_capture: {
				switch (resultCode) {
					case Activity.RESULT_OK: {
						enabled = true;
						mResultCode = resultCode;
						mResultData = resultData;
						if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onActivityResult: permission granted enabled=" + enabled);
						if (getActivity() != null) {
							go();
						}
					}
					break;
					default:
						if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onActivityResult: permission denied");
						stopRecording();
						Toast.makeText(getActivity(), "User canceled", Toast.LENGTH_SHORT).show();
				}
			}
			default:
				super.onActivityResult(requestCode, resultCode, resultData);
		}
	}

	@Override public void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onDestroy");
		if (mMediaProjection != null) {
			try {
				mMediaProjection.stop();
			}
			catch (Exception ignore) {
				Log.e(TAG, "ScreenCaptureFragment.onDestroy:", ignore);
			}
			finally {
				mMediaProjection = null;
			}
		}
	}

	@Override public void onPause() {
		super.onPause();
		stopRecording();
	}

	protected void go() {
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.go mResultData=" + mResultData + ", mResultCode=" + mResultCode);
		if (getActivity() != null) {
			if (mResultData != null && mMediaProjection == null) {
				mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
				// mMediaProjection.registerCallback(...
			}
			if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.go mMediaProjection=" + mMediaProjection);
			if (mMediaProjection != null && mVirtualDisplay == null) {
				try {
					if (DEBUG) Log.d(TAG, "mScreenWidth=" + mScreenWidth + ", mScreenHeight=" + mScreenHeight);
					mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
					// mMediaRecorder.setVideoEncodingBitRate(128 * 1000);
					// mMediaRecorder.setVideoFrameRate(12);
					mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
					mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
					mScreenWidth = ((mScreenWidth+15)/16) * 16; // round up to multiple of 16
					mMediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
					mMediaRecorder.setOutputFile(getMovieName().getPath());
					mMediaRecorder.prepare();
					mMediaRecorder.start();
					mVirtualDisplay = mMediaProjection.createVirtualDisplay(
						getClass().getSimpleName(),
						mScreenWidth,
						mScreenHeight,
						mScreenDensity,
						DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
						mMediaRecorder.getSurface(),
						null, // Callbacks
						null // Handler
						);
					if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.onActivityResult: recording started");
					Toast.makeText(getActivity(), "Recording started", Toast.LENGTH_SHORT).show();
				}
				catch (Exception e) {
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
					Log.e(TAG, "ScreenCaptureFragment.onActivityResult:", e);
					stopRecording();
				}
			}
		}
	}

	protected void startRecording() {
		if (mVirtualDisplay != null) {
			stopRecording();
		}
		if (mMediaProjection == null) {
			getActivity().startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), R.id.allow_screen_capture);
			enabled = true;
			if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.startRecording: ask for permission enabled=" + enabled);
		}
		else {
			go();
		}
	}

	protected void stopRecording() {
		enabled = false;
		if (mVirtualDisplay != null) {
			try {
				mVirtualDisplay.release();
			}
			catch (Exception ignore) {
				// Log.e(TAG, "ScreenCaptureFragment.stopRecording:", ignore);
			}
			finally {
				mVirtualDisplay = null;
			}
		}
		if (mMediaRecorder != null) {
			try {
				mMediaRecorder.stop();
			}
			catch (Exception ignore) {
				// Log.e(TAG, "ScreenCaptureFragment.stopRecording:", ignore);
			}
			finally {
				mMediaRecorder.reset();
				mMediaRecorder.release();
			}
		}
		if (DEBUG) Log.d(TAG, "ScreenCaptureFragment.stopRecording: recording stopped");
	}

	private static final Random random = new Random();
	File getMovieName() {
		File f;
		do {
			f = new File(((Tabulae)getActivity()).getMoviesDir(), "capture_" + random.nextLong() + ".mp4");
		}
		while (f.exists());
		if (DEBUG) Log.d(TAG, "getMovieName f=" + f);
		return f;
	}

	public void inform(int event, Bundle extra) {
		switch (event) {
			case R.id.event_screencapture: {
				if (enabled) {
					stopRecording();
					Toast.makeText(getActivity(), "Recording stopped", Toast.LENGTH_SHORT).show();
				}
				else {
					startRecording();
				}
			}
		}
	}
}