package de.emdete.tabulae.map;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import de.emdete.tabulae.Tabulae;

/**
 * Map Quest based layer
 */
class LayerWanderreitKarte extends LayerBase {
	static final String ID = "wanderreitkarte";

	LayerWanderreitKarte(Tabulae activity, MapView mapView) {
		super(activity, mapView, true);
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition, new Source(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(0, tileLayer);
		setVisible(false);
	}

	String getId() {
		return ID;
	}

	byte getZoomLevelMax() {
		return (byte) 19;
	}

	static class Source extends OnlineTileSource {
		Source() {
			super(new String[]{"www.wanderreitkarte.de"}, 80);
			userAgent = de.emdete.tabulae.Constants.USER_AGENT;
			followRedirects = false;
			setAlpha(false);
			setBaseUrl("/topo/");
			setExtension("png");
			setName(ID);
			setParallelRequestsLimit(8);
			setProtocol("http");
			setTileSize(256);
			setZoomLevelMax(getZoomLevelMax());
			setZoomLevelMin(getZoomLevelMin());
		}
	}
}
