package SocialNetwork.controller;

import SocialNetwork.domain.FriendshipDTO;
import SocialNetwork.domain.Message;
import SocialNetwork.domain.Prietenie;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.service.MessageService;
import SocialNetwork.service.PrietenieService;
import SocialNetwork.service.UtilizatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import utils.Constants;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observer;

import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.List;

public class ReportsController implements Observer<MessageTaskChangeEvent> {
    Stage dialogStage;
    Utilizator currentUser;
    UtilizatorService userService;
    PrietenieService friendshipService;
    MessageService messageService;
    @FXML
    DatePicker datePicker1;
    @FXML
    DatePicker datePicker2;
    @FXML
    DatePicker datePicker3;
    @FXML
    DatePicker datePicker4;
    @FXML
    ComboBox<Utilizator> comboBox;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    public void setService(UtilizatorService utilizatorService, PrietenieService prietenieService, MessageService messageService, Stage stage, Utilizator user){
        this.dialogStage = stage;
        this.currentUser = user;
        this.userService = utilizatorService;
        this.friendshipService = prietenieService;
        this.messageService = messageService;
        userService.addObserver(this);
        initModel();
    }

    @FXML
    private void initialize(){
        comboBox.setItems(model);
    }

    private void initModel(){
        model.setAll(StreamSupport.stream(userService.getAll().spliterator(),false).collect(Collectors.toList()));
    }

    @FXML
    public void handleReport1(){
        String text = "";
        LocalDate localDate1 = datePicker1.getValue();
        LocalDate localDate2 = datePicker2.getValue();
        if(localDate1 != null && localDate2 != null) {
            if(localDate1.isBefore(localDate2) || localDate1.equals(localDate2)) {
                List<FriendshipDTO> prietenii = friendshipService.friendsBetweenDates(localDate1, localDate2)
                        .stream().map(x -> {
                            if (x.getId().getRight() == currentUser.getId())
                                return new FriendshipDTO(userService.findOne(x.getId().getLeft()), x.getDate());
                            else return new FriendshipDTO(userService.findOne(x.getId().getRight()), x.getDate());
                        })
                        .collect(Collectors.toList());
                if (prietenii.size() != 0) {
                    text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                            .concat(" ").concat(currentUser.toString()).concat(" s-a imprietenit cu:\n");
                    for (FriendshipDTO friendshipDTO : prietenii) {
                        text.concat(friendshipDTO.getDate()).concat(" ").concat(friendshipDTO.getUsername()).concat("\n");
                    }
                } else {
                    text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                            .concat(" ").concat(currentUser.toString()).concat(" nu s-a imprietenit cu nicio persoana.\n");
                }
                List<Message> mesaje = messageService.messagesBetweenDates(localDate1, localDate2);
                if (mesaje.size() != 0) {
                    text.concat("\n").concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                            .concat(" ").concat(currentUser.toString()).concat(" a primit urmatoarele mesaje:\n");
                    for (Message message : mesaje) {
                        text.concat(message.getDate().format(Constants.DATE_TIME_FORMATTER)).concat(" ").concat(message.getFrom().toString()).concat(": ").concat(message.getMessage()).concat("\n");
                    }
                } else {
                    text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                            .concat(" ").concat(currentUser.toString()).concat(" nu a primit niciun mesaj.\n");
                }
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A type 1 pdf report was generated");
            }
            else{
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "Fierst date must be before second date");
            }
        }else{
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A date must be selected");
        }
    }

    @FXML
    public void handleReport2(){
        String text = "";
        LocalDate localDate1 = datePicker3.getValue();
        LocalDate localDate2 = datePicker4.getValue();
        Utilizator user = comboBox.getValue();
        if(localDate1 != null && localDate2 != null && user != null) {
            List<Message> mesaje = messageService.messagesBetweenDatesUser(localDate1,localDate2, user.getId());

            if(mesaje.size() != 0 ) {
                text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                        .concat(" ").concat(currentUser.toString()).concat(" a purtat urmatoarea discutie cu ").concat(user.toString()).concat(":\n");
                for (Message message : mesaje) {
                    if (message.getFrom().equals(currentUser)) {
                        text.concat(currentUser.toString()).concat(": ").concat(message.getMessage());
                        if (message.getReply() != -1l)
                            text.concat(" reply to: ").concat(messageService.findOne(message.getReply()).getMessage());
                        text.concat("\n");
                    } else {
                        text.concat(user.toString()).concat(": ").concat(message.getMessage());
                        if (message.getReply() != -1l)
                            text.concat(" reply to: ").concat(messageService.findOne(message.getReply()).getMessage());
                        text.concat("\n");
                    }
                }
            }
            else{
                text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
                        .concat(" ").concat(currentUser.toString()).concat(" nu a discutat nimic cu ").concat(user.toString()).concat(":\n");
            }
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A type 2 pdf report was generated");
        }
        else{
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A date and a user must be selected");
        }
    }
    @Override
    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
        initModel();
    }
}
