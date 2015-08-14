package org.pyneo.tabulae.map;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.pyneo.tabulae.Tabulae;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Google satellite based layer
 */
class LayerGoogleSat extends LayerBase {
	static final String ID = "google_sat";
	static final String strGalileo = new String("Galileo");

	LayerGoogleSat(Tabulae activity, MapView mapView) {
		super(activity, mapView, true);
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition,
			new Source(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(0, tileLayer);
		setVisible(false);
	}

	String getId() {
		return ID;
	}

	byte getZoomLevelMax() {
		return (byte)18;
	}

	static class Source extends OnlineTileSource {
		Source() {
			super(new String[]{"khms%d.google.com", }, 80);
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
			int servernum = (tile.tileX + 2 * tile.tileY) % 4;
			return new URL(getProtocol(), String.format(getHostName(), servernum), port, new StringBuilder()
				.append(getBaseUrl())
				.append("&hl=").append("en")
				.append("&src=").append("app")
				.append("&x=").append(tile.tileX)
				.append("&y=").append(tile.tileY)
				.append("&z=").append(tile.zoomLevel)
				.append("&scale=").append(true)
				.append("&s=").append(strGalileo.substring(0, (tile.tileX * 3 + tile.tileY) % 8))
				.toString());
		}
	}
}
