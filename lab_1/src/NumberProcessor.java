import java.util.ArrayList;
import java.util.Collections;

public class NumberProcessor implements Constants, Validator {

    @Override
    public void checkElementsQuantity(String[] args)
            throws ForbiddenElementsQuantityException {

        if (args.length == FORBIDDEN_ELEMENTS_QUANTITY) {
            throw new ForbiddenElementsQuantityException(FORBIDDEN_ELEMENTS_QUANTITY);
        }
    }

    @Override
    public int parseAndValidate(String s)
            throws StringContainsForbiddenCharacterException,
            ElementsHasForbiddenNumberException {

        if (s.indexOf(FORBIDDEN_CHAR) != -1) {
            throw new StringContainsForbiddenCharacterException(FORBIDDEN_CHAR);
        }

        int num = Integer.parseInt(s);

        if (num == FORBIDDEN_NUMBER) {
            throw new ElementsHasForbiddenNumberException(num);
        }

        return num;
    }

    public ArrayList<Integer> process(String[] args) {

        ArrayList<Integer> result = new ArrayList<>();

        try {
            checkElementsQuantity(args);
        } catch (ForbiddenElementsQuantityException e) {
            System.out.println("ОШИБКА! КОЛИЧЕСТВО ЭЛЕМЕНТОВ РАВНО "
                    + e.getForbiddenElementsQuantity());
        }

        for (String s : args) {

            try {
                int value = parseAndValidate(s);
                result.add(value);

            } catch (NumberFormatException e) {
                System.out.println("ОШИБКА! '" + s + "' НЕ ЧИСЛО");

            } catch (StringContainsForbiddenCharacterException e) {
                System.out.println("ОШИБКА! СТРОКА СОДЕРЖИТ ЗАПРЕЩЁННЫЙ СИМВОЛ '"
                        + e.getForbiddenCharacter() + "'");

            } catch (ElementsHasForbiddenNumberException e) {
                System.out.println("ОШИБКА! ЧИСЛО РАВНО ЗАПРЕЩЁННОМУ: "
                        + e.getForbiddenNumber());
            }
        }

        Collections.sort(result);
        return result;
    }
}