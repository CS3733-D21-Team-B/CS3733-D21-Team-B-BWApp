package edu.wpi.teamB.views.menus;

import com.jfoenix.controls.JFXButton;
import edu.wpi.teamB.App;
import edu.wpi.teamB.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDirectoryMenuController {

    @FXML
    private JFXButton btnDatabase;
    @FXML
    private JFXButton btnEmergency;
    @FXML
    private JFXButton btnExit;
    @FXML
    private JFXButton btnBack;
    @FXML
    private JFXButton btnMapEditor;


    @FXML
    public void handleButtonAction(ActionEvent actionEvent) throws IOException {
        JFXButton btn = (JFXButton) actionEvent.getSource();
        SceneSwitcher.pushScene("/edu/wpi/teamB/views/menus/adminDirectoryMenu.fxml");

        switch (btn.getId()) {
            case "btnMapEditor":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/mapEditor/editorIntermediateMenu.fxml");
                break;
            case "btnDatabase"://fix file path
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/requestForms/serviceRequestDatabase.fxml");
                break;
            case "btnBack":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/loginPages/adminLogin.fxml");
                break;
            case "btnExit":
                Platform.exit();
                break;
            case "btnEmergency":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/requestForms/emergencyForm.fxml");
                break;

        }

    }
}