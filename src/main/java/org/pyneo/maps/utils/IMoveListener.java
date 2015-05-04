package org.pyneo.maps.utils;

public interface IMoveListener {
	void onMoveDetected();

	void onZoomDetected();

	void onCenterDetected();
}
