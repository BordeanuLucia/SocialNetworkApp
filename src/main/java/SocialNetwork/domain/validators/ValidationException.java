package SocialNetwork.domain.validators;


public class ValidationException extends RuntimeException {
    private String erori;

    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
        erori = message;
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString(){
        return erori;
    }
}
