import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Лабораторная работа №4. Вариант 8. — TCPServer
 *
 * П.4 = 3 : порт из файла server.properties
 * П.6 = 1 : путь к журналу сервера с консоли
 * Задание №3 : три 2D-массива (индексы 0, 1, 2)
 */
public class TCPServer {

    // -------------------------------------------------------
    // Три 2D-массива (0=int, 1=double, 2=String)
    // -------------------------------------------------------
    static int[][] intArray = {
            {1,  2,  3,  4},
            {5,  6,  7,  8},
            {9,  10, 11, 12},
            {13, 14, 15, 16}
    };
    static double[][] doubleArray = {
            {1.1,  2.2,  3.3,  4.4},
            {5.5,  6.6,  7.7,  8.8},
            {9.9,  10.0, 11.1, 12.2},
            {13.3, 14.4, 15.5, 16.6}
    };
    static String[][] stringArray = {
            {"alpha",   "beta",    "gamma",  "delta"},
            {"epsilon", "zeta",    "eta",    "theta"},
            {"iota",    "kappa",   "lambda", "mu"},
            {"nu",      "xi",      "omicron","pi"}
    };

    static final int    INT_PRESET    = 0;
    static final double DOUBLE_PRESET = 0.0;
    static final String STRING_PRESET = "RESET_VALUE";
    static final String[] ARRAY_NAMES = {"int[][]", "double[][]", "String[][]"};

