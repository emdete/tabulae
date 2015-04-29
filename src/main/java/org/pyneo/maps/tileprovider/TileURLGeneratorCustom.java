package org.pyneo.maps.tileprovider;

public class TileURLGeneratorCustom extends TileURLGeneratorBase {
	private final static String X = "{x}";
	private final static String Y = "{y}";
	private final static String Z = "{z}";

	public TileURLGeneratorCustom(String baseurl) {
		super(baseurl);
	}

	@Override
	public String Get(int x, int y, int z) {
		return mName
			.replace(X, Integer.toString(x))
			.replace(Y, Integer.toString(y))
			.replace(Z, Integer.toString(z))
			;
	}
}
