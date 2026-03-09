import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Лабораторная работа №4. Вариант 8. — TCPClient
 *
 * П.3 = 1 : адрес и порт сервера с консоли
 * П.5 = 1 : путь к журналу клиента с консоли
 * П.7     : данные передаются за 2 запроса
 */
public class TCPClient {

    static PrintWriter logWriter;
    static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    // Буфер для сообщений ДО открытия лог-файла
    static final List<String> earlyBuffer = new ArrayList<>();

    // -------------------------------------------------------
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // ═══════════════════════════════════════════════════
        // П.5 = 1: путь к журналу
        // ═══════════════════════════════════════════════════
        System.out.print("Введите путь к файлу журнала клиента: ");
        String logPath = scanner.nextLine().trim();

        // Самое первое событие — буферизуем, лог ещё не открыт
        earlyLog("═══════════════════════════════════════════════════");
        earlyLog("СЕАНС КЛИЕНТА НАЧАТ");
        earlyLog("Введите путь к файлу журнала клиента: " + logPath);

        try {
            logWriter = new PrintWriter(new FileWriter(logPath, true), true);
        } catch (IOException e) {
            System.err.println("Не удалось открыть журнал клиента: " + e);
            return;
        }

        // Сбрасываем буфер в файл
        flushEarlyBuffer();

        // ═══════════════════════════════════════════════════
        // П.3 = 1: адрес и порт с консоли
        // ═══════════════════════════════════════════════════
        System.out.print("Введите адрес сервера (например, localhost или IP): ");
        String host = scanner.nextLine().trim();
        log("Введите адрес сервера (например, localhost или IP): " + host);

        System.out.print("Введите порт сервера: ");
        String portRaw = scanner.nextLine().trim();
        log("Введите порт сервера: " + portRaw);

        int port;
        try {
            port = Integer.parseInt(portRaw);
        } catch (NumberFormatException e) {
            log("ОШИБКА: НЕВЕРНЫЙ ФОРМАТ ПОРТА: \"" + portRaw + "\" — ожидается целое число");
            return;
        }

