Tabulae
=======

![screenshots](https://raw.githubusercontent.com/emdete/Tabulae/master/art/function.jpg)

Übersicht
---------

Tabulae ist eine App zum Anzeigen von verschiedensten Karten. Es zeigt
darauf die eigene Position, POIs, aufgezeichnete oder importierte
Wegstrecken und Streckenplanungen.

Es ist mit Gesten steuerbar (zoomen mit zwei Fingern) aber wesentlich wurde
Wert auf die Einhandbedienung gelegt um die Nutzung beim Sport zu vereinfachen.

Die Karten können vorab heruntergeladen werden, was die Nutzung in
Gegenden mit schlecher Netzabdeckung erlaubt. Die heruntergeladenen
Karten werden gespeichert um Bandbreite zu sparen. Kartenanbieter können
frei definiert werden, einige Beispiele sind vorkonfiguriert. Darüber
hinaus können eigene Karten direkt aus Dateien verwendet werden.

Auf verschiedene Weise können Positionen mit anderen Apps und Nutzern geteilt
oder angezeigt werden. Die Wegstrecken und POIs können als gpx und kml im- und
exportiert werden.

Tabulae benötigt keinen Account zu irgendeinem Dienst, speichert keine
Daten welcher Art auch immer im Internet. Das Zurverfügung-Stellen von
Daten liegt in der Kontrolle des Benutzers. Tabulae erlaubt das Senden
und Empfangen dieser Daten, der Nutzer jedoch bestimmt, wo diese
weiterverarbeitet werden.

Tabulae meldet keine privaten Informationen des Nutzers zu einem
zentralen Server. Die einzige Möglichkeit, private Daten von Nutzern zu
erhalten wäre aus den heruntergeladenen Kartenteile die grobe Position
des Nuzters zu ermitteln.

Tabulae enthält keine Werbung.

Die Quelltexte des Programms sind für jeden einsehbar und damit überprüfbar.

![screenshot main](https://raw.githubusercontent.com/emdete/Tabulae/master/art/screenshot.png){:width="600px"}

![screenshot statistics](https://raw.githubusercontent.com/emdete/Tabulae/master/art/screenshot-statistic.png){:width="600px"}

![screenshot portrait](https://raw.githubusercontent.com/emdete/Tabulae/master/art/screenshot-portrait.png){:width="600px"}

Bedienung
---------

### Instrumentenbrett

Das Instrumentenbrett (Anzeige von verschiedensten Werten über der Karte) kann
über das Menu ab- und angeschaltet werden. Auch jedes einzelne Element kann
entfernt und neue hinzugefügt werden (lange auf ein Element klicken) und die
Art des Wertes ausgewählt werden.

### Kartenauswahl

Der Anbieter der Karte kann aus einem Menü gewählt werden. Viele Anbieter sind
vorkonfiguriert und es können Strassekarten, Topographische Karten,
Stellitenbilder und andere ausgewählt werden.

Dem Benutzer obligt zu prüfen, ob der ausgewählte Kartenanbieter der Nutzung
zustimmt.

### Vector

Die App nutzt wo es geht Vector-Grafiken. Alle Icons sind Vectoren (die aus
SVGs konvertiert werden) um die leidige DPI Abhängigkeit zu umgehen
(https://developer.android.com/training/material/drawables.html). Auch
Vektorkarten werden genutzt (mapsforge).

### Wegstrecke

Wegstrecken können aufgezeichnet werden. Die Aufzeichnung kann pausiert werden.
Die Strecke ist in einer internen Datenbank gespeichert werden und in übliche
Formate (kml und gpx) exportiert werden und in anderen Programmen
weiterverarbeitet werden.

### POI

POI (Position von Interesse) können markiert werden. Sie können "geteilt",
"gesendet" und exportiert werden.

### Vermessen

Zur Planung einer Strecke kann diese vorab erfasst und vermessen werden.

### Kooperation

Die App kann die aktuelle Position mit einem Chatprogramm teilen (siehe
"[Conversations](market://search?q=pname:eu.siacs.conversations)").

Map Download
------------

Zur Vereinfachung des Installierens von Karten stellt pyneo rechteckige Karten
zum Download bereit. Sie sind unter

	[maps](https://pyneo.org/maps/)

verfügbar. Die Namen der Rechtecke werden aus lat/lon der unteren, linken Ecke
gebildet. Die Größe ist 1°. Später sollen diese Dateien von der App direkt
geladen werden (darum Rechtecke und Namen, die ein Program einfacher bestimmen
kann als "bremen.map" aus gegebener lat/lon). Im moment müssen diese manuell
heruntergeladen werden und and die richtige Stelle installiert werden. Tabulae
nutzt das Verzeichnis, das getExternalFilesDirs() zurückgibt. Initial wird
dasjenige Verzeichnis gewählt, das am meisten freien Platz bereitstellt. In
einem Gerät mit eingelegter zweiten SD müssen die map-Dateien für mapsforge
dann in

	/storage/sdcard1/Android/data/org.pyneo.tabulae/files/maps/mapsforge

installiert werden. Diejenigen für openandromaps (siehe
[openandromaps](http://www.openandromaps.org/)) entsprechend in

	/storage/sdcard1/Android/data/org.pyneo.tabulae/files/maps/openandromaps

Da diese map-Dateien eigene Themes benötigen, müssen auch diese installiert
werden. Der Pfad dahin ist dann:

	/storage/sdcard1/Android/data/org.pyneo.tabulae/files/maps/openandromaps/themes

Fehlersuche
-----------

Stürzt Tabulae ab, kann ein Fehlerbericht beim nächsten Start erzeugt
und versendet werden.

Tabulae logged mit den üblichen Android Board-Mitteln. Mit dem Befehl:

	adb logcat -v time -s org.pyneo.maps

lässt sich beobachten, was Tabulae zu sagen hat. Ein Debug-Build (siehe
nächstes Kapitel) logged mehr Informationen.

Selbst Compilieren
------------------

Die Quellen von Tabulae lassen sich von

	https://github.com/emdete/tabulae

herunterladen und mit dem Befehl

	./gradlew assembleRelease

übersetzen.

Kontakt
-------

Mails mit Fehlerberichten, Erweiterungswünschen, Bargeld und
Danksagungen an:

	tabulae@pyneo.org

