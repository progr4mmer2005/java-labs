<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.Arrays"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%-- П.6 = 1: заголовок «Три» --%>
        <title>Три</title>
    </head>
    <body>
        <jsp:useBean id="mybean" scope="session" class="jspappl.SortBean" />
        <%-- П.9 = 3: увеличиваем общий счётчик переходов --%>
        <% mybean.addCounter(); %>

        <h2>Финишная страница</h2>

        <p>Общее число переходов по приложению:
            <jsp:getProperty name="mybean" property="totalCounter" />
        </p>

        <%-- П.5 = 0: код функции сортировки размещён ЗДЕСЬ на странице (не в Bean) --%>
        <%
            String nums = request.getParameter("nums");
            String originalStr = "";
            String sortedStr   = "";
            String errorStr    = "";

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
                        sbOrig.append(arr[i] == (long)arr[i] ? String.valueOf((long)arr[i]) : arr[i]);
                        sbSorted.append(sorted[i] == (long)sorted[i] ? String.valueOf((long)sorted[i]) : sorted[i]);
                    }
                    originalStr = sbOrig.toString();
                    sortedStr   = sbSorted.toString();
                } catch (NumberFormatException e) {
                    errorStr = "Ошибка: введите числа через запятую";
                }
            } else {
                errorStr = "Числа не переданы";
            }
        %>

        <%-- П.8 = 1: результаты в ВИДИМОЙ таблице с рамкой --%>
        <% if (!errorStr.isEmpty()) { %>
            <p style="color:red;"><%= errorStr %></p>
        <% } else { %>
            <table border="1" cellpadding="6" cellspacing="0">
                <tr>
                    <th>Исходная последовательность</th>
                    <th>Отсортировано по возрастанию</th>
                </tr>
                <tr>
                    <td><%= originalStr %></td>
                    <td><%= sortedStr %></td>
                </tr>
            </table>
        <% } %>

        <br/>
        <form name="BackForm" action="jsp_2.jsp">
            <input type="submit" value="Возврат на главную" />
        </form>
    </body>
</html>
