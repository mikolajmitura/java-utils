package pl.jalokim.utils.terminal;

/**
 * Exception during invoke Os command.
 */
public class CommandException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message) {
        super(message);
    }
}
