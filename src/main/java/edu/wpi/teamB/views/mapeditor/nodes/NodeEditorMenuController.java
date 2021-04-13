package edu.wpi.teamB.views.mapeditor.nodes;

import com.jfoenix.controls.JFXButton;
import edu.wpi.teamB.App;
import edu.wpi.teamB.database.DatabaseHandler;
import edu.wpi.teamB.entities.Node;
import edu.wpi.teamB.util.CSVHandler;
import edu.wpi.teamB.util.NodeWrapper;
import edu.wpi.teamB.util.SceneSwitcher;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@SuppressWarnings("unchecked") // Added so Java doesn't get mad at the raw use of TableView that is necessary
public class NodeEditorMenuController implements Initializable {

    @FXML
    public JFXButton btnEmergency;

    @FXML
    public JFXButton btnLoad;

    @FXML
    public JFXButton btnSave;

    @FXML
    private JFXButton btnBack;

    @FXML
    private JFXButton btnAddNode;

    @FXML
    private TableView tblNodes;

    @FXML
    private TableColumn<String, JFXButton> editBtnCol;

    @FXML
    private TableColumn<String, Label> idCol;

    @FXML
    private TableColumn<String, Label> nameCol;

    @FXML
    private TableColumn<String, Label> typeCol;

    @FXML
    private TableColumn<String, Label> edgesCol;

    @FXML
    private TableColumn<String, JFXButton> delCol;

    FileChooser fileChooser = new FileChooser();
    DirectoryChooser directoryChooser = new DirectoryChooser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Map<String, Node> nodes = null;

        try {
            nodes = DatabaseHandler.getDatabaseHandler("main.db").getNodes();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ObservableList<TableColumn<String, Label>> cols = tblNodes.getColumns();
        for (TableColumn<String, Label> c : cols) {
            c.getId();
            switch (c.getId()) {
                case "editBtnCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("btnEdit"));
                    break;
                case "idCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("id"));
                    break;
                case "nameCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("name"));
                    break;
                case "typeCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("type"));
                    break;
                case "edgesCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("edges"));
                    break;
                case "delCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("btnDel"));
                    break;
            }
        }

        assert nodes != null;
        for (Node n : nodes.values()) {
            try {
                tblNodes.getItems().add(new NodeWrapper(n));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Set up Load and Save buttons
        btnLoad.setOnAction(
                event -> {
                    // Get the CSV file and load it
                    Stage stage = App.getPrimaryStage();
                    fileChooser.setTitle("Select Nodes CSV file");
                    fileChooser.setInitialDirectory(new File(new File("").getAbsolutePath()));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file == null) return;

                    List<Node> newNodes = new ArrayList<>();
                    try {
                        newNodes = CSVHandler.loadCSVNodes(file.toPath());
                        DatabaseHandler.getDatabaseHandler("main.db").loadDatabaseNodes(newNodes);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Add them to the refreshed table
                    tblNodes.getItems().clear();
                    for (Node n : newNodes) {
                        try {
                            tblNodes.getItems().add(new NodeWrapper(n));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        btnSave.setOnAction(
                event -> {
                    // Get the CSV directory location
                    Stage stage = App.getPrimaryStage();
                    directoryChooser.setTitle("Select directory to save Nodes CSV file to");
                    directoryChooser.setInitialDirectory(new File(new File("").getAbsolutePath()));
                    File file = directoryChooser.showDialog(stage);
                    if (file == null) return;

                    // Save the current database into that csv folder
                    try {
                        CSVHandler.saveCSVNodes(file.toPath(), false);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @FXML
    private void handleButtonAction(ActionEvent e) throws IOException {
        JFXButton btn = (JFXButton) e.getSource();

        switch (btn.getId()) {
            case "btnBack":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/mapeditor/editorIntermediateMenu.fxml");
                break;
            case "btnAddNode":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/mapeditor/nodes/addNodeMenu.fxml");
                break;
        }
    }
}