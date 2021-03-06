package edu.wpi.cs3733.D21.teamB.views.covidSurvey;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;
import edu.wpi.cs3733.D21.teamB.entities.User;
import edu.wpi.cs3733.D21.teamB.entities.requests.CovidSurveyRequest;
import edu.wpi.cs3733.D21.teamB.entities.requests.Request;
import edu.wpi.cs3733.D21.teamB.util.SceneSwitcher;
import edu.wpi.cs3733.D21.teamB.views.requestForms.DefaultServiceRequestFormController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;

public class CovidSurveyController extends DefaultServiceRequestFormController implements Initializable {

    @FXML
    private JFXButton btnSubmit;

    @FXML
    private JFXButton btnBack;

    @FXML
    private JFXCheckBox chkFever;

    @FXML
    private JFXCheckBox chkChills;

    @FXML
    private JFXCheckBox chkCough;

    @FXML
    private JFXCheckBox chkShortBreath;

    @FXML
    private JFXCheckBox chkSoreTht;

    @FXML
    private JFXCheckBox chkHeadache;

    @FXML
    private JFXCheckBox chkAches;

    @FXML
    private JFXCheckBox chkNose;

    @FXML
    private JFXCheckBox chkLostTaste;

    @FXML
    private JFXCheckBox chkNausea;

    @FXML
    private JFXCheckBox chkNone;

    @FXML
    private JFXButton btnExit;

    @FXML
    private JFXRadioButton btnCCYes;

    @FXML
    private JFXRadioButton btnCCNo;

    @FXML
    private JFXRadioButton btnTestYes;

    @FXML
    private JFXRadioButton btnTestNo;

    @FXML
    private ToggleGroup ccGroup;

    @FXML
    private ToggleGroup testGroup;

    @FXML
    private StackPane stackPane;

    @FXML
    private JFXTextField txtName;


    //State (per-view)
    private CovidSurveyRequest request;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        super.initialize(location, resources);
        btnCCYes.setToggleGroup(ccGroup);
        btnCCNo.setToggleGroup(ccGroup);
        btnTestYes.setToggleGroup(testGroup);
        btnTestNo.setToggleGroup(testGroup);

