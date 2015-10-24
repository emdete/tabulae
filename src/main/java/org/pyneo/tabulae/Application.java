package org.pyneo.tabulae;

import co.uk.rushorm.android.AndroidInitializeConfig;
import co.uk.rushorm.core.RushCore;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

public class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		AndroidGraphicFactory.createInstance(this);
		AndroidInitializeConfig config = new AndroidInitializeConfig(getApplicationContext());
		RushCore.initialize(config);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
