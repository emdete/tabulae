package org.pyneo.tabulae.map;

import android.util.Log;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.pyneo.tabulae.Tabulae;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Bing satellite based layer
 */
class LayerGoogleSat extends LayerB {
	static final public String ID = "google_sat";

	LayerGoogleSat(Tabulae activity, MapView mapView) {
		super(activity, mapView, ID);
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition,
			new Source(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(tileLayer);
		setVisible(false);
	}

	String getId() {
		return ID;
	}

	byte getZoomLevelMax() {
		return (byte)19;
	}

	static class Source extends OnlineTileSource {
		Source() {
			super(new String[]{"khms1.google.com", }, 80);
			setAlpha(false);
			setBaseUrl("/kh/v=140");
			setExtension("png");
			setName(ID);
			setParallelRequestsLimit(8);
			setProtocol("http");
			setTileSize(256);
			setZoomLevelMax(getZoomLevelMax());
			setZoomLevelMin(getZoomLevelMin());
		}

		@Override public URL getTileUrl(Tile tile) throws MalformedURLException {
			StringBuilder stringBuilder = new StringBuilder()
				.append(getBaseUrl())
				.append(getExtension());
			URL url = new URL(getProtocol(), getHostName(), port, stringBuilder.toString());
			return url;
		}
	}
}
