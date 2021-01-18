package controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import domain.*;
import domain.validators.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import repository.RepoException;
import service.*;
import utils.MyBCrypt;
import utils.events.ChangeEventEntity;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static utils.Constants.DATE_TIME_FORMATTER_WITHOUT_HOUR;
import static utils.Constants.DATE_TIME_FORMATTER_WITH_HOUR;

public class CurrentUserController implements Observer<MessageTaskChangeEvent> {
    @FXML
    Text auxiliar1;
    @FXML
    Text auxiliar2;

    private UtilizatorService userService;
    private PrietenieService friendshipService;
    private MessageService messageService;
    private EventService eventService;
    Stage dialogStage;
    Stage previousStage;
    Utilizator currentUser;
//    ObservableList<Eveniment> model6 = FXCollections.observableArrayList();

    public void setService(UtilizatorService userService, PrietenieService friendshipService, MessageService messageService, EventService eventService, Stage stage, Utilizator u, Stage stage1) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.eventService = eventService;
        friendshipService.addObserver(this);
        userService.addObserver(this);
        messageService.addObserver(this);
        eventService.addObserver(this);
        this.previousStage = stage1;
        this.dialogStage=stage;
        this.currentUser=u;
//        initModelAllUsers();
        initTryAllUsers();
//        initModelAllEvents();
        initTryAllEvent();
        initModelUsersReport();
       // initNoOfNotifications();
        initModelNotifications();
        initMyProfile();
        initMyEvents();
        initMyFriends();
        allUsersPagination.setPageCount(userService.numberOfPagesWithUsers());
        eventsPagination.setPageCount(eventService.numberOfEvents());
        messagesPagination.setVisible(false);
        textUsername.setText(currentUser.getUsername().toUpperCase());

        eventService.deleteOutdatedEvents();

        try {
            FileInputStream input = new FileInputStream(currentUser.getUrl());
            Image picture = new Image(input);
            profilePicture.setImage(picture);
        } catch (IOException | SecurityException e) {
            System.out.println("EROARE");
            try {
                FileInputStream input = new FileInputStream("E:\\MAP\\SocialNetwork\\src\\main\\resources\\images\\defaultUser.jpg");
                Image picture = new Image(input);
                profilePicture.setImage(picture);
            } catch (IOException | SecurityException s) {
                System.out.println("EROARE");
            }
        }
    }

    @FXML
    private void initialize() {
        tryAllUsers.setItems(modelTryAllUsers);
        tryAllEvents.setItems(modelTryAllEvents);
        friendsListView.setItems(modelMyFriends);
        eventsListView.setItems(modelMyEvents);
        chat.setItems(modelTryMessages);
        notificationsListView.setItems(modelNotifications);
        textFieldSearchUsername.textProperty().addListener(x -> handleFilter1());
        searchFriends.textProperty().addListener(x-> handleFilterFriends());

        allUsersPagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                initTryAllUsers();
                return auxiliar1;
            }
        });

        messagesPagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                tryShowConversation();
                return auxiliar2;
            }
        });

        myFriendsPagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                initMyFriends();
                return auxiliar2;
            }
        });

        eventsPagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                if (!eventsCheckBox.isSelected()) {
                    initTryAllEvent();
                    return auxiliar1;
                } else {
                    initModelOnlyGoingEvents();
                    return auxiliar1;
                }
            }
        });

        comboBox.setItems(modelUsersReport);

        ObservableList<String> hours = FXCollections.observableArrayList("00:00", "01:00", "02:00", "03:00", "04:00",
                "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00",
                "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00");
        SpinnerValueFactory<String> valueFactory1 = new SpinnerValueFactory.ListSpinnerValueFactory<String>(hours);
        valueFactory1.setValue("00:00");
        SpinnerValueFactory<String> valueFactory2 = new SpinnerValueFactory.ListSpinnerValueFactory<String>(hours);
        valueFactory2.setValue("00:00");
        beginsSpinner.setValueFactory(valueFactory1);
        endsSpinner.setValueFactory(valueFactory2);
        eventDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate aux = LocalDate.now();
                setDisable(empty || date.compareTo(aux) < 0);
            }
        });
    }

    @Override
    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
        if(messageTaskChangeEvent.getEventEntity().equals(ChangeEventEntity.MESSAGE))
            tryShowConversation();
        if(messageTaskChangeEvent.getEventEntity().equals(ChangeEventEntity.FRIENDSHIP)){
            allUsersPagination.setPageCount(userService.numberOfPagesWithUsers());
            initMyFriends();
        }
        if(messageTaskChangeEvent.getEventEntity().equals(ChangeEventEntity.EVENT)) {
            if(eventsCheckBox.isSelected())
                initModelOnlyGoingEvents();
            else {
                initTryAllEvent();
            }
            initMyEvents();
        }
        if(messageTaskChangeEvent.getEventEntity().equals(ChangeEventEntity.PARTICIPANT)) {
            initModelNotifications();
            if(eventsCheckBox.isSelected())
                initModelOnlyGoingEvents();
            else {
                initTryAllEvent();
            }
            initMyEvents();
        }
        if(messageTaskChangeEvent.getEventEntity().equals(ChangeEventEntity.USER) ||
                messageTaskChangeEvent.getEventEntity().equals(ChangeEventEntity.REQUEST)) {
            allUsersPagination.setPageCount(userService.numberOfPagesWithUsers());
            if(userProfile != null)
                openUserProfile(userProfile);
      //      initTryAllUsers();
            initModelUsersReport();
        }
    }

    //TODO standart left pane -------------------------------------------------------------------------------------------------------------

    @FXML
    private ImageView profilePicture;
    @FXML
    Label textUsername;

    private void updateCurrentUser(){
        currentUser = userService.findOne(currentUser.getId());
        textUsername.setText(currentUser.getUsername().toUpperCase());
        try {
            FileInputStream input = new FileInputStream(currentUser.getUrl());
            Image picture = new Image(input);
            profilePicture.setImage(picture);
        } catch (IOException | SecurityException e) {
            System.out.println("EROARE");
            try {
                FileInputStream input = new FileInputStream("E:\\MAP\\SocialNetwork\\src\\main\\resources\\images\\defaultUser.jpg");
                Image picture = new Image(input);
                profilePicture.setImage(picture);
            } catch (IOException | SecurityException s) {
                System.out.println("EROARE");
            }
        }
    }

    @FXML
    public void handleDeleteAccount(){
        Stage areYouSureWindow = new Stage();

        areYouSureWindow.initModality(Modality.APPLICATION_MODAL);
        areYouSureWindow.setTitle("Confirmation");
        Label label1= new Label("Are you sure?");
        label1.setStyle("-fx-font-size: 23");
        Button button1= new Button("Yes");
        button1.setStyle("-fx-font-size: 20");
        button1.setOnAction(e -> {
            areYouSureWindow.close();
            friendshipService.deleteAllFriendships();
            messageService.deleteMessages();
            userService.deleteUtilizator();
            dialogStage.close();
            previousStage.show();
        });
        Button button2 = new Button("No");
        button2.setStyle("-fx-font-size: 20");
        button2.setOnAction(e -> areYouSureWindow.close());
        VBox layout= new VBox(20);
        HBox hbox = new HBox(50);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(button1,button2);
        layout.getChildren().addAll(label1, hbox);
        layout.setAlignment(Pos.CENTER);
        Scene scene1= new Scene(layout, 300, 150);
        areYouSureWindow.setScene(scene1);
        areYouSureWindow.showAndWait();
    }

    @FXML
    public void handleLogOut(){
        Stage areYouSureWindow = new Stage();

        areYouSureWindow.initModality(Modality.APPLICATION_MODAL);
        areYouSureWindow.setTitle("Confirmation");
        Label label1= new Label("Are you sure?");
        label1.setStyle("-fx-font-size: 23");
        Button button1= new Button("Yes");
        button1.setStyle("-fx-font-size: 20");
        button1.setOnAction(e -> {
            areYouSureWindow.close();
            dialogStage.close();
            previousStage.show();
        });
        Button button2 = new Button("No");
        button2.setStyle("-fx-font-size: 20");
        button2.setOnAction(e -> areYouSureWindow.close());
        VBox layout= new VBox(20);
        HBox hbox = new HBox(50);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(button1,button2);
        layout.getChildren().addAll(label1, hbox);
        layout.setAlignment(Pos.CENTER);
        Scene scene1= new Scene(layout, 300, 150);
        areYouSureWindow.setScene(scene1);
        areYouSureWindow.showAndWait();
    }

    //TODO HOME tab ----------------------------------------------------------------------------------------------------------------------

