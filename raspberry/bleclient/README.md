# Bluetooth Low Energy auf dem Raspberry Pi

### Voraussetzungen
* Die Installation von `tinyb` war erfolgreich
* Ein bluetooth-fähiges Gerät befindet sich in der Nähe
* Im Ordner `lib` befindet sich die gebuildete Datei `tinyb.jar` von der Installation (`tinyb/build/java/tinyb.jar`)

## Builden

Zuerst müssen wir das JAR builden, damit wir `bleclient.jar` im `target`-Verzeichnis bekommen.
Zusätzlich werden alle Dependencies im `pom.xml` in `target/dependencies` abgelegt. Wir erreichen dies durch die Plugins 
`maven-install-plugin` und `maven-dependency-plugin`.

### Builden

     mvn clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

### Builden mit Tests und Javadoc

     mvn clean install

## Ausführen

Ausführung des Programms `bleclient.jar` mit `tinyb.jar` und den Dependencies in `target/dependencies/*`.
Auch Angabe des `fully-qualified name` der auszuführenden Java-Klasse:

     sudo java -cp target/bleclient.jar:./lib/tinyb.jar:./target/dependencies/* at.qe.skeleton.bleclient.Main


        
