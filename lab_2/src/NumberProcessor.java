import java.util.*;

public class NumberProcessor {
    private ResultListener resultListener;

    public void setResultListener(ResultListener rl) {
        this.resultListener = rl;
    }

    public List<Integer> process(String[] data) {
        List<Integer> result = new ArrayList<>();
        for (String s : data) {
            try {
                result.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {}
        }
        Collections.sort(result);

        if (resultListener != null) {
            resultListener.onResultObtained(result);
        }
        return result;
    }
}