        // ═══════════════════════════════════════════════════
        // Подключение к серверу
        // ═══════════════════════════════════════════════════
        try (
                Socket socket         = new Socket(host, port);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter writer    = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {
            socket.setKeepAlive(true);

            log("═══════════════════════════════════════════════════");
            log("ПОДКЛЮЧЕНО К СЕРВЕРУ: " + host + ":" + port);
            log("═══════════════════════════════════════════════════");

            printHelp();

            // ─── Главный цикл ──────────────────────────────
            while (true) {

                log("──────────────────────────────────────────────────");
                System.out.print("Введите команду: ");
                String input = scanner.nextLine().trim();
                log("Введите команду: " + input);

                // EXIT
                if (input.equalsIgnoreCase("EXIT")
                        || input.equalsIgnoreCase("ВЫХОД")) {
                    log("ЗАВЕРШЕНИЕ РАБОТЫ КЛИЕНТА");
                    break;
                }

                // HELP
                if (input.equalsIgnoreCase("HELP")
                        || input.equalsIgnoreCase("?")) {
                    printHelp();
                    continue;
                }

                // Пустой ввод
                if (input.isEmpty()) {
                    log("ПУСТОЙ ВВОД — ПРОПУСК");
                    continue;
                }

                // Разбираем команду
                String[] parsed = parseInput(input);
                if (parsed == null) {
                    log("ОШИБКА: НЕВЕРНЫЙ ФОРМАТ КОМАНДЫ. Введите HELP для справки.");
                    continue;
                }

                String command = parsed[0];
                String params  = parsed[1];

                // ════════════════════════════════════════════
                // ЗАПРОС 1/2: команда
                // ════════════════════════════════════════════
                log("[ЗАПРОС 1/2] ОТПРАВКА КОМАНДЫ: " + command);
                writer.println(command);

                String ack1 = reader.readLine();
                if (ack1 == null) {
                    log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
                    log("СОЕДИНЕНИЕ С СЕРВЕРОМ ПОТЕРЯНО!");
                    log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
                    break;
                }
                log("[ЗАПРОС 1/2] ОТВЕТ СЕРВЕРА: " + ack1);

                if (ack1.startsWith("ERROR")) {
                    log("ЗАПРОС 2/2 НЕ ОТПРАВЛЯЕТСЯ (ошибка на шаге 1/2)");
                    continue;
                }

                // ════════════════════════════════════════════
                // ЗАПРОС 2/2: параметры
                // ════════════════════════════════════════════
                try {
                    log("[ПАУЗА] ОЖИДАНИЕ 5 СЕК. ПЕРЕД ОТПРАВКОЙ ЗАПРОСА 2/2...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                log("[ЗАПРОС 2/2] ОТПРАВКА ПАРАМЕТРОВ: " + params);
                writer.println(params);

                String ack2 = reader.readLine();
                if (ack2 == null) {
                    log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
                    log("СОЕДИНЕНИЕ С СЕРВЕРОМ ПОТЕРЯНО!");
                    log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
                    break;
                }
                log("[ЗАПРОС 2/2] ОТВЕТ СЕРВЕРА: " + ack2);

                String result = reader.readLine();
                if (result == null) {
                    log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
                    log("СОЕДИНЕНИЕ С СЕРВЕРОМ ПОТЕРЯНО!");
                    log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
                    break;
                }
                log("[РЕЗУЛЬТАТ]  ОТВЕТ СЕРВЕРА: " + result);
            }

        } catch (UnknownHostException e) {
            log("ОШИБКА: НЕИЗВЕСТНЫЙ ХОСТ: " + host);
        } catch (SocketException e) {
            log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
            log("СОЕДИНЕНИЕ С СЕРВЕРОМ ПОТЕРЯНО!");
            log("Сервер " + host + ":" + port + " недоступен.");
            log("! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! ! !");
        } catch (IOException e) {
            log("ОШИБКА СОЕДИНЕНИЯ: " + e);
        } finally {
            log("═══════════════════════════════════════════════════");
            log("СЕАНС КЛИЕНТА ЗАВЕРШЁН");
            log("═══════════════════════════════════════════════════");
            if (logWriter != null) logWriter.close();
        }
    }

    // -------------------------------------------------------
    // Буферизация до открытия лога
    // -------------------------------------------------------
    static void earlyLog(String msg) {
        String line = "[" + SDF.format(new Date()) + "] " + msg;
        System.out.println(line);
        earlyBuffer.add(line);
    }

    static void flushEarlyBuffer() {
        for (String line : earlyBuffer) {
            logWriter.println(line);
        }
        earlyBuffer.clear();
    }

    // -------------------------------------------------------
    // Основной лог: и в консоль, и в файл
    // -------------------------------------------------------
    static void log(String msg) {
        String line = "[" + SDF.format(new Date()) + "] " + msg;
        System.out.println(line);
        if (logWriter != null) logWriter.println(line);
    }

    // -------------------------------------------------------
    // Разбор ввода пользователя:
    //   READ    <arrayIndex> <row> <col>
    //   WRITE   <arrayIndex> <row> <col> <value>
    //   MULTI   <a> <r> <c> [<a> <r> <c> ...]  — группы по 3
    //   GET_DIM <arrayIndex>
    // Возвращает { command, params } или null при ошибке
    // -------------------------------------------------------
    static String[] parseInput(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String operation = parts[0].toUpperCase();

        switch (operation) {
            case "READ": {
                if (parts.length < 2) return null;
                String[] rest = parts[1].split("\\s+");
                if (rest.length < 3) return null;
                return new String[]{"READ",
                        rest[0] + " " + rest[1] + " " + rest[2]};
            }
            case "WRITE": {
                if (parts.length < 2) return null;
                String[] rest = parts[1].split("\\s+", 4);
                if (rest.length < 4) return null;
                return new String[]{"WRITE", parts[1]};
            }
            case "MULTI": {
                if (parts.length < 2) return null;
                String[] tokens = parts[1].trim().split("\\s+");
                if (tokens.length < 3 || tokens.length % 3 != 0) return null;
                return new String[]{"MULTI", parts[1].trim()};
            }
            case "GET_DIM": {
                if (parts.length < 2) return null;
                return new String[]{"GET_DIM", parts[1].trim()};
            }
            default:
                return null;
        }
    }

    // -------------------------------------------------------
    // Справка — через log(), чтобы попала в файл
    // -------------------------------------------------------
    static void printHelp() {
        log("╔═══════════════════════════════════════════════════╗");
        log("║              ДОСТУПНЫЕ КОМАНДЫ                    ║");
        log("╠═══════════════════════════════════════════════════╣");
        log("║  READ <arrayIndex> <row> <col>                    ║");
        log("║    Пример: READ 0 1 2                             ║");
        log("║    arrayIndex: 0=int, 1=double, 2=String          ║");
        log("╠═══════════════════════════════════════════════════╣");
        log("║  WRITE <arrayIndex> <row> <col> <value>           ║");
        log("║    Пример: WRITE 1 2 3 9.99                       ║");
        log("║    Пример: WRITE 2 0 1 HelloWorld                 ║");
        log("╠═══════════════════════════════════════════════════╣");
        log("║  MULTI <arrayIndex> <row> <col> [...]             ║");
        log("║    Сброс ячеек. Параметры группами по 3 пробелом. ║");
        log("║    Количество параметров кратно 3.                ║");
        log("║    int→0  double→0.0  String→RESET_VALUE          ║");
        log("║    Пример: MULTI 0 0 0 1 1 1 2 2 2                ║");
        log("╠═══════════════════════════════════════════════════╣");
        log("║  GET_DIM <arrayIndex>                             ║");
        log("║    Пример: GET_DIM 2                              ║");
        log("╠═══════════════════════════════════════════════════╣");
        log("║  HELP — эта справка   |   EXIT — выход            ║");
        log("╚═══════════════════════════════════════════════════╝");
    }
}