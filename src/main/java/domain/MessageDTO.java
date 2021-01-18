package domain;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private Utilizator user;
    private String text;
    private String reply;
    private LocalDateTime date;

    public Long getId() {
        return id;
    }

    public MessageDTO(Long idMessage, Utilizator userTo, String text, String reply, LocalDateTime date) {
        this.id = idMessage;
        this.user = userTo;
        this.text = text;
        this.reply = reply;
        this.date = date;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getUser() {
        return user.toString();
    }

    public Utilizator getUserEntity(){return user;}

    public String getText() {
        return text;
    }

    public String getReply() {
        return reply;
    }

    @Override
    public String toString() {
        if (reply.equals(""))
            return "" + getUser() + ": " + text;
        return "" + getUser() + ": " + text + " reply to: " + reply;
    }

    public String getFullMessage(){
        if (reply.equals(""))
            return text;
        return text + "\nReply to: " + reply;
    }
}
