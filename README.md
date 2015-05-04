Tabulae
=======

Overview
--------

This is a simple rastermap viewing app with the following features:

*	Show maps from various providers like OSM, Google, BING, MapQuest, ArcGIS, Yandex and more

*	Record and show tracks

*	Record, show and edit POIs

*	Download tiles for offline usage

-	Share your location, your POIs or your track with others

-	Have a dashboard with configureabe parameters

Uses
----

The app uses the following libraries:

[osmdroid](https://github.com/osmdroid/osmdroid)

[ManuelPeinado](https://github.com/ManuelPeinado/MultiChoiceAdapter)

[cwac-loaderex](https://github.com/commonsguy/cwac-loaderex)

[android-ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference)

[filemanager](https://github.com/openintents/filemanager)

All libraries are copied as source into the project tree instead of
using subprojects or the like.

Debugging
---------

The app does alot of debug logging but that level must be enabled. you can do
so by issuing `setprop log.tag.org.pyneo.maps DEBUG` in a shell on the
device.

ToDos / known Bugs
------------------

- fix bug displaying multiple stored tracks on different zooms

- fix bug on scale limits of maps

- fix bug on location provider usage

- refactor code, remove as much code as possible

- allow auto generated colors when writing tracks

- allow multiple moving positions to see friends (for example via intent from Conversations)

- discard tiles from cache after time

- re check all map provider

- display tiles with error from download better

- go on when download stucks (dislpay cached ones with higher prio)

- fix bug in db usage (already closed)

- add pixel zoom

- make it possible to have follow not centered

- make https aware (disable all http tile provider)

Origin
------

This app is a fork from RMaps found at

[robertprojects](https://code.google.com/p/robertprojects/source)

the app is described at

[robertdeveloper](http://robertdeveloper.blogspot.com/2009/08/rmaps.html)


email of the developer is

[robertk506@gmail.com](mailto:robertk506@gmail.com)

