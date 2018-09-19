#!/usr/bin/env make -f
.PHONY: all run clean doc dist dbg art gradle

all: src/main/res/values/strings_news.xml
	./gradlew --quiet --offline assembleRelease

run: src/main/res/values/strings_news.xml
	./gradlew --quiet assembleDebug

dbg: run
	chmod 0644 art/ic_launcher.png build/outputs/apk/debug/*-debug.apk
	#rsync --verbose --archive build/outputs/apk/debug/*-debug.apk littlun.emdete.de:/var/www/emdete.de/y/.
	#rsync --verbose --archive art/ic_launcher.png littlelun.emdete.de:/var/www/emdete.de/y/Tabulae-logo.png
	adb install -r build/outputs/apk/debug/*-debug.apk
	adb shell am start de.emdete.tabulae/.Tabulae

clean:
	./gradlew -q clean

src/main/res/values/strings_news.xml: CHANGELOG
	awk -f $<.awk < $< > $@

