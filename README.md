Tabulae
=======

Origin
------

This app is a fork from RMaps found at

[robertprojects](https://code.google.com/p/robertprojects/source)

the app is described at

[robertdeveloper](http://robertdeveloper.blogspot.com/2009/08/rmaps.html)


email of the developer is

[robertk506@gmail.com](mailto:robertk506@gmail.com)

Uses
----

[osmdroid](https://github.com/osmdroid/osmdroid)

[ManuelPeinado](https://github.com/ManuelPeinado/MultiChoiceAdapter)

[cwac-loaderex](https://github.com/commonsguy/cwac-loaderex)

[android-ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference)

[filemanager](https://github.com/openintents/filemanager)

All libraries are copied as source into the project tree instead of
using subprojects or the like.

Changes
-------

I started to do some cosmetic changes:

-	use utf-8 encoding of the sources (cp1251 destroyed the display of the
	degree sign)

-	fix naming of activies (the head version seemed to be a development stuck
	in the middle)

-	remove google analytics

-	adapt some requirements for modern api (ActionBar)

-	reorder menu

-	translate russian comments

-	reformat sources

-	move build to gradle

-	dos2unix line endings

-	rename to Tabulae

ToDos
=====

- improve map selection

- refactor code

- remove as much external libraries as possible

- allow multiple moving positions (to see friends) via api

