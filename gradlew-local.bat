@echo off
setlocal

set "PROJECT_DIR=%~dp0"
if exist "%PROJECT_DIR%.gradle-local-env.bat" call "%PROJECT_DIR%.gradle-local-env.bat"
if not defined JAVA_HOME if exist "%PROJECT_DIR%.jdks\jdk-25.0.5+8\bin\java.exe" set "JAVA_HOME=%PROJECT_DIR%.jdks\jdk-25.0.5+8"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%PROJECT_DIR%.gradle-user-home"
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

call "%PROJECT_DIR%gradlew.bat" %*
exit /b %ERRORLEVEL%
