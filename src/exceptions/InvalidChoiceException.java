package exceptions;

public class InvalidChoiceException extends RuntimeException {

    public InvalidChoiceException() {
        super("Invalid choice input");
    }

    public InvalidChoiceException(String message) {
        super(message);
    }
}
