public class ForbiddenElementsQuantityException extends Exception {

    private final int forbiddenElementsQuantity;

    public ForbiddenElementsQuantityException(int quantity) {
        this.forbiddenElementsQuantity = quantity;
    }

    public int getForbiddenElementsQuantity() {
        return forbiddenElementsQuantity;
    }

    @Override
    public String toString() {
        return "Запрещённое количество элементов: " + forbiddenElementsQuantity;
    }
}