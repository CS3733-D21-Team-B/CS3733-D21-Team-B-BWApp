package edu.wpi.cs3733.D21.teamB.entities.map;

import edu.wpi.cs3733.D21.teamB.App;
import edu.wpi.cs3733.D21.teamB.entities.map.data.*;
import edu.wpi.cs3733.D21.teamB.entities.map.edge.AddEdgePopup;
import edu.wpi.cs3733.D21.teamB.entities.map.edge.DelEdgePopup;
import edu.wpi.cs3733.D21.teamB.entities.map.node.AddNodePopup;
import edu.wpi.cs3733.D21.teamB.entities.map.node.AlignNodePopup;
import edu.wpi.cs3733.D21.teamB.entities.map.node.NodeMenuPopup;
import edu.wpi.cs3733.D21.teamB.util.Popup.PoppableManager;
import edu.wpi.cs3733.D21.teamB.views.map.PathfindingMenuController;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import net.kurobako.gesturefx.GesturePane;

public class MapEditorPopupManager implements PoppableManager {

    private final MapDrawer md;
    private final MapCache mc;

    private final GesturePane gPane;
    private final StackPane mapStack;

    private AddNodePopup anPopup;
    private AddEdgePopup aePopup;
    private DelEdgePopup dePopup;
    private NodeMenuPopup nmPopup;
    private AlignNodePopup alignNodePopup;

    public MapEditorPopupManager(MapDrawer md, MapCache mc, GesturePane gPane, StackPane mapStack) {
        this.md = md;
        this.mc = mc;
        this.gPane = gPane;
        this.mapStack = mapStack;
    }

    /**
     * Show the popup to add a node
     *
     * @param event The mouse event that triggered the popup to be shown.
     */
    public void showAddNodePopup(MouseEvent event) {

        // Coordinates on the map
        double x = event.getX();
        double y = event.getY();


        // Only one window open at a time
        md.removeAllPopups();

        AddNodePopupData data = new AddNodePopupData(
                x * PathfindingMenuController.COORDINATE_SCALE,
                y * PathfindingMenuController.COORDINATE_SCALE,
                md,
                mc,
                gPane);

        anPopup = new AddNodePopup(mapStack, data);

        App.getPrimaryStage().setUserData(anPopup);

        anPopup.show();
    }

    /**
     * Show the popup to add a node
     *
     * @param event The mouse event that triggered the popup to be shown.
     */
    public void showAddEdgePopup(MouseEvent event) {
        // Only one window open at a time
        md.removeAllPopups();
        AddEdgePopupData data = new AddEdgePopupData(mc, md);
        aePopup = new AddEdgePopup(mapStack, data);
        App.getPrimaryStage().setUserData(aePopup);
        Circle c = (Circle) event.getSource();
        if (c.getId().equals(mc.getStartNode().getId())) aePopup.reset();
        else aePopup.show();
    }

    /**
     * Shows the popup to delete an edge.
     *
     * @param start The start node of the edge.
     * @param end   The end node of the edge.
     */
    public void showDelEdgePopup(Node start, Node end, Pane parent, MouseEvent event, Line line) {
        // Make sure there is only one editNodePopup at one time
        md.removeAllPopups();

        DelEdgePopupData delData = new DelEdgePopupData(start, end, gPane, md, mc,
                (int)(event.getX() * PathfindingMenuController.COORDINATE_SCALE),
                (int)(event.getY() * PathfindingMenuController.COORDINATE_SCALE),
                line);

        dePopup = new DelEdgePopup(parent, delData);

        // Pass window data
        App.getPrimaryStage().setUserData(dePopup);

        dePopup.show();
    }

    /**
     * Shows the edit node popup filled in with the information from n.
     *
     * @param n Node that is to be edited.
     */
    public void showEditNodePopup(Node n, MouseEvent event, boolean fromTreeView) {

        // Make sure there is only one editNodePopup at one time
        md.removeAllPopups();

        Circle c;
        if (fromTreeView) c = null;
        else c = (Circle) event.getSource();

        NodeMenuPopupData npData = new NodeMenuPopupData(
                n.getNodeID(),
                n.getXCoord(),
                n.getYCoord(),
                n.getFloor(),
                n.getBuilding(),
                n.getNodeType(),
                n.getLongName(),
                n.getShortName(),
                fromTreeView,
                md,
                c,
                mc,
                mapStack,
                n.getColor()
        );

        nmPopup = new NodeMenuPopup(mapStack, npData, gPane);

        // Data to pass to popup
        App.getPrimaryStage().setUserData(nmPopup);

        nmPopup.show();
    }

    /**
     * Shows the align popup
     *
     * @param mapDrawer the map drawer in stance
     */
    public void showAlignNodePopup(MapDrawer mapDrawer) {
        md.removeAllPopups();

        AlignNodePopupData data = new AlignNodePopupData(gPane, mapDrawer, mapDrawer.getAligned());

        alignNodePopup = new AlignNodePopup(mapStack, data);

        App.getPrimaryStage().setUserData(alignNodePopup);
        alignNodePopup.show();
        alignNodePopup = null;
    }

    /**
     * Hide all popups
     */
    public void removeAllPopups() {
        if (anPopup != null) {
            anPopup.hide();
            anPopup = null;
        }

        if (aePopup != null) {
            aePopup.hide();
            aePopup = null;
        }

        if (nmPopup != null) {
            nmPopup.hide();
            nmPopup = null;
        }

        if (dePopup != null) {
            dePopup.hide();
            dePopup = null;
        }

        if (alignNodePopup != null) {
            alignNodePopup.hide();
            alignNodePopup = null;
        }

    }
}
