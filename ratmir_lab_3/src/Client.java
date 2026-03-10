import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Properties;

public class Client {

    public static void main(String[] args) {
        Properties config = loadConfig("client_config.properties");
        String host = config.getProperty("server.host", "localhost");
        int port = Integer.parseInt(config.getProperty("server.port", "8888"));

        System.out.print("Введите путь к файлу Журнала клиента: ");
        Scanner console = new Scanner(System.in);
        String clientLogPath = console.nextLine().trim();

        Client client = new Client();
        client.connect(host, port, clientLogPath, console);
    }

    private static Properties loadConfig(String filename) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(filename)) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Не удалось загрузить конфиг: " + filename +
                    ", используются значения по умолчанию");
        }
        return props;
    }

    public void connect(String host, int port, String logPath, Scanner console) {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true)) {

            logToFile(logPath, "Подключено к серверу: " + host + ":" + port);
            System.out.println("Подключено к серверу: " + host + ":" + port);

            System.out.println("\nВводите части выражения (завершите '=' для получения результата):");

            while (true) {
                System.out.print("> ");
                String input = console.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                out.println(input);
                logToFile(logPath, "Отправлено: " + input);

                if (input.contains("=")) {
                    String response = in.readLine();
                    if (response != null) {
                        logToFile(logPath, "Получено от сервера: " + response);
                        System.out.println("Ответ сервера: " + response);
                    }
                    break;
                } else {
                    System.out.println("Часть выражения принята. Продолжайте ввод...");
                }
            }

            logToFile(logPath, "Сессия завершена");
            System.out.println("Соединение закрыто");

        } catch (UnknownHostException e) {
            logToFile(logPath, "Ошибка: хост не найден - " + host);
            System.err.println("Хост не найден: " + host);
        } catch (IOException e) {
            logToFile(logPath, "Ошибка соединения: " + e.getMessage());
            System.err.println("Ошибка соединения: " + e.getMessage());
        }
    }

    private static void logToFile(String path, String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            pw.println("[" + new java.util.Date() + "] " + message);
        } catch (IOException e) {
            System.err.println("Ошибка записи в журнал: " + e.getMessage());
        }
        System.out.println("[CLIENT LOG] " + message);
    }
}