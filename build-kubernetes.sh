#!/bin/bash
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for kubernetes environment (All Modules) *******************************'
cd integration-gateway && ./build-kubernetes.sh && cd ..
cd integration-service && ./build-kubernetes.sh && cd ..