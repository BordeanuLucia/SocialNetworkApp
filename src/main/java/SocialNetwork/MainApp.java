package SocialNetwork;

import SocialNetwork.controller.UserController;
import SocialNetwork.domain.*;
import SocialNetwork.domain.validators.PrietenieValidator;
import SocialNetwork.domain.validators.UtilizatorValidator;
import SocialNetwork.repository.*;
import SocialNetwork.service.MessageService;
import SocialNetwork.service.PrietenieService;
import SocialNetwork.service.UtilizatorService;
import SocialNetwork.config.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
    final String username= ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username");
    final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword");

    Repository<Long, Utilizator> userDB = new UserDBRepository(url, username, password, new UtilizatorValidator());
    Repository<Tuple<Long, Long>, Prietenie> friendshipDB = new FriendshipDBRepository(url,username,password,new PrietenieValidator());
    Repository<Tuple<Long, Long>, FriendRequest> requestDB = new RequestDBRepository(url,username,password,new PrietenieValidator());
    UtilizatorService userService = new UtilizatorService(userDB);
    PrietenieService friendshipService = new PrietenieService(friendshipDB, userDB, requestDB);
    Repository<Long, Message> messageDB = new MessageDBRepository(url,username,password,userDB);
    MessageService messageService = new MessageService(messageDB, userDB);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        initView(primaryStage);
        primaryStage.setWidth(700);
        primaryStage.show();
    }

    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader usersLoader = new FXMLLoader();
        usersLoader.setLocation(getClass().getResource("/sceneBuilder/logIn.fxml"));
        AnchorPane usersLayout = usersLoader.load();
        primaryStage.setScene(new Scene(usersLayout));

        UserController userController = usersLoader.getController();
        userController.setUtilizatorService(userService, friendshipService, messageService, primaryStage);

    }
}