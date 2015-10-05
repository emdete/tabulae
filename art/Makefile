# see https://inloop.github.io/svg2android/
# see https://www.google.com/design/icons/
all: poi_blue.svg ic_launcher.png
	./gen.py

run: all

dbg: all

ic_launcher.png: ic_launcher.svg
	inkscape -e $@ -C -h 512 -w 512 $<

poi_blue.svg: poi_black.svg
	# do all the others too
	./gen.sh

