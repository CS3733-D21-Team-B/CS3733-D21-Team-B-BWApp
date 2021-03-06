package edu.wpi.cs3733.D21.teamB.views.login;

import com.jfoenix.controls.JFXButton;
import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;
//import edu.wpi.cs3733.D21.teamB.entities.keyboard.TextAreaFocusable;
import edu.wpi.cs3733.D21.teamB.util.SceneSwitcher;
import edu.wpi.cs3733.D21.teamB.views.BasePageController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController extends BasePageController implements Initializable{

    @FXML
    public JFXButton btnLogin;

    @FXML
    public JFXButton btnRegister;

    @FXML
    private JFXButton btnCovid;

    @FXML
    private StackPane stackPane;

    @FXML
    private JFXButton btnSettings;

    @FXML
    public void handleButtonAction(ActionEvent e) {
        final String currentPath = "/edu/wpi/cs3733/D21/teamB/views/login/mainPage.fxml";
        super.handleButtonAction(e);
        JFXButton btn = (JFXButton) e.getSource();
        switch (btn.getId()) {
            case "btnLogin":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/login/loginPage.fxml");
                break;
            case "btnRegister":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/login/registerPage.fxml");
                break;
            case "btnDirections":
                switch (DatabaseHandler.getHandler().getAuthenticationUser().getCovidStatus()) {
                    case UNCHECKED:
                        SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidSurvey.fxml");
                        break;
                    case PENDING:
                        SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidFormPending.fxml");
                        //To insert back target
                        SceneSwitcher.pushPath("/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidSurvey.fxml");
                        break;
                    default:
                        SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/map/pathfindingMenu.fxml");
                        break;
                }
                break;
            case "btnGoogle":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/map/directionsMenu.fxml");
                break;
            case "btnCovid":
                switch (DatabaseHandler.getHandler().getAuthenticationUser().getCovidStatus()) {
                    case UNCHECKED:
                        SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidSurvey.fxml");
                        break;
                    case PENDING:
                        SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidFormPending.fxml");
                        //To insert back target
//                        SceneSwitcher.pushPath("/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidSurvey.fxml");
                        break;
                    default:
                        SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidFormAccepted.fxml");
                        //To insert back target
//                        SceneSwitcher.pushPath("/edu/wpi/cs3733/D21/teamB/views/covidSurvey/covidSurvey.fxml");
                        break;
                }
                break;
            case "btnSettings":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/menus/settingsMenu.fxml");
                break;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
    }
}
