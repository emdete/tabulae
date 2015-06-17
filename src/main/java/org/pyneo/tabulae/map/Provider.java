package org.pyneo.tabulae.map;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

public class Provider {
	static {
		TileSourceFactory.addTileSource(new ParedTileSource("Outdooractive: Map", 0, 18, 256, new String[]{"http://s3.outdooractive.com/portal/map/{z}/{x}/{y}.png", }));
		TileSourceFactory.addTileSource(new ParedTileSource("BING: Satellite", 1, 19, 256, new String[]{"http://ecn.t1.tiles.virtualearth.net/tiles/a{ms}.jpeg?g=1134&n=z", }));
	}
}
