package org.pyneo.tabulae.map;

import android.util.Log;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.MultiMapDataStore;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.pyneo.tabulae.Tabulae;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Bing satellite based layer
 */
class LayerBingSat extends LayerB {
	static final public String ID = "bing_sat";

	LayerBingSat(Tabulae activity, MapView mapView) {
		super(activity, mapView, true);
		tileLayer = new TileDownloadLayer(tileCache, mapView.getModel().mapViewPosition,
			new Source(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(tileLayer);
		setVisible(false);
	}

	byte getZoomLevelMax() {
		return (byte)19;
	}

	String getId() {
		return ID;
	}

	static class Source extends OnlineTileSource {
		protected static final char[] NUM_CHAR = {'0', '1', '2', '3'};

		private String encodeQuadTree(int zoom, int tilex, int tiley) {
			char[] tileNum = new char[zoom];
			for (int i = zoom - 1; i >= 0; i--) {
				// Binary encoding using ones for tilex and twos for tiley. if a
				// bit is set in tilex and tiley we get a three.
				int num = (tilex % 2) | ((tiley % 2) << 1);
				tileNum[i] = NUM_CHAR[num];
				tilex >>= 1;
				tiley >>= 1;
			}
			return new String(tileNum);
		}

		Source() {
			super(new String[]{"ecn.t1.tiles.virtualearth.net", }, 80);
			setAlpha(false);
			setBaseUrl("/tiles/a");
			setExtension(".jpeg?g=1134&n=z");
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
				.append(encodeQuadTree(tile.zoomLevel, tile.tileX, tile.tileY))
				.append(getExtension());
			URL url = new URL(getProtocol(), getHostName(), port, stringBuilder.toString());
			//Log.d(TAG, "getTileUrl url=" + url);
			return url;
		}
	}
}
