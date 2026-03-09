import java.util.*;

public class NumberProcessor implements Constants, Validator {
    private ResultListener resultListener;
    private ArraySizeListener sizeListener;
    private Logger logger;

    public void setListeners(ResultListener rl, ArraySizeListener sl, Logger log) {
        this.resultListener = rl;
        this.sizeListener = sl;
        this.logger = log;
    }

    @Override
    public void checkElementsQuantity(String[] args) throws ForbiddenElementsQuantityException {
        if (args.length == FORBIDDEN_ELEMENTS_QUANTITY) {
            throw new ForbiddenElementsQuantityException(FORBIDDEN_ELEMENTS_QUANTITY);
        }
    }

    @Override
    public int parseAndValidate(String s) throws StringContainsForbiddenCharacterException, ElementsHasForbiddenNumberException {
        if (s.indexOf(FORBIDDEN_CHAR) != -1) throw new StringContainsForbiddenCharacterException(FORBIDDEN_CHAR);
        int num = Integer.parseInt(s.trim());
        if (num == FORBIDDEN_NUMBER) throw new ElementsHasForbiddenNumberException(num);
        return num;
    }

    public List<Integer> process(String[] args) {
        
        if (sizeListener != null) {
            sizeListener.onSizeMatched(FORBIDDEN_ELEMENTS_QUANTITY, args.length);
        }

        try {
            checkElementsQuantity(args);
        } catch (ForbiddenElementsQuantityException e) {
            logger.log("ОШИБКА! " + e.getMessage());
        }

        List<Integer> result = new ArrayList<>();
        for (String s : args) {
            try {
                result.add(parseAndValidate(s));
            } catch (NumberFormatException e) {
                logger.log("ОШИБКА! '" + s + "' НЕ ЧИСЛО");
            } catch (Exception e) {
                logger.log("ОШИБКА! " + e.getMessage());
            }
        }

        Collections.sort(result);

        if (resultListener != null) {
            resultListener.onResultObtained(result);
        }
        return result;
    }
}