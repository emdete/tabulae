package de.emdete.tabulae.map;

import de.emdete.tabulae.Tabulae;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;

/**
 * Map Quest based layer
 */
class LayerMapQuest extends LayerBase {
	static final String ID = "mapquest";

	LayerMapQuest(Tabulae activity, MapView mapView) {
		super(activity, mapView, true);
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition, new Source(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(0, tileLayer);
		setVisible(false);
	}

	String getId() {
		return ID;
	}

	byte getZoomLevelMax() {
		return (byte) 18;
	}

	static class Source extends OnlineTileSource {
		Source() {
			super(new String[]{"otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com", "otile4.mqcdn.com"}, 80);
			setAlpha(false);
			setBaseUrl("/tiles/1.0.0/map/");
			setExtension("png");
			setName(ID);
			setParallelRequestsLimit(8);
			setProtocol("http");
			setTileSize(256);
			setZoomLevelMax(getZoomLevelMax());
			setZoomLevelMin(getZoomLevelMin());
		}
	}

	static boolean isAvaiable(Tabulae activity) {
		return false;
	}
}
