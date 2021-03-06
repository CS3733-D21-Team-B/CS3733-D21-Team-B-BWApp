package edu.wpi.cs3733.D21.teamB.views.login;

import com.jfoenix.controls.JFXButton;
import edu.wpi.cs3733.D21.teamB.util.SceneSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseInitController implements Initializable {

    @FXML
    public ImageView gif;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream inputStream = getClass().getResourceAsStream("/edu/wpi/cs3733/D21/teamB/images/login/databaseInit.gif");
        assert inputStream != null;
        Image image = new Image(inputStream);
        gif.setImage(image);
    }

    @FXML
    public void handleButtonAction(ActionEvent e) {
        final String currentPath = "/edu/wpi/cs3733/D21/teamB/views/login/mainPage.fxml";
        JFXButton btn = (JFXButton) e.getSource();
        switch (btn.getId()) {
            case "btnBack":
                SceneSwitcher.goBack(1);
                break;
            case "btnExit":
                Platform.exit();
                break;
            case "btnStaff":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/login/loginPage.fxml");
                break;
            case "btnGuest":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/menus/userDirectoryMenu.fxml");
                break;
            case "btnEmergency":
                SceneSwitcher.switchScene(currentPath, "/edu/wpi/cs3733/D21/teamB/views/requestForms/emergencyForm.fxml");
                break;
        }
    }
}
