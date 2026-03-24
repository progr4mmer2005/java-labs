package client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import compute.Task;



public class SortTask implements Task<List<Integer>>, Serializable {

    private static final long serialVersionUID = 56L;

    private final List<Integer> numbers;

    public SortTask(List<Integer> numbers) {
        this.numbers = new ArrayList<>(numbers);
    }


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
