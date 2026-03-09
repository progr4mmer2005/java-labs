import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static StringBuilder expressionBuffer = new StringBuilder();
    private static final Object bufferLock = new Object();

    public static void main(String[] args) {
        int port = 8888;

        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат порта, используется порт по умолчанию: 8888");
            }
        }

        System.out.print("Введите путь к файлу Журнала сервера: ");
        Scanner console = new Scanner(System.in);
        String serverLogPath = console.nextLine().trim();

        Server server = new Server();
        server.start(port, serverLogPath);
    }

    public void start(int port, String logPath) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
            logToFile(logPath, "Сервер запущен на порту: " + port);
            System.out.println("Сервер запущен на порту: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, logPath)).start();
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            if (serverSocket != null) {
                try { serverSocket.close(); } catch (IOException e) {}
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String serverLogPath;

        public ClientHandler(Socket socket, String logPath) {
            this.socket = socket;
            this.serverLogPath = logPath;
        }

        @Override
        public void run() {
            String clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            try (
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(
                            socket.getOutputStream(), true)
            ) {
                String line = in.readLine();
                if (line == null) return;

                logToFile(serverLogPath, "Запрос от [" + clientInfo + "]: " + line);

                synchronized (bufferLock) {
                    expressionBuffer.append(line);
                }

                if (line.contains("=")) {
                    String expr;
                    synchronized (bufferLock) {
                        expr = expressionBuffer.toString().replace("=", "").trim();
                        expressionBuffer.setLength(0);
                    }

                    String result = evaluateExpression(expr);
                    out.println(result);
                    logToFile(serverLogPath, "Ответ для [" + clientInfo + "]: " + result);
                }

            } catch (IOException e) {
                logToFile(serverLogPath, "Ошибка соединения с [" + clientInfo + "]: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }

    private static String evaluateExpression(String expr) {
        try {
            double result = parseExpression(expr);
            return "RESULT: " + result;
        } catch (Exception e) {
            return "ERROR: Неверное выражение";
        }
    }

    private static double parseExpression(String expr) {
        expr = expr.replaceAll("\\s+", "");
        int[] pos = {0};
        return parseAddSub(expr, pos);
    }

    private static double parseAddSub(String expr, int[] pos) {
        double left = parseMulDiv(expr, pos);

        while (pos[0] < expr.length()) {
            char c = expr.charAt(pos[0]);
            if (c == '+' || c == '-') {
                pos[0]++;
                double right = parseMulDiv(expr, pos);
                left = (c == '+') ? left + right : left - right;
            } else {
                break;
            }
        }
        return left;
    }

    private static double parseMulDiv(String expr, int[] pos) {
        double left = parseNumber(expr, pos);

        while (pos[0] < expr.length()) {
            char c = expr.charAt(pos[0]);
            if (c == '*' || c == '/') {
                pos[0]++;
                double right = parseNumber(expr, pos);
                left = (c == '*') ? left * right : left / right;
            } else {
                break;
            }
        }
        return left;
    }

    private static double parseNumber(String expr, int[] pos) {
        StringBuilder num = new StringBuilder();
        while (pos[0] < expr.length()) {
            char c = expr.charAt(pos[0]);
            if (Character.isDigit(c) || c == '.') {
                num.append(c);
                pos[0]++;
            } else {
                break;
            }
        }
        if (num.length() == 0) {
            throw new RuntimeException("Ожидалось число");
        }
        return Double.parseDouble(num.toString());
    }

    private static void logToFile(String path, String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path, true))) {
            pw.println("[" + new java.util.Date() + "] " + message);
        } catch (IOException e) {
            System.err.println("Ошибка записи в журнал: " + e.getMessage());
        }
        System.out.println("[SERVER LOG] " + message);
    }
}