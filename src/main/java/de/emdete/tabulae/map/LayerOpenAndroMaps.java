package de.emdete.tabulae.map;

import android.util.Log;
import de.emdete.tabulae.Constants;
import java.io.File;
import java.io.FileNotFoundException;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import de.emdete.tabulae.Tabulae;

/**
 * Default vector bases layer
 */
class LayerOpenAndroMaps extends LayerBase {
	static final String ID = "openandromaps";

	LayerOpenAndroMaps(Tabulae activity, MapView mapView) {
		super(activity, mapView, false);
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
		File mapsDir = new File(activity.getMapsDir(), ID);
		if (de.emdete.tabulae.Constants.DEBUG) Log.d(Constants.TAG, "LayerOpenAndroMaps searching in mapsDir=" + mapsDir);
		File[] maps = mapsDir.listFiles();
		if (maps != null) for (File map : maps) {
			if (map.isFile() && map.getPath().endsWith(".map")) {
				try {
					multiMapDataStore.addMapDataStore(new MapFile(map), true, true);
					if (Constants.DEBUG) Log.d(Constants.TAG, "LayerOpenAndroMaps loaded map=" + map);
				}
				catch (Exception e) {
					Log.e(Constants.TAG, "LayerOpenAndroMaps error map=" + map, e);
				}
			}
		}
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore,
				mapView.getModel().mapViewPosition, false, true, true, AndroidGraphicFactory.INSTANCE);
		boolean success = false;
		File themesDir = new File(mapsDir, "themes");
		File theme = new File(themesDir, "andromaps_light.xml");
//		File[] themes = themes.listFiles();
//		for (File theme: themes) {
		if (theme.isFile() && theme.getPath().endsWith(".xml")) {
			try {
				((TileRendererLayer) tileLayer).setXmlRenderTheme(new ExternalRenderTheme(theme));
				if (Constants.DEBUG) Log.d(Constants.TAG, "LayerOpenAndroMaps loaded theme=" + theme);
//					break;
				success = true;
			}
			catch (FileNotFoundException e) {
				Log.e(Constants.TAG, "LayerOpenAndroMaps error theme=" + theme, e);
			}
		}
//		}
		if (!success) {
			if (Constants.DEBUG) Log.d(Constants.TAG, "LayerOpenAndroMaps fallback to default theme");
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

	static boolean isAvaiable(Tabulae activity) {
		File mapsDir = new File(activity.getMapsDir(), ID);
		File[] maps = mapsDir.listFiles();
		if (maps != null) for (File map : maps) {
			if (map.isFile()) {
				return true;
			}
		}
		return false;
	}
}
