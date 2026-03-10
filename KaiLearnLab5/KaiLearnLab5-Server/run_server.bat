@echo off
setlocal

set SERVER_IP=10.100.40.218
set JDK_HOME=C:\Users\denna\.jdks\ms-11.0.30
set SERVER_HOME=C:\Work\java-labs\KaiLearnLab5\KaiLearnLab5-Server
set SERVER_SRC=%SERVER_HOME%\src
set COMPUTE_SRC=%SERVER_SRC%\some
set SHARED_FOLDER=\\10.100.40.148\Shared\Lab5

if "%1"=="clean" (
    del /Q "%SERVER_SRC%\*.class"
    del /Q "%COMPUTE_SRC%\*.class"
    del /Q "%COMPUTE_SRC%\some\*.class" 2>nul
    rmdir "%COMPUTE_SRC%\some" 2>nul
    echo Cleaned
    goto :end
)

if "%1"=="compile" (
    "%JDK_HOME%\bin\javac.exe" -d "%SERVER_SRC%" "%COMPUTE_SRC%\Compute.java" "%COMPUTE_SRC%\Task.java"
    "%JDK_HOME%\bin\javac.exe" -cp "%SERVER_SRC%" -d "%SERVER_SRC%" "%SERVER_SRC%\ComputeEngine.java"
    echo Server compilation complete
    goto :end
)

if "%1"=="registry" (
    set CLASSPATH=%SERVER_SRC%
    start "RMI Registry" cmd /k "%JDK_HOME%\bin\rmiregistry.exe"
    echo RMI Registry started
    goto :end
)

if "%1"=="server" (
    set CLASSPATH=%SERVER_SRC%
    "%JDK_HOME%\bin\java.exe" ^
        -cp "%CLASSPATH%" ^
        -Djava.rmi.server.codebase="file:///%SHARED_FOLDER%/" ^
        -Djava.rmi.server.hostname="%SERVER_IP%" ^
        -Djava.security.policy="%SERVER_SRC%\server.policy" ^
        ComputeEngine
    goto :end
)

echo Usage: %0 {clean^|compile^|registry^|server}
:end
