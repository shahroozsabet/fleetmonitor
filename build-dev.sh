#!/bin/bash
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for netflix environment (All Modules) *******************************'
cd integration-config-server && ./build-dev.sh && cd ..
cd integration-gateway && ./build-dev.sh && cd ..
cd integration-service-discovery && ./build-dev.sh && cd ..
cd integration-service && ./build-dev.sh && cd ..