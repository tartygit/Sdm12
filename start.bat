@echo off
echo =======================================================================
echo Software Development Document Environment (SDM)
echo Platform: Windows
echo Action: Building and starting up Spring Boot Web Application
echo =======================================================================

echo 1. Cleaning and packaging sdm with Maven...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed! Please check setup environment.
    pause
    exit /b %ERRORLEVEL%
)

echo 2. Launching SDM web server...
java -jar target/sdm-1.0.0.jar

pause
