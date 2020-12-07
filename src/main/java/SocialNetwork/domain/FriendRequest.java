package SocialNetwork.domain;

public class FriendRequest extends Prietenie{
    private Status status;
    private int number;

    public FriendRequest() {
        status = Status.PENDING;
        number = 1;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Status getStatus() {
        return status;
    }

    public int getNumber() {
        return number;
    }

}
