package exceptions.account;

public class InvalidDepartmentException extends Exception {
    public InvalidDepartmentException(String message) {
        super(message);
    }
}