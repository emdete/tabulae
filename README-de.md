Tabulae
=======

![screenshots](https://raw.githubusercontent.com/emdete/Tabulae/master/function.jpg)

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

![screenshot main](https://raw.githubusercontent.com/emdete/Tabulae/master/screenshot.png)
![screenshot statistics](https://raw.githubusercontent.com/emdete/Tabulae/master/screenshot-statistic.png)

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
[Conversations](market://search?q=pname:eu.siacs.conversations)).

Fehlersuche
-----------

Stürzt Tabulae ab, kann ein Fehlerbericht beim nächsten Start erzeugt
und versendet werden.

Tabulae logged mit den üblichen Android Board-Mitteln. Der Befehl
(Installation des Android SDK vorausgesetzt), um das mehr Informationen
zu erhalten lautet:

	adb shell setprop log.tag.org.pyneo.maps DEBUG

und mit dem Befehl:

	adb logcat -v time -s org.pyneo.maps

lässt sich beobachten, was Tabulae zu sagen hat.

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

