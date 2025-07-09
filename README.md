# Boatsharing Application
## Inhaltsverzeichnis

- [Einleitung](#einleitung)

- [Installation](#installation)

- [Architektur](#architektur)

## Einleitung
Die Boatsharing Application enstand aus dem Bedürfniss, Rechnungen und Reservationen eines mit Freunden gemeinsam genutzten Boot zu verwalten.

Der folgende Techstack wurde hierzu verwendet:
- Spring Boot v3.4
- Angular v19
- Postgresql
- Docker
- Nginx

## Installation

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

