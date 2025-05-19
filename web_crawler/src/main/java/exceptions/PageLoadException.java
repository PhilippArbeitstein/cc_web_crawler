package exceptions;

public class PageLoadException extends RuntimeException {
    public PageLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}