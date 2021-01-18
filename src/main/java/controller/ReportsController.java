//package controller;
//
//import domain.FriendshipDTO;
//import domain.FriendshipType;
//import domain.Message;
//import domain.Utilizator;
//import service.MessageService;
//import service.PrietenieService;
//import service.UtilizatorService;
//import utils.Constants;
//import utils.events.ChangeEventType;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.Paragraph;
//import com.itextpdf.text.pdf.PdfWriter;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.DatePicker;
//import javafx.stage.Stage;
//import utils.events.MessageTaskChangeEvent;
//import utils.observer.Observer;
//
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//
//public class ReportsController implements Observer<MessageTaskChangeEvent> {
//    Stage dialogStage;
//    Utilizator currentUser;
//    UtilizatorService userService;
//    PrietenieService friendshipService;
//    MessageService messageService;
//    @FXML
//    DatePicker datePicker1;
//    @FXML
//    DatePicker datePicker2;
//    @FXML
//    DatePicker datePicker3;
//    @FXML
//    DatePicker datePicker4;
//    @FXML
//    ComboBox<Utilizator> comboBox;
//    ObservableList<Utilizator> model = FXCollections.observableArrayList();
//
//    public void setService(UtilizatorService utilizatorService, PrietenieService prietenieService, MessageService messageService, Stage stage, Utilizator user){
//        this.dialogStage = stage;
//        this.currentUser = user;
//        this.userService = utilizatorService;
//        this.friendshipService = prietenieService;
//        this.messageService = messageService;
//        userService.addObserver(this);
//        initModel();
//    }
//
//    @FXML
//    private void initialize(){
//        comboBox.setItems(model);
//    }
//
//    private void initModel(){
//        model.setAll(StreamSupport.stream(userService.getAll().spliterator(),false).collect(Collectors.toList()));
//    }
//
//    @FXML
//    public void handleReport1(){
//        String text = "";
//        LocalDate localDate1 = datePicker1.getValue();
//        LocalDate localDate2 = datePicker2.getValue();
//        if(localDate1 != null && localDate2 != null) {
//            if(localDate1.isBefore(localDate2) || localDate1.equals(localDate2)) {
//                List<FriendshipDTO> prietenii = friendshipService.friendsBetweenDates(localDate1, localDate2)
//                        .stream().map(x -> {
//                            if (x.getId().getRight() == currentUser.getId())
//                                return new FriendshipDTO(userService.findOne(x.getId().getLeft()), x.getDate(), FriendshipType.FRIENDS,null);
//                            else return new FriendshipDTO(userService.findOne(x.getId().getRight()), x.getDate(), FriendshipType.FRIENDS, null);
//                        })
//                        .collect(Collectors.toList());
//                if (prietenii.size() != 0) {
//                    text = text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
//                            .concat(" ").concat(currentUser.toString()).concat(" s-a imprietenit cu:\n");
//                    for (FriendshipDTO friendshipDTO : prietenii) {
//                        text = text.concat(friendshipDTO.getDate()).concat(" ").concat(friendshipDTO.getUsername()).concat("\n");
//                    }
//                } else {
//                    text = text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
//                            .concat(" ").concat(currentUser.toString()).concat(" nu s-a imprietenit cu nicio persoana.\n");
//                }
//                List<Message> mesaje = messageService.messagesBetweenDates(localDate1, localDate2);
//                if (mesaje.size() != 0) {
//                    text = text.concat("\n").concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
//                            .concat(" ").concat(currentUser.toString()).concat(" a primit urmatoarele mesaje:\n");
//                    for (Message message : mesaje) {
//                        text = text.concat(message.getDate().format(Constants.DATE_TIME_FORMATTER)).concat(" ").concat(message.getFrom().toString()).concat(": ").concat(message.getMessage()).concat("\n");
//                    }
//                } else {
//                    text = text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
//                            .concat(" ").concat(currentUser.toString()).concat(" nu a primit niciun mesaj.\n");
//                }
//
//                Document document = new Document();
//                try {
//                    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("1.pdf"));
//                    document.open();
//                    document.add(new Paragraph(text));
//                    document.close();
//                    writer.close();
//                } catch (DocumentException e) {
//                    e.printStackTrace();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A type 1 pdf report was generated");
//            }
//            else{
//                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "Fierst date must be before second date");
//            }
//        }else{
//            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A date must be selected");
//        }
//    }
//
//    @FXML
//    public void handleReport2(){
//            String text = "";
//            LocalDate localDate1 = datePicker3.getValue();
//            LocalDate localDate2 = datePicker4.getValue();
//            Utilizator user = comboBox.getValue();
//            if(localDate1 != null && localDate2 != null && user != null) {
//                if(localDate1.isBefore(localDate2) || localDate1.equals(localDate2)) {
//                    List<Message> mesaje = messageService.messagesBetweenDatesUser(localDate1, localDate2, user.getId());
//                    if (mesaje.size() != 0) {
//                        text = text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
//                                .concat(" ").concat(currentUser.toString()).concat(" a purtat urmatoarea discutie cu ").concat(user.toString()).concat(":\n");
//                        for (Message message : mesaje) {
//                            if (message.getFrom().equals(currentUser)) {
//                                text = text.concat(message.getDate().format(Constants.DATE_TIME_FORMATTER)).concat(": ").concat(currentUser.toString()).concat(": ").concat(message.getMessage());
//                                if (message.getReply() != -1l)
//                                    text = text.concat(" reply to: ").concat(messageService.findOne(message.getReply()).getMessage());
//                                text = text.concat("\n");
//                            } else {
//                                text = text.concat(message.getDate().format(Constants.DATE_TIME_FORMATTER)).concat(": ").concat(user.toString()).concat(": ").concat(message.getMessage());
//                                if (message.getReply() != -1l)
//                                    text = text.concat(" reply to: ").concat(messageService.findOne(message.getReply()).getMessage());
//                                text = text.concat("\n");
//                            }
//                        }
//                    } else {
//                        text = text.concat("In perioada ").concat(localDate1.toString()).concat(" - ").concat(localDate2.toString())
//                                .concat(" ").concat(currentUser.toString()).concat(" nu a discutat nimic cu ").concat(user.toString()).concat(".\n");
//                    }
//                    Document document = new Document();
//                    try {
//                        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("2.pdf"));
//                        document.open();
//                        document.add(new Paragraph(text));
//
//                        document.close();
//                        writer.close();
//                    } catch (DocumentException e) {
//                        e.printStackTrace();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//
//                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A type 2 pdf report was generated");
//                }
//                else{
//                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "First date must be before second date");
//                }
//            }
//            else{
//                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Message", "A date and a user must be selected");
//            }
//    }
//    @Override
//    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
//        if(messageTaskChangeEvent.getType().equals(ChangeEventType.USER))
//            initModel();
//    }
//}
