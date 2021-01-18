import config.ApplicationContext;
import controller.UserController;
import domain.validators.EventValidator;
import domain.validators.PrietenieValidator;
import domain.validators.UtilizatorValidator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import repository.*;
import service.EventService;
import service.MessageService;
import service.PrietenieService;
import service.UtilizatorService;

import java.io.IOException;

public class MainApp extends Application {
    final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
    final String username= ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username");
    final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword");

    EventDBRepository eventDB = new EventDBRepository(url, username, password, new EventValidator());
    ParticipantDBRepository participantDB = new ParticipantDBRepository(url, username, password);
    UserDBRepository userDB = new UserDBRepository(url, username, password, new UtilizatorValidator());
    FriendshipDBRepository friendshipDB = new FriendshipDBRepository(url,username,password,new PrietenieValidator());
    RequestDBRepository requestDB = new RequestDBRepository(url,username,password,new PrietenieValidator());
    UtilizatorService userService = new UtilizatorService(userDB);
    EventService eventService = new EventService(eventDB, participantDB);
    PrietenieService friendshipService = new PrietenieService(friendshipDB, userDB, requestDB);
    MessageDBRepository messageDB = new MessageDBRepository(url,username,password);
    MessageService messageService = new MessageService(messageDB, userDB);


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        initView(primaryStage);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader usersLoader = new FXMLLoader();
        usersLoader.setLocation(getClass().getResource("/sceneBuilder/logInPage.fxml"));
        AnchorPane usersLayout = usersLoader.load();
        Scene scene = new Scene(usersLayout);
        primaryStage.setScene(scene);

        UserController userController = usersLoader.getController();
        userController.setUtilizatorService(userService, friendshipService, messageService, eventService, primaryStage, url, username, password);

    }
}