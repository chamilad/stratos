#!/bin/sh 

# Create autoscale policy
curl -X POST -H "Content-Type: application/json" -d @'artifacts/autoscale-policy.json' -k -v -u admin:admin https://localhost:9443/api/autoscalingPolicies

# Create tomcat cartridge
curl -X POST -H "Content-Type: application/json" -d @'artifacts/tomcat.json' -k -v -u admin:admin https://localhost:9443/api/cartridges

# Deploy tomcat1 cartride
curl -X POST -H "Content-Type: application/json" -d @'artifacts/tomcat1.json' -k -v -u admin:admin https://localhost:9443/api/cartridges

# Deploy tomcat2 cartride
curl -X POST -H "Content-Type: application/json" -d @'artifacts/tomcat2.json' -k -v -u admin:admin https://localhost:9443/api/cartridges

# Deploy group
curl -X POST -H "Content-Type: application/json" -d @'artifacts/group6c.json' -k -v -u admin:admin https://localhost:9443/api/groups

sleep 5
# Create application
curl -X POST -H "Content-Type: application/json" -d @'artifacts/application_definition.json' -k -v -u admin:admin https://localhost:9443/api/applications

sleep 5
# Deploy application
curl -X POST -H "Content-Type: application/json" -d@'artifacts/deployment-policy.json' -k -v -u admin:admin https://localhost:9443/api/applications/myapp1265/deploy

