@echo off
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for netflix environment (All Modules) *******************************'
call mvn clean install -Pdocker-build