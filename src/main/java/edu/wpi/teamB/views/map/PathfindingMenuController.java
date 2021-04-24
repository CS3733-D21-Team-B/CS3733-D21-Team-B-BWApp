package edu.wpi.teamB.views.map;

import com.jfoenix.controls.*;

import edu.wpi.teamB.App;
import edu.wpi.teamB.database.DatabaseHandler;
import edu.wpi.teamB.entities.User;
import edu.wpi.teamB.entities.map.*;
import edu.wpi.teamB.entities.map.data.*;
import edu.wpi.teamB.entities.map.node.AddNodePopup;
import edu.wpi.teamB.pathfinding.Graph;
import edu.wpi.teamB.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import net.kurobako.gesturefx.GesturePane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class PathfindingMenuController implements Initializable {

    @FXML
    private JFXTextField txtStartLocation;

    @FXML
    private JFXTextField txtEndLocation;

    @FXML
    private AnchorPane nodeHolder;

    @FXML
    private AnchorPane intermediateNodeHolder;

    @FXML
    private AnchorPane mapHolder;

    @FXML
    private ImageView map;

    @FXML
    private JFXButton btnFindPath;

    @FXML
    private JFXButton btnBack;

    @FXML
    private JFXButton btnEmergency;

    @FXML
    private JFXButton btnExit;

    @FXML
    private Label lblError;

    @FXML
    private JFXButton btnEditMap;

    @FXML
    private JFXButton btnLoad;

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXTreeView<String> treeLocations;

    @FXML
    private StackPane mapStack;

    @FXML
    private GesturePane gpane;

    @FXML
    private StackPane stackContainer;

    public static final double coordinateScale = 25 / 9.0;

    private VBox addNodePopup;
    private VBox editNodePopup;
    private VBox delEdgePopup;

    private Map<String, String> categoryNameMap = new HashMap<>();

    private final DatabaseHandler db = DatabaseHandler.getDatabaseHandler("main.db");

    private TreeItem<String> selectedLocation;

    final FileChooser fileChooser = new FileChooser();
    final DirectoryChooser directoryChooser = new DirectoryChooser();

    private MapCache mc = new MapCache();;
    private MapDrawer md;
    private MapEditorPopupManager mepm;
    private MapPathPopupManager mppm;

    // JavaFX code **************************************************************************************

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Add better category names to a hash map
        categoryNameMap.put("SERV", "Services");
        categoryNameMap.put("REST", "Restrooms");
        categoryNameMap.put("LABS", "Lab Rooms");
        categoryNameMap.put("ELEV", "Elevators");
        categoryNameMap.put("DEPT", "Departments");
        categoryNameMap.put("CONF", "Conference Rooms");
        categoryNameMap.put("INFO", "Information Locations");
        categoryNameMap.put("RETL", "Retail Locations");
        categoryNameMap.put("BATH", "Bathroom");
        categoryNameMap.put("EXIT", "Entrances");
        categoryNameMap.put("STAI", "Stairs");
        categoryNameMap.put("PARK", "Parking Spots");

        validateFindPathButton();

        //Adds all the destination names to locationNames and sort the nodes by floor
        mc.updateLocations();
        populateTreeView();

        md = new MapDrawer(mc, nodeHolder, mapHolder, intermediateNodeHolder, lblError, mapStack, gpane);

        mepm = new MapEditorPopupManager(md, mc, gpane, mapStack);
        md.setMepm(mepm);

        mppm = new MapPathPopupManager(md, txtStartLocation, txtEndLocation, mapStack, gpane, this);
        md.setMppm(mppm);

        // Draw the nodes on the map
        md.drawNodesOnFloor();


        //test if we came from a failed covid survey
        if (SceneSwitcher.peekLastScene().equals("/edu/wpi/teamB/views/covidSurvey/covidFormSubmittedWithSymp.fxml")) {
            txtEndLocation.setText("Emergency Department Entrance");
            SceneSwitcher.popLastScene();
        }

        initMapForEditing();

        // Set up Load and Save buttons
        btnLoad.setOnAction(
                event -> {
                    // Get the nodes CSV file and load it
                    Stage stage = App.getPrimaryStage();
                    fileChooser.setTitle("Select Nodes CSV file:");
                    fileChooser.setInitialDirectory(new File(new File("").getAbsolutePath()));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
                    File file = fileChooser.showOpenDialog(stage);
                    if (file == null) return;
                    List<Node> newNodes = CSVHandler.loadCSVNodesFromExternalPath(file.toPath());

                    // Get the edges CSV file and load it
                    fileChooser.setTitle("Select Edges CSV file:");
                    fileChooser.setInitialDirectory(new File(new File("").getAbsolutePath()));
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
                    file = fileChooser.showOpenDialog(stage);
                    if (file == null) return;
                    List<Edge> newEdges = CSVHandler.loadCSVEdgesFromExternalPath(file.toPath());

                    // Update the database
                    db.loadNodesEdges(newNodes, newEdges);
                    Graph.getGraph().updateGraph();

                    // Now delete and refresh the nodes
                    md.drawAllElements();
                }
        );

        btnSave.setOnAction(
                event -> {
                    // Get the CSV directory location
                    Stage stage = App.getPrimaryStage();
                    directoryChooser.setTitle("Select directory to save CSV files to:");
                    directoryChooser.setInitialDirectory(new File(new File("").getAbsolutePath()));
                    File file = directoryChooser.showDialog(stage);
                    if (file == null) return;

                    // Save the current database into that folder in CSV files
                    CSVHandler.saveCSVNodes(file.toPath(), false);
                    CSVHandler.saveCSVEdges(file.toPath(), false);
                }
        );

        // Disable editing if the user is not an admin
        checkPermissions();
    }

    /**
     * Only show edit map buttons if the user has the correct permissions.
     */
    private void checkPermissions() {
        btnEditMap.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
        btnLoad.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
        btnSave.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
    }

    /**
     * Event handler for when the tree view is clicked. When clicked it checks the selected location on the tree view
     * and if its a location not a category it finds the node and used the graphical input popup on that node
     *
     * @param mouseEvent the mouse event
     */
    @FXML
    public void handleLocationSelected(MouseEvent mouseEvent) {
        TreeItem<String> selectedItem = treeLocations.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        if (!selectedItem.equals(selectedLocation) && selectedItem.isLeaf()) {
            //Selected item is a valid location

            //For now only work on nodes that are on the first floor until multi-floor pathfinding is added
            Node tempLocation = Graph.getGraph().getNodes().get(
                    mc.makeLongToIDMap().get(
                            selectedItem.getValue()));

            if (tempLocation.getFloor().equals(mc.getCurrentFloor())) {

                if (md.isEditing())
                    mepm.showEditNodePopup(tempLocation, mouseEvent, true);
                else
                    mppm.createGraphicalInputPopup(tempLocation);

            }
        }

        selectedLocation = selectedItem;
        validateFindPathButton();
    }

    /**
     * Input validation for the pathfinding button. Button only enables when both input fields are populated and they
     * are not equal to each other.
     */
    public void validateFindPathButton() {
        btnFindPath.setDisable(txtStartLocation.getText().isEmpty() || txtEndLocation.getText().isEmpty() || txtStartLocation.getText().equals(txtEndLocation.getText()));
    }

    /**
     * Button handler for the scene
     *
     * @param e the action event being handled
     */
    @FXML
    private void handleButtonAction(ActionEvent e) {
        JFXButton b = (JFXButton) e.getSource();

        switch (b.getId()) {
            case "btnFindPath":
                md.removeOldPaths();
                md.drawPath(txtStartLocation.getText(), txtEndLocation.getText());
                break;
            case "btnEditMap":
                ImageView graphic = (ImageView) btnEditMap.getChildrenUnmodifiable().get(1);

                if (!md.isEditing()) {
                    graphic.setImage(new Image("edu/wpi/teamB/images/menus/directionsIcon.png"));
                    md.setEditing(true);
                } else {
                    graphic.setImage(new Image("edu/wpi/teamB/images/menus/wrench.png"));

                    // Remove the add node popup if it is on the map
                    if (addNodePopup != null) {
                        nodeHolder.getChildren().remove(addNodePopup);
                        addNodePopup = null;
                    }

                    // Remove the edit node popup if it is on the map
                    if (editNodePopup != null) {
                        nodeHolder.getChildren().remove(editNodePopup);
                        editNodePopup = null;
                    }

                    md.setEditing(false);
                }
                selectedLocation = null;
                md.drawAllElements();

                break;
            case "btnBack":
                SceneSwitcher.goBack(getClass(), 1);
                break;
            case "btnExit":
                Platform.exit();
                break;
            case "btnEmergency":
                SceneSwitcher.switchScene(getClass(), "/edu/wpi/teamB/views/map/pathfindingMenu.fxml", "/edu/wpi/teamB/views/requestForms/emergencyForm.fxml");
                break;
            case "btnHelp":
                loadHelpDialog();
                break;
        }
    }

    /**
     * Populates the tree view with nodes and categories
     */
    private void populateTreeView(){
        //Populating TreeView
        TreeItem<String> rootNode = new TreeItem<>("Locations");
        rootNode.setExpanded(true);
        treeLocations.setRoot(rootNode);

        //Adding Categories
        for (String category : mc.getCatNameMap().keySet()) {
            TreeItem<String> categoryTreeItem = new TreeItem<>(categoryNameMap.get(category));
            categoryTreeItem.getChildren().addAll(mc.getCatNameMap().get(category));
            rootNode.getChildren().add(categoryTreeItem);
        }
    }

    // Code for graphical map editor *********************************************************************

    /**
     * Shows the add node popup when double clicking on the map.
     */
    private void initMapForEditing() {

        map.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // Show popup on double clicks
                if (event.getClickCount() < 2) return;

                // Coordinates on the map
                double x = event.getX();
                double y = event.getY();

                // if in editing mode
                if (md.isEditing()) {

                    // Only one window open at a time;
                    md.removeAllPopups();

                    mepm.showAddNodePopup(event);

//                    AddNodePopupData anData = new AddNodePopupData(
//                            x * PathfindingMenuController.coordinateScale,
//                            y * PathfindingMenuController.coordinateScale,
//                            mc.getCurrentFloor(),
//                            md,
//                            gpane
//                    );
//
//                    AddNodePopup adp = new AddNodePopup(mapStack, anData);
//
//                    App.getPrimaryStage().setUserData(adp);
//
//                    try {
//                        FXMLLoader.load(Objects.requireNonNull(
//                                getClass().getClassLoader().getResource("edu/wpi/teamB/views/map/nodePopup/addNodePopup.fxml")));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

            }
        });

    }

    // Misc utility methods ****************************************************************************

    private void loadHelpDialog(){
        JFXDialogLayout helpLayout = new JFXDialogLayout();

        Text helpText = new Text();
        if(!md.isEditing()){
            helpText = new Text("Enter your start and end location graphically or using our menu selector. To use the graphical selection,\nsimply click on the node and click on the set button. To enter a location using the menu. Click on the appropriate\ndrop down and choose your location. The node you selected will show up on your map where you can either\nset it to your start or end location. Once both the start and end nodes are filled in you can press \"Go\" to generate your path");
        } else{
            helpText = new Text("Double click to add a node. Click on a node or an edge to edit or remove them. To add a new edge click on\none of the nodes, then add edge, and then start node. Go to the next node in the edge then, add edge, end node,\nand finally add node.");
        }
        helpText.setFont(new Font("MS Reference Sans Serif", 14));

        Label headerLabel = new Label("Help");
        headerLabel.setFont(new Font("MS Reference Sans Serif", 18));

        helpLayout.setHeading(headerLabel);
        helpLayout.setBody(helpText);
        JFXDialog helpWindow = new JFXDialog(stackContainer, helpLayout, JFXDialog.DialogTransition.CENTER);

        JFXButton button = new JFXButton("Close");
        button.setOnAction(event -> helpWindow.close());
        helpLayout.setActions(button);

        helpWindow.show();

    }
}
