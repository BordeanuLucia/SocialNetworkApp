package controller;

import domain.Utilizator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class UserController {
    private String url;
    private String username;
    private String password;
    private UtilizatorService userService;
    private EventService eventService;
    private PrietenieService friendshipService;
    private MessageService messageService;
    private Stage stage;

    @FXML
    private TextField textFieldEmail;
    @FXML
    private PasswordField textFieldPassword;
    @FXML
    private Text textInvalidUsernamePassword;
    @FXML
    private CheckBox rememberMeCheck;
    @FXML
    private Button forgotPasswordButton;

    @FXML
    private AnchorPane createAccountPane;
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private PasswordField confirmPasswordTextField;
    @FXML
    private TextField pictureURLTextField;
    @FXML
    private ProgressBar passwordStrengthBarRed;
    @FXML
    private ProgressBar passwordStrengthBarYellow;
    @FXML
    private ProgressBar passwordStrengthBarGreen;
    @FXML
    private Button pictureButton;

    @FXML
    private Text firstNameTriangle;
    @FXML
    private Text lastNameTriangle;
    @FXML
    private Text emailTriangle;
    @FXML
    private Text passwordTriangle;
    @FXML
    private Text confirmPasswordTriangle;
    @FXML
    private Text usernameTriangle;
    @FXML
    private Text urlTriangle;

    public void setUtilizatorService(UtilizatorService userService, PrietenieService friendshipService, MessageService messageService
            , EventService eventService, Stage stage, String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        rememberMe();
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.messageService = messageService;
        this.eventService = eventService;
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        textInvalidUsernamePassword.setText("");
        createAccountPane.setVisible(false);
        passwordTextField.textProperty().addListener(x->showPasswordStrength());

        firstNameTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateFirstName();
            }
        });
        lastNameTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateLastName();
            }
        });
        usernameTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateUsername();
            }
        });
        emailTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateEmail();
            }
        });
        confirmPasswordTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateConfirmPassword();
            }
        });
        pictureURLTextField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue)
            {
                if (!newPropertyValue)
                    handleValidateUrl();
            }
        });

    }

    private void showPasswordStrength(){
        int passwordLength = passwordTextField.getText().length();
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
            if(checkPasswordAccurance())
                confirmPasswordTriangle.setVisible(false);
        }
        if(passwordLength > 7) {
            passwordStrengthBarYellow.setVisible(false);
            passwordStrengthBarRed.setVisible(false);
            passwordStrengthBarGreen.setVisible(true);
            if(checkPasswordAccurance())
                confirmPasswordTriangle.setVisible(false);
        }
    }

    @FXML
    public void handleCreateAccount() {
        if (handleValidateFirstName() && handleValidateLastName() && handleValidateUsername() && handleValidateEmail()
                && handleValidateConfirmPassword() && checkPasswordAccurance() && handleValidateUrl()){
            try {
                if(pictureURLTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "").equals(""))
                    userService.addUtilizator(firstNameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), lastNameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), usernameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), passwordTextField.getText(), emailTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), "E:\\MAP\\SocialNetwork\\src\\main\\resources\\images\\defaultUser.jpg");
                else
                    userService.addUtilizator(firstNameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), lastNameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), usernameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), passwordTextField.getText(), emailTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""), pictureURLTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""));
                firstNameTextField.setText("");
                lastNameTextField.setText("");
                passwordTextField.setText("");
                confirmPasswordTextField.setText("");
                emailTextField.setText("");
                usernameTextField.setText("");
                pictureURLTextField.setText("");
                passwordStrengthBarRed.setVisible(false);
                passwordStrengthBarGreen.setVisible(false);
                passwordStrengthBarYellow.setVisible(false);
            } catch (ServiceException s) {

            }
        }
    }

