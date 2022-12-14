package domain;

import utils.Constants;

import java.time.LocalDateTime;

public class RequestDTO {
    Utilizator user;
    LocalDateTime date;
    Status status;

    public RequestDTO(Utilizator user, LocalDateTime date, Status status) {
        this.user = user;
        this.date = date;
        this.status = status;
    }

    public Utilizator getUser() {
        return user;
    }

    public String getDate() {
        return date.format(Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR);
    }

    public Status getStatus() {
        return status;
    }
}
