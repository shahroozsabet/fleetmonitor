#!/bin/bash

echo '*****************building integration-config-service'
cd integration-config-server
mvn clean package dockerfile:build
:
echo '*****************building integration-gateway'
cd integration-gateway
mvn clean package dockerfile:build -Pdev
: