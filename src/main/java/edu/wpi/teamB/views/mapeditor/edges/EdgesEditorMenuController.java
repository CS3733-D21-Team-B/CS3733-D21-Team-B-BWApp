package edu.wpi.teamB.views.mapeditor.edges;

import com.jfoenix.controls.JFXButton;
import edu.wpi.teamB.database.DatabaseHandler;
import edu.wpi.teamB.entities.Edge;
import edu.wpi.teamB.util.EdgeWrapper;
import edu.wpi.teamB.util.SceneSwitcher;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

@SuppressWarnings("unchecked") // Added so Java doesn't get mad at the raw use of TableView that is necessary
public class EdgesEditorMenuController implements Initializable {

    @FXML
    public JFXButton btnEmergency;

    @FXML
    public JFXButton btnLoad;

    @FXML
    public JFXButton btnSave;

    @FXML
    private JFXButton btnBack;

    @FXML
    private JFXButton btnAddEdge;

    @FXML
    private TableView tblEdges;

    @FXML
    private TableColumn<String, JFXButton> editBtnCol;

    @FXML
    private TableColumn<String, Label> idCol;

    @FXML
    private TableColumn<String, Label> startNodeCol;

    @FXML
    private TableColumn<String, Label> endNodeCol;

    @FXML
    private TableColumn<String, JFXButton> delCol;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Map<String, Edge> edges = null;

        try {
            edges = DatabaseHandler.getDatabaseHandler("main.db").getEdges();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ObservableList<TableColumn<String, Label>> cols = tblEdges.getColumns();
        for (TableColumn<String, Label> c : cols) {
            c.getId();
            switch (c.getId()) {
                case "editBtnCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("btnEdit"));
                    break;
                case "idCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("id"));
                    break;
                case "startNodeCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("startNode"));
                    break;
                case "endNodeCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("endNode"));
                    break;
                case "delCol":
                    c.setCellValueFactory(new PropertyValueFactory<>("btnDel"));
                    break;
            }
        }

        assert edges != null;
        for (Edge e : edges.values()) {
            try {
                tblEdges.getItems().add(new EdgeWrapper(e));
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent e) throws IOException {
        JFXButton btn = (JFXButton) e.getSource();

        switch (btn.getId()) {
            case "btnBack":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/mapeditor/editorIntermediateMenu.fxml");
                break;
            case "btnAddEdge":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/mapeditor/edges/addEdgeMenu.fxml");
                break;
        }
    }
}
