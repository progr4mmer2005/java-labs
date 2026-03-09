public class StringContainsForbiddenCharacterException extends Exception {

    private final char forbiddenCharacter;

    public StringContainsForbiddenCharacterException(char ch) {
        this.forbiddenCharacter = ch;
    }

    public char getForbiddenCharacter() {
        return forbiddenCharacter;
    }

    @Override
    public String toString() {
        return "Строка содержит запрещённый символ: " + forbiddenCharacter;
    }
}