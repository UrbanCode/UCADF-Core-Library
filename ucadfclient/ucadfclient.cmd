@echo off
REM ####################################################################################################
REM # The ucadfclient wrapper for Windows.
REM ####################################################################################################

setlocal

if "%JAVA_HOME%" == "" goto noJavaHome

set JAVACOMMAND="%JAVA_HOME%\bin\java"
set UCADFJAR=%~dp0UCADF-Core-Library.jar

if exist "%UCADFJAR%" goto runJar

set UCADFJAR=%~dp0target\UCADF-Core-Library.jar
if exist "%UCADFJAR%" goto runJar

echo %UCADFJAR% not found in %~dp0 or %~dp0target
set EXITCODE=1
exit /b %EXITCODE%

:runJar
%JAVACOMMAND% -Dlog4j.configuration=file:"%~dp0log4j.properties" -jar "%UCADFJAR%" %*
set EXITCODE=%ERRORLEVEL%
exit /b %EXITCODE%

:noJavaHome
echo No JAVA_HOME has been set on your environment.
set EXITCODE=1
exit /b %EXITCODE%
