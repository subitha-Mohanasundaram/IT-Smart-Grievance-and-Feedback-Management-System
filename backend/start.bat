@echo off
echo Starting Spring Boot Backend...
echo.

REM Set Java
set JAVA_HOME=C:\Program Files\Java\jdk-24
echo Java Home: %JAVA_HOME%

REM Add to PATH
set PATH=%JAVA_HOME%\bin;%PATH%
echo Java added to PATH

REM Verify Java
echo Checking Java version...
java -version
echo.

REM Run Spring Boot
echo Starting application...
call mvnw.cmd spring-boot:run

pause