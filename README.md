# microservices-demo1


## Installation

Quarkus Service bauen und docker-image erzeugen

$ ./code-with-quarkus-kotlin1/build.sh

docker compose starten

$ docker compose up -d

routen in APISIX eintragen

$ ./apisix/create-routes.sh



## Tests

Tests aller Quarkus-Services ausführen (mit zusammenfassender Übersicht am Ende)

$ ./run-tests.sh

Nur Services testen, deren Verzeichnisname zum Filter passt, z.B. nur kotlin1

$ ./run-tests.sh kotlin1

Detaillierte Reports je Service liegen danach unter `<service>/target/surefire-reports/`.


## Services

Webseite

http://localhost:8080


hello Schnittstelle

http://localhost:8080/api/hello


Grafana

http://localhost:8080/grafana


Prometheus

http://localhost:9090

