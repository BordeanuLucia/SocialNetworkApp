package utils.events;


public class MessageTaskChangeEvent implements Event {
    private ChangeEventEntity eventEntity;
    private ChangeEventType eventType;

    public MessageTaskChangeEvent(ChangeEventEntity entity, ChangeEventType type) {
        this.eventEntity = entity;
        this.eventType = type;
    }

    public ChangeEventEntity getEventEntity() { return eventEntity; }

    public ChangeEventType getEventType() { return eventType; }

}