//    @FXML
//    TableView<FriendshipDTO> tableView1;
//    @FXML
//    TableColumn<FriendshipDTO, Button> tableColumnButton1;
//    @FXML
//    TableColumn<FriendshipDTO, String> tableColumnDate1;
//    @FXML
//    TableColumn<FriendshipDTO,String> tableColumnUsername1;
//    @FXML
//    CheckBox checkBoxOnlyFriends;
    @FXML
    TextField textFieldSearchUsername;
    @FXML
    Pagination allUsersPagination;
    @FXML
    Pagination messagesPagination;
    @FXML
    Label conversationUser;
    @FXML
    TextField textFieldMessage;

    ObservableList<Label> modelNotifications = FXCollections.observableArrayList();
    int noOfUnseenNotifications = 0;
    @FXML
    private AnchorPane notificationsPane;
    @FXML
    private Button notificationsButton;
    @FXML
    private ListView<Label> notificationsListView;
    @FXML
    private Text noOfNotificationsText;
    @FXML
    Circle notificationCircle;

    private void handleFilter1() {
        allUsersPagination.setCurrentPageIndex(0);
        List<HBox> list = new ArrayList<>();
        list = StreamSupport.stream(userService.getUsersOnPageByName(allUsersPagination.getCurrentPageIndex(),
                textFieldSearchUsername.getText().replaceAll("^[ \t]+|[ \t]+$", "")).spliterator(), false)
                .map(x->{
                    HBox hbox = new HBox();
                    hbox.setMaxWidth(230d);
                    hbox.setId(x.getId().toString());
                    Label label = new Label(x.getUsername());
                    label.setMaxWidth(170d);
                    label.setWrapText(true);
                    label.setFont(Font.font(16));
                    Pane vbox = new Pane();
                    Button profil = new Button("\uD83D\uDC64");
                    profil.setStyle("-fx-background-color: transparent");
                    profil.setCursor(Cursor.HAND);
                    profil.setMaxWidth(35d);
                    profil.setFont(new Font(12));
                    profil.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            selectedUserForConversation = userService.findOne(x.getId());
                            tryAllUsers.getSelectionModel().select(hbox);
                            tryShowConversation();
                            openUserProfile(x.getId());
                        }
                    });
                    Label viewProfile = new Label("View profile");
                    viewProfile.setStyle("-fx-font-size: 9");
                    viewProfile.setVisible(false);
                    profil.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            viewProfile.setVisible(true);
                        }
                    });
                    profil.setOnMouseExited(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            viewProfile.setVisible(false);
                        }
                    });
                    vbox.getChildren().add(viewProfile);
                    vbox.getChildren().add(profil);
                    viewProfile.setLayoutX(-5d);
                    viewProfile.setLayoutY(18d);
                    hbox.getChildren().add(vbox);
                    hbox.getChildren().add(label);
                    return hbox;
                }).collect(Collectors.toList());
        this.friendshipDTOList = list;
        modelTryAllUsers.setAll(list);
        //putem la on key pressed daca ii spatiu sa facem filtrarea
//        Predicate<FriendshipDTO> namePredicate = x -> x.getUsername().startsWith(textFieldSearchUsername.getText());
//        Predicate<FriendshipDTO> lastPredicate = x -> x.getUsername().contains(" " + textFieldSearchUsername.getText());
////        modelUsers.setAll(this.friendshipDTOList.stream().filter(namePredicate.or(lastPredicate)).collect(Collectors.toList()));
//        modelTryAllUsers.setAll(this.friendshipDTOList.stream().filter(namePredicate.or(lastPredicate)).collect(Collectors.toList()));
    }

    List<HBox> friendshipDTOList = new ArrayList<>();
