#!/bin/bash
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for kubernetes environment (All Modules) *******************************'
mvn --projects integration-gateway,integration-service -amd clean install -Pdocker-build -Denvironment=kubernetes