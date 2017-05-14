package compiler;

public class SemanticException extends Exception {

    public SemanticException() {
    }

    public SemanticException(String message) {
        super(message);
    }

    public SemanticException(Throwable cause) {
        super(cause);
    }

    public SemanticException(String message, Throwable cause) {
        super(message, cause);
    }

}
