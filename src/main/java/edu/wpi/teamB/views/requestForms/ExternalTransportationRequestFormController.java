package edu.wpi.teamB.views.requestForms;

import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import edu.wpi.teamB.App;
import edu.wpi.teamB.database.DatabaseHandler;
import edu.wpi.teamB.entities.requests.ExternalTransportRequest;
import edu.wpi.teamB.entities.requests.Request;
import edu.wpi.teamB.util.SceneSwitcher;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;

public class ExternalTransportationRequestFormController extends DefaultServiceRequestFormController implements Initializable {

    @FXML
    private JFXTextField name;

    @FXML
    private JFXComboBox<Label> comboTranspType;

    @FXML
    private JFXTextField destination;

    @FXML
    private JFXTextArea description;

    @FXML
    private JFXTextArea allergies;

    @FXML
    private JFXCheckBox unconscious;

    @FXML
    private JFXCheckBox infectious;

    @FXML
    private JFXCheckBox outNetwork;

    private String id;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        comboTranspType.getItems().add(new Label("Bus"));
        comboTranspType.getItems().add(new Label("Ambulance"));
        comboTranspType.getItems().add(new Label("Helicopter"));

        if (SceneSwitcher.peekLastScene().equals("/edu/wpi/teamB/views/menus/serviceRequestDatabase.fxml")) {
            this.id = (String) App.getPrimaryStage().getUserData();
            ExternalTransportRequest externalTransportRequest = null;
            try {
                externalTransportRequest = (ExternalTransportRequest) DatabaseHandler.getDatabaseHandler("main.db").getSpecificRequestById(id, Request.RequestType.EXTERNAL_TRANSPORT);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            name.setText(externalTransportRequest.getPatientName());
            getLocationIndex(externalTransportRequest.getLocation());
            int index = -1;
            switch (externalTransportRequest.getTransportType()) {
                case "Bus":
                    index = 0;
                    break;
                case "Ambulance":
                    index = 1;
                    break;
                case "Helicopter":
                    index = 2;
                    break;
            }
            comboTranspType.getSelectionModel().select(index);
            destination.setText(externalTransportRequest.getDestination());
            description.setText(externalTransportRequest.getDescription());
            allergies.setText(externalTransportRequest.getPatientAllergies());
            unconscious.setSelected(externalTransportRequest.getUnconscious().equals("T"));
            infectious.setSelected(externalTransportRequest.getInfectious().equals("T"));
            outNetwork.setSelected(externalTransportRequest.getOutNetwork().equals("T"));
        }
        validateButton();

        //creating a pop-up error message when a text field is left empty
        //name text field
        RequiredFieldValidator validatorName = new RequiredFieldValidator();

        name.getValidators().add(validatorName);
        validatorName.setMessage("Please input your name!");

        name.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    name.validate();
                }
            }
        });

        //destination text field
        RequiredFieldValidator validatorDestination = new RequiredFieldValidator();

        destination.getValidators().add(validatorDestination);
        validatorDestination.setMessage("Please input the patient name!");

        destination.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    destination.validate();
                }
            }
        });

        //name text field
        RequiredFieldValidator validatorDescription = new RequiredFieldValidator();

        description.getValidators().add(validatorDescription);
        validatorDescription.setMessage("Please input transportation details");

        description.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    description.validate();
                }
            }
        });

        //allergies text field
        RequiredFieldValidator validatorAllergies = new RequiredFieldValidator();

        allergies.getValidators().add(validatorAllergies);
        validatorAllergies.setMessage("Please input any allergies or write 'none'");

        allergies.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    allergies.validate();
                }
            }
        });
    }

    public void handleButtonAction(ActionEvent actionEvent) {
        super.handleButtonAction(actionEvent);

        JFXButton btn = (JFXButton) actionEvent.getSource();
        if (btn.getId().equals("btnSubmit")) {
            String givenPatientName = name.getText();
            String givenTransportType = comboTranspType.getValue().getText();
            String givenDestination = destination.getText();
            String givenPatientAllergies = allergies.getText();
            String givenOutNetwork = outNetwork.isSelected() ? "T" : "F";
            String givenInfectious = infectious.isSelected() ? "T" : "F";
            String givenUnconscious = unconscious.isSelected() ? "T" : "F";

            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateInfo = new Date();

            String requestID;
            if (SceneSwitcher.peekLastScene().equals("/edu/wpi/teamB/views/menus/serviceRequestDatabase.fxml")) {
                requestID = this.id;
            } else {
                requestID = UUID.randomUUID().toString();
            }

            String time = timeFormat.format(dateInfo); // Stored as HH:MM (24 hour time)
            String date = dateFormat.format(dateInfo); // Stored as YYYY-MM-DD
            String complete = "F";
            String givenDescription = description.getText();

            String employeeName;
            if (SceneSwitcher.peekLastScene().equals("/edu/wpi/teamB/views/menus/serviceRequestDatabase.fxml")) {
                try {
                    employeeName = DatabaseHandler.getDatabaseHandler("main.db").getSpecificRequestById(this.id, Request.RequestType.EXTERNAL_TRANSPORT).getEmployeeName();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                employeeName = null;
            }

            ExternalTransportRequest request = new ExternalTransportRequest(givenPatientName, givenTransportType, givenDestination, givenPatientAllergies, givenOutNetwork, givenInfectious, givenUnconscious,
                    requestID, time, date, complete, employeeName, getLocation(), givenDescription);

            try {
                if (SceneSwitcher.peekLastScene().equals("/edu/wpi/teamB/views/menus/serviceRequestDatabase.fxml"))
                    DatabaseHandler.getDatabaseHandler("main.db").updateRequest(request);
                else DatabaseHandler.getDatabaseHandler("main.db").addRequest(request);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void validateButton() {
        btnSubmit.setDisable(
                name.getText().isEmpty() || loc.getValue() == null || comboTranspType.getValue() == null ||
                        description.getText().isEmpty() || allergies.getText().isEmpty() || destination.getText().isEmpty()
        );
    }
}
