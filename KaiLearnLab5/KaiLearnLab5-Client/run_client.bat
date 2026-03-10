@echo off
setlocal

set SERVER_IP=10.255.48.179
set SHARED_FOLDER=\\ARIZAVAMAC\Users\ratmir\Shared\Lab5
set CLIENT_HOME=C:\Users\boyar\Рабочий стол\учеба\Java\Lab5\labClient
set CLIENT_SRC=%CLIENT_HOME%\src
set COMPUTE_SRC=%CLIENT_SRC%\some

if "%1"=="compile" (
    javac -d "%COMPUTE_SRC%" "%COMPUTE_SRC%\Compute.java" "%COMPUTE_SRC%\Task.java"
    javac -cp "%CLIENT_SRC%" "%CLIENT_SRC%\Main.java"
    javac -cp "%CLIENT_SRC%" "%CLIENT_SRC%\Calculate.java"
    copy "%CLIENT_SRC%\Calculate.class" "%SHARED_FOLDER%"
    echo Client compilation complete
    goto :end
)

if "%1"=="run" (
    shift
    set CLASSPATH=%CLIENT_SRC%;%COMPUTE_SRC%
    "C:\Program Files\Java\jdk-15.0.2\bin\java.exe" ^
        -cp "%CLASSPATH%" ^
        -Djava.rmi.server.codebase="file:///%SHARED_FOLDER%/" ^
        -Djava.security.policy="%CLIENT_SRC%\client.policy" ^
        Main %SERVER_IP% %*
    goto :end
)

echo Usage: %0 {compile^|run ^<numbers...^>}
echo Example: %0 run 5 10 15 20 25
:end
