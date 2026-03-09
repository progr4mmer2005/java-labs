public interface Validator {
    void checkElementsQuantity(String[] args) throws ForbiddenElementsQuantityException;
    int parseAndValidate(String s) throws StringContainsForbiddenCharacterException, ElementsHasForbiddenNumberException;
}