import java.util.ArrayList;

public class Task2 {

    public static void main(String[] args) {

        NumberProcessor processor = new NumberProcessor();

        ArrayList<Integer> result = processor.process(args);

        System.out.println("Результат:");
        for (Integer i : result) {
            System.out.println(i);
        }
    }
}