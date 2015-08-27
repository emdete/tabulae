package org.pyneo.tabulae;

import android.os.Bundle;

oneway interface Informer {
	void inform(int msg, in Bundle extra);
}
