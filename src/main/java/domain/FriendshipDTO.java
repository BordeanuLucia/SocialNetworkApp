package domain;

import controller.MessageAlert;
import domain.validators.ValidationException;
import repository.RepoException;
import service.PrietenieService;
import service.ServiceException;
import utils.Constants;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.time.LocalDateTime;

public class FriendshipDTO {
    Utilizator user;
    private String username;
    private String date;
    private FriendshipType friendshipType;
    private Button button;
    private PrietenieService friendshipService;

    public FriendshipDTO(Utilizator u1, LocalDateTime date, FriendshipType friendshipType, PrietenieService friendshipService) {
        this.user = u1;
        this.username = u1.toString();
        if(date!= null)
            this.date = date.format(Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR);
        else
            this.date = "";
        this.friendshipType = friendshipType;
        this.friendshipService = friendshipService;
        this.createButton();
    }

    public Utilizator getUser() {
        return user;
    }

    public String getUsername() {
        return username;
    }

    public FriendshipType getFriendshipType() {
        return friendshipType;
    }

    public String getDate() {
        return date;
    }

    private void createButton(){
        button = new Button();
        if(friendshipType.equals(FriendshipType.FRIENDS)){
            button.setText("Unfriend");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    friendshipService.deleteFriendship(user.getId());
                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Delete","Friend removed");
                }
            });
        }
        if(friendshipType.equals(FriendshipType.NOT_FRIENDS)){
            button.setText("Befriend");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        friendshipService.addFriendshipRequest(user.getId());
                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add Friend", "Request sent");
                    }catch(ServiceException | RepoException | ValidationException e){
                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add Friend", e.toString());
                    }
                }
            });
        }
        if(friendshipType.equals(FriendshipType.REQUEST_RECEIVED)){
            button.setText("Add");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    friendshipService.respondFriendship(user.getId(), Status.APPROVED);
                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Respond", "Request approved");
                }
            });
        }
        if(friendshipType.equals(FriendshipType.REQUEST_SEND)){
            button.setText("Unsend");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    friendshipService.deleteRequest(user);
                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Unsend","Request unsent");
                }
            });
        }
    }

    public Button getButton(){
        return button;
    }
}
