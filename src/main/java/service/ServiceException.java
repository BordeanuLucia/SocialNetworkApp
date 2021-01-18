package service;

public class ServiceException extends RuntimeException{
    private String erori;

    public ServiceException() { }

    /**
     * creates an exception based on a message
     * @param message - string
     */
    public ServiceException(String message) {
        super(message);
        erori = message;
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString(){
        return erori;
    }
}
