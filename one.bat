@echo off
:: Enhanced Java run script for DTNSim
setlocal EnableDelayedExpansion

:: Configuration variables
set "targetdir=target"
set "libdir=lib"
set "mainclass=core.DTNSim"
set "memory=512M"

echo [INFO] Starting DTNSim...

:: Validate target directory exists
if NOT EXIST "%targetdir%" (
    echo [ERROR] Target directory '%targetdir%' not found. Did you compile the project?
    echo [INFO] Try running compile.bat first.
    exit /b 1
)

:: Build classpath from all JAR files
echo [INFO] Building classpath...
set "CLASSPATH=%targetdir%"

:: Check if lib directory exists
if NOT EXIST "%libdir%" (
    echo [WARNING] Library directory '%libdir%' not found. Continuing without external libraries.
) else (
    if EXIST "%libdir%\ECLA.jar" set "CLASSPATH=!CLASSPATH!;%libdir%\ECLA.jar"
    if EXIST "%libdir%\DTNConsoleConnection.jar" set "CLASSPATH=!CLASSPATH!;%libdir%\DTNConsoleConnection.jar"
    if EXIST "%libdir%\lombok.jar" set "CLASSPATH=!CLASSPATH!;%libdir%\lombok.jar"
    
    for %%f in (%libdir%\*.jar) do (
        echo !CLASSPATH! | findstr /C:"%%f" >nul || set "CLASSPATH=!CLASSPATH!;%%f"
    )
)

:: Run the application with all parameters passed to this script
echo [INFO] Running with memory limit: %memory%
echo [INFO] Classpath: %CLASSPATH%
echo [INFO] Main class: %mainclass%

java -Xmx%memory% -cp "%CLASSPATH%" %mainclass% %*

:: Check if Java executed successfully
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Execution failed with error code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
) else (
    echo [INFO] Execution completed successfully.
)

endlocal
