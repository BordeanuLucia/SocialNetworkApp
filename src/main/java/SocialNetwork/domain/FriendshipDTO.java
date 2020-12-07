package SocialNetwork.domain;


import SocialNetwork.utils.Constants;

import java.time.LocalDateTime;

public class FriendshipDTO {
    Utilizator user;
    private String username;
    private String date;

    public FriendshipDTO(Utilizator u1, LocalDateTime date) {
        this.user = u1;
        this.username = u1.toString();
        this.date = date.format(Constants.DATE_TIME_FORMATTER);
    }

    public Utilizator getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }
}