//    private void initModelAllUsers() {
//        List<FriendshipDTO> list = StreamSupport.stream(userService.getUsersOnPage(allUsersPagination.getCurrentPageIndex()).spliterator(), false)
//                .map(x->{
//                    //TODO de aici merge greu paginarea --------------------------------------------------------------------------------------
////                    FriendRequest friendRequest1 = friendshipService.findRequest(currentUser.getId(), x.getId());
////                    FriendRequest friendRequest2 = friendshipService.findRequest(x.getId(), currentUser.getId());
////                    if((friendRequest1 != null && friendRequest1.getStatus().equals(Status.APPROVED)) || (friendRequest2 != null && friendRequest2.getStatus().equals(Status.APPROVED)))
////                        return new FriendshipDTO(x, friendshipService.findFriendship(currentUser.getId(), x.getId()).getDate(), FriendshipType.FRIENDS, friendshipService);
////                    if(friendRequest1 != null && friendRequest1.getStatus().equals(Status.PENDING))
////                        return new FriendshipDTO(x,null, FriendshipType.REQUEST_SEND, friendshipService);
////                    if(friendRequest2 != null && friendRequest2.getStatus().equals(Status.PENDING))
////                        return new FriendshipDTO(x,null, FriendshipType.REQUEST_RECEIVED, friendshipService);
//                    return new FriendshipDTO(x,null, FriendshipType.NOT_FRIENDS, friendshipService);
//                }).collect(Collectors.toList());
//        this.friendshipDTOList = list;
//        modelUsers.setAll(list);
//    }
    @FXML
    private ListView<HBox> tryAllUsers;
    ObservableList<HBox> modelTryAllUsers = FXCollections.observableArrayList();
    private void initTryAllUsers(){
        List<HBox> list = new ArrayList<>();
        list = StreamSupport.stream(userService.getUsersOnPage(allUsersPagination.getCurrentPageIndex()).spliterator(), false)
                .map(x->{
                    HBox hbox = new HBox();
                    hbox.setMaxWidth(230d);
                    hbox.setId(x.getId().toString());
                    Label label = new Label(x.getUsername());
                    label.setMaxWidth(170d);
                    label.setWrapText(true);
                    label.setFont(Font.font(16));
                    Pane vbox = new Pane();
                    Button profil = new Button("\uD83D\uDC64");
                    profil.setStyle("-fx-background-color: transparent");
                    profil.setCursor(Cursor.HAND);
                    profil.setMaxWidth(35d);
                    profil.setFont(new Font(12));
                    profil.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            selectedUserForConversation = userService.findOne(x.getId());
                            tryAllUsers.getSelectionModel().select(hbox);
                            tryShowConversation();
                            openUserProfile(x.getId());
                        }
                    });
                    Label viewProfile = new Label("View profile");
                    viewProfile.setStyle("-fx-font-size: 9");
                    viewProfile.setVisible(false);
                    profil.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            viewProfile.setVisible(true);
                        }
                    });
                    profil.setOnMouseExited(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            viewProfile.setVisible(false);
                        }
                    });
                    vbox.getChildren().add(viewProfile);
                    vbox.getChildren().add(profil);
                    viewProfile.setLayoutX(-5d);
                    viewProfile.setLayoutY(18d);
                    hbox.getChildren().add(vbox);
                    hbox.getChildren().add(label);
                    return hbox;
                }).collect(Collectors.toList());
        this.friendshipDTOList = list;
        modelTryAllUsers.setAll(list);
    }

    @FXML
    public void handleShowNotifications(){
        initModelNotifications();
        notificationsPane.setVisible(true);
    }

    public void initModelNotifications(){
        noOfUnseenNotifications = 0;
        notificationsListView.getItems().setAll(new ArrayList<>());
        List<Eveniment> nearEventsUnseen = eventService.getEventsWithUnseenNotifications();
        nearEventsUnseen.forEach(Eveniment::notifica);
        nearEventsUnseen.forEach(x->{if(!x.getAnnouncement().equals("")) {
            noOfUnseenNotifications++;
            Label label = new Label(x.getTitlu() + "\n" + x.getAnnouncement());
            label.setFont(Font.font(null, FontWeight.BOLD,14));
            label.setMaxWidth(180);
            label.setWrapText(true);
            modelNotifications.add(label);
        }});
        List<Eveniment> nearEventsSeen = eventService.getEventsWithSeenNotifications();
        nearEventsSeen.forEach(Eveniment::notifica);
        nearEventsSeen.forEach(x->{if(!x.getAnnouncement().equals("")) {
            Label label = new Label(x.getTitlu() + "\n" + x.getAnnouncement());
            label.setFont(Font.font(null, FontWeight.NORMAL,14));
            label.setMaxWidth(180);
            label.setWrapText(true);
            modelNotifications.add(label);
        }});
        if(noOfUnseenNotifications > 0) {
            notificationCircle.setVisible(true);
            noOfNotificationsText.setText("" + noOfUnseenNotifications);
        }
        else {
            notificationCircle.setVisible(false);
            noOfNotificationsText.setText("");
        }
    }

    public void initNoOfNotifications(){
        noOfUnseenNotifications = eventService.getEventsWithUnseenNotifications().size();
        if(noOfUnseenNotifications > 0) {
            notificationCircle.setVisible(true);
            noOfNotificationsText.setText("" + noOfUnseenNotifications);
        }
        else {
            noOfNotificationsText.setText("");
            notificationCircle.setVisible(false);
        }
    }

    @FXML
    public void handleBackNotifications(){
        notificationsPane.setVisible(false);
        eventService.setAllNotificationsSeen();
        noOfNotificationsText.setText("");
        notificationCircle.setVisible(false);
    }

    private Utilizator selectedUserForConversation = null;
