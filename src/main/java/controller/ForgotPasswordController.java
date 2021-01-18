//package controller;
//
//import domain.Utilizator;
//import service.UtilizatorService;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.TextField;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//
//
//public class ForgotPasswordController {
//    @FXML
//    TextField emailTextField;
//    @FXML
//    Text resetPasswordText1;
//    @FXML
//    Text resetPasswordText2;
//    @FXML
//    Button resetPasswordButton;
//
//    private UtilizatorService userService;
//    Stage dialogStage;
//    Stage previousStage;
//
//    public void setService(UtilizatorService userService, Stage stage, Stage stage1) {
//        this.userService = userService;
//        this.previousStage = stage1;
//        this.dialogStage=stage;
//    }
//
//    @FXML
//    public void handleResetPassword(){
//        Utilizator user = userService.findUserByEmail(emailTextField.getText());
//        if(user == null) {
//            resetPasswordText2.setText("");
//            resetPasswordText1.setText("No account with given email");
//        }
//        else {
//            resetPasswordText1.setText("");
//            String newPassword = userService.getRandomPassword();
//            userService.updatePassword(newPassword,user);
//            resetPasswordText2.setText("Password changed to: " + newPassword);
//        }
//    }
//
//    @FXML
//    public void handleExit(){
//        dialogStage.close();
//        previousStage.show();
//    }
//}
