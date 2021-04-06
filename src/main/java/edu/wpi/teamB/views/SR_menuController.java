package edu.wpi.teamB.views;

import com.jfoenix.controls.JFXButton;
import edu.wpi.teamB.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;

import java.io.IOException;

public class SR_menuController {
    @FXML
    private JFXButton btnMedicine;

    @FXML
    private JFXButton btnSecurity;

    @FXML
    private JFXButton btnSanitation;

    @FXML
    private void handleButtonAction(ActionEvent e) {

        Button btn = (Button) e.getSource();

        switch (btn.getId()) {
            case "btnMedicine":
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/edu/wpi/teamB/views/Med_Delivery_Form.fxml"));
                    App.getPrimaryStage().getScene().setRoot(root);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }
            case "btnSecurity":
                // Open security service request form
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/edu/wpi/teamB/views/securityRequestForm.fxml"));
                    App.getPrimaryStage().getScene().setRoot(root);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    break;
                }
            case "btnSanitation":
                try{
                    Parent root = FXMLLoader.load(getClass().getResource("/edu/wpi/teamB/views/sanitationRequestForm.fxml"));
                    App.getPrimaryStage().getScene().setRoot(root);
                    break;
                } catch(IOException ex){
                    ex.printStackTrace();
                    break;
                }


        }
    }
}