//    @FXML
//    private void showConversation() {
////        for(int i=0;i<10;i++){
////            GridPane bubble;
////            if(i%2==0)
////                bubble = Bubble.createBubble("hello what are you doing tonight do you wan to come put", LocalDateTime.now(), true);
////            else
////                bubble = Bubble.createBubble("hi", LocalDateTime.now(), false);
////            chat.getItems().add(bubble);
////        }
////        FriendshipDTO selected = tableView1.getSelectionModel().getSelectedItem();
////        if(selected != null) {
////            Utilizator selectedUser = selected.getUser();
////            if (selectedUser == null)
////                selectedUser = selectedUserForConversation;
////            else
////                selectedUserForConversation = selectedUser;
////            if (selectedUser != null) {
////                messagesPagination.setVisible(true);
////                messagesPagination.setPageCount(messageService.countMessagesWithSomeone(selectedUser));
////                //    List<MessageDTO> messages = messageService.conversation(selectedUser);
////                List<MessageDTO> messages = messageService.getMessagesOnPage(messagesPagination.getCurrentPageIndex(), selectedUser);
////                modelMessages.setAll(messages);
////            } else {
//////           MessageAlert.showErrorMessage(null, "A user must be selected");
////            }
////        }
//        HBox selected = tryAllUsers.getSelectionModel().getSelectedItem();
//        Utilizator selectedUser;
//        if (selected != null) {
//            selectedUser = userService.findOne(Long.parseLong(selected.getId()));
//            selectedUserForConversation = selectedUser;
//        } else
//            selectedUser = selectedUserForConversation;
//
//        if (selectedUser != null) {
//            conversationUser.setText(selectedUser.getUsername());
//            messagesPagination.setVisible(true);
////            messagesPagination.setPageCount(messageService.countMessagesWithSomeone(selectedUser));
//            //    List<MessageDTO> messages = messageService.conversation(selectedUser);
//            List<MessageDTO> messages = messageService.getMessagesOnPage(messagesPagination.getCurrentPageIndex(), selectedUser);
//            modelMessages.setAll(messages);
//        } else {
////           MessageAlert.showErrorMessage(null, "A user must be selected");
//        }
//    }

    @FXML
    private ListView<GridPane> chat;
    ObservableList<GridPane> modelTryMessages = FXCollections.observableArrayList();
    @FXML
    private void tryShowConversation() {
        HBox selected = tryAllUsers.getSelectionModel().getSelectedItem();
        Utilizator selectedUser;
        if (selected != null) {
            selectedUser = userService.findOne(Long.parseLong(selected.getId()));
            selectedUserForConversation = selectedUser;
        } else
            selectedUser = selectedUserForConversation;

        if (selectedUser != null) {
            conversationUser.setText(selectedUser.getUsername());
            messagesPagination.setVisible(true);
            List<GridPane> messages = messageService.getMessagesOnPage(messagesPagination.getCurrentPageIndex(), selectedUser)
                    .stream().map(x->{
                        GridPane gridPane = new GridPane();
                        gridPane.setId(x.getId().toString());
                        gridPane.setHgap(-1d);
                        gridPane.setMaxWidth(410);
                        HBox hbox1 = new HBox();
                        HBox hbox2 = new HBox();
                        SVGPath triangle = new SVGPath();
                        hbox1.getChildren().add(triangle);
                        triangle.setFill(Paint.valueOf("#2f75d2"));
                        Label mesaj = new Label(x.getFullMessage());
                        mesaj.setTextFill(Paint.valueOf("white"));
                        mesaj.setWrapText(true);
                        mesaj.setStyle("-fx-background-color: #2f75d2; -fx-font-size: 13");
                        mesaj.setPadding(new Insets(5,5,5,5));
                        hbox2.getChildren().add(mesaj);
                        Label date = new Label(x.getDate().format(DATE_TIME_FORMATTER_WITH_HOUR));
                        date.setStyle("-fx-font-size: 9");
                        if(x.getUserEntity().getId() == currentUser.getId()) {
                            //am trimis mesajul
                            ColumnConstraints col1 = new ColumnConstraints();
                            col1.setPercentWidth(96);
                            gridPane.getColumnConstraints().add(col1);
                            hbox1.setAlignment(Pos.TOP_LEFT);
                            hbox2.setAlignment(Pos.TOP_RIGHT);
                            triangle.setContent("M15 0 L0 15 L0 0 Z");
                            GridPane.setHalignment(mesaj, HPos.RIGHT);
                            GridPane.setHalignment(date, HPos.RIGHT);
                            gridPane.add(hbox1,1,0);
                            gridPane.add(hbox2,0,0);
                            gridPane.add(date,0,1);
                            triangle.setFill(Paint.valueOf("#356db7"));
                            mesaj.setStyle("-fx-background-color: #356db7; -fx-font-size: 13");
                        }else{
                            hbox1.setAlignment(Pos.TOP_RIGHT);
                            hbox2.setAlignment(Pos.TOP_LEFT);
                            triangle.setContent("M0 0 L16 0 L16 16 Z");
                            GridPane.setHalignment(mesaj, HPos.LEFT);
                            GridPane.setHalignment(date, HPos.LEFT);
                            gridPane.add(hbox1,0,0);
                            gridPane.add(hbox2,1,0);
                            gridPane.add(date,1,1);
                        }
                        return gridPane;
                    }).collect(Collectors.toList());
            modelTryMessages.setAll(messages);
        } else {
//           MessageAlert.showErrorMessage(null, "A user must be selected");
        }
    }

    @FXML
    public void handleSendMessage(){
        String message = textFieldMessage.getText();
        if(!message.equals("")) {
            if (selectedUserForConversation != null) {
                if (chat.getSelectionModel().getSelectedItem() != null) {
                    try {
                        Long selectedMessage = Long.parseLong(chat.getSelectionModel().getSelectedItem().getId());
                        messageService.addReply(selectedMessage, message);
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
    Pane friendPagePane;
    @FXML
    ImageView friendProfilePicture;
    @FXML
    Label friendUsernameLabel;
    @FXML
    Label friendFirstNameLabel;
    @FXML
    Label friendLastNameLabel;
    @FXML
    Label friendDateLabel;
    @FXML
    Button friendButton;
    @FXML
    Button declineFriendButton;

    @FXML
    public void handleBackFriendPage(){
        friendPagePane.setVisible(false);
    }

    private Long userProfile = null;
    public void openUserProfile(Long userId) {
        userProfile = userId;
        declineFriendButton.setVisible(false);
        friendDateLabel.setVisible(false);
        Utilizator user = userService.findOne(userId);
        friendUsernameLabel.setText(user.getUsername());
        friendFirstNameLabel.setText(user.getFirstName());
        friendLastNameLabel.setText(user.getLastName());

        if(userId != currentUser.getId()) {
            friendButton.setVisible(true);
            Boolean gasit = false;
            FriendRequest friendRequest1 = friendshipService.findRequest(currentUser.getId(), userId);
            FriendRequest friendRequest2 = friendshipService.findRequest(userId, currentUser.getId());
            if ((friendRequest1 != null && friendRequest1.getStatus().equals(Status.APPROVED)) || (friendRequest2 != null && friendRequest2.getStatus().equals(Status.APPROVED))) {
                friendDateLabel.setText("Friends since: " + friendshipService.findFriendship(currentUser.getId(), userId).getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR));
                friendDateLabel.setVisible(true);
                friendButton.setText("Unfriend");
                friendButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        friendshipService.deleteFriendship(user.getId());
//                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Delete","Friend removed");
                        openUserProfile(userId);
                    }
                });
                gasit = true;
            }
            if (friendRequest1 != null && friendRequest1.getStatus().equals(Status.PENDING)) {
                friendButton.setText("Unsend request");
                friendButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        friendshipService.deleteRequest(user);
//                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"Unsend","Request unsent");
                        openUserProfile(userId);
                    }
                });
                gasit = true;
            }
            if (friendRequest2 != null && friendRequest2.getStatus().equals(Status.PENDING)) {
                declineFriendButton.setVisible(true);
                declineFriendButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        friendshipService.respondFriendship(user.getId(), Status.REJECTED);
//                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Respond", "Request approved");
                        openUserProfile(userId);
                    }
                });
                friendButton.setText("Accept");
                friendButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        friendshipService.respondFriendship(user.getId(), Status.APPROVED);
//                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Respond", "Request approved");
                        openUserProfile(userId);
                    }
                });
                gasit = true;
            }
            if (!gasit) {
                friendButton.setText("Add friend");
                friendButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            friendshipService.addFriendshipRequest(user.getId());
//                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add Friend", "Request sent");
                            openUserProfile(userId);
                        } catch (ServiceException | RepoException | ValidationException e) {
                            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Add Friend", e.toString());
                        }
                    }
                });
            }
        }else{
            friendButton.setVisible(false);
        }

        try {
            FileInputStream input = new FileInputStream(user.getUrl());
            Image picture = new Image(input);
            friendProfilePicture.setImage(picture);
        } catch (IOException | SecurityException e) {
            System.out.println("EROARE LA USER PROFILE PICTURE");
            try {
                FileInputStream input = new FileInputStream("E:\\MAP\\SocialNetwork\\src\\main\\resources\\images\\defaultUser.jpg");
                Image picture = new Image(input);
                profilePicture.setImage(picture);
            } catch (IOException | SecurityException s) {
                System.out.println("EROARE");
            }
        }
        friendPagePane.setVisible(true);
    }

    //TODO EVENTS tab -------------------------------------------------------------------------------------------------------------------

    @FXML
    private Spinner<String> beginsSpinner;
    @FXML
    private Spinner<String> endsSpinner;
    @FXML
    private DatePicker eventDatePicker;
    @FXML
    private TextField eventTitleTextField;
    @FXML
    private Pagination eventsPagination;
//    @FXML
//    private TableView<EvenimentDTO> eventsTableView;
//    @FXML
//    private TableColumn<EvenimentDTO,String> eventTableColumn;
//    @FXML
//    private TableColumn<EvenimentDTO, Button> eventButtonTableColumn;
    @FXML
    private CheckBox eventsCheckBox;
//
//    ObservableList<EvenimentDTO> modelEvents = FXCollections.observableArrayList();

