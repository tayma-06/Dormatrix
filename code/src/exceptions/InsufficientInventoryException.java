package exceptions;

public class InsufficientInventoryException extends Exception {
    public InsufficientInventoryException(String msg) {
        super(msg);
    }
}
