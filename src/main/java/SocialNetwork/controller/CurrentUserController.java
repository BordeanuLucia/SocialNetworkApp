package SocialNetwork.controller;

import SocialNetwork.domain.*;
import SocialNetwork.domain.validators.ValidationException;
import SocialNetwork.repository.RepoException;
import SocialNetwork.service.MessageService;
import SocialNetwork.service.PrietenieService;
import SocialNetwork.service.ServiceException;
import SocialNetwork.service.UtilizatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CurrentUserController implements Observer<MessageTaskChangeEvent> {
    @FXML
    TableView<FriendshipDTO> tableView1;
    @FXML
    TableColumn<FriendshipDTO, Button> tableColumnButton1;
    @FXML
    TableColumn<FriendshipDTO, String> tableColumnDate1;
    @FXML
    TableColumn<FriendshipDTO,String> tableColumnUsername1;
    @FXML
    CheckBox checkBox1;
    @FXML
    TextField textFieldUsername;
    @FXML
    TextField textFieldUserMessage;
    @FXML
    TableView<Utilizator> tableView4;
    @FXML
    TableColumn<Utilizator,String> tableColumnUsername4;
    @FXML
    ListView<MessageDTO> listView;
    @FXML
    TextField textFieldMessage;


    private UtilizatorService userService;
    private PrietenieService friendshipService;
    private MessageService messageService;
    Stage dialogStage;
    Stage previousStage;
    Utilizator currentUser;
    ObservableList<FriendshipDTO> model1 = FXCollections.observableArrayList();
    ObservableList<Utilizator> model4 = FXCollections.observableArrayList();
    ObservableList<MessageDTO> model5 = FXCollections.observableArrayList();

    public void setService(UtilizatorService userService, PrietenieService friendshipService, MessageService messageService, Stage stage, Utilizator u, Stage stage1) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        friendshipService.addObserver(this);
        userService.addObserver(this);
        messageService.addObserver(this);
        this.previousStage = stage1;
        this.dialogStage=stage;
        this.currentUser=u;
        initModel1();
        initModel4();
    }

    @FXML
    private void initialize() {
        tableColumnDate1.setCellValueFactory(new PropertyValueFactory<FriendshipDTO,String>("date"));
        tableColumnUsername1.setCellValueFactory(new PropertyValueFactory<FriendshipDTO,String>("username"));
        tableColumnButton1.setCellValueFactory(new PropertyValueFactory<FriendshipDTO,Button>("button"));
        tableView1.setItems(model1);
        tableColumnUsername4.setCellValueFactory(new PropertyValueFactory<Utilizator,String>("username"));
        tableView4.setItems(model4);
        listView.setItems(model5);
        textFieldUsername.textProperty().addListener(x->handleFilter1());
        textFieldUserMessage.textProperty().addListener(x->handleFilter2());
    }

    List<FriendshipDTO> friendshipDTOList = new ArrayList<>();
    private void initModel1() {
        List<FriendshipDTO> list = StreamSupport.stream(userService.getAll().spliterator(), false)
                .map(x->{
                    FriendRequest friendRequest1 = friendshipService.findRequest(currentUser.getId(), x.getId());
                    FriendRequest friendRequest2 = friendshipService.findRequest(x.getId(), currentUser.getId());
                    if((friendRequest1 != null && friendRequest1.getStatus().equals(Status.APPROVED)) || (friendRequest2 != null && friendRequest2.getStatus().equals(Status.APPROVED)))
                        return new FriendshipDTO(x, friendshipService.findFriendship(currentUser.getId(), x.getId()).getDate(), FriendshipType.FRIENDS, friendshipService);
                    if(friendRequest1 != null && friendRequest1.getStatus().equals(Status.PENDING))
                        return new FriendshipDTO(x,null, FriendshipType.REQUEST_SEND, friendshipService);
                    if(friendRequest2 != null && friendRequest2.getStatus().equals(Status.PENDING))
                        return new FriendshipDTO(x,null, FriendshipType.REQUEST_RECEIVED, friendshipService);
                    return new FriendshipDTO(x,null, FriendshipType.NOT_FRIENDS, friendshipService);
                }).collect(Collectors.toList());
        this.friendshipDTOList = list;
        model1.setAll(list);
    }

    @FXML
    public void handleOnlyFriends(){
        if(checkBox1.isSelected()){
            List<FriendshipDTO> list = this.friendshipDTOList.stream()
                    .filter(x->{if(x.getFriendshipType().equals(FriendshipType.FRIENDS)) return true; return false;})
                    .collect(Collectors.toList());
            model1.setAll(list);
        }
        else{
            model1.setAll(this.friendshipDTOList);
        }
    }

    private void initModel4(){
        List<Utilizator> users = StreamSupport.stream(userService.getAll().spliterator(),false).collect(Collectors.toList());
        model4.setAll(users);
    }

    private Utilizator selectedUserForConversation = null;
    @FXML
    private void showConversation(){
        Utilizator selectedUser = tableView4.getSelectionModel().getSelectedItem();
        if(selectedUser == null)
            selectedUser = selectedUserForConversation;
        else
            selectedUserForConversation = selectedUser;
        if(selectedUser != null){
            List<MessageDTO> messages = messageService.findAll().stream()
                    .filter(x->{if((x.getFrom().equals(currentUser) && x.getTo().contains(selectedUserForConversation)) || (x.getFrom().equals(selectedUserForConversation) && x.getTo().contains(currentUser))) return true; return false;})
                    .map(x->{ String reply = ""; if(x.getReply() != -1l) reply = messageService.findOne(x.getReply()).getMessage();
                        if(x.getFrom().equals(currentUser)) return new MessageDTO(x.getId(), currentUser,x.getMessage(),reply);
                        else return new MessageDTO(x.getId(), selectedUserForConversation, x.getMessage(), reply);
                    }).collect(Collectors.toList());
            model5.setAll(messages);
        }else {
//           MessageAlert.showErrorMessage(null, "A user must be selected");
        }
    }

    private void handleFilter1() {
        Predicate<FriendshipDTO> namePredicate = x -> x.getUsername().startsWith(textFieldUsername.getText());
        Predicate<FriendshipDTO> lastPredicate = x -> x.getUsername().contains(" " + textFieldUsername.getText());
        model1.setAll(this.friendshipDTOList.stream().filter(namePredicate.or(lastPredicate)).collect(Collectors.toList()));
    }

    private void handleFilter2(){
        Predicate<Utilizator> namePredicate = x->x.getUsername().startsWith(textFieldUserMessage.getText());
        Predicate<Utilizator> lastPredicate = x->x.getUsername().contains(" "+ textFieldUserMessage.getText());
        model4.setAll(StreamSupport.stream(userService.getAll().spliterator(),false).filter(namePredicate.or(lastPredicate)).collect(Collectors.toList()));
    }

    @FXML
    public void handleSendMessage(){
        String message = textFieldMessage.getText();
        MessageDTO selectedMessage = listView.getSelectionModel().getSelectedItem();
        if(!message.equals("")) {
            if (selectedUserForConversation != null) {
                if (selectedMessage != null) {
                    try {
                        messageService.addReply(selectedMessage.getId(), message);
                    } catch (ServiceException e) {
                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", e.toString());
                    }
                } else {
                    try {
                        messageService.addMessage(selectedUserForConversation.getId().toString(), message);
                    } catch (ServiceException e) {
                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", e.toString());
                    }
                }
            }
            textFieldMessage.clear();
        }
    }


    @FXML
    public void handleShowRequests(){
        showFriendRequestDialog(currentUser);
    }

    private void showFriendRequestDialog(Utilizator user){
        try{
            //setup
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/sceneBuilder/friendRequests.fxml"));
            AnchorPane root = (AnchorPane)loader.load();

            //stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Requests");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            FriendshipRequestController friendshipRequestController = loader.getController();
            friendshipRequestController.setService(userService, friendshipService, dialogStage, user);
            dialogStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowReports(){
        try{
            //setup
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/sceneBuilder/reports.fxml"));
            AnchorPane root = (AnchorPane)loader.load();

            //stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reports");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            ReportsController reportsController = loader.getController();
            reportsController.setService(userService, friendshipService, messageService, dialogStage, currentUser);
            dialogStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogOut(){
        dialogStage.close();
        previousStage.show();
    }


    @Override
    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
        initModel1();
        initModel4();
        showConversation();
    }
}
