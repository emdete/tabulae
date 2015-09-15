#!/bin/sh -eu
for n in poi attribute; do
	sed 's/fill:#000000;/fill:#ffdd00;/' < ${n}_black.svg > ${n}_yellow.svg
	sed 's/fill:#000000;/fill:#0000dd;/' < ${n}_black.svg > ${n}_blue.svg
	sed 's/fill:#000000;/fill:#ff0000;/' < ${n}_black.svg > ${n}_red.svg
	sed 's/fill:#000000;/fill:#ffffff;/' < ${n}_black.svg > ${n}_white.svg
	sed 's/fill:#000000;/fill:#00ee00;/' < ${n}_black.svg > ${n}_green.svg
done
