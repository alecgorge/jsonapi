@echo off

:input
set INPUT=
set /P INPUT=Release version: %=%
if "%INPUT%"=="" goto input

mkdir "release\JSONAPI v%INPUT%\plugins\JSONAPI"
xcopy "test\plugins\JSONAPI" "release\JSONAPI v%INPUT%\plugins\JSONAPI" /E /C /R /I /K /Y
copy "test\plugins\JSONAPI.jar" "release\JSONAPI v%INPUT%\plugins\JSONAPI.jar"
pause