package de.emdete.tabulae.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import static de.emdete.tabulae.gui.Constants.*;

public class DocumentAvtivity extends Activity {
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
