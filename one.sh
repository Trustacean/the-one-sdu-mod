#!/bin/bash

# Configuration variables
TARGETDIR="target"
LIBDIR="lib"
MAINCLASS="core.DTNSim"
MEMORY="512M"
# ^ Modify the memory limit as needed, 512 is kinda low for dtnsim lol, but
# this is here to not let dtnsim to hog resources when you want to multitask

echo "[INFO] Starting DTNSim..."

# Validate target directory exists
if [ ! -d "$TARGETDIR" ]; then
    echo "[ERROR] Target directory '$TARGETDIR' not found. Did you compile the project?"
    echo "[INFO] Try running compile.sh first."
    exit 1
fi

# Build classpath with platform-appropriate separator
CLASSPATH="$TARGETDIR"

# Check if lib directory exists
if [ ! -d "$LIBDIR" ]; then
    echo "[WARNING] Library directory '$LIBDIR' not found. Continuing without external libraries."
else
    for jar in "$LIBDIR"/*.jar; do
        [ -f "$jar" ] || continue
        
        if [[ ":$CLASSPATH:" != *":$jar:"* ]]; then
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

# Run the application with all parameters passed to this script
echo "[INFO] Running with memory limit: $MEMORY"
echo "[INFO] Classpath: $CLASSPATH"
echo "[INFO] Main class: $MAINCLASS"

java -Xmx"$MEMORY" -cp "$CLASSPATH" "$MAINCLASS" "$@"

# Check if Java executed successfully
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo "[ERROR] Execution failed with error code $EXIT_CODE."
    exit $EXIT_CODE
else
    echo "[INFO] Execution completed successfully."
fi
