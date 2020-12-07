package SocialNetwork.domain;

public class MessageDTO {
    private Utilizator user;
    private String text;
    private String reply;

    public MessageDTO(Utilizator user, String text, String reply) {
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
