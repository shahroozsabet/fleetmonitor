@echo off
echo '*******************************make sure to install latest integration commons and utils*******************************'
echo
echo '*******************************  building for kubernetes environment (All Modules) *******************************'
call mvn --projects integration-gateway,integration-stream -amd clean install -Pdocker-build -Denvironment=kubernetes