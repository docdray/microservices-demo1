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
  
curl -i "http://localhost:9180/apisix/admin/global_rules/1" \
  -H "X-API-KEY: edd1c9f034335f136f87ad84b625c8f" \
  -X PUT \
  -d '{
    "plugins": {
      "prometheus": {}
    }
  }'
