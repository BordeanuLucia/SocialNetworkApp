package SocialNetwork.domain;

import utils.Constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Message extends Entity<Long>{
    private Utilizator from;
    private List<Utilizator> to;
    private String message;
    private LocalDateTime date;
    private Long reply;

    public Message(Utilizator from, List<Utilizator> to, String message, Long reply) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = LocalDateTime.now();
        this.reply = reply;
    }

    public void addTo(Utilizator user){
        to.add(user);
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getReply() { return reply; }

    public Utilizator getFrom() {
        return from;
    }

    public List<Utilizator> getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString(){
        String string = "";
        string+=from.getFirstName() + " " + from.getLastName() + " | ";
        for(Utilizator user : to)
            string+=user.getFirstName() + " " + user.getLastName() + " | ";
        string+=message + " | " + date.format(Constants.DATE_TIME_FORMATTER);
        return string;
    }
}
