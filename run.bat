@echo off
setlocal
echo ============================================================
echo   Starting AI-Based Recommendation System
echo ============================================================

REM Check if JDK 17 is available in user's installed path
if exist "C:\Users\asus\.jdks\jdk-17.0.20.1+1\bin\javac.exe" (
    set "PATH=C:\Users\asus\.jdks\jdk-17.0.20.1+1\bin;%PATH%"
)

echo [1/2] Compiling Java source files...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin src\main\java\com\recommendation\model\*.java src\main\java\com\recommendation\service\*.java src\main\java\com\recommendation\server\*.java src\main\java\com\recommendation\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching AI Recommendation System on http://localhost:8080 ...
java -cp bin com.recommendation.Main 8080
pause
