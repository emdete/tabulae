package org.pyneo.tabulae.gui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.Date;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.pyneo.tabulae.fawlty.Fawlty;
import org.pyneo.tabulae.gui.Controller;
import org.pyneo.tabulae.gui.Dashboard;
import org.pyneo.tabulae.locus.Locus;
import org.pyneo.tabulae.map.Map;
import org.pyneo.tabulae.poi.Poi;
import org.pyneo.tabulae.screencapture.ScreenCaptureFragment;
import org.pyneo.tabulae.track.Track;

public class DocumentAvtivity extends Activity implements Constants {
	Context context;
	HtmlViewer browser;
	String url;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate intent=" + getIntent());
		url = getIntent().getExtras().getString("url");
		context = getBaseContext();
		browser = new HtmlViewer(context, null);
		browser.loadUrl(url);
		setContentView(browser);
	}

	@Override public void onBackPressed() {
		if (!browser.back()) {
			finish();
		}
	}

	class HtmlViewer extends WebView {
		public HtmlViewer(Context context, AttributeSet a) {
			super(context, a);
			getSettings().setLoadsImagesAutomatically(true);
			getSettings().setJavaScriptEnabled(false);
			getSettings().setAllowContentAccess(false);
			setWebViewClient(new WebViewClient (){
				@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (DEBUG) Log.d(TAG, "shouldOverrideUrlLoading url=" + url);
					if (!url.startsWith("file:///")) {
						view.loadUrl(DocumentAvtivity.this.url);
						return true;
					}
					return false;
				}
				@Override public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request) {
					if (DEBUG) Log.d(TAG, "shouldInterceptRequest request=" + request);
					return null;//super.shouldInterceptRequest(view, request);
				}
				@Override public WebResourceResponse shouldInterceptRequest (WebView view, String url) {
					if (DEBUG) Log.d(TAG, "shouldInterceptRequest url=" + url);
					return null;//super.shouldInterceptRequest(view, url);
				}
				@Override public void onLoadResource (WebView view, String url) {
					if (DEBUG) Log.d(TAG, "onLoadResource url=" + url);
					super.onLoadResource(view, url);
				}
			});
			setWebChromeClient(new WebChromeClient() {
			});
		}

		boolean back() {
			if (canGoBack()) {
				goBack();
				return true;
			}
			return false;
		}
	}
}
