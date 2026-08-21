@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "JAVA_HOME=%PROJECT_DIR%.jdks\jdk-25.0.3"
set "GRADLE_USER_HOME=%PROJECT_DIR%.gradle-user-home"
set "PATH=%JAVA_HOME%\bin;%PATH%"

call "%PROJECT_DIR%gradlew.bat" %*
exit /b %ERRORLEVEL%
