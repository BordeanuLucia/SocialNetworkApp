package repository;

public class RepoException extends RuntimeException{
    private String erori;

    public RepoException() { }

    /**
     * creates an RepoException based on a string
     * @param message - string
     */
    public RepoException(String message) {
        super(message);
        erori = message;
    }

    public RepoException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepoException(Throwable cause) {
        super(cause);
    }

    public RepoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String toString(){
        return erori;
    }
}
