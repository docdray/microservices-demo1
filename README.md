# microservices-demo1


## Installation

Quarkus Service bauen und docker-image erzeugen

$ ./code-with-quarkus-kotlin1/build.sh

docker compose starten

$ docker compose up -d

routen in APISIX eintragen

$ ./apisix/create-routes.sh



## Services

Webseite

http://localhost:8080


hello Schnittstelle

http://localhost:8080/api/hello


Grafana

http://localhost:8080/grafana


Prometheus

http://localhost:9090

