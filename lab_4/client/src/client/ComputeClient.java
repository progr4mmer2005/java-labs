package client;

import compute.Compute;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

/**
 * Лабораторная работа №5. Вариант 56. — Клиент RMI
 *
 * П.4  — параметры запуска клиента передаются через командную строку:
 *         args[0]       = адрес сервера (IP или hostname)
 *         args[1..N]    = целые числа для сортировки
 *
 * Пример запуска:
 *   java ... client.ComputeClient 192.168.1.5 5 3 1 4 1 5 9 2 6
 *
 * П.6  = 1 → используется compute.jar
 * П.7  = 1 → compute.jar берётся из СЕТЕВОЙ папки
 * П.9  = 0 → client.policy лежит ЛОКАЛЬНО на клиенте
 */
public class ComputeClient {

    public static void main(String[] args) {

        // Проверка аргументов командной строки
        if (args.length < 2) {
            System.err.println("Использование: java client.ComputeClient <адрес_сервера> <число1> [число2] ...");
            System.err.println("Пример: java client.ComputeClient localhost 5 3 1 9 2 7");
            System.exit(1);
        }

        // Установка менеджера безопасности (обязательно для RMI)
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        // П.4: адрес сервера из командной строки
        String serverHost = args[0];

        // Парсим числа из аргументов командной строки
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            try {
                numbers.add(Integer.parseInt(args[i]));
            } catch (NumberFormatException e) {
                System.err.println("[КЛИЕНТ] Ошибка: \"" + args[i] + "\" не является целым числом.");
                System.exit(1);
            }
        }

        System.out.println("[КЛИЕНТ] Подключение к серверу: " + serverHost);
        System.out.println("[КЛИЕНТ] Числа для сортировки: " + numbers);

        try {
            // Получаем реестр RMI на сервере (стандартный порт 1099)
            Registry registry = LocateRegistry.getRegistry(serverHost);

            // Ищем объект Compute в реестре по имени
            Compute comp = (Compute) registry.lookup("Compute");
            System.out.println("[КЛИЕНТ] Соединение с вычислителем установлено.");

            // Создаём задачу (сортировка из ЛР №1) и отправляем на сервер
            SortTask task = new SortTask(numbers);
            System.out.println("[КЛИЕНТ] Отправка задачи на сервер...");

            // Удалённый вызов: задача выполняется на сервере
            List<Integer> result = comp.executeTask(task);

            // Выводим результат
            System.out.println("[КЛИЕНТ] Результат получен от сервера.");
            System.out.println("[КЛИЕНТ] Отсортированный массив (убывание):");
            result.forEach(n -> System.out.println("  " + n));

        } catch (Exception e) {
            System.err.println("[КЛИЕНТ] Ошибка при выполнении задачи:");
            e.printStackTrace();
        }
    }
}
