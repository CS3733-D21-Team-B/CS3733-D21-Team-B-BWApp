package edu.wpi.teamB.views.requestForms;

import com.jfoenix.controls.JFXButton;
import edu.wpi.teamB.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public abstract class DefaultServiceRequestFormController {

    @FXML
    private JFXButton btnSubmit;

    @FXML
    private JFXButton btnCancel;

    @FXML
    private JFXButton btnHelp;

    @FXML
    private JFXButton btnEmergency;

    public void handleButtonAction(ActionEvent actionEvent) {
        JFXButton btn = (JFXButton) actionEvent.getSource();
        switch (btn.getId()) {
            case "btnSubmit":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/menus/serviceRequestMenu.fxml","/edu/wpi/teamB/views/requestForms/formSubmitted.fxml");
                break;
            case "btnCancel":
                SceneSwitcher.goBack(getClass());
                break;
            case "btnHelp":
                break;
            case "btnEmergency":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/menus/serviceRequestMenu.fxml", "/edu/wpi/teamB/views/requestForms/emergencyForm.fxml");
                break;
        }
    }
}
