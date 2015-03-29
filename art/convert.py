#!/usr/bin/env python
from Image import ANTIALIAS, BICUBIC, open as image_open
from os.path import isdir

# see https://developer.android.com/design/style/iconography.html

# So, to create an icon for different densities, you should follow the
# 2:3:4:6:8 scaling ratio between the five primary densities (medium, high,
# x-high, xx-high, and xxx-high respectively). For example, consider that the
# size for a launcher icon is specified to be 48x48 dp. This means the baseline
# (MDPI) asset is 48x48 px, and the high-density(HDPI) asset should be 1.5x the
# baseline at 72x72 px, and the x-high density (XHDPI) asset should be 2x the
# baseline at 96x96 px, and so on.


def main(*filenames):
	for filename in filenames:
		img = image_open(filename)
		orig_w, orig_h = img.size
		th = img.copy()
		print(filename, orig_w, orig_h)
		for name, scale in dict(mdpi=2, hdpi=3, xhdpi=4, xxhdpi=6, xxxhdpi=8, ).items():
			print(name, scale/2.0)
			scale = scale/2.0
			sizedname = "../src/main/res/drawable-{name}".format(name=name, filename=filename, )
			if isdir(sizedname):
				sizedname = "../src/main/res/drawable-{name}/{filename}".format(name=name, filename=filename, )
				size = (48 * scale, 48 * scale, )
				print(sizedname, size)
				th.thumbnail(size, ANTIALIAS)
				th.save(sizedname, option=dict(quality=90, optimize=True, progression=True, ))

if __name__ == '__main__':
	from sys import argv
	main(*argv[1:])
# vim:tw=0:nowrap