//    private void initModelAllEvents(){
//        List<EvenimentDTO> events = StreamSupport.stream(eventService.getEventsOnPage(eventsPagination.getCurrentPageIndex()).spliterator(), false)
//                .map(x->{
//                    if(eventService.isSubscribed(x.getId()))
//                        return new EvenimentDTO(x,true,eventService);
//                    else
//                        return new EvenimentDTO(x,false,eventService);
//                }).collect(Collectors.toList());
//        modelEvents.setAll(events);
//    }

    @FXML
    ListView<HBox> tryAllEvents;
    ObservableList<HBox> modelTryAllEvents = FXCollections.observableArrayList();
    public void initTryAllEvent(){
        List<HBox> events = StreamSupport.stream(eventService.getEventsOnPage(eventsPagination.getCurrentPageIndex()).spliterator(), false)
                .map(x->{
                    x.notifica();
                    x.calculateDistance();
                    HBox hbox = new HBox();
                    hbox.setMaxWidth(378d);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setId(x.getId().toString());
                    Label label;
                    if(!x.getAnnouncement().equals(""))
                        label = new Label(x.getTitlu() + "\n" + x.getAnnouncement());
                    else
                        label = new Label(x.getTitlu() + "\n" + x.getDistance());
                    label.setMaxWidth(220d);
                    label.setPrefWidth(220d);
                    label.setWrapText(true);
                    Label date = new Label(x.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR) + "\n"
                            + x.getBegins().toString() + " - " + x.getEnds());
                    date.setTextAlignment(TextAlignment.CENTER);
                    date.setMaxWidth(100d);
                    if(!x.getAnnouncement().equals("") && eventService.findOneParticipant(currentUser.getId(),x.getId()) != null){
                        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15");
                        date.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
                    }
                    label.setFont(Font.font(15d));
                    date.setFont(Font.font(14d));

                    MenuButton options = new MenuButton("...");
                    options.setStyle("-fx-background-color: transparent");
                    options.setCursor(Cursor.HAND);
                    options.setPrefWidth(30d);
                    options.setFont(new Font(17));
                    options.setTextAlignment(TextAlignment.CENTER);
                    options.setAlignment(Pos.TOP_CENTER);

                    if(eventService.isSubscribed(x.getId())){
                        MenuItem unsuscribe = new MenuItem("Unsubscribe");
                        unsuscribe.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                eventService.removeParticipant(x.getId());
                            }
                        });
                        MenuItem muteNotifications;
                        if(!eventService.areNotificationsMuted(x.getId())) {
                            muteNotifications = new MenuItem("Mute notifications");
                            muteNotifications.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    eventService.muteNotifications(x.getId());
                                }
                            });
                        }else{
                            muteNotifications = new MenuItem("Unmute notifications");
                            muteNotifications.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    eventService.unmuteNotifications(x.getId());
                                }
                            });
                        }
                        options.getItems().add(unsuscribe);
                        options.getItems().add(muteNotifications);
                    }
                    else {
                        MenuItem subscribe = new MenuItem("Subscribe");
                        subscribe.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                eventService.addParticipant(x.getId());
                            }
                        });
                        options.getItems().add(subscribe);
                    }
                    hbox.getChildren().add(options);
                    hbox.getChildren().add(label);
                    hbox.getChildren().add(date);
                    return hbox;
                }).collect(Collectors.toList());
        modelTryAllEvents.setAll(events);
    }

    @FXML
    public void handleOnlyGoing(){
        eventsPagination.setCurrentPageIndex(0);
        if(eventsCheckBox.isSelected()){
            initModelOnlyGoingEvents();
        }else{
//            initModelAllEvents();
            initTryAllEvent();
        }
    }

    private void initModelOnlyGoingEvents(){
        List<HBox> events = StreamSupport.stream(eventService.getGoingEventsOnPage(eventsPagination.getCurrentPageIndex()).spliterator(), false)
                .map(x->{
                    x.notifica();
                    x.calculateDistance();
                    HBox hbox = new HBox();
                    hbox.setMaxWidth(378d);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setId(x.getId().toString());
                    Label label;
                    if(!x.getAnnouncement().equals(""))
                        label = new Label(x.getTitlu() + "\n" + x.getAnnouncement());
                    else
                        label = new Label(x.getTitlu() + "\n" + x.getDistance());
                    label.setMaxWidth(220d);
                    label.setPrefWidth(220d);
                    label.setWrapText(true);
                    Label date = new Label(x.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR) + "\n"
                            + x.getBegins().toString() + " - " + x.getEnds());
                    date.setTextAlignment(TextAlignment.CENTER);
                    date.setMaxWidth(100d);
                    if(!x.getAnnouncement().equals("") && eventService.findOneParticipant(currentUser.getId(),x.getId()) != null){
                        label.setStyle("-fx-font-weight: bold; -fx-font-size: 15");
                        date.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
                    }
                    label.setFont(Font.font(15d));
                    date.setFont(Font.font(14d));

                    MenuButton options = new MenuButton("...");
                    options.setStyle("-fx-background-color: transparent");
                    options.setCursor(Cursor.HAND);
                    options.setPrefWidth(30d);
                    options.setFont(new Font(17));
                    options.setTextAlignment(TextAlignment.CENTER);
                    options.setAlignment(Pos.TOP_CENTER);

                    if(eventService.isSubscribed(x.getId())){
                        MenuItem unsuscribe = new MenuItem("Unsubscribe");
                        unsuscribe.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                eventService.removeParticipant(x.getId());
                            }
                        });
                        MenuItem muteNotifications;
                        if(!eventService.areNotificationsMuted(x.getId())) {
                            muteNotifications = new MenuItem("Mute notifications");
                            muteNotifications.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    eventService.muteNotifications(x.getId());
                                }
                            });
                        }else{
                            muteNotifications = new MenuItem("Unmute notifications");
                            muteNotifications.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    eventService.unmuteNotifications(x.getId());
                                }
                            });
                        }
                        options.getItems().add(unsuscribe);
                        options.getItems().add(muteNotifications);
                    }
                    hbox.getChildren().add(options);
                    hbox.getChildren().add(label);
                    hbox.getChildren().add(date);
                    return hbox;
                }).collect(Collectors.toList());
        modelTryAllEvents.setAll(events);
