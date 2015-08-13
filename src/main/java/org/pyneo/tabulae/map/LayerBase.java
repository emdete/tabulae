package org.pyneo.tabulae.map;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.pyneo.tabulae.Tabulae;

import java.io.File;

/**
 * Base of the layers, adds features like proper hide, force zoom limits, ...
 */
abstract class LayerBase implements Constants {
	protected MapView mapView;
	protected TileLayer tileLayer;
	protected TileCache tileCache;
	protected InMemoryTileCache memCache;

	LayerBase(Tabulae activity, MapView mapView, boolean persistant) {
		this.mapView = mapView;
		if (persistant) {
			memCache = new InMemoryTileCache(10);
			tileCache = new TwoLevelTileCache(
				memCache,
				new FileSystemTileCache(99999, new File(activity.getTilesDir(), getId()), AndroidGraphicFactory.INSTANCE, true)
				);
		}
		else {
			tileCache = AndroidUtil.createTileCache(activity, getId(), mapView.getModel().displayModel.getTileSize(),
				.9f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		}
	}

	byte getZoomLevelMin() {
		return (byte)4;
	}

	byte getZoomLevelMax() {
		return (byte)23;
	}

	abstract String getId();

	void setVisible(boolean visible) {
		tileLayer.setVisible(visible);
		if (visible) {
			mapView.getModel().mapViewPosition.setZoomLevelMin(getZoomLevelMin());
			mapView.getModel().mapViewPosition.setZoomLevelMax(getZoomLevelMax());
		}
		else {
			if (memCache != null) memCache.purge();
		}
	}

	void onDestroy() {
		mapView.getLayerManager().getLayers().remove(tileLayer);
		tileLayer.onDestroy();
		tileCache.destroy();
	}

	public void onPause() {
		if (tileLayer instanceof TileDownloadLayer)
			((TileDownloadLayer)tileLayer).onPause();
	}

	public void onResume() {
		if (tileLayer instanceof TileDownloadLayer)
			((TileDownloadLayer)tileLayer).onResume();
	}
}
