# Java und Bluetooth Low Energy auf dem Raspberry Pi

Die Intention dieses Projekts ist es, einen Startpunkt für die Entwicklung auf dem Raspberry Pi mit Java + Bluetooth LE zur Verfügung zu stellen. Es handelt
sich hierbei um einen Workshop der im Rahmen vom Proseminar "Software Engineering" angeboten wird.
Es wird gezeigt wie man alles Nötige installiert und konfiguriert als auch wie man auf dem Raspberry testet (z.B. mit Java Mockito).
Weiters agiert dieses Projekt auch als Bespiel wie man GitLab CI/CD in Kombination mit Java verwendet, um Testausführung und statische Codeanalyse zu automatisieren.

## Voraussetzungen

### Grundsätzliche Einstellungen

- `Raspberry Pi OS Lite` auf SD-Karte von Raspberry Pi geflashed
- Anmeldung mit Benutzername `pi` und Passwort `raspberry` und Änderung des Passworts mit `passwd`
- Bluetooth and WLAN sind aktiviert (weder `hard` noch `soft`-blocked)
  
      sudo rfkill list all

- Aktivierung von z.B. WiFi mit (sollte WiFi der erste Eintrag sein):

      sudo rfkill unblock 0

### Verwendung von WiFI

- Setzen des WiFi-Landes mit:

      sudo raspi-config nonint do_wifi_country AT
      sudo raspi-config nonint get_wifi_country

