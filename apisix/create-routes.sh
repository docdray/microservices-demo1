#!/bin/bash
curl -i "http://localhost:9180/apisix/admin/routes/1" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "uri": "/api/hello",
    "upstream": {
      "type": "roundrobin",
      "nodes": {
        "quarkus-app:8080": 1
      }
    },
    "plugins": {
      "proxy-rewrite": {
        "uri": "/hello"
      }
    }
  }'
  
  
curl -i "http://localhost:9180/apisix/admin/routes/2" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "uri": "/ws/*",
    "enable_websocket": true,
    "upstream": {
      "type": "roundrobin",
      "nodes": {
        "quarkus-ws-app:8080": 1
      }
    }
  }'

curl -i "http://localhost:9180/apisix/admin/routes/3" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "uri": "/api/world",
    "upstream": {
      "type": "roundrobin",
      "nodes": {
        "quarkus-world-app:8080": 1
      }
    },
    "plugins": {
      "proxy-rewrite": {
        "uri": "/world"
      }
    }
  }'

curl -i "http://localhost:9180/apisix/admin/routes/4" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "uri": "/api/books*",
    "upstream": {
      "type": "roundrobin",
      "nodes": {
        "quarkus-book-crud:8080": 1
      }
    },
    "plugins": {
      "proxy-rewrite": {
        "regex_uri": ["^/api/books(.*)", "/books$1"]
      }
    }
  }'

curl -i "http://localhost:9180/apisix/admin/routes/5" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "uri": "/api/authors*",
    "upstream": {
      "type": "roundrobin",
      "nodes": {
        "quarkus-author-crud:8080": 1
      }
    },
    "plugins": {
      "proxy-rewrite": {
        "regex_uri": ["^/api/authors(.*)", "/authors$1"]
      }
    }
  }'

curl -i "http://localhost:9180/apisix/admin/routes/6" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "uri": "/api/messages*",
    "upstream": {
      "type": "roundrobin",
      "nodes": {
        "quarkus-app:8080": 1
      }
    },
    "plugins": {
      "proxy-rewrite": {
        "regex_uri": ["^/api/messages(.*)", "/messages$1"]
      }
    }
  }'

curl -i "http://localhost:9180/apisix/admin/global_rules/1" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "plugins": {
      "prometheus": {}
    }
  }'
