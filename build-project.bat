@echo off
REM ===================================
REM Script de build du projet Mini-ERP
REM ===================================
REM
REM Ce script construit le projet dans le bon ordre:
REM 1. Module rmi-common (dependance)
REM 2. Services (qui dependent de rmi-common)
REM

setlocal enabledelayedexpansion

echo.
echo ====================================================================
echo              Mini-ERP - Build Script
echo ====================================================================
echo.

echo Step 1: Construire le module RMI commun (rmi-common)...
echo.
cd rmi-common
call mvn clean install
if !errorlevel! neq 0 (
    echo Erreur lors de la construction de rmi-common
    cd ..
    exit /b 1
)
cd ..
echo rmi-common construit avec succes
echo.

echo Step 2: Construire les services...
echo.

echo Construction du stock-service...
cd stock-service
call mvn clean install
if !errorlevel! neq 0 (
    echo Erreur lors de la construction de stock-service
    cd ..
    exit /b 1
)
cd ..
echo stock-service construit
echo.

echo Construction du ventes-service...
cd ventes-service
call mvn clean install
if !errorlevel! neq 0 (
    echo Erreur lors de la construction de ventes-service
    cd ..
    exit /b 1
)
cd ..
echo ventes-service construit
echo.

echo Construction du finance-service...
cd finance-service
call mvn clean install
if !errorlevel! neq 0 (
    echo Erreur lors de la construction de finance-service
    cd ..
    exit /b 1
)
cd ..
echo finance-service construit
echo.

echo Construction du api-gateway...
cd api-gateway
call mvn clean install
if !errorlevel! neq 0 (
    echo Erreur lors de la construction de api-gateway
    cd ..
    exit /b 1
)
cd ..
echo api-gateway construit
echo.

echo ====================================================================
echo BUILD TERMINE AVEC SUCCES!
echo ====================================================================
echo.
echo Tous les modules ont ete construits avec succes.
echo Les erreurs dans l'IDE devraient maintenant disparaitre.
echo.
echo Vous pouvez maintenant:
echo   - Demarrer les services: start-with-rmi.bat docker
echo   - Ou relancer le projet dans votre IDE
echo.

endlocal
"