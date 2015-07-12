package org.pyneo.tabulae.map;

import android.util.Log;

public class ParedTileSource implements Constants {
	protected static final char[] NUM_CHAR = {'0', '1', '2', '3'};

		//TileSourceFactory.addTileSource(new ParedTileSource("Outdooractive: Map", 0, 18, 256, new String[]{"http://s3.outdooractive.com/portal/map/{z}/{x}/{y}.png", }));
		//TileSourceFactory.addTileSource(new ParedTileSource("BING: Satellite", 1, 19, 256, new String[]{"http://ecn.t1.tiles.virtualearth.net/tiles/a{ms}.jpeg?g=1134&n=z", }));
	public ParedTileSource(final String aName, final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels, final String[] aBaseUrl) {
	}

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

	//public String getTileURLString(final MapTile aTile) {
		//String url = getBaseUrl()
			//.replace("{x}", Integer.toString(aTile.getX()))
			//.replace("{y}", Integer.toString(aTile.getY()))
			//.replace("{z}", Integer.toString(aTile.getZoomLevel()))
			//.replace("{ms}", encodeQuadTree(aTile.getZoomLevel(), aTile.getX(), aTile.getY()));
		//if (DEBUG) Log.d(TAG, "ParedTileSource.getZoomLevel: url=" + url);
		//return url;
	//}
}
