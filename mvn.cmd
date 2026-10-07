@echo off
setlocal enabledelayedexpansion
set args=%*
set args=!args:C:\Asrax\Asrax\am-trade-management=/app!
set args=!args:\=/!
if "%GITHUB_ACTOR%"=="" set GITHUB_ACTOR=%GITHUB_PACKAGES_USERNAME%
if "%GITHUB_TOKEN%"=="" set GITHUB_TOKEN=%GITHUB_PACKAGES_TOKEN%
if "%GITHUB_TOKEN%"=="" set GITHUB_TOKEN=%GHCR_TOKEN%
docker run --rm -v "C:\Asrax\Asrax\am-trade-management:/app" -v "%USERPROFILE%\.m2:/root/.m2" -e GHCR_TOKEN="%GHCR_TOKEN%" -e GITHUB_TOKEN="%GITHUB_TOKEN%" -e GITHUB_ACTOR="%GITHUB_ACTOR%" -e GITHUB_PACKAGES_USERNAME="%GITHUB_PACKAGES_USERNAME%" -e GITHUB_PACKAGES_TOKEN="%GITHUB_PACKAGES_TOKEN%" -w /app maven:3.9-eclipse-temurin-17 mvn -s /app/settings.xml !args!
