package domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long>{
    private Long userFrom;
    private List<Long> usersTo;
    private String message;
    private LocalDateTime date;
    private Long replyMessage;

    public Message(Long userFrom, List<Long> usersTo, String message, Long replyMessage) {
        this.userFrom = userFrom;
        this.usersTo = usersTo;
        this.message = message;
        this.date = LocalDateTime.now();
        this.replyMessage = replyMessage;
    }

    public void addTo(Long userId){
        usersTo.add(userId);
    }

    public void setDate(LocalDateTime date) { this.date = date; }

    public Long getReply() { return replyMessage; }

    public Long getUserFrom() { return userFrom; }

    public List<Long> getUsersTo() { return usersTo; }

    public String getMessage() { return message; }

    public LocalDateTime getDate() { return date; }

    @Override
    public String toString(){ return "Message" + this.getId().toString(); }
}
