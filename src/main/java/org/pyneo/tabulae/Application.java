package org.pyneo.tabulae;

import co.uk.rushorm.android.AndroidInitializeConfig;
import co.uk.rushorm.core.RushCore;

public class Application extends android.app.Application {
	@Override public void onCreate() {
		super.onCreate();
		AndroidInitializeConfig config = new AndroidInitializeConfig(getApplicationContext());
		RushCore.initialize(config);
	}

	@Override public void onTerminate() {
		super.onTerminate();
	}
}
