package edu.wpi.cs3733.D21.teamB.views;

import com.jfoenix.controls.JFXButton;
import edu.wpi.cs3733.D21.teamB.util.SceneSwitcher;
import edu.wpi.cs3733.D21.teamB.util.tts.TextToSpeech;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class BasePageController implements Initializable {

    public static boolean ttsOn = false;

    @FXML
    private JFXButton btnBack;

    @FXML
    private JFXButton btnExit;

    @FXML
    private StackPane stackPane;

    public TextToSpeech tts = new TextToSpeech();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (Node aNode : stackPane.lookupAll("*")) {
            aNode.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    String speechOut = aNode.getAccessibleText();
                    if (speechOut != null) {
                        if (ttsOn) {
                            tts.speak(speechOut, 1.0f, false, false);
                        }
                    }
                }
            });
        }
    }

    public void handleButtonAction(ActionEvent e) {
        JFXButton btn = (JFXButton) e.getSource();
        switch (btn.getId()) {
            case "btnBack":
                SceneSwitcher.goBack(getClass(), 1);
                break;
            case "btnExit":
                Platform.exit();
                break;
        }
    }


}
