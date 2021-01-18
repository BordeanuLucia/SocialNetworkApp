package domain;

public class Participant extends Entity<Tuple<Long,Long>>{
    private boolean getNotifications;
    private boolean sawNotifications;

    public Participant(Long event, Long user) {
        this.setId(new Tuple<>(event,user));
        getNotifications = true;
        sawNotifications = false;
    }

    public boolean isGetNotifications() {
        return getNotifications;
    }

    public boolean isSawNotifications() {
        return sawNotifications;
    }

    public void setGetNotifications(boolean getNotifications) {
        this.getNotifications = getNotifications;
    }

    public void setSawNotifications(boolean sawNotifications) {
        this.sawNotifications = sawNotifications;
    }
}
