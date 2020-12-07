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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CurrentUserController implements Observer<MessageTaskChangeEvent> {
    @FXML
    TableView<FriendshipDTO> tableView1;
    @FXML
    TableColumn<FriendshipDTO, String> tableColumnDate1;
    @FXML
    TableColumn<FriendshipDTO,String> tableColumnUsername1;
    @FXML
    TableView<Utilizator> tableView2;
    @FXML
    TableColumn<Utilizator,String> tableColumnUsername2;
    @FXML
    TableView<FriendshipDTO> tableView3;
    @FXML
    TableColumn<FriendshipDTO,String> tableColumnUsername3;
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
    ObservableList<Utilizator> model2 = FXCollections.observableArrayList();
    ObservableList<FriendshipDTO> model3 = FXCollections.observableArrayList();
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
        initModel2();
        initModel3();
        initModel4();
    }

    @FXML
    private void initialize() {
        tableColumnDate1.setCellValueFactory(new PropertyValueFactory<FriendshipDTO,String>("date"));
        tableColumnUsername1.setCellValueFactory(new PropertyValueFactory<FriendshipDTO,String>("username"));
        tableView1.setItems(model1);
        tableColumnUsername2.setCellValueFactory(new PropertyValueFactory<Utilizator,String>("username"));
        tableView2.setItems(model2);
        tableColumnUsername3.setCellValueFactory(new PropertyValueFactory<FriendshipDTO,String>("username"));
        tableView3.setItems(model3);
        tableColumnUsername4.setCellValueFactory(new PropertyValueFactory<Utilizator,String>("username"));
        tableView4.setItems(model4);
        listView.setItems(model5);
        textFieldUsername.textProperty().addListener(x->handleFilter1());
        textFieldUserMessage.textProperty().addListener(x->handleFilter2());
    }

    private void initModel1() {
        Iterable<Prietenie> friendships = friendshipService.getAllFriendships();
        List<FriendshipDTO> friendsList = StreamSupport.stream(friendships.spliterator(),false)
                .filter(x->{if(x.getId().getLeft()== currentUser.getId() || x.getId().getRight()== currentUser.getId()) return true; return false;})
                .map(x->{if(x.getId().getLeft()== currentUser.getId())
                            return new FriendshipDTO(userService.findOne(x.getId().getRight()),x.getDate());
                         return new FriendshipDTO(userService.findOne(x.getId().getLeft()), x.getDate());
                })
                .collect(Collectors.toList());
        model1.setAll(friendsList);
    }

    private void initModel2() {
        Iterable<FriendRequest> friendships = friendshipService.getAllRequests();
        List<FriendshipDTO> friendsList = StreamSupport.stream(friendships.spliterator(),false)
                .filter(x->{if((x.getId().getLeft()== currentUser.getId() || x.getId().getRight()== currentUser.getId())&& !x.getStatus().equals(Status.REJECTED)) return true; return false;})
                .map(x->{if(x.getId().getLeft()== currentUser.getId())
                    return new FriendshipDTO(userService.findOne(x.getId().getRight()),x.getDate());
                    return new FriendshipDTO(userService.findOne(x.getId().getLeft()), x.getDate());
                })
                .collect(Collectors.toList());
        Iterable<Utilizator> u = userService.getAll();
        List<Utilizator> users = StreamSupport.stream(u.spliterator(),false).collect(Collectors.toList());
        List<Utilizator> us = new ArrayList<>();
        for(Utilizator ut : users){
            Boolean gasit = false;
            for(FriendshipDTO friendshipDTO : friendsList)
                if(friendshipDTO.getUser().getUsername().equals(ut.getUsername()))
                    gasit = true;
                if(!gasit)
                    us.add(ut);
        }
        model2.setAll(us);
    }

    private void initModel3(){
        List<FriendshipDTO> sentRequests = StreamSupport.stream(friendshipService.getAllRequests().spliterator(),false)
                .filter(x->{if(x.getStatus().equals(Status.PENDING) && x.getId().getLeft() == currentUser.getId()) return true; return false;})
                .map(x->{return new FriendshipDTO(userService.findOne(x.getId().getRight()),x.getDate());})
                .collect(Collectors.toList());
        model3.setAll(sentRequests);
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
        if (textFieldUsername.getText().equals(""))
            initModel2();
        else {
            Predicate<Utilizator> namePredicate = x -> x.getUsername().startsWith(textFieldUsername.getText());
            Predicate<Utilizator> lastPredicate = x -> x.getUsername().contains(" " + textFieldUsername.getText());
            model2.setAll(StreamSupport.stream(userService.getAll().spliterator(), false).filter(namePredicate.or(lastPredicate)).collect(Collectors.toList()));
        }
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
        message.replace(" ","");
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
        }
    }

    @FXML
    private void handleUnsendRequest(){
        FriendshipDTO dto = tableView3.getSelectionModel().getSelectedItem();
        if(dto != null){
            friendshipService.deleteRequest(dto.getUser());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Unsend","Request unsent");
        }else {
            MessageAlert.showErrorMessage(null, "A user must be selected");
        }
    }

    @FXML
    public void handleRemoveFriend(){
        FriendshipDTO dto = tableView1.getSelectionModel().getSelectedItem();
        if(dto != null){
            Utilizator selectedUser = dto.getUser();
            friendshipService.deleteFriendship(selectedUser.getId());
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Delete","Friend removed");
        }else {
            MessageAlert.showErrorMessage(null, "A user must be selected");
        }
    }

    @FXML
    public void handleAddFriend(){
        Utilizator selectedUser = tableView2.getSelectionModel().getSelectedItem();
        if(selectedUser != null) {
            try {
                friendshipService.addFriendship(selectedUser.getId());
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add Friend", "Request sent");
            }catch(ServiceException | RepoException | ValidationException e){
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add Friend", e.toString());
            }
        }else {
            MessageAlert.showErrorMessage(null, "A user to befriend with must be selected");
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
    public void handleLogOut(){
        dialogStage.close();
        previousStage.show();
    }


    @Override
    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
        initModel1();
        initModel2();
        initModel3();
        initModel4();
        showConversation();
    }
}
