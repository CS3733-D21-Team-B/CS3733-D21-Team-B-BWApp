package edu.wpi.cs3733.D21.teamB.entities.map;

import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;

import edu.wpi.cs3733.D21.teamB.entities.map.data.Edge;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Node;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Path;
import edu.wpi.cs3733.D21.teamB.pathfinding.*;
import edu.wpi.cs3733.D21.teamB.util.Popup.PoppableManager;
import edu.wpi.cs3733.D21.teamB.views.map.PathfindingMenuController;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import net.kurobako.gesturefx.GesturePane;

import java.io.IOException;
import java.util.*;

public class MapDrawer implements PoppableManager {

    private final PathfindingMenuController pfmc;
    private final MapCache mc;
    @Setter
    private MapPathPopupManager mppm;
    @Setter
    private MapEditorPopupManager mepm;
    private final AnchorPane nodeHolder;
    private final AnchorPane mapHolder;
    private final AnchorPane intermediateNodeHolder;
    private final Label lblError;
    private final GesturePane gpane;
    private final StackPane mapStack;
    private ETAPopup etaPopup;
    private final Circle head = new Circle(10);
    private final DatabaseHandler db = DatabaseHandler.getDatabaseHandler("main.db");

    @Getter
    @Setter
    private boolean isEditing = false;

    @Getter
    @Setter
    private boolean mobility = false;

    public MapDrawer(PathfindingMenuController pfmc, MapCache mc, AnchorPane nodeHolder, AnchorPane mapHolder, AnchorPane intermediateNodeHolder,
                     Label lblError, StackPane mapStack, GesturePane gpane) {
        this.pfmc = pfmc;
        this.mc = mc;
        this.nodeHolder = nodeHolder;
        this.mapHolder = mapHolder;
        this.intermediateNodeHolder = intermediateNodeHolder;
        this.lblError = lblError;
        this.mapStack = mapStack;
        this.gpane = gpane;
    }

    /**
     * Draws the path on the map
     */
    public void drawPath(String start, String end) {
        javafx.scene.shape.Path animationPath = new javafx.scene.shape.Path();
        int steps = 0;
        if (!nodeHolder.getChildren().contains(head)) {
            nodeHolder.getChildren().add(head);
        }
        head.setVisible(true);
        head.setFill(Color.valueOf("#0067B1"));
        Graph.getGraph().updateGraph();
        List<String> sl = mc.getStopsList();
        Stack<String> allStops = new Stack<>();
        allStops.push(mc.makeLongToIDMap().get(end));
        for (int i = sl.size() - 1; i >= 0; i--) {
            allStops.push(mc.makeLongToIDMap().get(sl.get(i)));
        }
        allStops.push(mc.makeLongToIDMap().get(start));

        Pathfinder pathfinder;
        switch (pfmc.getComboPathingType().getSelectionModel().getSelectedItem()) {
            case "A*":
                pathfinder = new AStar();
                break;
            case "DFS":
                pathfinder = new DFS();
                break;
            case "BFS":
                pathfinder = new BFS();
                break;
            default:
                throw new IllegalStateException("Extra option in combo box?");
        }
        Path wholePath = pathfinder.findPath(allStops, mobility);

        if (wholePath.getPath().isEmpty()) {
            lblError.setVisible(true);
        } else {

            for (int i = 0; i < wholePath.getPath().size() - 1; i++) {
                steps++;
                placeEdge(Graph.getGraph().getNodes().get(wholePath.getPath().get(i)), Graph.getGraph().getNodes().get(wholePath.getPath().get(i + 1)));
                double x = Graph.getGraph().getNodes().get(wholePath.getPath().get(i)).getXCoord() / PathfindingMenuController.coordinateScale;
                double y = Graph.getGraph().getNodes().get(wholePath.getPath().get(i)).getYCoord() / PathfindingMenuController.coordinateScale;
                if (i == 0) {
                    animationPath.getElements().add(new MoveTo(x, y));
                } else {
                    animationPath.getElements().add(new LineTo(x, y));
                }

            }

            // Animate the last edge
            double x = Graph.getGraph().getNodes().get(wholePath.getPath().get(wholePath.getPath().size() - 1)).getXCoord() / PathfindingMenuController.coordinateScale;
            double y = Graph.getGraph().getNodes().get(wholePath.getPath().get(wholePath.getPath().size() - 1)).getYCoord() / PathfindingMenuController.coordinateScale;
            animationPath.getElements().add(new LineTo(x, y));

            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.millis(steps * 300));
            pathTransition.setNode(head);
            pathTransition.setPath(animationPath);
            pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            pathTransition.setCycleCount(Animation.INDEFINITE);
            pathTransition.setAutoReverse(false);
            pathTransition.play();
        }

        if (etaPopup != null) {
            etaPopup.hide();
            etaPopup = null;
        }

