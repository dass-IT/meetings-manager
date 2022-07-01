Meetings-Manager
================
Meetings-Manager ist eine minimalistische Webapplikation zur Organisation von Meetings mit einem Videokonferenzsystem wie Jitsi Meet.

  * Meetings können vorab geplant werden
  * Teilnehmer erhalten eine E-Mail mit einem Termin und einem Link zur Videokonferenz
  * der Link zur Videokonferenz enthält einen zufälligen String, was das Erraten der URL
    erschwert
  * für den Organisator/Moderator des Meetings wird automatisch ein Benutzer angelegt
  * der Organisator erhält eine E-Mail mit seinen Zugangsdaten
  * Ressourcen und (virtuelle) Räume können Meetings zugewiesen werden


Die Webapplikation besteht aus einer Java-Servlet-Applikation, die unter Tomcat läuft, sowie einigen
 Python-Skripten, die Mails verschicken und Aufräumarbeiten erledigen. Standardmäßig wird eine SQLite-Datenbank
 für die Datenhaltung verwendet.


Bauen der Applikation
---------------------
Voraussetzungen:
  * JDK >= 8
  * Maven
  * npm
  * SQLite 3
  * Python 3


~~~
git clone https://github.com/sebastianlederer/meetings-manager.git
cd meetings-manager/meetings-manager-project
mvn package -Pproduction
~~~

Zur Installation:

  * Verzeichnis `/var/lib/meetingsmanager` anlegen, dort eine Datei `config.properties` anlegen.
Beispielinhalt:

~~~
rooms=Raum 1, Raum 2, Raum 3
roomColors=tomato,orange,dodgerblue,mediumseagreen,gray,slateblue,violet

resources=Tablet 1, Tablet 2, Tablet 3

jdbcUrl=jdbc:sqlite:/var/lib/meetingsmanager/meetings.sqlite
baseUrl=https://jitsi.my-domain.de/
~~~

  * WAR-File aus Unterverzeichnis `target` nach `/var/lib/tomcat.../webapps` kopieren.

  * Leere SQLite-Datenbank `/var/lib/meetingsmanager/meetings.sqlite` anlegen mit Berechtigungen für den Tomcat-User.

  * Mit Systemd-Service: /var/lib/meetingsmanager beschreibbar machen für Tomcat-Service

Skripte zur Jitsi-Anbindung
---------------------------
  * meetingsdb.py: Sollte per cron jede Minute aufgerufen werden mit dem Parameter "mail". Dadurch werden Mails für anstehende Meetings verschickt. Sollte unter dem Benutzer ausgeführt werden, der Schreibzugriff auf die SQLite-Datenbank hat. Zusätzlich sollte *meetingsdb.py* einmal am Tag mit dem Parameter "clean" aufgerufen werden. Dabei werden vergangene Meetings und nicht mehr benötigte externe Teilnehmer entfernt.
  * syncusers.sh: Erzeugt Benutzer in Jitsi. Sollte mindestens einmal täglich aufgerufen werden. Benutzer werden erzeugt, wenn ein Meeting am selben Tag geplant ist. Benutzer ohne anstehende Meetings werden gelöscht.
Bedienung
---------
Es wird zwischen internen und externen Benutzern unterschieden. Interne Benutzer werden aus einer vorhandenen Quelle importiert
(z.B. aus einem LDAP-Verzeichnis) und in der SQLite-Datenbank abgelegt.

Externe Benutzer werden entweder beim Erstellen eines Meetings als externe Teilnehmer angelegt oder explizit in der Benutzeransicht.

Übersicht
---------
Eine Kalenderansicht, in der vorhandene Meetings angezeigt werden. Dient nur der Übersicht, Bearbeiten oder Neuanlage von
Meetings ist nicht möglich.

Besprechungen
-------------
Hier werden Vorhandene Meetings angezeigt, neue können angelegt werden. Über den Button "Teilnehmer hinzufügen" können interne Teilnehmer aus einer Liste ausgewählt werden. Im selben Dialogfeld können externe Teilnehmer durch Eingabe der E-Mail-Adresse hinzugefügt werden.

Beim Anlegen eines Meetings kann die URL leer gelassen werden. Dann wird eine URL automatisch erzeugt, die den Namen und einen zufälligen String enthält.

Werden vorhandene Meetings bearbeitet, wird keine neue Mail verschickt. Ist das gewünscht, muss das Meeting gelöscht und neu angelegt werden.

Benutzer
--------
Externe Benutzer werden hier verwaltet. Außerdem werden interne Benutzer angezeigt, die als Organisator ein anstehendes Meeting haben. Durch setzen der Checkbox "Dauerhaft" wird verhindert, dass Benutzer gesperrt/gelöscht werden, wenn kein Meeting mehr ansteht. Diese Benutzer können dann dauerhaft mit den konfigurierten Zugangsdaten Meetings starten.

