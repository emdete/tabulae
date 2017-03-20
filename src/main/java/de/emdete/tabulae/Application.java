package de.emdete.tabulae;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

public class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		AndroidGraphicFactory.createInstance(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