        etaPopup = mppm.createETAPopup(wholePath);
    }

    /**
     * Draws all the nodes on a given floor with the default graphic
     */
    public void drawNodesOnFloor() {
        Map<String, List<Node>> nodes = mc.getFloorNodes();
        // If the floor has no nodes, return
        if (!nodes.containsKey(mc.getCurrentFloor())) return;

        for (Node n : nodes.get(mc.getCurrentFloor())) {
            if (!(n.getNodeType().equals("WALK") || n.getNodeType().equals("HALL"))) {
                placeNode(n);
            }
        }
    }

    /**
     * Draws all the nodes on a given floor with the alternate graphic
     */
    public void drawAltNodesOnFloor() {

        Map<String, Node> nodes = db.getNodes();

        if (nodes.isEmpty()) return;

        for (Node n : nodes.values()) {
            if ((!(n.getNodeType().equals("WALK") || n.getNodeType().equals("HALL"))) &&
                    n.getFloor().equals(mc.getCurrentFloor())) {
                placeAltNode(n);
            }
        }
    }

    /**
     * Draws all the intermediate nodes on a floor
     */
    private void drawIntermediateNodesOnFloor() {
        Map<String, Node> nodes = db.getNodes();

        if (nodes.isEmpty()) return;

        for (Node n : nodes.values()) {
            if ((n.getNodeType().equals("WALK") || n.getNodeType().equals("HALL")) && n.getFloor().equals(mc.getCurrentFloor())) {
                placeIntermediateNode(n);
            }
        }
    }

    /**
     * Draws all edges on a floor
     */
    private void drawEdgesOnFloor() {
        Map<String, Edge> edges = Graph.getGraph().getEdges();
        for (Edge e : edges.values()) {
            Node start = db.getNodeById(e.getStartNodeID());
            Node end = db.getNodeById(e.getEndNodeID());

            if (start.getFloor().equals(mc.getCurrentFloor()) &&
                    end.getFloor().equals(mc.getCurrentFloor())) {
                placeEdge(start, end);
            }
        }
    }

    /**
     * Places an image for a node on the map at the given pixel coordinates.
     *
     * @param n Node object to place on the map
     */
    private void placeNode(Node n) {
        try {

            ImageView i = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/edu/wpi/cs3733/D21/teamB/views/map/misc/node.fxml")));

            Image image = i.getImage();
            PixelReader reader = image.getPixelReader();
            int w = (int) image.getWidth();
            int h = (int) image.getHeight();
            WritableImage wImage = new WritableImage(w, h);
            PixelWriter writer = wImage.getPixelWriter();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color color = reader.getColor(x, y);
                    if (!(color.hashCode() == 0x00000000)) {
                        writer.setColor(x, y, n.getColor());
                    }
                }
            }

            i.setImage(wImage);

            i.setLayoutX((n.getXCoord() / PathfindingMenuController.coordinateScale) - (i.getFitWidth() / 4));
            i.setLayoutY((n.getYCoord() / PathfindingMenuController.coordinateScale) - (i.getFitHeight()));

            i.setId(n.getNodeID() + "Icon");

            // Show graphical input for pathfinding when clicked
            i.setOnMouseClicked((MouseEvent e) -> {
                removeAllPopups();
                mppm.createGraphicalInputPopup(n);
            });

            nodeHolder.getChildren().add(i);
            mc.getNodePlaced().add(i);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Place alternate node type on the map
     *
     * @param n the Node object to place
     */
    private void placeAltNode(Node n) {
        try {
            Circle c = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/edu/wpi/cs3733/D21/teamB/views/map/misc/nodeAlt.fxml")));

            c.setLayoutX((n.getXCoord() / PathfindingMenuController.coordinateScale));
            c.setLayoutY((n.getYCoord() / PathfindingMenuController.coordinateScale));

            c.setId(n.getNodeID() + "Icon");

            c.setOnMouseClicked((MouseEvent e) -> {
                if (mc.getStartNode() != null) {
                    mc.setNewEdgeEnd(n.getNodeID());
                    mepm.showAddEdgePopup(e);
                } else mepm.showEditNodePopup(n, e, false);
            });

            c.setOnMouseEntered(event -> {
                if (isEditing && !(mc.getStartNode() != null && c.getId().equals(mc.getStartNode().getId())))
                    c.setStroke(Color.GREEN);
            });
            c.setOnMouseExited(event -> {
                if (isEditing && !(mc.getStartNode() != null && c.getId().equals(mc.getStartNode().getId())))
                    c.setStroke(Color.BLACK);
            });

            nodeHolder.getChildren().add(c);
            mc.getNodePlaced().add(c);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Places an image for a node on the map at the given pixel coordinates.
     *
     * @param n Node object to place on the map
     */
    public void placeIntermediateNode(Node n) {
        try {
            Circle c = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/edu/wpi/cs3733/D21/teamB/views/map/misc/intermediateNode.fxml")));

            c.setCenterX((n.getXCoord() / PathfindingMenuController.coordinateScale));
            c.setCenterY((n.getYCoord() / PathfindingMenuController.coordinateScale));

            c.setOnMouseClicked(event -> {
                if (mc.getStartNode() != null) {
                    mc.setNewEdgeEnd(n.getNodeID());
                    mepm.showAddEdgePopup(event);
                } else mepm.showEditNodePopup(n, event, false);
            });

            c.setId(n.getNodeID() + "IntIcon");

            c.setOnMouseEntered(event -> {
                if (isEditing && !(mc.getStartNode() != null && c.getId().equals(mc.getStartNode().getId())))
                    c.setStroke(Color.GREEN);
            });
            c.setOnMouseExited(event -> {
                if (isEditing && !(mc.getStartNode() != null && c.getId().equals(mc.getStartNode().getId())))
                    c.setStroke(Color.BLACK);
            });

            intermediateNodeHolder.getChildren().add(c);
            mc.getIntermediateNodePlaced().add(c);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draws an edge between 2 points on the map.
     *
     * @param start start node
     * @param end   end node
     */
    public void placeEdge(Node start, Node end) {
        try {
            Line l = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/edu/wpi/cs3733/D21/teamB/views/map/misc/edge.fxml")));

            l.setStartX(start.getXCoord() / PathfindingMenuController.coordinateScale);
            l.setStartY(start.getYCoord() / PathfindingMenuController.coordinateScale);

            l.setEndX(end.getXCoord() / PathfindingMenuController.coordinateScale);
            l.setEndY(end.getYCoord() / PathfindingMenuController.coordinateScale);

            l.setOnMouseClicked(e -> {
                if (isEditing) {
                    mepm.showDelEdgePopup(start, end, mapStack);
                }
            });

            l.setOnMouseEntered(event -> {
                if (isEditing) l.setStroke(Color.RED);
            });
            l.setOnMouseExited(event -> {
                if (isEditing) l.setStroke(Color.rgb(0, 103, 177));
            });

            l.setId(start.getNodeID() + "_" + end.getNodeID() + "Icon");

            mapHolder.getChildren().add(l);
            mc.getEdgesPlaced().add(l);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the nodes on the map.
     * <p>
     * FOR MAP EDITOR MODE ONLY!!!
     * <p>
     */
    public void refreshEditor() {
        mc.updateLocations();
        removeAllEdges();
        removeIntermediateNodes();
        removeNodes();
        drawEdgesOnFloor();
        drawAltNodesOnFloor();
        drawIntermediateNodesOnFloor();
    }

    /**
     * Draws all the elements of the map base on direction or map edit mode.
     */
    public void drawAllElements() {
        removeAllPopups();

        if (isEditing) {
            removeAllEdges();
            removeNodes();
            removeIntermediateNodes();
            drawEdgesOnFloor();
            drawAltNodesOnFloor();
            drawIntermediateNodesOnFloor();
            redrawHighlightedNode();
        } else {
            mc.updateLocations();
            removeAllEdges();
            removeIntermediateNodes();
            removeNodes();
            drawNodesOnFloor();
        }
    }

    private void redrawHighlightedNode() {
        if (mc.getStartNode() != null) {
            List<javafx.scene.Node> nodes = new ArrayList<>();
            nodes.addAll(mc.getNodePlaced());
            nodes.addAll(mc.getIntermediateNodePlaced());
            for (javafx.scene.Node n : nodes) {
                if (n.getId().equals(mc.getStartNode().getId())) {
                    Circle c = (Circle) n;
                    c.setStroke(Color.RED);
                }
            }
        }
    }

    /**
     * Remove all the popups on the map
     */
    public void removeAllPopups() {
        mepm.removeAllPopups();
        mppm.removeAllPopups();
        head.setVisible(false);
        gpane.setGestureEnabled(true);
    }

    /**
     * Removes any edges drawn on the map
     */
    public void removeAllEdges() {
        mppm.removeETAPopup();
        lblError.setVisible(false);
        for (Line l : mc.getEdgesPlaced())
            mapHolder.getChildren().remove(l);

        mc.setEdgesPlaced(new ArrayList<>());
    }

    /**
     * Removes all nodes from the map
     */
    private void removeNodes() {
        for (javafx.scene.Node n : mc.getNodePlaced())
            nodeHolder.getChildren().remove(n);

        mc.setNodePlaced(new ArrayList<>());
    }

    /**
     * Removes all intermediate nodes from the map
     */
    private void removeIntermediateNodes() {
        for (javafx.scene.Node n : mc.getIntermediateNodePlaced())
            intermediateNodeHolder.getChildren().remove(n);

        mc.setIntermediateNodePlaced(new ArrayList<>());
    }

    /**
     * Redraws all nodes
     */
    public void redrawNodes() {
        removeNodes();
        drawNodesOnFloor();
    }
}
