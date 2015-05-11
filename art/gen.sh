#!/bin/sh -eu
sed 's/fill:#000000;/fill:#ffdd00;/' < poi.svg > poi_yellow.svg
sed 's/fill:#000000;/fill:#0000dd;/' < poi.svg > poi_blue.svg
sed 's/fill:#000000;/fill:#ff0000;/' < poi.svg > poi_red.svg
sed 's/fill:#000000;/fill:#ffffff;/' < poi.svg > poi_white.svg
sed 's/fill:#000000;/fill:#00ee00;/' < poi.svg > poi_green.svg
