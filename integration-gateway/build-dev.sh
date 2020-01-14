#!/bin/bash
echo '*******************************  building for netflix environment *******************************'
mvn clean install dockerfile:build -Pdev 