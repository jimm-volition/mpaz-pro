@echo off
setlocal EnableExtensions
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB%" set ADB=adb

echo Dispositivos:
"%ADB%" devices -l
echo.

rem Preferir el Samsung / cualquier device que NO sea SoftwinerEvb (iVCam)
set PHONE=
for /f "tokens=1,2,*" %%A in ('"%ADB%" devices -l ^| findstr /i "device product:"') do (
  echo %%C | findstr /i "SoftwinerEvb craneimx" >nul
  if errorlevel 1 (
    if not defined PHONE set PHONE=%%A
  )
)

if not defined PHONE (
  echo ERROR: No se detecto un celular Android (solo iVCam u otro falso).
  echo Activa Depuracion USB y acepta el dialogo en el telefono.
  pause
  exit /b 1
)

echo Usando dispositivo: %PHONE%
"%ADB%" -s %PHONE% reverse --remove-all 2>nul
"%ADB%" -s %PHONE% reverse tcp:8080 tcp:8080
echo.
echo Tuneles activos:
"%ADB%" -s %PHONE% reverse --list
echo.
echo Listo. Deja el USB conectado y vuelve a intentar el login.
pause
