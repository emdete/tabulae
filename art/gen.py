#!/usr/bin/env python3
from os import listdir, system
from PIL.Image import frombytes, open as fromfile, eval as image_eval, merge as image_merge
from PIL.ImageOps import invert, autocontrast, grayscale, equalize, solarize
from gi.repository import Rsvg
# TODO use cairo like in
#import cairo
#surface = cairo.SVGSurface('output.svg', width, height)
#context = cairo.Context(surface)
#svg.render_cairo(context)

# see https://developer.android.com/guide/practices/screens_support.html
# see https://developer.android.com/design/style/iconography.html

# So, to create an icon for different densities, you should follow the
# 2:3:4:6:8 scaling ratio between the five primary densities (medium, high,
# x-high, xx-high, and xxx-high respectively). For example, consider that the
# size for a launcher icon is specified to be 48x48 dp. This means the baseline
# (MDPI) asset is 48x48 px, and the high-density(HDPI) asset should be 1.5x the
# baseline at 72x72 px, and the x-high density (XHDPI) asset should be 2x the
# baseline at 96x96 px, and so on.
SIZES = (
		(16, 'xxxh', ),
		(12, 'xxh', ),
		(8, 'xh', ),
		(6, 'h', ),
		(4, 'm', ),
		#(3, 'l', ),
	)

def glob(w):
	for n in listdir('.'):
		if n.endswith(w):
			yield n[:-len(w)]

def conv_svg(width, height, basename):
	for d, suffix in SIZES:
		system("inkscape -e ../src/main/res/drawable-{}dpi/{}.png -C -w {} -h {} {}.svg".format(
			suffix, basename, int(width * d / 4), int(height * d / 4), basename,
			))

handle = Rsvg.Handle()
for n in glob('.svg'):
	svg = handle.new_from_file(n+'.svg')
	width, height = svg.get_properties('width', 'height')
	conv_svg(width, height, n)

