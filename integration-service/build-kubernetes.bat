@echo off
echo '*******************************  building for kubernetes environment *******************************'
call mvn clean install dockerfile:build  -P kubernetes