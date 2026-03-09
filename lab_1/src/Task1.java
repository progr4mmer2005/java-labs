import java.util.ArrayList;
import java.util.Collections;


public class Task1 {


    public static void main(String[] args) {

        ArrayList<Integer> result = new ArrayList<>();

        for (String s : args){
            result.add(Integer.parseInt(s));
        }

        result.sort(Collections.reverseOrder());

        result.forEach(System.out::println);
    }
}

