#!/bin/bash

SERVER_IP="10.255.48.179"
SHARED_FOLDER="/Volumes/Shared/Lab5"
CLIENT_HOME="$HOME/KaiLearnLab5/KaiLearnLab5-Client"
CLIENT_SRC="$CLIENT_HOME/src"
COMPUTE_SRC="$CLIENT_SRC/some"

if [ "$1" == "compile" ]; then
    javac -d "$COMPUTE_SRC" "$COMPUTE_SRC"/Compute.java "$COMPUTE_SRC"/Task.java
    javac -cp "$CLIENT_SRC" "$CLIENT_SRC"/Main.java
    javac -cp "$CLIENT_SRC" "$CLIENT_SRC"/Calculate.java
    cp "$CLIENT_SRC"/Calculate.class "$SHARED_FOLDER"/
    echo "Client compilation complete"
elif [ "$1" == "run" ]; then
    shift
    export CLASSPATH="$CLIENT_SRC:$COMPUTE_SRC"
    java -cp "$CLASSPATH" \
        -Djava.rmi.server.codebase="file://$SHARED_FOLDER/" \
        -Djava.security.policy="$CLIENT_SRC/client.policy" \
        Main $SERVER_IP "$@"
else
    echo "Usage: $0 {compile|run <numbers...>}"
    echo "Example: $0 run 5 10 15 20 25"
    exit 1
fi