    static final ReentrantLock arrLock = new ReentrantLock();
    static final ReentrantLock logLock = new ReentrantLock();
    static PrintWriter logWriter;
    static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    // -------------------------------------------------------
    public static void main(String[] args) {

        int port = readPortFromConfig();
        if (port < 0) return;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите путь к файлу журнала сервера: ");
        String logPath = scanner.nextLine().trim();

        try {
            logWriter = new PrintWriter(new FileWriter(logPath, true), true);
        } catch (IOException e) {
            System.err.println("Не удалось открыть журнал: " + e);
            return;
        }

        // Shutdown hook — срабатывает при любой остановке сервера
        // (Ctrl+C, закрытие окна терминала, System.exit())
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log("═══════════════════════════════════════════════════");
            log("СЕРВЕР ОСТАНОВЛЕН");
            log("═══════════════════════════════════════════════════");
            if (logWriter != null) logWriter.close();
        }));

        log("═══════════════════════════════════════════════════");
        log("СЕАНС СЕРВЕРА НАЧАТ");
        log("Введите путь к файлу журнала сервера: " + logPath);
        log("СЕРВЕР ЗАПУЩЕН НА ПОРТУ: " + port);
        log("═══════════════════════════════════════════════════");
        printArrays(null);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientAddr = clientSocket.getInetAddress().getHostAddress();
                log("НОВОЕ ПОДКЛЮЧЕНИЕ ОТ КЛИЕНТА: " + clientAddr);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            log("ОШИБКА СЕРВЕРА: " + e);
        }
    }

    // -------------------------------------------------------
    static int readPortFromConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("server.properties")) {
            props.load(fis);
            String portStr = props.getProperty("port");
            if (portStr == null) {
                System.err.println("Параметр 'port' не найден в server.properties");
                return -1;
            }
            return Integer.parseInt(portStr.trim());
        } catch (FileNotFoundException e) {
            System.err.println("Файл server.properties не найден.");
            return -1;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Ошибка чтения server.properties: " + e);
            return -1;
        }
    }

    // -------------------------------------------------------
    // Лог
    // -------------------------------------------------------
    static void log(String msg) {
        logLock.lock();
        try {
            String line = "[" + SDF.format(new Date()) + "] " + msg;
            System.out.println(line);
            logWriter.println(line);
        } finally {
            logLock.unlock();
        }
    }

    // -------------------------------------------------------
    // Вывод всех массивов
    // -------------------------------------------------------
    static void printArrays(String clientAddr) {
        String who = (clientAddr != null)
                ? " (после операции клиента " + clientAddr + ")"
                : " (начальное состояние)";
        log("───────────────────────────────────────────────────");
        log("СОСТОЯНИЕ МАССИВОВ" + who + ":");
        log("");
        log("  Массив 0 | " + ARRAY_NAMES[0] + " | размерность: "
                + intArray.length + "x" + intArray[0].length);
        for (int r = 0; r < intArray.length; r++)
            log("    [" + r + "] " + Arrays.toString(intArray[r]));
        log("");
        log("  Массив 1 | " + ARRAY_NAMES[1] + " | размерность: "
                + doubleArray.length + "x" + doubleArray[0].length);
        for (int r = 0; r < doubleArray.length; r++)
            log("    [" + r + "] " + Arrays.toString(doubleArray[r]));
        log("");
        log("  Массив 2 | " + ARRAY_NAMES[2] + " | размерность: "
                + stringArray.length + "x" + stringArray[0].length);
        for (int r = 0; r < stringArray.length; r++)
            log("    [" + r + "] " + Arrays.toString(stringArray[r]));
        log("───────────────────────────────────────────────────");
    }

    // -------------------------------------------------------
    // Обработчик клиента
    // -------------------------------------------------------
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final String clientAddr;

        ClientHandler(Socket s) {
            this.socket = s;
            this.clientAddr = s.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    PrintWriter writer = new PrintWriter(
                            socket.getOutputStream(), true)
            ) {
                String commandLine;
                while ((commandLine = reader.readLine()) != null) {

                    String command = commandLine.trim().toUpperCase();

                    // ---- Запрос 1/2: команда ----
                    log("[ЗАПРОС 1/2] ПОЛУЧЕНА КОМАНДА [" + command + "] ОТ КЛИЕНТА: " + clientAddr);

                    // Проверка команды
                    if (!command.equals("READ") && !command.equals("WRITE")
                            && !command.equals("MULTI") && !command.equals("GET_DIM")) {
                        String err = "ОШИБКА: НЕИЗВЕСТНАЯ КОМАНДА [" + command + "]";
                        log("[ЗАПРОС 1/2] ОТПРАВКА КЛИЕНТУ " + clientAddr + ": " + err);
                        writer.println("ERROR " + err);
                        continue;
                    }

                    // Подтверждение команды
                    String ackCmd = "КОМАНДА [" + command + "] ПОЛУЧЕНА";
                    log("[ЗАПРОС 1/2] ОТПРАВКА КЛИЕНТУ " + clientAddr + ": " + ackCmd);
                    writer.println(ackCmd);

                    // ---- Запрос 2/2: параметры ----
                    String paramsLine = reader.readLine();
                    if (paramsLine == null) break;

                    log("[ЗАПРОС 2/2] ПОЛУЧЕНЫ ПАРАМЕТРЫ [" + paramsLine.trim() + "] ОТ КЛИЕНТА: " + clientAddr);

                    // Подтверждение параметров
                    String ackParams = "ПАРАМЕТРЫ ПОЛУЧЕНЫ";
                    log("[ЗАПРОС 2/2] ОТПРАВКА КЛИЕНТУ " + clientAddr + ": " + ackParams);
                    writer.println(ackParams);

                    // Обработка и ответ
                    String result = processCommand(command, paramsLine.trim());
                    log("РЕЗУЛЬТАТ ДЛЯ КЛИЕНТА " + clientAddr + ": " + result);
                    writer.println(result);

                    // Вывод массивов
                    printArrays(clientAddr);
                }

            } catch (IOException e) {
                log("ОШИБКА СОЕДИНЕНИЯ С КЛИЕНТОМ " + clientAddr + ": " + e);
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
                log("КЛИЕНТ " + clientAddr + " ОТКЛЮЧИЛСЯ");
            }
        }

        private String processCommand(String command, String params) {
            arrLock.lock();
            try {
                switch (command) {
                    case "READ":    return handleRead(params);
                    case "WRITE":   return handleWrite(params);
                    case "RESET":
                    case "MULTI":   return handleMulti(params);
                    case "GET_DIM": return handleGetDim(params);
                    default:        return "ОШИБКА: НЕИЗВЕСТНАЯ КОМАНДА";
                }
            } finally {
                arrLock.unlock();
            }
        }

        // READ: "<arrayIndex> <row> <col>"
        private String handleRead(String params) {
            String[] p = params.split("\\s+");
            if (p.length < 3)
                return "ОШИБКА: READ требует 3 параметра: <arrayIndex> <row> <col>";
            try {
                int arr = Integer.parseInt(p[0]);
                int row = Integer.parseInt(p[1]);
                int col = Integer.parseInt(p[2]);
                String val;
                switch (arr) {
                    case 0: val = String.valueOf(intArray[row][col]);    break;
                    case 1: val = String.valueOf(doubleArray[row][col]); break;
                    case 2: val = stringArray[row][col];                 break;
                    default: return "ОШИБКА: НОМЕР МАССИВА ДОЛЖЕН БЫТЬ 0, 1 ИЛИ 2";
                }
                return "ЗНАЧЕНИЕ ЯЧЕЙКИ [" + row + "][" + col + "] МАССИВА "
                        + arr + " (" + ARRAY_NAMES[arr] + ") = " + val;
            } catch (ArrayIndexOutOfBoundsException e) {
                return "ОШИБКА: ИНДЕКС ВЫХОДИТ ЗА ПРЕДЕЛЫ МАССИВА";
            } catch (NumberFormatException e) {
                return "ОШИБКА: НЕВЕРНЫЙ ФОРМАТ ЧИСЛА В ПАРАМЕТРАХ";
            }
        }

        // WRITE: "<arrayIndex> <row> <col> <value>"
        private String handleWrite(String params) {
            String[] p = params.split("\\s+", 4);
            if (p.length < 4)
                return "ОШИБКА: WRITE требует 4 параметра: <arrayIndex> <row> <col> <value>";
            try {
                int arr    = Integer.parseInt(p[0]);
                int row    = Integer.parseInt(p[1]);
                int col    = Integer.parseInt(p[2]);
                String val = p[3];
                switch (arr) {
                    case 0: intArray[row][col]    = Integer.parseInt(val);   break;
                    case 1: doubleArray[row][col] = Double.parseDouble(val); break;
                    case 2: stringArray[row][col] = val;                     break;
                    default: return "ОШИБКА: НОМЕР МАССИВА ДОЛЖЕН БЫТЬ 0, 1 ИЛИ 2";
                }
                return "ЗАПИСЬ ВЫПОЛНЕНА: ЯЧЕЙКА [" + row + "][" + col + "] МАССИВА "
                        + arr + " (" + ARRAY_NAMES[arr] + ") = " + val;
            } catch (ArrayIndexOutOfBoundsException e) {
                return "ОШИБКА: ИНДЕКС ВЫХОДИТ ЗА ПРЕДЕЛЫ МАССИВА";
            } catch (NumberFormatException e) {
                return "ОШИБКА: НЕВЕРНЫЙ ФОРМАТ ЧИСЛА ИЛИ ЗНАЧЕНИЯ";
            }
        }

        // MULTI: "<a> <r> <c> [<a> <r> <c> ...]"  — параметры через пробел группами по 3
        private String handleMulti(String params) {
            if (params == null || params.trim().isEmpty())
                return "ОШИБКА: MULTI требует параметры: <arrayIndex> <row> <col> [<arrayIndex> <row> <col> ...]";

            String[] tokens = params.trim().split("\\s+");

            // Проверка: кол-во токенов должно быть кратно 3
            if (tokens.length % 3 != 0)
                return "ОШИБКА: MULTI — количество параметров должно быть кратно 3. "
                        + "Получено: " + tokens.length + " параметр(а/ов). "
                        + "Формат: <arrayIndex> <row> <col> [<arrayIndex> <row> <col> ...]";

            int cellCount = tokens.length / 3;
            StringBuilder sb = new StringBuilder(
                    "MULTI ВЫПОЛНЕН: СБРОШЕНО ЯЧЕЕК — " + cellCount + ". СПИСОК: ");

            for (int i = 0; i < tokens.length; i += 3) {
                String tArr = tokens[i];
                String tRow = tokens[i + 1];
                String tCol = tokens[i + 2];
                int groupNum = (i / 3) + 1;

                // Проверка что токены — числа
                int arr, row, col;
                try {
                    arr = Integer.parseInt(tArr);
                } catch (NumberFormatException e) {
                    return "ОШИБКА: MULTI — в группе " + groupNum + " неверный arrayIndex: \""
                            + tArr + "\" (ожидается 0, 1 или 2)";
                }
                try {
                    row = Integer.parseInt(tRow);
                } catch (NumberFormatException e) {
                    return "ОШИБКА: MULTI — в группе " + groupNum + " неверный row: \""
                            + tRow + "\" (ожидается целое число)";
                }
                try {
                    col = Integer.parseInt(tCol);
                } catch (NumberFormatException e) {
                    return "ОШИБКА: MULTI — в группе " + groupNum + " неверный col: \""
                            + tCol + "\" (ожидается целое число)";
                }

                // Проверка номера массива
                if (arr < 0 || arr > 2)
                    return "ОШИБКА: MULTI — в группе " + groupNum + " arrayIndex=" + arr
                            + " недопустим. Допустимые значения: 0 (int), 1 (double), 2 (String)";

                // Проверка границ и запись
                try {
                    switch (arr) {
                        case 0: intArray[row][col]    = INT_PRESET;    break;
                        case 1: doubleArray[row][col] = DOUBLE_PRESET; break;
                        case 2: stringArray[row][col] = STRING_PRESET; break;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    int maxRow = (arr == 0) ? intArray.length
                            : (arr == 1) ? doubleArray.length : stringArray.length;
                    int maxCol = (arr == 0) ? intArray[0].length
                            : (arr == 1) ? doubleArray[0].length : stringArray[0].length;
                    return "ОШИБКА: MULTI — в группе " + groupNum + " индекс [" + row + "]["
                            + col + "] выходит за пределы массива " + arr
                            + " (размер: " + maxRow + "×" + maxCol + ")";
                }

                sb.append("массив ").append(arr).append("[").append(row)
                        .append("][").append(col).append("] ");
            }
            return sb.toString().trim();
        }

        // GET_DIM: "<arrayIndex>"
        private String handleGetDim(String params) {
            String[] p = params.split("\\s+");
            if (p.length < 1 || p[0].isEmpty())
                return "ОШИБКА: GET_DIM требует 1 параметр: <arrayIndex>";
            try {
                int arr = Integer.parseInt(p[0]);
                int rows, cols;
                switch (arr) {
                    case 0: rows = intArray.length;    cols = intArray[0].length;    break;
                    case 1: rows = doubleArray.length; cols = doubleArray[0].length; break;
                    case 2: rows = stringArray.length; cols = stringArray[0].length; break;
                    default: return "ОШИБКА: НОМЕР МАССИВА ДОЛЖЕН БЫТЬ 0, 1 ИЛИ 2";
                }
                return "РАЗМЕРНОСТЬ МАССИВА " + arr + " (" + ARRAY_NAMES[arr] + "): "
                        + rows + " строк × " + cols + " столбцов";
            } catch (NumberFormatException e) {
                return "ОШИБКА: НЕВЕРНЫЙ ФОРМАТ НОМЕРА МАССИВА";
            }
        }
    }
}