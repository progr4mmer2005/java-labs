@echo off
setlocal

set SERVER_IP=10.255.48.179
set SERVER_HOME=C:\Users\boyar\Рабочий стол\учеба\Java\Lab5\labServer
set SERVER_SRC=%SERVER_HOME%\src
set COMPUTE_SRC=%SERVER_SRC%\some
set SHARED_FOLDER=\\DESKTOP-EO7G4BH\Users\boyar\Shared\Lab5

if "%1"=="compile" (
    javac -d "%COMPUTE_SRC%" "%COMPUTE_SRC%\Compute.java" "%COMPUTE_SRC%\Task.java"
    javac -cp "%SERVER_SRC%" "%SERVER_SRC%\ComputeEngine.java"
    echo Server compilation complete
    goto :end
)

if "%1"=="registry" (
    set CLASSPATH=%COMPUTE_SRC%
    start "RMI Registry" cmd /k "rmiregistry"
    echo RMI Registry started
    goto :end
)

if "%1"=="server" (
    set CLASSPATH=%SERVER_SRC%;%COMPUTE_SRC%
    "C:\Program Files\Java\jdk-15.0.2\bin\java.exe" ^
        -cp "%CLASSPATH%" ^
        -Djava.rmi.server.codebase="file:///%SHARED_FOLDER%/" ^
        -Djava.rmi.server.hostname="%SERVER_IP%" ^
        -Djava.security.policy="%SERVER_SRC%\server.policy" ^
        ComputeEngine
    goto :end
)

echo Usage: %0 {compile^|registry^|server}
:end
