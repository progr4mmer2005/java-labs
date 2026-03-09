class ElementsHasForbiddenNumberException extends Exception {
    private final int num;
    public ElementsHasForbiddenNumberException(int num) {
        super("Запрещённое число: " + num);
        this.num = num;
    }
    @Override public String toString() { return getMessage(); }
}

class ForbiddenElementsQuantityException extends Exception {
    private final int quantity;
    public ForbiddenElementsQuantityException(int q) {
        super("Запрещённое количество элементов: " + q);
        this.quantity = q;
    }
    public int getForbiddenElementsQuantity() { return quantity; }
    @Override public String toString() { return getMessage(); }
}

class StringContainsForbiddenCharacterException extends Exception {
    private final char ch;
    public StringContainsForbiddenCharacterException(char ch) {
        super("Строка содержит запрещённый символ: " + ch);
        this.ch = ch;
    }
    public char getForbiddenCharacter() { return ch; }
    @Override public String toString() { return getMessage(); }
}