        //check none all uncheck
        chkNone.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkFever.setSelected(false);
                chkChills.setSelected(false);
                chkCough.setSelected(false);
                chkShortBreath.setSelected(false);
                chkSoreTht.setSelected(false);
                chkHeadache.setSelected(false);
                chkAches.setSelected(false);
                chkNose.setSelected(false);
                chkLostTaste.setSelected(false);
                chkNausea.setSelected(false);
            }
        });

        //check something none unchecks
        uncheckNone(chkFever, chkChills, chkCough, chkShortBreath, chkSoreTht, chkHeadache, chkAches, chkNose, chkLostTaste, chkNausea);


        request = null;
        try {
            for (Request r : DatabaseHandler.getHandler().getRequests().values()) {
                if (r.getRequestType().equals(Request.RequestType.COVID)) {
                    CovidSurveyRequest cr = (CovidSurveyRequest) DatabaseHandler.getHandler().getSpecificRequestById(r.getRequestID(), Request.RequestType.COVID);
                    if (cr.getSubmitter().equals(DatabaseHandler.getHandler().getAuthenticationUser().getUsername())) {
                        request = cr;
                    } else if (DatabaseHandler.getHandler().getAuthenticationUser().getAuthenticationLevel().equals(User.AuthenticationLevel.GUEST)) {
                        if (r.getSubmitter().equals("null")) {
                            request = cr;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (request != null) {
            getLocationIndex(request.getLocation());
            chkFever.setSelected(request.getSymptomFever().equals("T"));
            chkChills.setSelected(request.getSymptomChills().equals("T"));
            chkCough.setSelected(request.getSymptomCough().equals("T"));
            chkShortBreath.setSelected(request.getSymptomShortBreath().equals("T"));
            chkSoreTht.setSelected(request.getSymptomSoreTht().equals("T"));
            chkHeadache.setSelected(request.getSymptomHeadache().equals("T"));
            chkAches.setSelected(request.getSymptomAches().equals("T"));
            chkNose.setSelected(request.getSymptomNose().equals("T"));
            chkLostTaste.setSelected(request.getSymptomLostTaste().equals("T"));
            chkNausea.setSelected(request.getSymptomNausea().equals("T"));
            chkNone.setSelected(request.getSymptomNone().equals("T"));
            btnCCYes.setSelected(request.getHadCloseContact().equals("T"));
            btnCCNo.setSelected(!request.getHadCloseContact().equals("T"));
            btnTestYes.setSelected(request.getHadPositiveTest().equals("T"));
            btnTestNo.setSelected(!request.getHadPositiveTest().equals("T"));
            txtName.setText(request.getName());
        }

        this.validateButton();
    }

    

    private void uncheckNone(JFXCheckBox chkFever, JFXCheckBox chkChills, JFXCheckBox chkCough, JFXCheckBox chkShortBreath, JFXCheckBox chkSoreTht,   JFXCheckBox chkHeadache, JFXCheckBox chkAches, JFXCheckBox chkNose, JFXCheckBox chkLostTaste, JFXCheckBox chkNausea) {

        chkFever.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });

        chkChills.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });

        chkCough.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });

        chkShortBreath.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });

        chkSoreTht.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });

        chkHeadache.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });
        chkAches.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });
        chkNose.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });
        chkLostTaste.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });
        chkNausea.setOnAction(event -> {
            if(chkNone.isSelected()){
                chkNone.setSelected(false);
            }
        });
    }

    @FXML
    public void handleButtonAction(ActionEvent e) {
        final String currentPath = "/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidSurvey.fxml";
        JFXButton btn = (JFXButton) e.getSource();

        switch (btn.getId()) {
            case "btnSubmit":
                this.handleSubmission();
                SceneSwitcher.switchFromTemp("/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidFormPending.fxml");
                return; // Don't go to form submission view from superclass
            case "btnExit":
                Platform.exit();
                break;
        }
        super.handleButtonAction(e);
    }

    private void handleSubmission() {
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateInfo = new Date();

        String id = UUID.randomUUID().toString();
        if (request != null) {
            id = request.getRequestID();
        }
        String username = DatabaseHandler.getHandler().getAuthenticationUser().getUsername();
        if (username == null) username = "null";

        CovidSurveyRequest newRequest = new CovidSurveyRequest(id,
                timeFormat.format(dateInfo),
                dateFormat.format(dateInfo),
                "F",
                null,
                getLocation(),
                "",
                username,
                txtName.getText(),
                User.CovidStatus.PENDING,
                "F",
                chkFever.isSelected() ? "T" : "F",
                chkChills.isSelected() ? "T" : "F",
                chkCough.isSelected() ? "T" : "F",
                chkShortBreath.isSelected() ? "T" : "F",
                chkSoreTht.isSelected() ? "T" : "F",
                chkHeadache.isSelected() ? "T" : "F",
                chkAches.isSelected() ? "T" : "F",
                chkNose.isSelected() ? "T" : "F",
                chkLostTaste.isSelected() ? "T" : "F",
                chkNausea.isSelected() ? "T" : "F",
                chkNone.isSelected() ? "T" : "F",
                btnCCYes.isSelected() ? "T" : "F",
                btnTestYes.isSelected() ? "T" : "F"
        );

        try {
            if (request == null) {
                DatabaseHandler.getHandler().addRequest(newRequest);
            } else {
                DatabaseHandler.getHandler().updateRequest(newRequest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void validateButton() {
        btnSubmit.setDisable(
                !(btnCCYes.isSelected() || btnCCNo.isSelected()) || !(btnTestYes.isSelected() || btnTestNo.isSelected()) || loc.getValue() == null
        );
    }
}