@echo off
set targetdir=target
IF NOT EXIST "%targetdir%" mkdir %targetdir%

:: Create classpath with all JARs in lib directory
setlocal EnableDelayedExpansion
set CLASSPATH=
for %%f in (lib\*.jar) do (
    if "!CLASSPATH!"=="" (
        set CLASSPATH=%%f
    ) else (
        set CLASSPATH=!CLASSPATH!;%%f
    )
)

:: Create a temporary file listing all Java source files
dir /s /b src\*.java > sources.txt
javac -sourcepath src -d %targetdir% -cp !CLASSPATH! @sources.txt
del sources.txt

IF NOT EXIST "%targetdir%\gui\buttonGraphics" (
    mkdir %targetdir%\gui\buttonGraphics
    copy src\gui\buttonGraphics\* %targetdir%\gui\buttonGraphics\
)
endlocal