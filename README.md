# Boatsharing Application
## Inhaltsverzeichnis

- [Domain](#domain)

- [Einleitung](#einleitung)

- [Installation](#installation)

- [Architektur](#architektur)

## Einleitung
Die Boatsharing Application enstand aus dem Bedürfniss, Rechnungen und Reservationen eines mit Freunden gemeinsam genutzten Boot zu verwalten. 
Diese Dokumentation dient dazu einen Überblick über die Verwendung, die gefragten Technologien und transparenz über den Entwicklungsprozess zu geben.

Der folgende Techstack wurde hierzu verwendet:
- Spring Boot v3.4
- Angular v19
- Postgresql
- Docker
- Nginx

## Domain
### Reservation
Ein authentifizierter Nutzer hat die Möglichkeit, das Boot für eine Ausfahrt für sich zu reservieren. Zu einem bestimmten Zeitpunkt ist es nur einem Nutzer möglich das Boot zu nutzen.
Die Nutzungsdauer der jeweiligen Nutzer wird persistiert und für weitere Funktionen bereitgestellt. 
Jeder authentifizierte Nutzer hat die Möglichkeit sich über die Bootnutzung zu informieren und einen für sich geeigneten Slot zu finden.
Allfällige Anpassungen wie anpassen des Zeitfensters oder das Löschen der Reservationen sind für Termine in der Zukunft möglich. Liegt der Zeitpunkt der Reservation in der Vergangenheit ist dies nicht mehr möglich.
Eine Ausnahme gilt für das Update der Bootstunden, heisst ein Nutzer kann nachträglich die effektiven Motorstunden die während einer Ausfahrt angefallen sind zu dokumentieren.
Es ist einem Nutzer nur erlaubt persönliche Reservationen zu buchen. Ebenfalls ist das Löschen und Updaten der Reservationen nur für persönliche Reservationen möglich.

### Zahlungen
Allfallende Kosten am Boot werden transparent und fair strukturiert. Die Applikation ermöglicht hierzu eine Dokumentation von anfallenden Kosten welche ein Nutzer tätigt.
Tätigt ein Nutzer eine Zahlung kann dieser die Kosten in der Applikation dokumentieren und entsprechende Zahlungsaufforderungen an die Mitnutzer stellen.
Das Löschen einer Zahlung ist nur für persönliche Zahlungen möglich.
Zahlungsaufforderungen können von Debitoren nach getätigter Zahlung quitiert werden und erlauben dem Kreditor das bestätigen der erhaltenen Zahlung. 
Eine Zahlung gilt als abgeschlossen, wenn alle Zahlungsaufforderungen durchd en Kreditor bestätigt wurden.
Der Umgang mit Benzinkosten werden spezifisch behandelt. Der Nutzer hat die Möglichkeit eine Zahlung als Benzinzahlung zu markieren. Es werden zu diesem Zeitpunkt keine Zahlungsaufforderungen an die anderen Nutzer definiert. 
Zu einem geignenten Zeitpunkt kann von jedem Nutzer eine Benzinabrechnung durchgeführt werden. Dies hat zur Folge, dass die angefallenen Benzinkosten proportional zur Nutzungsdauer des Bootes verrechnet werden. Die Ausgleichszahlungen folgen der Logik der üblichen Zahlungen.

## Installation
Um die Applikation mit geringem Aufwand zu deployen und Systemkonfigurationen minimal zu halten, habe ich mich dafür entschieden, Frontend, Backend und Datenbank über Docker Container zur verfügung zu stellen. 
Ebenfalls bieten Container Vorteile bezüglich Skalierung, Ressourcennutzung und Isolation von anderen Prozessen. 

### Download
Um die Applikation lokal zu Deployen benötigen Sie folgende Files: 
- [docker-compose.prod.yml](docker-compose.prod.yml)
- [init/init.sql](init/init.sql)
- [.env](.env)

### Run
Nach dem Download können Sie in das entsprechende Verzeichniss wechseln und mit folgendem Befehl die Applikation starten:
`docker compose -f docker-compose.prod.yml up -d`

Es sollten nun 3 Container gestartet sein: Reverse Proxy (inkl. statische Frontend Dateien), Backend, Datenbank.
Die Applikation ist über einen Reverse Proxy auf dem Port 4201 erreichbar. Folgende User stehen zum login zur verfügung
| Username | Password |
| -------- | -------- |
| testUser | password123 |
| testUser2 | password123 |
| testUser3 | password123 |

Ebenfalls wurde auf dem lokalen Rechner ein Volumen erstellt und ermöglicht das Persistieren der Daten über die Laufzeit der Container hinweg. Siehe [Clean up](#clean-up) um das Volumen zu entfernen.

### Clean up
Um Ihre Festplatte von der Applikation zu bereinigen sollten folgende Befehle ausgeführt werden:

`docker compose -f docker-compose.prod.yml down -v` mit dem Flag `-v` wird das erstellte Volumen entfernt.

Anschliessend können Sie die Docker images entfernen, falls Sie die Applikation nicht weiter verwenden möchten.

`docker image rm <container-id>`

## Architektur
### Systemarchitektur
![System_Diagram_L1](https://github.com/user-attachments/assets/335f3300-ff72-4b00-acb2-dde5f531a91b)

Wie in [Run](#run) erläutert werden 3 Container gestartet. Die Kommunikation findet auf allen Layern vertikal statt.
Der Client ist der Initiator und kommuniziert mit dem Nginx Reverse Proxy, welcher 2 Funktionen hat 1. Weiterleiten der API Anfragen an den Backend Container 2. Ausliefern der statischen Files der Angular Applikation
Die API kommuniziert mit der Datenbank und verwendet hierzu als ORM Lösung JPA.

### Security Konzept
Das Sicherheitskonzept behandelt hauptsächlich die domänenspezifische Anforderung, dass mehrere Nutzer die Applikation verwenden und jeweils unterschiedliche Nutzerrechte bezüglicher der Bearbeitung der Daten erhalten.
Hierzu muss die Applikation in der Lage sein, Nutzer authentifizieren und für Operationen authorisieren zu können. Schutz vor dem injizieren von bösartigen Daten und manipulationen der Daten über bewusst gewählte Userinputs ist zweitrangig, da die Applikation nur von ausgewählten Nutzer verwendet wird. Jedoch werden grundlegende Schutzmechanismen bereits von den verwendeten Frameworks implizit zur verfügung gestellt und während der Implementation darauf geachtet, best practices zu respektieren. Details hierzu folgen in den weiteren Abschnitten.
Schutz vor Man in the Middle basierten Angriffen wie das Auslesen von Nutzerdaten oder das Manipulieren der Daten zwischen Browser und Server werden in diesem Setup nicht berücksichtigt. Durch hinzufügen eines weitern Proxy kann mit wenig Aufwand HTTPS Verschlüsselung ermöglicht werden, was vor den zuvor genannten Angriffen Schutz bietet.

#### Assets
| Asset | Beschreibung | Speicherort | Bedrohungen | Schutzmassnahmen | 
| -------- | -------- | -------- | -------- | -------- |
| User Passwort | Passwort zur User authentifizierung | Datenbank | Brute force, Datenbank Zugriff | Bcrypt hashing |
| JWT Token | Token zur Authentifizierung von API abfragen | Localstorage | Diebstahl, Replay-Angriff, CSRF-Angriff | Gültigkeitsdauer, CSRF-Schutz mittels Localstorage |
| Userspezifische Daten | Manipulation durch dritte | Datenbank | Unauthorisierter Zugriff | Authorisierungsmechanismen |
| API Schnittstellen | HTTP Endpunkte | Spring Boot | Unauthorisierter Zugriff, Input Manipulationen | Authentifizierung und Authorisierung, Bereinigung der Inputdaten |
| Datenbank Zugriff | Zugangsdaten und Schnittstellen zu Datenbank | .env Datei | Zugriff durch Angreifer von Aussen | Zugriff auf Docker-Netzwerk einschränken | 

#### Authentifizierung & Authorisierung
Zur Authentifizierung werden Username und Passwort verlangt. Aus Gründen der Userexperience wird keine Passwort-Policy erzwungen.
Die Authoriserung und authentifizierung von eingeloggten Usern wird mit JWT Tokens umgesetzt. Dies ermöglicht eine bessere Skalierung und da keine Sessionbasierten Daten Serverseitig gespeichert werden müssen. Ebenfalls bieten JWT Tokens impliziten Schutz vor CSRF Angriffen.

#### Backend
Das Sicherheitskonzept wird mit Spring Security implementiert. Hierzu wurden nach den best practice Vorgaben von Spring Security zusätzliche [Filter](backend/src/main/java/org/example/backend/infrastructure/security/jwt/JwtFilter.java), [Provider](backend/src/main/java/org/example/backend/infrastructure/security/jwt/JwtAuthenticationProvider.java) und [Authentification-Tokens](backend/src/main/java/org/example/backend/infrastructure/security/jwt/JwtAuthenticationToken.java) implementiert und in der [SecurityConfiguration](backend/src/main/java/org/example/backend/infrastructure/security/SecurityConfiguration.java) registriert.

##### XSS
Keine Security In-Depth Massnahmen umgesetzt, da API nur von Angular-Frontend verwendet und somit Userinhalt bereits bereinigt. Zusätzlich gelten User als Vertrauenswürdig.

##### CORS
Keine Einschränkung obwohl API nur von Angular-Frontend verwendet.

##### CSRF
Schutz mittels Localstorage. Keine Security In-Depth durch beispielsweise CORS.

##### SQL Injection
DataJPA verwendet hierzu prepared Queries. Weitere Schadenminderungsmassnahmen wie DB-Rechte spezifizieren wurden nicht implementiert.

#### Frontend

