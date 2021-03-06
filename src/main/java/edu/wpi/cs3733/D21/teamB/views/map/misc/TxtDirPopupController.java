package edu.wpi.cs3733.D21.teamB.views.map.misc;

import com.jfoenix.controls.JFXButton;
import edu.wpi.cs3733.D21.teamB.App;
import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;
import edu.wpi.cs3733.D21.teamB.entities.User;
import edu.wpi.cs3733.D21.teamB.entities.map.TxtDirPopup;
import edu.wpi.cs3733.D21.teamB.pathfinding.Directions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TxtDirPopupController implements Initializable {

    @FXML
    private JFXButton btnEmail;

    @FXML
    private JFXButton btnRestart;

    @FXML
    private JFXButton btnPrevious;

    @FXML
    private JFXButton btnNext;

    @FXML
    private JFXButton btnClose;

    @FXML
    private VBox textHolder;

    @FXML
    private ScrollPane scrollPane;

    private TxtDirPopup popup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        popup = (TxtDirPopup) App.getPrimaryStage().getUserData();
        popup.setScrollPane(scrollPane);
        popup.setInstructionBox(textHolder);
        popup.updateFloor();

        // Add all the text directions to the holder
        List<Directions.Direction> directions = popup.getDirections();

        // For every instruction:
        for (int i = 0; i < directions.size(); i++) {
            Directions.Direction dir = directions.get(i);
            if (i == 0) {
                HBox first = new HBox();
                first.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                Label firstText = new Label(directions.get(0).getInstruction());
                firstText.setFont(Font.font("MS Reference Sans Serif", FontWeight.BOLD, 16));
                first.getChildren().add(firstText);
                textHolder.getChildren().add(first);
            } else {
                // HBox for each row
                HBox instructionBox = new HBox();
                instructionBox.setSpacing(20);

                // Image in HBox
                instructionBox.getChildren().add(getDirectionImage(dir.getDirection()));
                instructionBox.setAlignment(Pos.CENTER_LEFT);
                // Label in HBox
                Label text = new Label(dir.getInstruction());
                text.setFont(new Font("MS Reference Sans Serif", 13));
                text.setPadding(new Insets(10, 10, 10, 10));
                text.setAlignment(Pos.TOP_LEFT);
                instructionBox.getChildren().add(text);

                // If clicked, update Index
                instructionBox.setOnMouseClicked(e -> {
                    popup.setIndex(textHolder.getChildren().indexOf(instructionBox));
                    if (!popup.getMapCache().getCurrentFloor().equals(popup.getDirections().get(popup.getIndex()).getFloor()))
                        popup.updateFloor();
                    popup.highlight(false);
                });
                textHolder.getChildren().add(instructionBox);
            }
        }

        // Hide email button for text directions if the user is not logged in
        if (!DatabaseHandler.getHandler().getAuthenticationUser().isAtLeast(User.AuthenticationLevel.PATIENT)) {
            btnEmail.setVisible(false);
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent e) {
        JFXButton btn = (JFXButton) e.getSource();
        switch (btn.getId()) {
            case "btnEmail":
                popup.email();
                btnEmail.setStyle("-fx-background-color: #0f9d58");
                btnEmail.setDisable(true);
                break;
            case "btnRestart":
                popup.restart();
                break;
            case "btnPrevious":
                popup.previous();
                break;
            case "btnNext":
                popup.next();
                break;
            case "btnClose":
                popup.close();
                break;
        }
    }

    /**
     * Returns an image based on the direction type
     *
     * @param d the direction type
     * @return the image relating to that direction type
     */
    private VBox getDirectionImage(Directions.DirectionType d) {
        String path;
        switch (d) {
            case STOP:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/stop.png";
                break;
            case STRAIGHT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/straightArrow.png";
                break;
            case TURN_AROUND:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/turnAround.png";
                break;
            case LEFT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/leftArrow.png";
                break;
            case SLIGHT_LEFT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/slightLeftArrow.png";
                break;
            case SHARP_LEFT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/sharpLeftArrow.png";
                break;
            case RIGHT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/rightArrow.png";
                break;
            case SLIGHT_RIGHT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/slightRightArrow.png";
                break;
            case SHARP_RIGHT:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/sharpRightArrow.png";
                break;
            case UP_STAIRS:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/goUpStairs.png";
                break;
            case DOWN_STAIRS:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/goDownStairs.png";
                break;
            case UP_ELEVATOR:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/goUpElevator.png";
                break;
            case DOWN_ELEVATOR:
                path = "/edu/wpi/cs3733/D21/teamB/images/maps/directions/goDownElevator.png";
                break;
            default:
                throw new IllegalStateException("How did we get here?");
        }

        ImageView imageView = new ImageView(path);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(50);

        VBox r = new VBox();
        r.setAlignment(Pos.CENTER);
        r.getChildren().add(imageView);
        return r;
    }
}
