#!/usr/bin/env bash
# =======================================================================
# Software Development Document Environment (SDM)
# Platform: Linux / Unix
# Action: Building and starting up Spring Boot Web Application
# =======================================================================

set -e

echo "======================================================================="
echo "Software Development Document Environment (SDM)"
echo "Platform: Linux"
echo "======================================================================="

echo "1. Cleaning and packaging sdm with Maven..."
mvn clean package -DskipTests

echo "2. Launching SDM web server..."
java -jar target/sdm-1.0.0.jar
