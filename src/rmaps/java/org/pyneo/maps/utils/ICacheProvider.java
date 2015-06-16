package org.pyneo.maps.utils;

public interface ICacheProvider {
	byte[] getTile(final String aURLstring, final int aX, final int aY, final int aZ);

	void putTile(final String aURLstring, final int aX, final int aY, final int aZ, final byte[] aData) throws RException;

	void Free();

	double getTileLenght();

	void deleteTile(final String aURLstring, final int aX, final int aY, final int aZ);
}