//    @FXML
//    public void firstNameTriangleDisappear(){ firstNameTriangle.setVisible(false); }
//    @FXML
//    public void lastNameTriangleDisappear(){ lastNameTriangle.setVisible(false); }
//    @FXML
//    public void emailTriangleDisappear(){ emailTriangle.setVisible(false); }
//    @FXML
//    public void passwordTriangleDisappear(){ passwordTriangle.setVisible(false); }
//    @FXML
//    public void confirmPasswordTriangleDisappear(){ confirmPasswordTriangle.setVisible(false); }
//    @FXML
//    public void usernameTriangleDisappear(){ usernameTriangle.setVisible(false); }
//    @FXML
//    public void urlTriangleDisappear(){ urlTriangle.setVisible(false); }

    @FXML
    public boolean handleValidateFirstName(){
        String str = firstNameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.length() < 3 || str.length() > 30) {
//            firstNameTriangle.setVisible(true);
            firstNameTextField.setStyle("-fx-border-color: red");
            return false;
        }
        firstNameTextField.setStyle("-fx-border-color: transparent");
        return true;
    }
    @FXML
    public boolean handleValidateLastName(){
        String str = lastNameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.length() < 3 || str.length() > 30) {
//            lastNameTriangle.setVisible(true);
            lastNameTextField.setStyle("-fx-border-color: red");
            return false;
        }
        lastNameTextField.setStyle("-fx-border-color: transparent");
        return true;
    }
    @FXML
    public boolean handleValidateUsername(){
        String str = usernameTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.length() < 3 || str.length() > 50) {
//            usernameTriangle.setVisible(true);
            usernameTextField.setStyle("-fx-border-color: red");
            return false;
        }
        usernameTextField.setStyle("-fx-border-color: transparent");
        return true;
    }
    @FXML
    public boolean handleValidateEmail(){
        String str = emailTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(!str.endsWith("@yahoo.com") && !str.endsWith("@gmail.com") && !str.endsWith("@scs.ubbcluj.ro") && !str.endsWith("@cs.ubbcluj.ro")) {
//            emailTriangle.setVisible(true);
            emailTextField.setStyle("-fx-border-color: red");
            return false;
        }
        emailTextField.setStyle("-fx-border-color: transparent");
        return true;
    }
    @FXML
    public boolean handleValidateConfirmPassword(){
        if(!passwordTextField.getText().equals(confirmPasswordTextField.getText()) || passwordTextField.getText().length() < 5 || passwordTextField.getText().length() > 50) {
//            confirmPasswordTriangle.setVisible(true);
            confirmPasswordTextField.setStyle("-fx-border-color: red");
            return false;
        }
        confirmPasswordTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    @FXML
    public boolean handleValidateUrl() {
        String str = pictureURLTextField.getText().replaceAll("^[ \t]+|[ \t]+$", "");
        if(str.equals(""))
            return true;
        try {
            FileInputStream input = new FileInputStream(str);
        } catch (IOException | SecurityException e) {
//            urlTriangle.setVisible(true);
            pictureURLTextField.setStyle("-fx-border-color: red");
            urlRestrictions.setText("Picture cannot be accessed");
            return false;
        }
        pictureURLTextField.setStyle("-fx-border-color: transparent");
        return true;
    }

    private boolean checkPasswordAccurance(){
        return passwordTextField.getText().equals(confirmPasswordTextField.getText()) && passwordTextField.getText().length() > 4;
    }

    @FXML
    public void handleGoCreateAccount(){ createAccountPane.setVisible(true); }

    @FXML
    public void handleBack(){
        createAccountPane.setVisible(false);
        firstNameTextField.setText("");
        lastNameTextField.setText("");
        emailTextField.setText("");
        passwordTextField.setText("");
        confirmPasswordTextField.setText("");
        usernameTextField.setText("");
        pictureURLTextField.setText("");
        passwordStrengthBarGreen.setVisible(false);
        passwordStrengthBarRed.setVisible(false);
        passwordStrengthBarYellow.setVisible(false);
//        firstNameTriangle.setVisible(false);
//        lastNameTriangle.setVisible(false);
//        emailTriangle.setVisible(false);
        passwordTriangle.setVisible(false);
        confirmPasswordTriangle.setVisible(false);
//        usernameTriangle.setVisible(false);
//        urlTriangle.setVisible(false);
    }



    public void rememberMe() {
       List<String> list = UtilizatorService.rememberMe();
        if(list.size() != 0 ) {
            textFieldEmail.setText(list.get(0));
            textFieldPassword.setText(list.get(1));
            if (!list.get(0).equals(""))
                rememberMeCheck.setSelected(true);
        }
    }

    @FXML
    public void handleChangeColorBlue(){ forgotPasswordButton.setStyle("-fx-text-fill: #014bac;-fx-background-color: transparent;"); }

    @FXML
    public void handleChangeColorWhite(){ forgotPasswordButton.setStyle("-fx-text-fill: white;-fx-background-color: transparent;"); }

    public void handleRememberMe() {
        UtilizatorService.handleRememberMe(rememberMeCheck.isSelected(), textFieldEmail.getText(),textFieldPassword.getText());
    }

    @FXML
    private void handleChoosePicture(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter extFilter1 = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilter3 = new FileChooser.ExtensionFilter("JPEG files (*.jpeg)", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter1);
        fileChooser.getExtensionFilters().add(extFilter2);
        fileChooser.getExtensionFilters().add(extFilter3);
        File file = fileChooser.showOpenDialog(stage);
        if(file != null)
            pictureURLTextField.setText(file.getAbsolutePath());
    }

    @FXML
    public void handleSignIn(ActionEvent ev){
        String email = textFieldEmail.getText();
        String password = textFieldPassword.getText();
        try {
            Utilizator user = userService.logIn(email, password);
            friendshipService.setCurrentUser(user);
            messageService.setCurrentUser(user);
            eventService.setCurrentUser(user);
            handleRememberMe();
            this.textInvalidUsernamePassword.setText("");
            showSignInDialog(user);
            this.stage.close();
        }catch (ServiceException s) {
            textInvalidUsernamePassword.setText("Invalid email or password");
        }
    }


    public void showSignInDialog(Utilizator user) {
        try{
            //setup
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/sceneBuilder/userPage.fxml"));
            AnchorPane root = (AnchorPane)loader.load();

            //stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("UserPage");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            CurrentUserController currentUserController = loader.getController();
            currentUserController.setService(userService, friendshipService, messageService, eventService, dialogStage, user, stage);
            dialogStage.show();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private Label firstNameRestrictions;
    @FXML
    public void showFirstNameRestrictions(){
        firstNameRestrictions.setVisible(true);
    }
    @FXML
    public void dontShowFirstNameRestrictions(){
        firstNameRestrictions.setVisible(false);
    }
    @FXML
    private Label lastNameRestrictions;
    @FXML
    public void showLastNameRestrictions(){
        lastNameRestrictions.setVisible(true);
    }
    @FXML
    public void dontShowLastNameRestrictions(){
        lastNameRestrictions.setVisible(false);
    }
    @FXML
    private Label emailRestrictions;
    @FXML
    public void showEmailRestrictions(){
        emailRestrictions.setVisible(true);
    }
    @FXML
    public void dontShowEmailRestrictions(){
        emailRestrictions.setVisible(false);
    }
    @FXML
    private Label usernameRestrictions;
    @FXML
    public void showUsernameRestrictions(){
        usernameRestrictions.setVisible(true);
    }
    @FXML
    public void dontShowUsernameRestrictions(){
        usernameRestrictions.setVisible(false);
    }
    @FXML
    private Label confirmPasswordRestrictions;
    @FXML
    public void showConfirmPasswordRestrictions(){
        confirmPasswordRestrictions.setVisible(true);
    }
    @FXML
    public void dontShowConfirmPasswordRestrictions(){
        confirmPasswordRestrictions.setVisible(false);
    }
    @FXML
    private Label urlRestrictions;
    @FXML
    public void showUrlRestrictions(){
        urlRestrictions.setVisible(true);
    }
    @FXML
    public void dontShowUrlRestrictions(){
        urlRestrictions.setVisible(false);
    }

    @FXML
    public void handleGoForgotPassword(){
        forgotPasswordPane.setVisible(true);
    }

    @FXML
    private AnchorPane forgotPasswordPane;
    @FXML
    private TextField forgotPasswordEmailTextField;
    @FXML
    private Text resetPasswordText;
    @FXML
    private TextField newPasswordTextField;

    @FXML
    public void handleBackForgotPassword(){
        forgotPasswordPane.setVisible(false);
        forgotPasswordEmailTextField.setText("");
        newPasswordTextField.setText("");
    }

    @FXML
    public void handleResetPassword(){
        Utilizator user = userService.findUserByEmail(forgotPasswordEmailTextField.getText().replaceAll("^[ \t]+|[ \t]+$", ""));
        if(user == null) {
            resetPasswordText.setText("No account with given email");
        }
        else {
            resetPasswordText.setText("");
            String newPassword = userService.getRandomPassword();
            userService.updatePassword(newPassword,user);
            newPasswordTextField.setText(newPassword);
        }
    }
}
