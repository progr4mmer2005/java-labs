public class ElementsHasForbiddenNumberException extends Exception {

    private final int forbiddenNumber;

    public ElementsHasForbiddenNumberException(int forbiddenNumber) {
        this.forbiddenNumber = forbiddenNumber;
    }

    public int getForbiddenNumber() {
        return forbiddenNumber;
    }

    @Override
    public String toString() {
        return "Запрещённое число: " + forbiddenNumber;
    }
}