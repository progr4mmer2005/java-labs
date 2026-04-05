/*
 * Лабораторная работа №7 (ЛР8). Вариант 56.
 *
 * Число варианта: 15
 * Двоичное (5 бит): 0 1 1 1 1
 *
 * П.5 = 0  — код функции размещён НА «Главной странице» (не в Bean)
 * П.6 = 1  — заголовки страниц: «Раз», «Два», «Три»
 * П.7 = 1  — Стартовая страница: текст задания, ГРУППА студента и КНОПКА перехода
 * П.8 = 1  — результаты в ВИДИМОЙ таблице (с рамкой)
 * П.9 = 11 = 3 — общее число переходов (обновлений) всех страниц, хранится в Bean
 *
 * Функция из ЛР1: сортировка последовательности чисел по возрастанию
 */

package jspappl;

public class SortBean {

    // П.9 = 3: общее число переходов по всем страницам приложения
    private int totalCounter;

    public SortBean() {
        totalCounter = 0;
    }

    /**
     * @return the totalCounter
     */
    public int getTotalCounter() {
        return totalCounter;
    }

    /**
     * @param totalCounter the totalCounter to set
     */
    public void setTotalCounter(int totalCounter) {
        this.totalCounter = totalCounter;
    }

    public void addCounter() {
        this.totalCounter = this.totalCounter + 1;
    }
}