@echo off
echo '*******************************  building for netflix environment *******************************'
call mvn clean install dockerfile:build  -P dev