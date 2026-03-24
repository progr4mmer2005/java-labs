/*
 * Лабораторная работа №7. Вариант 56.
 *
 * Число варианта: 15
 * Двоичное (6 бит): 0 0 1 1 1 1
 *                   | | | | | |
 *                П.5 П.6 П.7 П.8 П.9 П.11
 *
 * П.5  = 0 — при перезагрузке отображается изменение триггера (bool flip)
 * П.6  = 0 — вывод строк одна за другой (таблица БЕЗ видимых границ, 1 столбец)
 * П.7  = 1 — при обновлении размер текста УВЕЛИЧИВАЕТСЯ до максимума,
 *            затем надпись «дальнейшее увеличение невозможно» (вне таблицы)
 * П.8  = 1 — сброс размера текста через параметр URL до УКАЗАННОГО значения
 *            (параметр reset=N, где N — уровень от 1 до 6)
 * П.9  = 1 — ФИО и номер группы выводятся НА СТРАНИЦЕ (не в заголовке вкладки)
 * П.11 = 1 — порт Tomcat изменён на произвольный (например, 8088);
 *            настраивается в NetBeans: Tools -> Servers -> HTTP Port
 *
 * П.10 — основная функция из ЛР1: сортировка последовательности чисел по возрастанию.
 *         Числа передаются через параметр URL: nums=5,3,1,4,2
 *
 * Пример запуска:
 *   http://localhost:8088/ServletAppl?nums=5,3,1,4,2&name=Наумов+Денис+Олегович&institute=ИКТЗИ&group=4311
 *
 * Сброс размера текста до уровня 4:
 *   http://localhost:8088/ServletAppl?nums=5,3,1&reset=2&name=Наумов+Денис+Олегович&institute=ИКТЗИ&group=4311
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/ServletAppl"})
public class Servlet1 extends HttpServlet {

    // П.5 = 0: триггер — переключается при каждом обращении
    static boolean trigger;

    // П.7 = 1: уровень заголовка h6..h1 (h6 мелкий -> h1 крупный)
    static int cycle;

    public Servlet1() {
        Servlet1.trigger = false;
        Servlet1.cycle   = 6;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            // ---- параметры из URL ----
            String nums      = request.getParameter("nums");
            String reset     = request.getParameter("reset");
            String name      = request.getParameter("name");
            String institute = request.getParameter("institute");
            String group     = request.getParameter("group");

            // П.8 = 1: сброс цикла до указанного значения через параметр reset=N
            if (reset != null) {
                try {
                    int r = Integer.parseInt(reset.trim());
                    if (r >= 1 && r <= 6) Servlet1.cycle = r;
                } catch (NumberFormatException e) {}
            }

            // П.5 = 0: триггер переключается при каждом обращении
            if (Servlet1.trigger) Servlet1.trigger = false;
            else                  Servlet1.trigger = true;

            // П.7 = 1: увеличиваем размер текста при каждом обновлении
            boolean maxReached = false;
            if (reset == null) {
                if (Servlet1.cycle > 1) Servlet1.cycle--;
                else                   maxReached = true;
            }

            // П.10: сортировка по возрастанию (функция из ЛР1)
            String sortedResult   = "";
            String originalResult = "";
            String sortError      = "";

            if (nums != null && !nums.trim().isEmpty()) {
                try {
                    String[] parts = nums.trim().split(",");
                    double[] arr   = new double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        arr[i] = Double.parseDouble(parts[i].trim());
                    }
                    double[] sorted = arr.clone();
                    Arrays.sort(sorted);

                    StringBuilder sbOrig   = new StringBuilder();
                    StringBuilder sbSorted = new StringBuilder();
                    for (int i = 0; i < arr.length; i++) {
                        if (i > 0) { sbOrig.append(", "); sbSorted.append(", "); }
                        sbOrig.append(arr[i]);
                        sbSorted.append(sorted[i]);
                    }
                    originalResult = sbOrig.toString();
                    sortedResult   = sbSorted.toString();

                } catch (NumberFormatException e) {
                    sortError = "Ошибка: передайте числа через запятую, например nums=5,3,1,4,2";
                }
            } else {
                sortError = "Числа не переданы. Пример: ?nums=5,3,1,4,2";
            }

            // ---- HTML ----
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet1 — ЛР7</title>");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1>ServletAppl" + request.getServletPath() + "</h1>");

            // П.9 = 1: ФИО и группа из параметров URL, выводятся на странице
            if (name != null && institute != null && group != null) {
                out.println("<p><b>Студент:</b> " + name +
                            " &nbsp;&nbsp; <b>Институт:</b> " + institute +
                            " &nbsp;&nbsp; <b>Группа:</b> " + group + "</p>");
            } else {
                out.println("<p><i>Укажите в URL: name=, institute=, group=</i></p>");
            }

            // П.5 = 0: показываем триггер
            out.println("<p>Триггер (меняется при каждом обращении): <b>" + Servlet1.trigger + "</b></p>");

            // П.7 = 1: если достигнут максимум — надпись ВНЕ таблицы
            if (maxReached) {
                out.println("<p style=\"color:red;\"><b>Дальнейшее увеличение размера текста невозможно!</b></p>");
            }

            // П.6 = 0: таблица БЕЗ видимых границ, ОДИН столбец, строки сверху вниз
            out.println("<h" + Servlet1.cycle + ">");
            out.println("<table>");

            out.println("<tr><td>Текущий уровень заголовка: h" + Servlet1.cycle + "</td></tr>");

            if (!sortError.isEmpty()) {
                out.println("<tr><td>" + sortError + "</td></tr>");
            } else {
                out.println("<tr><td>Исходная последовательность: " + originalResult + "</td></tr>");
                out.println("<tr><td>Отсортировано по возрастанию: " + sortedResult + "</td></tr>");
            }

            out.println("</table>");
            out.println("</h" + Servlet1.cycle + ">");

            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "ЛР7 Вариант 56";
    }
}