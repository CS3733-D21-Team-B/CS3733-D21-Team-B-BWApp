package edu.wpi.teamB.entities.map.node;

import edu.wpi.teamB.database.DatabaseHandler;
import edu.wpi.teamB.entities.map.data.Node;
import edu.wpi.teamB.entities.map.data.NodeMenuPopupData;
import edu.wpi.teamB.entities.map.data.NodeType;
import edu.wpi.teamB.util.Popup.Popup;
import edu.wpi.teamB.util.Popup.Window;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EditNodeWindow extends Window<VBox, NodeMenuPopupData, VBox> {

    public EditNodeWindow(Pane parent, NodeMenuPopupData data, VBox previous) {
        super(parent, data, previous);
    }

    public void updateNode(int x, int y, String floor, String building, String type, String longName, String shortName){

        // if the node types are different, delete and remake so the nodeID is up to date
        DatabaseHandler db = DatabaseHandler.getDatabaseHandler("main.db");
        if (!data.getNodeType().equals(type)) {
            try {
                db.removeNode(data.getNodeID());
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Figure out what the index should be
            List<Node> nodes = null;
            try {
                nodes = db.getNodesByCategory(NodeType.valueOf(type));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            List<Integer> indexes = new ArrayList<>();
            assert nodes != null;

            nodes.forEach(node -> {
                if (node.getNodeID().startsWith("b"))
                    indexes.add(Integer.parseInt(node.getNodeID().substring(5, 8)));
            });

            Collections.sort(indexes);
            int index = 1;

            for (Integer i : indexes)
                if (i != index++) break;

            String aNodeId = "b" + type + String.format("%3s", index).replace(' ', '0') + String.format("%2s", floor).replace(' ', '0');
            Node node = new Node(aNodeId, x, y, floor, building, type, longName, shortName);
            try {
                db.addNode(node);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            Node node = new Node(
                    data.getNodeID(),
                    x,
                    y,
                    floor,
                    building,
                    type,
                    longName,
                    shortName);

            // Update database and graph
            try {
                db.updateNode(node);
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

        }

        // Remove popup from map and refresh map nodes
        data.getMd().removeAllPopups();
        data.getMd().refreshEditor();
    }

    public void show(){

        VBox nodeEditMenu = null;

        // Load window
        try {
            nodeEditMenu = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getClassLoader().getResource("edu/wpi/teamB/views/map/nodePopup/editNodePopup.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.show(nodeEditMenu);
    }

    @Override
    public void hide() {
        super.hide();
    }
}
