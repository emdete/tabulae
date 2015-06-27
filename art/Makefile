# https://inloop.github.io/svg2android/
all: poi_blue.svg ic_launcher.png

run: all

dbg: all
	make -C .. dbg

ic_launcher.png: ic_launcher.svg
	inkscape -e $@ -C -h 512 -w 512 $<

poi_blue.svg: poi.svg
	./gen.sh

