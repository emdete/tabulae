#!/bin/sh -eu
for n in poi attribute; do
	sed 's/fill:#000000;/fill:#ffdd00;/' < ${n}.svg > ${n}_yellow.svg
	sed 's/fill:#000000;/fill:#0000dd;/' < ${n}.svg > ${n}_blue.svg
	sed 's/fill:#000000;/fill:#ff0000;/' < ${n}.svg > ${n}_red.svg
	sed 's/fill:#000000;/fill:#ffffff;/' < ${n}.svg > ${n}_white.svg
	sed 's/fill:#000000;/fill:#00ee00;/' < ${n}.svg > ${n}_green.svg
done
