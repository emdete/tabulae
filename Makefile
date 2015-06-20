#!/usr/bin/env make -f
.PHONY: all run clean doc dist dbg art gradle

all: src/main/res/values/strings_news.xml
	./gradlew -q assembleRelease

run: src/main/res/values/strings_news.xml
	./gradlew -q assembleDebug

dbg: run
	adb install -r build/outputs/apk/*-debug.apk
	adb shell am start org.pyneo.tabulae/.Tabulae

clean:
	./gradlew -q clean

src/main/res/values/strings_news.xml: CHANGELOG
	awk -f $<.awk < $< > $@

