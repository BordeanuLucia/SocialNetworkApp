package SocialNetwork.controller;

import SocialNetwork.domain.Utilizator;
import SocialNetwork.service.MessageService;
import SocialNetwork.service.PrietenieService;
import SocialNetwork.service.ServiceException;
import SocialNetwork.service.UtilizatorService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class UserController {
    UtilizatorService userService;
    PrietenieService friendshipService;
    MessageService messageService;
    Stage stage;
    @FXML
    private TextField textFieldUsername;
    @FXML
    private PasswordField textFieldPassword;


    public void setUtilizatorService(UtilizatorService userService, PrietenieService friendshipService, MessageService messageService, Stage stage) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.stage = stage;
    }

    @FXML
    public void initialize() { }

    private void clerFields(){
        textFieldUsername.setText("");
        textFieldPassword.setText("");
    }

    @FXML
    public void handleSignIn(ActionEvent ev){
        String username = textFieldUsername.getText();
        String password = textFieldPassword.getText();
        try {
            Utilizator user = userService.logIn(username, password);
            friendshipService.setCurrentUser(user.getId());
            messageService.setCurrentUser(user.getId());
            this.clerFields();
            showSignInDialog(user);
            this.stage.close();
        }catch (ServiceException s) {
            MessageAlert.showErrorMessage(null, s.toString());
        }
    }

    public void showSignInDialog(Utilizator user) {
        try{
            //setup
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/sceneBuilder/profilePage.fxml"));
            AnchorPane root = (AnchorPane)loader.load();

            //stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("UserPage");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            CurrentUserController currentUserController = loader.getController();
            currentUserController.setService(userService, friendshipService, messageService, dialogStage, user, stage);
            dialogStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
