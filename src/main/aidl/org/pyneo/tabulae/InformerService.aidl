package org.pyneo.tabulae;

import org.pyneo.tabulae.Informer;

interface InformerService {
	void registerCallback(Informer cb);
	void unregisterCallback(Informer cb);
}
