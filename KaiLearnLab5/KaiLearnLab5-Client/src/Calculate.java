import java.io.Serializable;
import java.util.ArrayList;
import some.Task;

public class Calculate implements Task<String>, Serializable {
    private ArrayList<Integer> list;

    public Calculate(ArrayList<Integer> list) {
        this.list = list;
    }

    @Override
    public String execute() {
        long sumEvenPositive = 0;
        long sumOddPositive = 0;

        for (Integer num : list) {
            if (num > 0) {
                if (num % 2 == 0) {
                    sumEvenPositive += num;
                } else {
                    sumOddPositive += num;
                }
            }
        }

        return sumEvenPositive + ":" + sumOddPositive;
    }
}