package client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import compute.Task;    // в SortTask.java


/**
 * Лабораторная работа №5. Вариант 56. — Задача (Task)
 *
 * Реализует задание из Лабораторной работы №1:
 *   Сортировка массива целых чисел в обратном (убывающем) порядке.
 *
 * Объект сериализуется и передаётся на сервер-вычислитель.
 * Сервер вызывает execute() — задача выполняется на сервере.
 * Результат (отсортированный список) сериализуется и возвращается клиенту.
 *
 * Serializable обязателен — RMI передаёт объект по значению (через сериализацию).
 *
 * П.11 = 0 → байт-код этого класса (SortTask.class) хранится
 *            ЛОКАЛЬНО на компьютере сервера.
 */
public class SortTask implements Task<List<Integer>>, Serializable {

    private static final long serialVersionUID = 56L;

    // Входные данные: список чисел для сортировки
    private final List<Integer> numbers;

    /**
     * Конструктор принимает список чисел, которые нужно отсортировать.
     */
    public SortTask(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }

    /**
     * Выполняется на сервере.
     * Сортирует числа в обратном (убывающем) порядке — задание из ЛР №1.
     *
     * @return отсортированный список целых чисел
     */
    @Override
    public List<Integer> execute() {
        System.out.println("[ЗАДАЧА] Получено чисел для сортировки: " + numbers.size());
        System.out.println("[ЗАДАЧА] Входные данные:  " + numbers);

        List<Integer> sorted = new ArrayList<>(numbers);
        sorted.sort(Collections.reverseOrder());

        System.out.println("[ЗАДАЧА] Результат (убывание): " + sorted);
        return sorted;
    }
}
