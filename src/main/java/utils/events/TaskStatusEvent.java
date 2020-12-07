package utils.events;


import SocialNetwork.domain.Utilizator;

public class TaskStatusEvent implements Event {
    private TaskExecutionStatusEventType type;
    private Utilizator task;
    public TaskStatusEvent(TaskExecutionStatusEventType type, Utilizator task) {
        this.task=task;
        this.type=type;
    }

    public Utilizator getTask() {
        return task;
    }

    public void setTask(Utilizator task) {
        this.task = task;
    }

    public TaskExecutionStatusEventType getType() {
        return type;
    }

    public void setType(TaskExecutionStatusEventType type) {
        this.type = type;
    }
}
