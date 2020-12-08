package SocialNetwork.domain;

import java.util.List;

public class Page {
    private Utilizator user;
    private List<Prietenie> friends;
    private List<Message> messages;
    private List<FriendRequest> friendRequests;

    public Page(Utilizator user, List<Prietenie> friends, List<Message> messages, List<FriendRequest> friendRequests) {
        this.user = user;
        this.friends = friends;
        this.messages = messages;
        this.friendRequests = friendRequests;
    }
}
