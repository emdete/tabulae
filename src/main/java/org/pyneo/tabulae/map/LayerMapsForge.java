package org.pyneo.tabulae.map;

import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.pyneo.tabulae.Tabulae;
import static org.pyneo.tabulae.map.Constants.*;

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
		if (maps != null) for (File map : maps) {
			if (map.isFile()) {
				try {
					MapFile mapfile = new MapFile(map);
					multiMapDataStore.addMapDataStore(mapfile, true, true);
					if (DEBUG) Log.d(TAG, "LayerMapsForge loaded map=" + map + ", bb=" + mapfile.boundingBox());
				}
				catch (Exception e) {
					Log.e(TAG, "LayerMapsForge error map=" + map, e);
				}
			}
		}
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore,
				mapView.getModel().mapViewPosition, false, true, true, AndroidGraphicFactory.INSTANCE);
		boolean success = false;
		File themesDir = new File(mapsDir, "themes");
		File theme = new File(themesDir, "theme.xml");
//		File[] themes = themes.listFiles();
//		for (File theme: themes) {
		if (theme.isFile() && theme.getPath().endsWith(".xml")) {
			try {
				((TileRendererLayer) tileLayer).setXmlRenderTheme(new ExternalRenderTheme(theme));
				if (DEBUG) Log.d(TAG, "LayerMapsForge loaded theme=" + theme);
//					break;
				success = true;
			}
			catch (FileNotFoundException e) {
				Log.e(TAG, "LayerMapsForge error theme=" + theme, e);
			}
		}
//		}
		if (!success) {
			if (DEBUG) Log.d(TAG, "LayerMapsForge fallback to default theme");
			((TileRendererLayer) tileLayer).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		}
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
