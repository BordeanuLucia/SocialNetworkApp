package utils.events;

import SocialNetwork.domain.Utilizator;

public class MessageTaskChangeEvent implements Event {
    private ChangeEventType type;
    private Utilizator data, oldData;

    public MessageTaskChangeEvent(ChangeEventType type, Utilizator data) {
        this.type = type;
        this.data = data;
    }

    public MessageTaskChangeEvent(ChangeEventType type, Utilizator data, Utilizator oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public Utilizator getData() {
        return data;
    }

    public Utilizator getOldData() {
        return oldData;
    }
}