package exceptions;

/**
 * Thrown when a user attempts to book a facility slot (Study Room, Fridge, or Laundry)
 * that is already occupied or otherwise unavailable.
 */
public class SlotUnavailableException extends Exception {

    public SlotUnavailableException() {
        super("The requested slot is currently unavailable.");
    }

    public SlotUnavailableException(String message) {
        super(message);
    }
}