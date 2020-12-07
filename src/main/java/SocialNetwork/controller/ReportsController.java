package SocialNetwork.controller;

import SocialNetwork.domain.FriendshipDTO;
import SocialNetwork.domain.Utilizator;
import SocialNetwork.service.MessageService;
import SocialNetwork.service.PrietenieService;
import SocialNetwork.service.UtilizatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observer;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    }

    @FXML
    public void handleReport2(){

    }
    @Override
    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
        initModel();
    }
}
