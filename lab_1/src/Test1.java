import java.util.ArrayList;



public class Test1 {
    public static void main(String[] args) {
        ArrayList al1 = new ArrayList();//создане списка для нечётных чисел
        ArrayList al2 = new ArrayList();//создане списка для чётных чисел
        for (String x : args) {//Вывод всех заданных параметров командной строки

            if (Integer.parseInt(x) % 2 == 0) {
                al2.add(x);//добавление в список чётного
            } else {
                al1.add(x);//добавление в список нечётного
            }
        }
        System.out.println(al1);//вывод "нечётного" списка
        System.out.println(al2);//вывод "чётного" списка

    }
}


