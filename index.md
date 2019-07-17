*For an English version see [here](english.md)*

----------------

**Übersicht**

- [Android Einstellungen](#android-einstellungen)
    - [Energiesparmodus](#energiesparmodus)
    - [GPS Standortservice](#gps-standortservice)
    - [Hintergrundverarbeitung](#hintergrundverarbeitung)
- [Herstellerspezifische Einstellungen](#herstellerspezifische-einstellungen)
    - [Huawei und Honor](#huawei-und-honor)
    - [Xiaomi und ZTE](#xiaomi-und-zte)
    - [Samsung](#samsung)
    - [Sony](#sony)
- [FAQ](#faq)

----------------

Unser [Android SDK](https://github.com/cyface-de/android-backend/) hält sich an die offizielle Dokumentation von
Android um ein unterbrechungsfreies GPS tracking im Hintergrund zu ermöglichen.

Im Folgenden sind Systemeinstellungen beschrieben die die Hintergrunderfassung beeinflussen können. 


Android Einstellungen
----------------------------------

**Diese Einstellungen prüfen wir automatisch in der App:**

Mit Android 8 wurden mehrere Einstellungen eingeführt um Batterielaufzeit zu verlängern.

Diese Einstellungen ermöglichen dem Smartphonebesitzer zu bestimmen welche Apps batterieintensive
Funktionen nutzen dürfen - abhängig von der App Nutzung und dem Energiesparmodus.

Das ganze ist von Google dokumentiert und erlaubt App Entwicklern die Einstellungen automatisch zu prüfen
und den Nutzer auf problematische Einstellungen hinzuweisen.

### Energiesparmodus

Darf beim Tracking nicht aktiv sein.

Grund: Die GPS Position wird nicht mehr neu berechnet sobald der Display ausgeschaltet ist.

**Korrekte Einstellung:**

- (Android 9) Einstellungen - Akku - Energiesparmodus - Jetzt deaktivieren

**Achtung:** Falls Sie eingestellt haben, dass der Energiesparmodus bei geringem Batteriestand automatisch aktiviert wird
sollten Sie das berücksichtigen.

### GPS Standortservice

Muss aktiviert sein.
 
Grund: Wir verwenden GPS um möglichst genaue Position zu erfassen.

**Korrekte Einstellung:**

- (Android 9) Einstellungen - Sicherheit & Standort - Standort - aktivieren

**Achtung:** Auf manchen Geräten kann man zwischen GPS und anderen Standortdiensten unterscheiden. Hier muss GPS (bzw. "hohe Genauigkeit") ausgewählt sein.

### Hintergrundverarbeitung

Hintergrundverarbeitung darf nicht eingeschränkt sein.

Grund: Das Tracking wird ansonsten pausiert oder unterbrochen sobald die App minimiert oder der Display ausgeschaltet wird.

Betrifft: **Android 8 und neuer**

Auf vielen Geräten ist diese Einstellung bereits korrekt vorausgewählt. Lediglich einzelne Hersteller schränken Apps standardmäßig ein.

**Korrekte Einstellung:**

- (Android 9) Einstellungen - Apps - Alle Apps - Unsere App auswählen - Erweitert - Akku > Hintergrundnutzung einschränken
    - hier muss **Akkunutzung im Hintergrund zulassen** ausgewählt sein


Herstellerspezifische Einstellungen
---------------------------------------

Einige Hersteller nutzen noch eigene, undokumentierte EnergieEinstellungen.

Vor allem die Android-Varianten von Huawei ("EMUI"), Xiaomi ("MIUI") verhindern
standardmäßig auch außerhalb des Energiesparmodus die kontinuierliche Aufzeichnung
im Hintergrund.

Problematisch wird dies dadurch, dass das Verhalten hier nicht dokumentiert ist und man
nicht automatisch sichergehen kann, dass alle Einstellungen so sind, wie sie für das
Tracking notwendig sind. Manche Hersteller setzen diese Einstellungen sogar zum Ärgernis
aller Beteiligten bei Updates zurück.

Dadurch müssen wir Besitzer von den Betroffenen Geräten bitten, die verlinkten
herstellerspezifischen Einstellungen zu prüfen und ggf. anzupassen.

### Huawei und Honor

Betroffen sind unter anderen: 

- Mate 20 (Pro und Lite)
- Mate 10 (Pro und Lite)
- Mate 9
- P10, P20 (Lite)
- P8 (Lite)
- Honor 8 und 8X
- FIX-LX1

#### Anleitung für Huawei's EMUI

##### EMUI ab Android 8

* Einstellungen - Akku - <b>App-Start</b> auf <b>Manuell Verwalten</b> setzen und <b>alle Optionen dort aktivieren</b>

##### EMUI bis Android 6 oder 7

* Einstellungen - Erweiterte Einstellungen - <b>Akkumanager</b> - <b>Geschützte Apps</b> - unsere App zu den geschützten Apps hinzufügen

### Xiaomi und ZTE

Betroffen sind unter anderen:

- MI6
- Redmi Note 4
- Redmi Note 6 (Pro) / ZTE Axon 7 Mini

#### MIUI ab Android 8 (?)

**Korrekte Einstellungen:**

- (Android 8.1) Einstellungen - Akku + Leistung - <b>App-Energiesparmodus</b> - unsere App auswählen - Energiesparmodus <b>auf "Keine Beschränkungen"</b>
- (Android 8.1) Einstellungen - Akku + Leistung - Sonstige Berechtigungen - <b>Starten im Hintergrund auf "erlaubt"

Falls Sie diese Einstellungen nicht finden, suchen Sie nach folgenden Einstellungen:

- Einstellungen - Berechtigungen - <b>Auto-Start</b> - unsere App auswählen - "Erlaube App automatisch zu starten" auf <b>"erlaubt"</b>

### Samsung

Möglicherweise betroffen sind unter anderem:

- Galaxy S9, S8, S7 (edge)
- Galaxy A7

#### Anleitung für Samsung

##### Samsung ab Android 7

**Korrekte Einstellungen:**

- Einstellungen - <b>Gerätewartung - Akku</b> - Unsere App zu den <b>Nicht überwachten Apps</b> hinzuzufügen

Falls das nicht klappt oder dieser Punkt nicht existiert sollte folgendes helfen:

- Einstellungen - Gerätewartung - <b>Akku</b> - oben rechts 3-Punkte-Button - Einstellungen
    - hier <b>alle Einstellungen außer "Benachrichtigungen" deaktivieren</b>
    - Nun unter dem Punkt <b>Schlafende Apps</b> sicher gehen, dass unsere App nicht aufgelistet ist.

##### Samsung Android 5-6

Diese Einstellung müsste standardmäßig korrekt eingestellt sein.

**Korrekte Einstellung:**

- (Android 6) Einstellungen - Smart Manager - <b>Akku</b> - <b>App-Energiesparmodus</b> - Details - Unsere App auswählen - auf <b>Deaktiviert</b>

### Sony

Auf Sony Geräten gibt es einen herstellerspezifischen Energiesparmodus namens <b>STAMINA</b>.
In diesem Modus wird das Hintergrundtracking automatisch vom System beendet.

Leider kann dieser spezielle Energiesparmodus nicht automatisch von der App geprüft werden.

Falls Sie Probleme mit der Aufzeichnung haben deaktivieren Sie bitte STAMINA komplett.


FAQ
-----------------------------------

### Wieso schließen wir die betroffenen Geräte nicht komplett aus?

Fast alle Geräte lassen sich manuell so einstellen, dass man die App auch auf sehr restriktiven Geräten ohne Probleme nutzen kann.

### Andere Apps laufen auch auf meinem Gerät - wieso schafft ihr das nicht ohne mein Zutun?

Auch andere, größere App-Entwickler wie z.B. [Runtastic](https://help.runtastic.com/hc/de/articles/212633165-Hilfe-bei-GPS-Problemen-auf-bestimmten-Android-Phones),
[Komoot](https://support.komoot.com/hc/de/articles/360023076311-Die-Aufzeichnung-bricht-ab-Android-Ger%C3%A4te-),
[Sleep on Android](http://sleep.urbandroid.org/documentation/faq/alarms-sleep-tracking-dont-work/),
[Zombie, Run!](https://www.reddit.com/r/Android/comments/8qjhx7/huawei_oneplus_and_xiaomis_overlyaggressive/),
[Strava](https://www.rennrad-news.de/forum/threads/strava-android-app-abst%C3%BCrze.148864/),
[Lady Pill Reminder](https://baviux.com/ladypillreminder/troubleshooting/), "Bikecitizenz", "Strava" und "Garmin" haben auch damit zu kämpfen.

An Google wurde das Problem des gemeldet, derzeit noch mit [wenig sichtbaren Erfolg](https://issuetracker.google.com/issues/122098785).

Prinzipiell kann man “Workarounds” einbauen (z.B. indem man das Gerät regelmäßig aufweckt). Leider klappt das auch nicht immer, d.h. man muss hier viele Workarounds kombinieren und testen.
Das hat jedoch negative Auswirkungen auf die Batterielaufzeit für die Nutzer, bei denen das Tracking mit den richtigen Einstellungen funktioniert.
Außerdem müssten wir viele Test-Handys kaufen, sehr viel Zeit investieren um das undokumentierte Verhalten aller betroffenen Modelle zu analisieren und bei Updates prüfen, ob sich das Verhalten geändert hat.

Da wir nur ein sehr kleines Team sind das sich um die Entwicklung kümmert, können wir das leider nicht leisten.

### Wieso verhalten sich die Smartphones der verschiedenen Hersteller so unterschiedlich?

Diese restriktiven Einstellungen sind unseres Wissens von den chinesischen Herstellern v.a. für den chinesischen Markt implementiert worden,
da das Gesetz vor Ort kein Hintergrundtracking ohne Weiteres erlaubt.

Nokia hat z.B. etwas ähnliches implementiert, jedoch Anfang 2019 beschlossen, diese Einschränkung nur noch auf dem
Chinesischen Markt zu aktivieren und hält sich in Europa und den USA wieder an die offiziellen und dokumentieren Vorgaben von Android.

Die Hoffnung besteht, dass die anderen Hersteller hier irgendwann mitziehen könnten.
