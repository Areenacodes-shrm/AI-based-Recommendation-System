@echo off
setlocal
echo ============================================================
echo   Launching Pre-built AI Recommendation System JAR
echo ============================================================

REM Check if JDK 17 is available in user's installed path
if exist "C:\Users\asus\.jdks\jdk-17.0.20.1+1\bin\java.exe" (
    set "PATH=C:\Users\asus\.jdks\jdk-17.0.20.1+1\bin;%PATH%"
)

echo Starting server on http://localhost:8080 ...
java -jar recommendation-system.jar 8080
pause