- Siehe: [Setting up a wireless LAN via the command line](https://www.raspberrypi.org/documentation/configuration/wireless/wireless-cli.md)

- Überprüfung, ob man mit dem richtigen Netzwerk verbunden ist:

      iw wlan0 link

- Überprüfung, ob Internetverbindung besteht mit:

      ping google.com

### Verbindung mit Raspberry Pi

- SSH wurde aktiviert (Ausgabe soll 0 sein):
  
      sudo raspi-config nonint get_ssh

- Aktivierung von SSH mit:

      sudo raspi-config nonint do_ssh 0

- Hostname des Raspberry Pi ist bekannt:

      hostname -I

### Optional: Private/Public Key Authentifizierung

Siehe: [Passwordless SSH access](https://www.raspberrypi.org/documentation/remote-access/ssh/passwordless.md)

So vermeidet man beim Verbinden zum Raspberry Pi jedes Mal das Passwort einzugeben.

## 1) Installation

Mit Raspberry Pi verbinden:

    ssh pi@<RASPBERRY_IP_ADDRESS>

### a) Initiale Packages

Auf den neuesten Stand bringen:

    sudo apt update
    sudo apt upgrade

Essentielle Tools installieren:

    sudo apt install git
    sudo apt install cmake

### b) Maven und JDK

Installation der JDK 1.8:

    sudo apt install openjdk-8-jdk

Sicherstellen, dass tatsächlich JDK 1.8 verwendet wird:

    sudo update-alternatives --config java

Version überprüfen:

    java -version

Sicherstellen, dass sich in `usr/lib/jvm` nun `java-8-openjdk-armhf` befindet:

    sudo find / -name "java"

Editieren von `bashrc`:

    sudo nano ~/.bashrc


Es soll die Zeile `export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-armhf/` am Ende hinzugefügt werden und gespeichert werden.

Nun müssen wir das Terminal neu laden, um zu überprüfen, ob die Änderungen wirksam waren – notwendig für nächste Schritte:

    bash
    echo $JAVA_HOME

Maven installieren:

    sudo apt install maven

### c) Installation von BlueZ 5.47

Installation der Build-Tools:

    sudo apt install libglib2.0-dev libdbus-1-dev libudev-dev libical-dev libreadline-dev

Download des BlueZ Source-Codes (in geeignetem Directory):

    wget http://www.kernel.org/pub/linux/bluetooth/bluez-5.47.tar.xz

Das tar Archiv extrahieren und in den Ordner gehen:

    tar -xf bluez-5.47.tar.xz && cd bluez-5.47

Konfigurieren des BlueZ Projekts:

    ./configure --prefix=/usr --mandir=/usr/share/man --sysconfdir=/etc --localstatedir=/var

BlueZ builden:

    make
    sudo make install

#### BlueZ Installation überprüfen

Der BlueZ Start-up Service soll nun auf das neu gebuildete BlueZ zeigen:

    cat /lib/systemd/system/bluetooth.service

Dort sollte man die Zeile `ExecStart=/usr/libexec/bluetooth/bluetoothd` vorfinden.

Ausgabe der BlueZ Version – sollte nun `5.47` sein:

    /usr/libexec/bluetooth/bluetoothd --version

#### Erlaubnis für BlueZ hinzufügen

Damit BlueZ Zugriff auf die Bluetooth-Gruppe hat, müssen wir eine eigene Erlaubnis hinzufügen. Dafür
editiert man die BlueZ DBus Konfiguration:

    sudo nano /etc/dbus-1/system.d/bluetooth.conf

Anschließend fügt man nur die Policy für die Gruppe `bluetooth` hinzu:

```xml
<busconfig>
  <policy user="root">
    ...
  </policy>
  <policy group="bluetooth">
    <allow send_destination="org.bluez"/>
  </policy>
  ...
</busconfig>
```

#### Zusätzliche Konfiguration und neu starten

OpenHab User zur Bluetooth-Gruppe hinzufügen (wir benötigten zwar OpenHab nicht, aber zur Vollständigkeit):

    sudo adduser --system --no-create-home --group --disabled-login openhab
    sudo usermod -a -G bluetooth openhab

Service Definitionen neu laden:

    sudo systemctl daemon-reload

BlueZ neu starten:

    sudo systemctl restart bluetooth
   
Überprüfen, ob Bluetooth-Service aktiv ist, die richtige Version läuft (`5.47`) und es keine Fehler gibt:

    sudo systemctl status bluetooth
   
### d) Installation von tinyb

Installation der Abhängigkeiten von `tinyb`:

    sudo apt install graphviz
    sudo apt install doxygen
   
Klonen von tinyb (an geeigneter Stelle) und in den Ordner gehen:

    git clone https://github.com/intel-iot-devkit/tinyb.git && cd tinyb
   
Ordner `build` erstellen und hineingehen:

    mkdir build
    cd build
   
Builden von `tinyb` mit `cmake` (`-E` steht für experimental und stellt sicher, dass `JAVA_HOME` verwendet wird, `cmake ..` generiert das Makefile im aktuellen Verzeichnis basierend auf `CMakeLists.txt` im parent-Verzeichnis, der Prefix `/usr/` stellt sicher, dass sich die native Libraries `libjavatinyb.so` und `libtinyb.so` im Java Library Path befinden):

    sudo -E cmake -DBUILDJAVA=ON -DCMAKE_INSTALL_PREFIX=/usr ..

Ausführen von `make` und `make install`:

    sudo make
    sudo make install

## 2) Ausführen

Die Ausführung ist eher umständlich, da wir `tinyb.jar` nicht nur zur Compile-Zeit, sondern auch dynamisch zur Laufzeit laden müssen. Für diese Bluetooth-Library ist dies leider notwendig. Siehe auch: [Java* for Bluetooth® Low Energy Applications](https://web.archive.org/web/20190414051809/https:/software.intel.com/en-us/java-for-bluetooth-le-apps)

Sollten alle Befehle erfolgreich sein, dann können wir das Beispiel-Maven-Programm `bleclient` ausführen.
Dafür gehen wir in das Verzeichnis `bleclient` und führen das Programm basierend auf dem dort liegendem [README.md](bleclient/README.md) aus.

## 3) Optional: Installation z.B. auf Ubuntu 18.04/20.04

Führen Sie die Schritte `1a` und `1b` aus:

- Für `1b` ändert sich, dass wir statt `java-8-openjdk-armhf` die `java-8-openjdk-amd64` verwenden

Führen Sie Schritt `1c` aus:

- Sollte es bei `make` zum Fehler `error: ‘SIOCGSTAMP’ undeclared (first use in this function)` kommen, dann includen Sie `#include <linux/sockios.h>` in den nötigen Dateien z.B. `bluez-5.47/tools/rctest.c` und `bluez-5.47/tools/l2test.c`
- Dieses Problem scheint ab Ubuntu 20.04 aufzutreten. In Ubuntu 18.04 kann es sein, dass dieses Problem nicht auftritt.

Führen Sie die restlichen Schritte aus. Fertig.

Hinweis: Passen Sie beim Ausführen von Updaten/Upgrades auf. Es kann sein, dass dann Bluetooth upgegradet wird. Dies wollen wir vermeiden. Wir wollen maximal Version `5.47` verwenden.

## Fragen und Antworten

### Wie kann ich GitLab CI/CD auf mein eigenes Projekt anwenden?

- Dafür muss man in [gitlab-ci/MakeFile](gitlab-ci/Makefile) das Target Repository auf das eigene ändern, das Image builden und pushen. In `Packages` -> `Container Registry` kann man sehen, ob das Docker Image tatsächlich gepushed wurde.
- Weiters muss das Base-Image in [.gitlab-ci.yml](.gitlab-ci.yml) auf das eigene Image zeigen.
- Damit das Jacoco-Coverage Badge funktioniert muss man in die Einstellungen gehen und für das eigene Projekt eine Badge hinzufügen. Diesbezüglich gilt es sicherzustellen, dass die `Badge Image URL` korrekt ist d.h. die URL muss auf das eigene Repository zeigen und auf das `.svg`-Bild, welches von der GitLab Pipeline beim `bleclient-test`-Job generiert wurde.

### Was mache ich, wenn irgendetwas in der Installation schief läuft?

- Sicherstellen, dass alle Befehle richtig ausgeführt wurden.
- Generell kann es helfen, noch einmal alle Befehle von vorne auszuführen.

### Was wenn der der Java BluetoothManager eine Exception wirf (z.B. NullPointerException)?

- Sicherstellen, dass das gebuildete `tinyb.jar`zur Verfügung steht
- `tinyb.jar` muss zur Laufzeit als Parameter übergeben werden z.B. `–cp target/<JAR_FILE>:./lib/tinyb.jar:./target/dependencies/*`

### Was wenn ich beim Ausführen ein Problem mit der nativen API Version bekomme?

- Wahrscheinlich wurde die `tinyb.jar` nicht korrekt zur Laufzeit geladen
- Sicherstellen, dass sie richtig geladen wird z.B. `–cp target/<JAR_FILE>:./lib/tinyb.jar:./target/dependencies/*`

### Wieso kann ich das Programm nicht mit -jar ausführen?

- Entweder man verwendet `-cp` für Classpath oder `-jar`, aber nicht beides
- Für `-jar` benötigt man weiters auch noch eine Manifest-Datei (diese müsste man in Bezug auf das `tinyb.jar` auch konfigurieren)

### Wieso können die native Libraries nicht gefunden werden?

- Sollte grundsätzlich kein Problem sein, wenn man der Anleitung gefolgt hat
- Durch `-DCMAKE_INSTALL_PREFIX=/usr` sollten diese korrekt gesetzt sein
- Siehe auch bei Installation `tinyb/build/install_manifest.txt`
- Mit `java -cp` korrekt ausführen und nicht mit `-jar`
- Wenn es immer noch nicht möglich ist, dann muss man wirklich sicherstellen, dass sich `libjavatinyb.so` und `libtinyb.so` tatsächlich im Java Library Path befinden.
    - Prinzipiell könnte man diese Dateien `tinyb/build/java/jni/libjavatinyb.so` und `tinyb/build/src/libtinby.so` auch direkt nach `/usr/lib` kopieren

### Wieso bekomme ich eine BluetoothException mit Timeout was reached?

- Sicherstellen, dass sich der TimeFlip tatsächlich in Reichweite befindet
- Sicherstellen, dass die Batterie richtig im TimeFlip ist

### Wieso bekomme ich Exceptions beim Auslesen von Charakteristiken?

- Sicherstellen, dass das Passwort richtig vorher an den TimeFlip geschrieben wurde, dann sollte man alle Charakteristiken auslesen können
- Es kann sein, dass man versehentlich das Passwort über die TimeFlip App gesetzt hat. Wenn man die Batterie kurz herausnimmt und noch einmal einsetzt, dann sollte das Passwort zurückgesetzt sein.

### Was mache ich, wenn das bleclient Programm keinen TimeFlip ausgibt?

- Sicherstellen, dass der TimeFlip eingeschalten ist (z.B. Überprüfung mit Handy-App wie `nRF Connect`)
    - Am besten notiert man sich die Bluetooth Addresse des TimeFlips
- Sicherstellen, dass der TimeFlip mit keinem anderen Gerät gekoppelt ist
    - Entkoppeln von Bluetooth, TimeFlip App, etc.
- Theoretisch ist es möglich, dass aus irgendeinem Grund das Passwort auf dem TimeFlip z.B. mit der TimeFlip App gesetzt wurde. In diesem Fall sollte man die Batterie kurz entfernen und wieder reinstecken


## Links
* [Raspberry Pi Bluetooth Manager TinyB - Building bluez 5.47 from sources](https://github.com/sputnikdev/bluetooth-manager-tinyb)
* [TinyB Bluetooth LE Library](https://github.com/intel-iot-devkit/tinyb)
* [Raspberry Pi Installation of TinyB (Note: do not install bluez)](http://www.martinnaughton.com/2017/07/install-intel-tinyb-java-bluetooth.html)
* [Java for Bluetooth LE applications](https://www.codeproject.com/Articles/1086361/Java-for-Bluetooth-LE-applications)
* [TinyB Java examples (HelloTinyB.java, etc.)](https://github.com/intel-iot-devkit/tinyb/tree/master/examples/java)
* [Non-interactive raspi-config interface](https://github.com/raspberrypi-ui/rc_gui/blob/master/src/rc_gui.c#L23-L70)
