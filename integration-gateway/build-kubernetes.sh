#!/bin/bash
echo '*******************************  building for kubernetes environment *******************************'
mvn clean install dockerfile:build -Pkubernetes 