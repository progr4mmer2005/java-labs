49.
0
0
0
0
0
1

№ (П.6) (П.7) (П.8) (П.9) (П.10) (П.11)

Практическая часть
2. Задание, полученное в первой лабораторной работе (В последовательности чисел найти сумму «чётных и положительных» и «нечётных и
положительных» чисел. 
public class Lab1Service {

    //В последовательности чисел найти сумму «чётных и положительных» и «нечётных и положительных» чисел.

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Ошибка: не переданы аргументы. Укажите числа через пробел.");
            return;
        }

        long sumEvenPositive = 0;
        long sumOddPositive = 0;
        int validNumbersCount = 0;

        for (String arg : args) {
            int num = Integer.parseInt(arg);
            validNumbersCount++;

            if (num > 0) {
                if (num % 2 == 0) {
                    sumEvenPositive += num;
                } else {
                    sumOddPositive += num;
                }
            }
        }

        if (validNumbersCount == 0) {
            System.out.println("Не найдено корректных чисел для обработки.");
            return;
        }

        System.out.println("Сумма чётных и положительных чисел: " + sumEvenPositive);
        System.out.println("Сумма нечётных и положительных чисел: " + sumOddPositive);
    }
}), необходимо представить как
задачу «Task», которая должна решаться вычислителем.
3. Распределённое приложение должно запускаться минимум на двух различных
компьютерах (клиентов м.б. сколько угодно).
4. При реализации клиента параметры его запуска (адрес сервера и прочее в
зависимости от задания) передавать через командную строку.
5. Исходные файлы пакетов, а также файлы байт-кода должны находится в отдельных
каталогах. Например: «d:\Temp\home\server\», «d:\Temp\home\client\» и
«d:\Temp\home\interface\». (у меня macos, нужно как то сделать иначе, вот так у меня файлы лежат:
ratmir@arizavamac KaiLearnLab5 % ls -la
total 8
drwxr-xr-x  5 ratmir  staff  160 Mar  9 23:55 .
drwxr-xr-x@ 5 ratmir  staff  160 Mar  9 23:53 ..
drwxr-xr-x@ 6 ratmir  staff  192 Mar  9 23:53 KaiLearnLab5-Client
drwxr-xr-x@ 6 ratmir  staff  192 Mar  9 23:54 KaiLearnLab5-Server
-rw-r--r--@ 1 ratmir  staff   65 Mar  9 23:55 task.md

ratmir@arizavamac KaiLearnLab5-Client % ls -la
total 16
drwxr-xr-x@ 6 ratmir  staff  192 Mar  9 23:53 .
drwxr-xr-x  5 ratmir  staff  160 Mar  9 23:55 ..
-rw-r--r--@ 1 ratmir  staff  344 Mar  9 23:53 .gitignore
drwxr-xr-x@ 6 ratmir  staff  192 Mar  9 23:59 .idea
-rw-r--r--@ 1 ratmir  staff  423 Mar  9 23:53 KaiLearnLab5-Client.iml
drwxr-xr-x@ 3 ratmir  staff   96 Mar  9 23:53 src

ratmir@arizavamac KaiLearnLab5-Server % ls -la                
total 16
drwxr-xr-x@ 6 ratmir  staff  192 Mar  9 23:54 .
drwxr-xr-x  5 ratmir  staff  160 Mar  9 23:55 ..
-rw-r--r--@ 1 ratmir  staff  344 Mar  9 23:54 .gitignore
drwxr-xr-x@ 6 ratmir  staff  192 Mar  9 23:59 .idea
-rw-r--r--@ 1 ratmir  staff  423 Mar  9 23:54 KaiLearnLab5-Server.iml
drwxr-xr-x@ 3 ratmir  staff   96 Mar  9 23:54 src)
6. Создать либо же нет, в зависимости от варианта, jar-файл пакета «compute»: 0 - не
создавать.
7. Размещение файлов пакета «compute» для клиента: 0 - в локальной директории компьютера клиента.
8. Размещение файлов пакета «compute» для сервера: 0 - в локальной директории компьютера сервера, 1 – в каталоге в
сетевой папке обмена.
9. Размещение файла безопасности клиента: 0 - в локальной директории клиента.
10. Размещение файла безопасности сервера: 0 - в локальной директории сервера.
11. Размещение файла байт-кода задачи «Task» (в примере это «client\Pi.class»): 1 – в каталоге в сетевой папке обмена.


Мак сервер
chmod +x run_server.sh
./run_server.sh compile
./run_server.sh registry
./run_server.sh server

Винда клиент
run_client.bat compile
run_client.bat run 5 10 15 20 25 -3 -8

Винда сервер
run_server.bat compile
run_server.bat registry
run_server.bat server

Мак клиент
chmod +x run_client.sh
./run_client.sh compile
./run_client.sh run 5 10 15 20 25 -3 -8