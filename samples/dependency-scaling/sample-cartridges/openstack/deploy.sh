#!/bin/sh 

echo "Adding autoscaling policy..."
curl -X POST -H "Content-Type: application/json" -d @'artifacts/autoscale-policy.json' -k -v -u admin:admin https://localhost:9443/api/autoscalingPolicies

echo "Adding tomcat cartridge..."
curl -X POST -H "Content-Type: application/json" -d @'artifacts/tomcat.json' -k -v -u admin:admin https://localhost:9443/api/cartridges

echo "Adding tomcat1 cartridge..."
curl -X POST -H "Content-Type: application/json" -d @'artifacts/tomcat1.json' -k -v -u admin:admin https://localhost:9443/api/cartridges

sleep 2
echo "Creating application..."
curl -X POST -H "Content-Type: application/json" -d @'artifacts/app_dependency_scaling.json' -k -v -u admin:admin https://localhost:9443/api/applications

sleep 2
echo "Deploying application..."
curl -X POST -H "Content-Type: application/json" -d@'artifacts/dep_dependency_scaling.json' -k -v -u admin:admin https://localhost:9443/api/applications/app_group_v1/deploy

