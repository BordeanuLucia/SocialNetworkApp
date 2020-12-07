package SocialNetwork.domain;

public class MessageDTO {
    private Long id;
    private Utilizator user;
    private String text;
    private String reply;

    public Long getId() {
        return id;
    }

    public MessageDTO(Long id, Utilizator user, String text, String reply) {
        this.id = id;
        this.user = user;
        this.text = text;
        this.reply = reply;
    }

    public String getUser() {
        return user.toString();
    }

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
}
