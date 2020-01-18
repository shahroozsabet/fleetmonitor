#!/bin/bash
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for netflix environment (All Modules) *******************************'
mvn clean install -Pdocker-build