//        List<EvenimentDTO> events = StreamSupport.stream(eventService.getGoingEventsOnPage(eventsPagination.getCurrentPageIndex()).spliterator(), false)
//                .map(x-> new EvenimentDTO(x,true,eventService))
//                .collect(Collectors.toList());
//        modelEvents.setAll(events);
    }

    @FXML
    private Text eventTitleRestrictionsText;
    @FXML
    public void handleAddEvent() {
        String title = eventTitleTextField.getText();
        LocalDate date = eventDatePicker.getValue();
        Boolean gresit = false;
        if(title.replaceAll("^[ \t]+|[ \t]+$", "").length() < 3){
            gresit = true;
            eventTitleTextField.setStyle("-fx-border-color: red");
        }
        if (date == null){
            gresit = true;
            eventDatePicker.setStyle("-fx-border-color: red");
        }
        if(!gresit) {
            try {
                eventService.addEvent(date, title, LocalTime.parse(beginsSpinner.getValue()), LocalTime.parse(endsSpinner.getValue()));
                eventTitleTextField.setText("");
                eventDatePicker.setValue(null);
            } catch (ValidationException v) {
//                invalidEventTitleText.setText(v.getMessage());
            }
        }
    }

    @FXML
    public void handleDateEvent() { eventDatePicker.setStyle("-fx-border-color: transparent"); }
    @FXML
    public void handleTitleEvent() { eventTitleTextField.setStyle("-fx-border-color: transparent"); }

    @FXML
    public void showEventTitleRestrictions(){ eventTitleRestrictionsText.setVisible(true); }
    @FXML
    public void dontShowEventTitleRestrictions(){ eventTitleRestrictionsText.setVisible(false); }



    //TODO REPORTS tab --------------------------------------------------------------------------------------------------------------------
    @FXML
    DatePicker datePicker1R1;
    @FXML
    DatePicker datePicker2R1;
    @FXML
    DatePicker datePicker1R2;
    @FXML
    DatePicker datePicker2R2;
    @FXML
    ComboBox<Utilizator> comboBox;
    @FXML
    TextArea reportTextArea;
    @FXML
    TabPane tabPaneReports;
    ObservableList<Utilizator> modelUsersReport = FXCollections.observableArrayList();
    String textReport = "";


    private void initModelUsersReport(){
        tabPaneReports.getStyleClass().add("floating");
        modelUsersReport.setAll(StreamSupport.stream(userService.getAllOrderedByUsername().spliterator(),false)
                .collect(Collectors.toList()));
    }

