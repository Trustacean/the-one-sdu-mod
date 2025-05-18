@echo off
:: ^Turn on echo if you need to debug
setlocal EnableDelayedExpansion

:: Configuration variables - easier to modify
set "targetdir=target"
set "srcdir=src"
set "libdir=lib"
set "resourcesdir=src\gui\buttonGraphics"

echo [INFO] Starting build process...

:: Create target directory if it doesn't exist
IF NOT EXIST "%targetdir%" (
    echo [INFO] Creating target directory...
    mkdir "%targetdir%" || (
        echo [ERROR] Failed to create target directory.
        exit /b 1
    )
)

:: Build classpath from all JAR files
echo [INFO] Building classpath...
set "CLASSPATH="
if NOT EXIST "%libdir%" (
    echo [WARNING] Library directory '%libdir%' not found. Continuing without external libraries.
) else (
    for %%f in (%libdir%\*.jar) do (
        if defined CLASSPATH (
            set "CLASSPATH=!CLASSPATH!;%%f"
        ) else (
            set "CLASSPATH=%%f"
        )
    )
)

:: Verify source directory exists
if NOT EXIST "%srcdir%" (
    echo [ERROR] Source directory '%srcdir%' not found.
    exit /b 1
)

:: Collect all Java source files
echo [INFO] Collecting Java source files...
dir /s /b "%srcdir%\*.java" > sources.txt 2>nul
if not exist sources.txt (
    echo [ERROR] No Java source files found in '%srcdir%'.
    exit /b 1
)

:: Compile the sources
echo [INFO] Compiling Java sources...
javac -sourcepath "%srcdir%" -d "%targetdir%" -cp "%CLASSPATH%" @sources.txt
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed with error code %ERRORLEVEL%.
    del sources.txt 2>nul
    exit /b %ERRORLEVEL%
)

:: Clean up the sources list
del sources.txt 2>nul

:: Copy resources if they exist
if exist "%resourcesdir%" (
    echo [INFO] Copying resources...
    if not exist "%targetdir%\gui\buttonGraphics\" (
        mkdir "%targetdir%\gui\buttonGraphics" || (
            echo [WARNING] Failed to create resource directory in target. Resources may not be copied.
        )
    )
    xcopy /y /q "%resourcesdir%\*" "%targetdir%\gui\buttonGraphics\" >nul
    if %ERRORLEVEL% NEQ 0 (
        echo [WARNING] Some resources may not have been copied properly.
    ) else (
        echo [INFO] Resources copied successfully.
    )
)

echo [INFO] Build completed successfully!
endlocal