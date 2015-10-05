package org.pyneo.tabulae.map;

import android.util.Log;

import java.io.File;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.MultiMapDataStore;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.pyneo.tabulae.Tabulae;

/**
 * Default vector bases layer
 */
class LayerMapsForge extends LayerBase {
	static final String ID = "mapsforge";

	LayerMapsForge(Tabulae activity, MapView mapView) {
		super(activity, mapView, false);
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
		File mapsDir = new File(activity.getMapsDir(), ID);
		if (DEBUG) Log.d(TAG, "LayerMapsForge searching in mapsDir=" + mapsDir);
		File[] maps = mapsDir.listFiles();
		if (maps != null) for (File map: maps) {
			if (map.isFile()) {
				try {
					multiMapDataStore.addMapDataStore(new MapFile(map), true, true);
					if (DEBUG) Log.d(TAG, "LayerMapsForge loaded map=" + map);
				}
				catch (Exception e) {
					Log.e(TAG, "LayerMapsForge error map=" + map, e);
				}
			}
		}
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore,
			mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
		((TileRendererLayer)tileLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(0, tileLayer);
		setVisible(false);
	}

	byte getZoomLevelMin() {
		return 8;
	}

	String getId() {
		return ID;
	}
}
