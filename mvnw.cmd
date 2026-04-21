@REM ----------------------------------------------------------------------------
@REM Maven wrapper script for Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set MAVEN_WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
set MAVEN_WRAPPER_PROPERTIES="%~dp0.mvn\wrapper\maven-wrapper.properties"
set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

for /f "usebackq tokens=1,2 delims==" %%a in (%MAVEN_WRAPPER_PROPERTIES%) do (
    if "%%a"=="distributionUrl" set DISTRIBUTION_URL=%%b
)

if not exist %MAVEN_WRAPPER_JAR% (
    echo [..] Downloading Maven wrapper jar...
    powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%~dp0.mvn\wrapper\maven-wrapper.jar'" 2>&1
)

set JAVA_HOME_CMD="%JAVA_HOME%\bin\java.exe"
if not exist %JAVA_HOME_CMD% (
    set JAVA_HOME_CMD=java
)

%JAVA_HOME_CMD% -jar %MAVEN_WRAPPER_JAR% %*
