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

    public void setUser(Utilizator user) {
        this.user = user;
    }

    public void setFriends(List<Prietenie> friends) {
        this.friends = friends;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setFriendRequests(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public Utilizator getUser() {
        return user;
    }

    public List<Prietenie> getFriends() {
        return friends;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<FriendRequest> getFriendRequests() {
        return friendRequests;
    }
}
