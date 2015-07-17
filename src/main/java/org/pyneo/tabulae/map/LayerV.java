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
class LayerV extends LayerB {
	static final String ID = "mapsforge";

	LayerV(Tabulae activity, MapView mapView) {
		super(activity, mapView, false);
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
		File mapsDir = activity.getMapsDir();
		File[] maps = mapsDir.listFiles();
		if (maps != null) for (File map: maps) {
			if (map.isFile()) {
				try {
					multiMapDataStore.addMapDataStore(new MapFile(map), true, true);
					Log.d(TAG, "LayerV loaded map=" + map);
				}
				catch (Exception e) {
					Log.e(TAG, "LayerV error map=" + map, e);
				}
			}
		}
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore,
			mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
		((TileRendererLayer)tileLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(tileLayer);
		setVisible(false);
	}

	String getId() {
		return ID;
	}
}
