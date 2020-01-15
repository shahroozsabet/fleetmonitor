@echo off
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for netflix environment (All Modules) *******************************'
cd integration-config-server
start build-dev.bat
cd ..
cd integration-gateway
start build-dev.bat
cd ..
cd integration-service-discovery
start build-dev.bat
cd ..
cd integration-service
start build-dev.bat
cd ..