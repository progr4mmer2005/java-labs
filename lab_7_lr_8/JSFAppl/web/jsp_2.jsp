<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.Arrays"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%-- П.6 = 1: заголовок «Два» --%>
        <title>Два</title>
    </head>
    <body>
        <jsp:useBean id="mybean" scope="session" class="jspappl.SortBean" />
        <%-- П.9 = 3: увеличиваем общий счётчик переходов --%>
        <% mybean.addCounter(); %>

        <h2>Главная страница</h2>

        <p>Общее число переходов по приложению:
            <jsp:getProperty name="mybean" property="totalCounter" />
        </p>

        <%-- Форма ввода чисел для сортировки --%>
        <form name="MainForm" action="jsp_3.jsp">
            <p>Введите числа через запятую (например: 5,3,1,4,2):</p>
            <input type="text" name="nums" size="40" value="5,3,1,4,2" />
            <br/><br/>
            <input type="submit" value="Сортировать" />
        </form>

        <br/>
        <form name="BackForm" action="jsp_1.jsp">
            <input type="submit" value="Назад на стартовую" />
        </form>
    </body>
</html>
