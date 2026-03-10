#!/bin/bash

SERVER_IP="10.255.48.179"
SHARED_FOLDER="/Volumes/Shared/Lab5"
SERVER_HOME="$HOME/KaiLearnLab5/KaiLearnLab5-Server"
SERVER_SRC="$SERVER_HOME/src"
COMPUTE_SRC="$SERVER_SRC/some"

if [ "$1" == "compile" ]; then
    javac -d "$COMPUTE_SRC" "$COMPUTE_SRC"/Compute.java "$COMPUTE_SRC"/Task.java
    javac -cp "$SERVER_SRC" "$SERVER_SRC"/ComputeEngine.java
    echo "Server compilation complete"
elif [ "$1" == "registry" ]; then
    export CLASSPATH="$COMPUTE_SRC"
    rmiregistry &
    echo "RMI Registry started on port 1099"
elif [ "$1" == "server" ]; then
    export CLASSPATH="$SERVER_SRC:$COMPUTE_SRC"
    java -cp "$CLASSPATH" \
        -Djava.rmi.server.codebase="file://$SHARED_FOLDER/" \
        -Djava.rmi.server.hostname="$SERVER_IP" \
        -Djava.security.policy="$SERVER_SRC/server.policy" \
        ComputeEngine
else
    echo "Usage: $0 {compile|registry|server}"
    exit 1
fi
