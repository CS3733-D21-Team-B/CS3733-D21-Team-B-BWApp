package edu.wpi.cs3733.D21.teamB.views.requestForms;

import com.jfoenix.controls.*;
import com.jfoenix.validation.RequiredFieldValidator;
import edu.wpi.cs3733.D21.teamB.App;
import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;
import edu.wpi.cs3733.D21.teamB.entities.requests.LanguageRequest;
import edu.wpi.cs3733.D21.teamB.entities.requests.Request;
import edu.wpi.cs3733.D21.teamB.util.SceneSwitcher;
import edu.wpi.cs3733.D21.teamB.util.AutoCompleteComboBoxListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;

public class LanguageRequestFormController extends DefaultServiceRequestFormController implements Initializable {

    @FXML
    private JFXTextField patientName;

    @FXML
    private JFXComboBox<Label> loc;

    @FXML
    private JFXComboBox<String> language;

    @FXML
    private JFXDatePicker arrivalDate;

    @FXML
    private JFXTimePicker timeForArrival;

    @FXML
    private JFXTextArea message;

    private String id;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        language.setEditable(true);

        language.getItems().add("Chinese");
        language.getItems().add("French");
        language.getItems().add("Russian");
        language.getItems().add("Spanish");
        language.getItems().add("Vietnamese");

        //implement searchable combo box
        language.setVisibleRowCount(5);
        new AutoCompleteComboBoxListener<>(language);

        if (SceneSwitcher.peekLastScene().equals("/edu/wpi/cs3733/D21/teamB/views/menus/serviceRequestDatabase.fxml")) {
            this.id = (String) App.getPrimaryStage().getUserData();
            LanguageRequest languageRequest;
            try {
                languageRequest = (LanguageRequest) DatabaseHandler.getHandler().getSpecificRequestById(id, Request.RequestType.LANGUAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            patientName.setText(languageRequest.getPatientName());
            getLocationIndex(languageRequest.getLocation());
            String date = languageRequest.getArrivalDate();
            LocalDate ld = LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(5, 7)), Integer.parseInt(date.substring(8, 10)));
            arrivalDate.setValue(ld);
            String time = languageRequest.getTimeForArrival();
            LocalTime lt = LocalTime.of(Integer.parseInt(time.substring(0, 2)), Integer.parseInt(time.substring(3, 5)));
            timeForArrival.setValue(lt);
            message.setText(languageRequest.getDescription());

            getLocationIndex(languageRequest.getLocation());
            int indexType = -1;

            switch (languageRequest.getLanguage()) {
                case "Chinese":
                    indexType = 0;
                    break;
                case "French":
                    indexType = 1;
                    break;
                case "Russian":
                    indexType = 2;
                    break;
                case "Spanish":
                    indexType = 3;
                    break;
                case "Vietnamese":
                    indexType = 4;
                    break;
            }
            language.getSelectionModel().select(indexType);
        }
        validateButton();

        //patient name text field
        RequiredFieldValidator validatorName = new RequiredFieldValidator();

        patientName.getValidators().add(validatorName);
        validatorName.setMessage("Please input the patient's name!");

        patientName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                patientName.validate();
            }
        });

        //location combo box
        RequiredFieldValidator validatorLocation = new RequiredFieldValidator();

        loc.getValidators().add(validatorLocation);
        validatorLocation.setMessage("Please select the location!");

        loc.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                loc.validate();
            }
        });

        //language combo box
        RequiredFieldValidator validatorLanguage = new RequiredFieldValidator();

        language.getValidators().add(validatorLanguage);
        validatorLanguage.setMessage("Please select a language!");

        language.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                language.validate();
            }
        });

        //arrival date picker
        RequiredFieldValidator validatorDate = new RequiredFieldValidator();

        arrivalDate.getValidators().add(validatorDate);
        validatorDate.setMessage("Please select a valid date of arrival!");

        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DATE, -1);
        Calendar selectedDate = Calendar.getInstance();

        arrivalDate.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                try {
                    selectedDate.setTime(Date.from(arrivalDate.getValue().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
                    if (selectedDate.compareTo(currentDate) < 0) {
                        arrivalDate.setValue(null);
                    }
                } catch (Exception ignored) {

                }
                arrivalDate.validate();
            }
        });

        //arrival time picker
        RequiredFieldValidator validatorTimeForArrival = new RequiredFieldValidator();

        timeForArrival.getValidators().add(validatorTimeForArrival);
        validatorTimeForArrival.setMessage("Please select the time of arrival!");

        timeForArrival.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                timeForArrival.validate();
            }
        });

        //message text field
        RequiredFieldValidator validatorMessage = new RequiredFieldValidator();

        message.getValidators().add(validatorMessage);
        validatorMessage.setMessage("Please input any additional details or type 'none' !");

        message.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                message.validate();
            }
        });

        Platform.runLater(() -> patientName.requestFocus());
    }

    public void handleButtonAction(ActionEvent e) {
        super.handleButtonAction(e);

        JFXButton btn = (JFXButton) e.getSource();
        if (btn.getId().equals("btnSubmit")) {
            String givenPatientName = patientName.getText();
            String languageChosen = language.getValue();
            String givenArrivalDate = arrivalDate.getValue().toString();
            String givenTimeForArrival = timeForArrival.getValue().toString();

            DateFormat timeFormat = new SimpleDateFormat("HH:mm");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dateInfo = new Date();

            String requestID;
            if (SceneSwitcher.peekLastScene().equals("/edu/wpi/cs3733/D21/teamB/views/menus/serviceRequestDatabase.fxml")) {
                requestID = this.id;
            } else {
                requestID = UUID.randomUUID().toString();
            }

            String time = timeFormat.format(dateInfo); // Stored as HH:MM (24 hour time)
            String date = dateFormat.format(dateInfo); // Stored as YYYY-MM-DD
            String complete = "F";
            String givenDescription = message.getText();

            String employeeName;
            if (SceneSwitcher.peekLastScene().equals("/edu/wpi/cs3733/D21/teamB/views/menus/serviceRequestDatabase.fxml")) {
                try {
                    employeeName = DatabaseHandler.getHandler().getSpecificRequestById(this.id, Request.RequestType.LANGUAGE).getEmployeeName();
                } catch (SQLException err) {
                    err.printStackTrace();
                    return;
                }
            } else {
                employeeName = null;
            }

            LanguageRequest request = new LanguageRequest(languageChosen, givenPatientName, givenArrivalDate, givenTimeForArrival,
                    requestID, time, date, complete, employeeName, getLocation(), givenDescription);

            try {
                if (SceneSwitcher.peekLastScene().equals("/edu/wpi/cs3733/D21/teamB/views/menus/serviceRequestDatabase.fxml")) {
                    DatabaseHandler.getHandler().updateRequest(request);
                } else {
                    DatabaseHandler.getHandler().addRequest(request);
                }
            } catch (SQLException err) {
                err.printStackTrace();
            }
        }
    }

    @FXML
    private void validateButton() {
        btnSubmit.setDisable(
                patientName.getText().isEmpty() || loc.getValue() == null || arrivalDate.getValue() == null || timeForArrival.getValue() == null ||
                        message.getText().isEmpty() || super.validateCommon() || !language.getItems().contains(language.getValue())
        );
    }
}