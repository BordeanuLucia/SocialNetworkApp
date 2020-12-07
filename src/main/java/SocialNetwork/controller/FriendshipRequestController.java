package SocialNetwork.controller;

import SocialNetwork.domain.*;
import SocialNetwork.service.PrietenieService;
import SocialNetwork.service.UtilizatorService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import utils.events.MessageTaskChangeEvent;
import utils.observer.Observer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendshipRequestController implements Observer<MessageTaskChangeEvent> {
    @FXML
    TableView<RequestDTO> tableView1;
    @FXML
    TableColumn<RequestDTO, Utilizator> tableColumnUsername1;
    @FXML
    TableColumn<RequestDTO, String> tableColumnDate1;
    @FXML
    TableColumn<RequestDTO, Status> tableColumnStatus1;


    private UtilizatorService userService;
    private PrietenieService friendshipService;
    Stage dialogStage;
    Utilizator currentUser;
    ObservableList<RequestDTO> model = FXCollections.observableArrayList();

    public void setService(UtilizatorService userService, PrietenieService friendshipService, Stage stage, Utilizator u) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        friendshipService.addObserver(this);
        userService.addObserver(this);
        this.dialogStage = stage;
        this.currentUser = u;
        initModel();
    }

    @FXML
    private void initialize() {
        tableColumnUsername1.setCellValueFactory(new PropertyValueFactory<RequestDTO, Utilizator>("user"));
        tableColumnDate1.setCellValueFactory(new PropertyValueFactory<RequestDTO, String>("date"));
        tableColumnStatus1.setCellValueFactory(new PropertyValueFactory<RequestDTO, Status>("status"));
        tableView1.setItems(model);
    }

    private void initModel() {
        Iterable<FriendRequest> requests = friendshipService.getAllRequests();
        List<RequestDTO> requestsList = StreamSupport.stream(requests.spliterator(), false)
                .filter(x -> {
                    if (x.getStatus().equals(Status.PENDING) && x.getId().getRight() == currentUser.getId())
                        return true;
                    return false;
                })
                .map(x->new RequestDTO(userService.findOne(x.getId().getLeft()),x.getDate(),x.getStatus()))
                .collect(Collectors.toList());
        model.setAll(requestsList);
    }


    @FXML
    public void handleRejectRequest() {
        RequestDTO dto = tableView1.getSelectionModel().getSelectedItem();
        if (dto != null) {
            Utilizator selectedUser = dto.getUser();
            friendshipService.respondFriendship(selectedUser.getId(), Status.REJECTED);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Respond", "Request rejected");
        } else {
            MessageAlert.showErrorMessage(null, "A request must be selected");
        }
    }

    @FXML
    public void handleApproveRequest() {
        RequestDTO dto = tableView1.getSelectionModel().getSelectedItem();
        if (dto != null) {
            Utilizator selectedUser = dto.getUser();
            friendshipService.respondFriendship(selectedUser.getId(), Status.APPROVED);
            MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Respond", "Request approved");
        } else {
            MessageAlert.showErrorMessage(null, "A request must be selected");
        }
    }


    @Override
    public void update(MessageTaskChangeEvent messageTaskChangeEvent) {
        initModel();
    }
}
