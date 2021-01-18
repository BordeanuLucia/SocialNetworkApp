package domain;

public class FriendRequest extends Prietenie{
    private Status status;
    private int noOfRequestsSent;

    public FriendRequest() {
        status = Status.PENDING;
        noOfRequestsSent = 1;
    }

    public void setStatus(Status status) { this.status = status; }

    public void setNoOfRequestsSent(int noOfRequestsSent) { this.noOfRequestsSent = noOfRequestsSent; }

    public Status getStatus() { return status; }

    public int getNoOfRequestsSent() { return noOfRequestsSent; }
}
