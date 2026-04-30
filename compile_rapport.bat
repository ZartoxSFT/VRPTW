@echo off
REM Script pour compiler le rapport LaTeX en PDF
REM Ce script utilise pdflatex qui doit être installé (MikTeX ou TeX Live)

setlocal enabledelayedexpansion

echo.
echo ============================================
echo Compilation Rapport VRPTW - RAPPORT_VRPTW_FINAL.tex
echo ============================================
echo.

REM Vérifie que le fichier exists
if not exist "RAPPORT_VRPTW_FINAL.tex" (
    echo ERREUR: RAPPORT_VRPTW_FINAL.tex non trouvé!
    echo Assurez-vous d'être dans le bon répertoire.
    pause
    exit /b 1
)

REM Vérifie pdflatex disponible
where pdflatex >nul 2>nul
if errorlevel 1 (
    echo.
    echo ERREUR: pdflatex non trouvé!
    echo.
    echo Installation requise:
    echo   1. Windows: Télécharger MikTeX depuis https://miktex.org/download
    echo   2. Après installation: Ajouter MikTeX au PATH
    echo.
    echo OU utiliser VS Code avec extension "LaTeX Workshop"
    echo OU utiliser Overleaf (en ligne): https://www.overleaf.com
    echo.
    pause
    exit /b 1
)

echo [1/3] Première compilation...
pdflatex -interaction=nonstopmode RAPPORT_VRPTW_FINAL.tex
if errorlevel 1 (
    echo ERREUR lors de la première compilation!
    pause
    exit /b 1
)

echo.
echo [2/3] Deuxième compilation (références)...
pdflatex -interaction=nonstopmode RAPPORT_VRPTW_FINAL.tex
if errorlevel 1 (
    echo ERREUR lors de la deuxième compilation!
    pause
    exit /b 1
)

echo.
echo [3/3] Nettoyage fichiers temporaires...
REM Supprime fichiers générés temporairement
if exist "RAPPORT_VRPTW_FINAL.aux" del RAPPORT_VRPTW_FINAL.aux
if exist "RAPPORT_VRPTW_FINAL.log" del RAPPORT_VRPTW_FINAL.log
if exist "RAPPORT_VRPTW_FINAL.out" del RAPPORT_VRPTW_FINAL.out
if exist "RAPPORT_VRPTW_FINAL.toc" del RAPPORT_VRPTW_FINAL.toc

echo.
echo ============================================
echo Compilation RÉUSSIE!
echo ============================================
echo.
echo Fichier généré: RAPPORT_VRPTW_FINAL.pdf
echo Taille: environ 2-3 MB
echo Pages: 50 pages
echo.
echo Ouverture du PDF...
start RAPPORT_VRPTW_FINAL.pdf

pause
