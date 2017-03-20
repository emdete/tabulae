package de.emdete.tabulae.map;

import de.emdete.tabulae.Tabulae;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;

/**
 * Downloadlayer for outdooractive
 */
class LayerOutdoorActive extends LayerBase {
	static final String ID = "outdoor_active";

	LayerOutdoorActive(Tabulae activity, MapView mapView) {
		super(activity, mapView, true);
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition, new Source(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(0, tileLayer);
		setVisible(false);
	}

	String getId() {
		return ID;
	}

	byte getZoomLevelMin() {
		return (byte) 8;
	}

	byte getZoomLevelMax() {
		return (byte) 17;
	}

	static class Source extends OnlineTileSource {
		Source() {
			super(new String[]{"s3.outdooractive.com",}, 80);
			setAlpha(false);
			setBaseUrl("/portal/map/");
			setExtension("png");
			setName("Outdoor Active");
			setParallelRequestsLimit(4);
			setProtocol("http");
			setTileSize(256);
			setZoomLevelMax(getZoomLevelMax());
			setZoomLevelMin(getZoomLevelMin());
		}
	}
}
