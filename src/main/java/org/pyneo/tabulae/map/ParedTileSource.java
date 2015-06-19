package org.pyneo.tabulae.map;

import android.util.Log;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public class ParedTileSource extends OnlineTileSourceBase implements Constants {
	protected static final char[] NUM_CHAR = {'0', '1', '2', '3'};

	public ParedTileSource(final String aName, final int aZoomMinLevel, final int aZoomMaxLevel, final int aTileSizePixels, final String[] aBaseUrl) {
		super(aName, null, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels, null, aBaseUrl);
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

	@Override
	public String getTileURLString(final MapTile aTile) {
		String url = getBaseUrl().replace("{x}", Integer.toString(aTile.getX())).replace("{y}", Integer.toString(aTile.getY())).replace("{z}", Integer.toString(aTile.getZoomLevel())).replace("{ms}", encodeQuadTree(aTile.getZoomLevel(), aTile.getX(), aTile.getY()));
		if (DEBUG) Log.d(TAG, "ParedTileSource.getZoomLevel: url=" + url);
		return url;
	}
}