//    LocalDate localDate1R1 = null;
//    LocalDate localDate2R1 = null;
    @FXML
    public void handleReport1(){
        textReport = "";
        LocalDate localDate1 = datePicker1R1.getValue();
        LocalDate localDate2 = datePicker2R1.getValue();
        if(localDate1 != null && localDate2 != null) {
//            localDate1R1 = localDate1;
//            localDate2R1 = localDate2;
            datePicker1R1.setStyle("-fx-border-color: transparent");
            datePicker2R1.setStyle("-fx-border-color: transparent");
            if(localDate1.isBefore(localDate2) || localDate1.equals(localDate2)) {
                List<FriendshipDTO> prietenii = friendshipService.friendsBetweenDates(localDate1, localDate2)
                        .stream().map(x -> {
                            if (x.getId().getRight() == currentUser.getId())
                                return new FriendshipDTO(userService.findOne(x.getId().getLeft()), x.getDate(), FriendshipType.FRIENDS,null);
                            else return new FriendshipDTO(userService.findOne(x.getId().getRight()), x.getDate(), FriendshipType.FRIENDS, null);
                        })
                        .collect(Collectors.toList());
                List<Message> mesaje = messageService.messagesBetweenDates(localDate1, localDate2);
                textReport = userService.getRaport1(localDate1,localDate2,prietenii,mesaje);
                reportTextArea.setStyle("-fx-font-size: 15");
                reportTextArea.setText(textReport);
            }
            else{
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "First date must be before second date");
            }
        }else{
            if(localDate1 == null)
                datePicker1R1.setStyle("-fx-border-color: red");
            if(localDate2 == null)
                datePicker2R1.setStyle("-fx-border-color: red");
        }
    }

    @FXML
    public void handleReport2(){
        textReport = "";
        LocalDate localDate1 = datePicker1R2.getValue();
        LocalDate localDate2 = datePicker2R2.getValue();
        Utilizator user = comboBox.getValue();
        if(localDate1 != null && localDate2 != null && user != null) {
            datePicker1R2.setStyle("-fx-border-color: transparent");
            datePicker2R2.setStyle("-fx-border-color: transparent");
            comboBox.setStyle("-fx-border-color: transparent");
            if(localDate1.isBefore(localDate2) || localDate1.equals(localDate2)) {
                textReport = messageService.getRaport2(localDate1,localDate2,user);
                reportTextArea.setStyle("-fx-font-size: 15");
                reportTextArea.setText(textReport);
            }
            else{
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "First date must be before second date");
            }
        }
        else{
            if(localDate1 == null)
                datePicker1R2.setStyle("-fx-border-color: red");
            if(localDate2 == null)
                datePicker2R2.setStyle("-fx-border-color: red");
            if(user == null)
                comboBox.setStyle("-fx-border-color: red");
        }
    }

    @FXML
    public void handleSaveReport1(){
        LocalDate localDate1 = datePicker1R1.getValue();
        LocalDate localDate2 = datePicker2R1.getValue();
        if(localDate1 != null && localDate2 != null) {
            datePicker1R1.setStyle("-fx-border-color: transparent");
            datePicker2R1.setStyle("-fx-border-color: transparent");
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("untitled.pdf");
                fileChooser.setTitle("Save file");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                File dest = fileChooser.showSaveDialog(dialogStage);
                if (dest != null) {
                    try {
                        Document document = new Document();
                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest.getAbsolutePath()));
                        List<FriendshipDTO> prietenii = friendshipService.friendsBetweenDates(localDate1, localDate2)
                                .stream().map(x -> {
                                    if (x.getId().getRight() == currentUser.getId())
                                        return new FriendshipDTO(userService.findOne(x.getId().getLeft()), x.getDate(), FriendshipType.FRIENDS,null);
                                    else return new FriendshipDTO(userService.findOne(x.getId().getRight()), x.getDate(), FriendshipType.FRIENDS, null);
                                })
                                .collect(Collectors.toList());
                        List<Message> mesaje = messageService.messagesBetweenDates(localDate1, localDate2);
                        userService.saveReport1(document,prietenii,mesaje,localDate1,localDate2);
                        document.close();
                        writer.close();
                    } catch (IOException ex) {
                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Report", "Something went wrong, the document was not saved");
                    }
                }
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }else{
            if(localDate1 == null)
                datePicker1R1.setStyle("-fx-border-color: red");
            if(localDate2 == null)
                datePicker2R1.setStyle("-fx-border-color: red");
        }
    }

    @FXML
    public void handleSaveReport2(){
        LocalDate localDate1 = datePicker1R2.getValue();
        LocalDate localDate2 = datePicker2R2.getValue();
        Utilizator user = comboBox.getValue();
        if(localDate1 != null && localDate2 != null && user != null) {
            datePicker1R2.setStyle("-fx-border-color: transparent");
            datePicker2R2.setStyle("-fx-border-color: transparent");
            comboBox.setStyle("-fx-border-color: transparent");
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("untitled.pdf");
                fileChooser.setTitle("Save file");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                File dest = fileChooser.showSaveDialog(dialogStage);
                if (dest != null) {
                    try {
                        Document document = new Document();
                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest.getAbsolutePath()));
                        messageService.saveReport2(document,localDate1,localDate2,user);

                        document.close();
                        writer.close();
                    } catch (IOException ex) {
                        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Report", "Something went wrong, the document was not saved");
                    }
                }
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }else{
            if(localDate1 == null)
                datePicker1R2.setStyle("-fx-border-color: red");
            if(localDate2 == null)
                datePicker2R2.setStyle("-fx-border-color: red");
            if(user == null)
                comboBox.setStyle("-fx-border-color: red");
        }
    }

    @FXML
    public void handleDate1R2() {
        datePicker1R2.setStyle("-fx-border-color: transparent");
        datePicker2R2.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate aux = datePicker1R2.getValue();
                setDisable(empty || date.compareTo(aux) < 0);
            }
        });
    }
    @FXML
    public void handleDate2R2(){
        datePicker2R2.setStyle("-fx-border-color: transparent");
            datePicker1R2.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate aux = datePicker2R2.getValue();
                    setDisable(empty || date.compareTo(aux) > 0);
                }
            });
    }
    @FXML
    public void handleCombo(){comboBox.setStyle("-fx-border-color: transparent");}
    @FXML
    public void handleDate1R1(){
        datePicker1R1.setStyle("-fx-border-color: transparent");
            datePicker2R1.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate aux = datePicker1R1.getValue();
                    setDisable(empty || date.compareTo(aux) < 0);
                }
            });
    }
    @FXML
    public void handleDate2R1(){
        datePicker2R1.setStyle("-fx-border-color: transparent");
            datePicker1R1.setDayCellFactory(picker -> new DateCell() {
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    LocalDate aux = datePicker2R1.getValue();
                    setDisable(empty || date.compareTo(aux) > 0);
                }
            });
    }


    //TODO MY PROFILE tab -----------------------------------------------------------------------------------------------------------------
    @FXML
    TabPane tabPane;
    @FXML
    TextField firstNameEditTextField;
    @FXML
    TextField lastNameEditTextField;
    @FXML
    TextField usernameEditTextField;
    @FXML
    TextField emailEditTextField;
    @FXML
    TextField urlEditTextField;
    @FXML
    PasswordField actualPasswordEditTextField;
    @FXML
    PasswordField passwordEditTextField;
    @FXML
    PasswordField confirmPasswordEditTextField;
    @FXML
    ProgressBar passwordStrengthBarGreen;
    @FXML
    ProgressBar passwordStrengthBarYellow;
    @FXML
    ProgressBar passwordStrengthBarRed;
    @FXML
    Label passwordInfoLabel;
    @FXML
    ListView<HBox> eventsListView;
    @FXML
    ListView<Label> friendsListView;
    @FXML
    TextField searchFriends;
    @FXML
    Pagination myFriendsPagination;
    ObservableList<Label> modelMyFriends = FXCollections.observableArrayList();
    ObservableList<HBox> modelMyEvents = FXCollections.observableArrayList();
    Boolean editMode = false;

    private void handleFilterFriends(){
        Predicate<Utilizator> namePredicate = x -> x.getUsername().startsWith(searchFriends.getText().replaceAll("^[ \t]+|[ \t]+$", ""));
        modelMyFriends.setAll(this.friendsShown.stream().filter(namePredicate)
                .map(x->{
                    Label l = new Label(x.getUsername());
                    l.setStyle("-fx-font-size: 16");
                    return l;
                }).collect(Collectors.toList()));
    }
    List<Utilizator> friendsShown = new ArrayList<>();
    private void initMyFriends(){
        friendsShown.clear();
        List<Label> friends = StreamSupport.stream(friendshipService.getUsersFriendsOnPage(myFriendsPagination.getCurrentPageIndex()).spliterator(),false)
                .map(x->{
                    Label l = new Label(x.getUsername());
                    friendsShown.add(x);
                    l.setStyle("-fx-font-size: 16");
                    return l;
                }).collect(Collectors.toList());
        modelMyFriends.setAll(friends);
    }

    public void initMyEvents(){
        List<HBox> events = StreamSupport.stream(eventService.getCurrentUserEvents().spliterator(), false)
                .map(x->{
                    x.notifica();
                    x.calculateDistance();
                    HBox hbox = new HBox();
                    hbox.setMaxWidth(444d);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.setId(x.getId().toString());
                    Label label;
                    if(!x.getAnnouncement().equals(""))
                        label = new Label(x.getTitlu() + "\n" + x.getAnnouncement());
                    else
                        label = new Label(x.getTitlu() + "\n" + x.getDistance());
                    label.setMaxWidth(300d);
                    label.setPrefWidth(300d);
                    label.setWrapText(true);
                    Label date = new Label(x.getDate().format(DATE_TIME_FORMATTER_WITHOUT_HOUR) + "\n"
                            + x.getBegins().toString() + " - " + x.getEnds());
                    date.setTextAlignment(TextAlignment.CENTER);
                    date.setMaxWidth(140d);
                    if(!x.getAnnouncement().equals("") && eventService.findOneParticipant(currentUser.getId(),x.getId()) != null){
                        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13");
                        date.setStyle("-fx-font-weight: bold; -fx-font-size: 12");
                    }
                    label.setFont(Font.font(13d));
                    date.setFont(Font.font(12d));

                    MenuButton options = new MenuButton("...");
                    options.setStyle("-fx-background-color: transparent");
                    options.setCursor(Cursor.HAND);
                    options.setPrefWidth(40d);
                    options.setFont(new Font(16));
                    options.setTextAlignment(TextAlignment.CENTER);
                    options.setAlignment(Pos.TOP_CENTER);

                    if(eventService.isSubscribed(x.getId())){
                        MenuItem unsuscribe = new MenuItem("Unsubscribe");
                        unsuscribe.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                eventService.removeParticipant(x.getId());
                            }
                        });
                        MenuItem muteNotifications;
                        if(!eventService.areNotificationsMuted(x.getId())) {
                            muteNotifications = new MenuItem("Mute notifications");
                            muteNotifications.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    eventService.muteNotifications(x.getId());
                                }
                            });
                        }else{
                            muteNotifications = new MenuItem("Unmute notifications");
                            muteNotifications.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    eventService.unmuteNotifications(x.getId());
                                }
                            });
                        }
                        options.getItems().add(unsuscribe);
                        options.getItems().add(muteNotifications);
                    }
                    else {
                        MenuItem subscribe = new MenuItem("Subscribe");
                        subscribe.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                eventService.addParticipant(x.getId());
                            }
                        });
                        options.getItems().add(subscribe);
                    }
                    MenuItem delete = new MenuItem("Delete");
                    delete.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            eventService.deleteEvent(x.getId());
                        }
                    });
                    options.getItems().add(delete);
                    hbox.getChildren().add(options);
                    hbox.getChildren().add(label);
                    hbox.getChildren().add(date);
                    return hbox;
                }).collect(Collectors.toList());
        modelMyEvents.setAll(events);
    }

    private void initMyProfile(){
        passwordInfoLabel.setText("");
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                        initMyProfile();
                    }
                }
        );
        firstNameEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateFirstName();
            }
        });
        lastNameEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateLastName();
            }
        });
        usernameEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateUsername();
            }
        });
        emailEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateEmail();
            }
        });
        confirmPasswordEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateConfirmPassword();
            }
        });
        actualPasswordEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateActualPassword();
            }
        });
        urlEditTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateUrl();
            }
        });
        passwordEditTextField.textProperty().addListener(x->showStrengthBar());
        firstNameEditTextField.setText(currentUser.getFirstName());
        lastNameEditTextField.setText(currentUser.getLastName());
        usernameEditTextField.setText(currentUser.getUsername());
        emailEditTextField.setText(currentUser.getEmail());
        urlEditTextField.setText(currentUser.getUrl());
    }

    public boolean handleValidateFirstName(){
        String str = firstNameEditTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.length() < 3 || str.length() > 30) {
//            firstNameTriangle.setVisible(true);
            firstNameEditTextField.setStyle("-fx-border-color: red");
            return false;
        }
        firstNameEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    public boolean handleValidateLastName(){
        String str = lastNameEditTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.length() < 3 || str.length() > 30) {
//            lastNameTriangle.setVisible(true);
            lastNameEditTextField.setStyle("-fx-border-color: red");
            return false;
        }
        lastNameEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    public boolean handleValidateUsername(){
        String str = usernameEditTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.length() < 3 || str.length() > 50) {
//            usernameTriangle.setVisible(true);
            usernameEditTextField.setStyle("-fx-border-color: red");
            return false;
        }
        usernameEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    public boolean handleValidateEmail(){
        String str = emailEditTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(!str.endsWith("@yahoo.com") && !str.endsWith("@gmail.com") && !str.endsWith("@scs.ubbcluj.ro") && !str.endsWith("@cs.ubbcluj.ro")) {
//            emailTriangle.setVisible(true);
            emailEditTextField.setStyle("-fx-border-color: red");
            return false;
        }
        emailEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    public boolean handleValidateConfirmPassword(){
        if(!passwordEditTextField.getText().equals(confirmPasswordEditTextField.getText()) || passwordEditTextField.getText().length() < 5 || passwordEditTextField.getText().length() > 50) {
//            confirmPasswordTriangle.setVisible(true);
            confirmPasswordEditTextField.setStyle("-fx-border-color: red");
            return false;
        }
        confirmPasswordEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    public boolean handleValidateActualPassword(){
        if(!MyBCrypt.verifyHash(actualPasswordEditTextField.getText(),currentUser.getPassword())) {
//            confirmPasswordTriangle.setVisible(true);
            actualPasswordEditTextField.setStyle("-fx-border-color: red");
            return false;
        }
        actualPasswordEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    public boolean handleValidateUrl() {
        String str = urlEditTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.equals(""))
            return true;
        try {
            FileInputStream input = new FileInputStream(str);
        } catch (IOException | SecurityException e) {
//            urlTriangle.setVisible(true);
            urlEditTextField.setStyle("-fx-border-color: red");
 //           urlRestrictions.setText("Picture cannot be accessed");
            return false;
        }
        urlEditTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    private void showStrengthBar(){
        int passwordLength = passwordEditTextField.getText().length();
        if(passwordLength == 0)
            passwordStrengthBarRed.setVisible(false);
        if(passwordLength > 0 && passwordLength < 5) {
            passwordStrengthBarYellow.setVisible(false);
            passwordStrengthBarGreen.setVisible(false);
            passwordStrengthBarRed.setVisible(true);
        }
        if(passwordLength > 4 && passwordLength < 8) {
            passwordStrengthBarRed.setVisible(false);
            passwordStrengthBarGreen.setVisible(false);
            passwordStrengthBarYellow.setVisible(true);
        }
        if(passwordLength > 7) {
            passwordStrengthBarYellow.setVisible(false);
            passwordStrengthBarRed.setVisible(false);
            passwordStrengthBarGreen.setVisible(true);
        }
    }

    private boolean checkPasswordAccurance(){
        return passwordEditTextField.getText().equals(confirmPasswordEditTextField.getText()) && passwordEditTextField.getText().length() > 4;
    }

    @FXML
    public void handleEditData(){
        if(!editMode){
            firstNameEditTextField.setEditable(true);
            lastNameEditTextField.setEditable(true);
            usernameEditTextField.setEditable(true);
            emailEditTextField.setEditable(true);
            urlEditTextField.setEditable(true);
            editMode = true;
        }else{
            //se editeaza datele
            if(handleValidateFirstName() && handleValidateLastName() && handleValidateUsername() && handleValidateEmail() && handleValidateUrl()) {
                userService.update(firstNameEditTextField.getText(),lastNameEditTextField.getText(),usernameEditTextField.getText(),emailEditTextField.getText(),urlEditTextField.getText());
                firstNameEditTextField.setEditable(false);
                lastNameEditTextField.setEditable(false);
                usernameEditTextField.setEditable(false);
                emailEditTextField.setEditable(false);
                urlEditTextField.setEditable(false);
                initMyProfile();
                updateCurrentUser();
                editMode = false;
            }
        }

    }

    @FXML
    public void handleChoosePicture(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilter3 = new FileChooser.ExtensionFilter("JPEG files (*.jpeg)", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter1);
        fileChooser.getExtensionFilters().add(extFilter2);
        fileChooser.getExtensionFilters().add(extFilter3);
        File file = fileChooser.showOpenDialog(dialogStage);
        if(file != null)
            urlEditTextField.setText(file.getAbsolutePath());
    }

    @FXML
    public void handleChangePassword(){
        if(handleValidateConfirmPassword() && handleValidateActualPassword()) {
            userService.updatePassword(passwordEditTextField.getText(), currentUser);
            passwordEditTextField.setText("");
            confirmPasswordEditTextField.setText((""));
            actualPasswordEditTextField.setText("");
        }
    }

//    @FXML
//    public void handleOnlyFriends(){
//        allUsersPagination.setCurrentPageIndex(0);
//        if(checkBoxOnlyFriends.isSelected()){
//            initModelOnlyFriends();
//        }
//        else{
//            initTryAllUsers();
////            initModelAllUsers();
//        }
//    }

//    private void initModelOnlyFriends() {
//        List<FriendshipDTO> list = StreamSupport.stream(friendshipService.getUsersFriendsOnPage(allUsersPagination.getCurrentPageIndex()).spliterator(), false)
//                .map(x->{
//                    //TODO am putea rezolva cu un inner join
//                    //TODO de aici merge greu paginarea -----------------------------------------------------------------------------------------
//                    FriendRequest friendRequest1 = friendshipService.findRequest(currentUser.getId(), x.getId());
//                    FriendRequest friendRequest2 = friendshipService.findRequest(x.getId(), currentUser.getId());
//                    if((friendRequest1 != null && friendRequest1.getStatus().equals(Status.APPROVED)) || (friendRequest2 != null && friendRequest2.getStatus().equals(Status.APPROVED)))
//                        return new FriendshipDTO(x, friendshipService.findFriendship(currentUser.getId(), x.getId()).getDate(), FriendshipType.FRIENDS, friendshipService);
//                    if(friendRequest1 != null && friendRequest1.getStatus().equals(Status.PENDING))
//                        return new FriendshipDTO(x,null, FriendshipType.REQUEST_SEND, friendshipService);
//                    if(friendRequest2 != null && friendRequest2.getStatus().equals(Status.PENDING))
//                        return new FriendshipDTO(x,null, FriendshipType.REQUEST_RECEIVED, friendshipService);
//                    return new FriendshipDTO(x,null, FriendshipType.NOT_FRIENDS, friendshipService);
//                }).collect(Collectors.toList());
//        this.friendshipDTOList = list;
//        modelUsers.setAll(list);
//    }

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
}
