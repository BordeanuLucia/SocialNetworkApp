package domain;

import service.EventService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

public class EvenimentDTO {
    String eventText = "";
    Button subscribeButton;
    Eveniment eveniment;
    EventService eventService;

    public String getEventText() {
        return eventText;
    }

    public Button getSubscribeButton() {
        return subscribeButton;
    }

    public EvenimentDTO(Eveniment eveniment, Boolean subscribed, EventService eventService) {
        this.eveniment = eveniment;
        this.eventService = eventService;
        if(subscribed) {
            subscribeButton = new Button("Unsubscribe");
            subscribeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    eventService.removeParticipant(eveniment.getId());
                }
            });
        }else{
            subscribeButton = new Button("Subscribe");
            subscribeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    eventService.addParticipant(eveniment.getId());
                }
            });
        }
        eventText = "Event: " + eveniment.getTitlu() + "\nWill take place in " + eveniment.getDate().toString();
    }

}
