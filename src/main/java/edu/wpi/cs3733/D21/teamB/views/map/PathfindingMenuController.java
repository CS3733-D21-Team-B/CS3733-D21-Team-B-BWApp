package edu.wpi.cs3733.D21.teamB.views.map;

import com.jfoenix.controls.*;
import edu.wpi.cs3733.D21.teamB.App;
import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;
import edu.wpi.cs3733.D21.teamB.entities.User;
import edu.wpi.cs3733.D21.teamB.entities.map.*;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Edge;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Node;
import edu.wpi.cs3733.D21.teamB.entities.map.data.NodeType;

import edu.wpi.cs3733.D21.teamB.entities.requests.Request;
import edu.wpi.cs3733.D21.teamB.pathfinding.Dijkstra;
import edu.wpi.cs3733.D21.teamB.pathfinding.Graph;
import edu.wpi.cs3733.D21.teamB.util.CSVHandler;
import edu.wpi.cs3733.D21.teamB.util.HelpDialog;
import edu.wpi.cs3733.D21.teamB.util.SceneSwitcher;
import edu.wpi.cs3733.D21.teamB.views.BasePageController;
import edu.wpi.cs3733.D21.teamB.views.covidSurvey.CovidSurveyController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import net.kurobako.gesturefx.GesturePane;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathfindingMenuController extends BasePageController implements Initializable {

    @FXML
    private JFXTextField txtStartLocation,
            txtEndLocation,
            txtSearch;

    @FXML
    private AnchorPane nodeHolder,
            intermediateNodeHolder,
            mapHolder;

    @FXML
    private ImageView map;

    @FXML
    private JFXButton btnFindPath,
            btnEditMap,
            btnLoad,
            btnSave,
            btnRemoveStop,
            btnTxtDir,
            btnF3,
            btnF2,
            btnF1,
            btnFL1,
            btnFL2;

    @FXML
    private Label lblError;

    @FXML
    @Getter
    private JFXComboBox<String> comboPathingType;

    @FXML
    private JFXCheckBox btnMobility;

    @FXML
    private JFXButton btnAbout;

    @FXML
    private JFXTreeView<String> treeLocations;

    @FXML
    private TreeItem<String> favorites;

    @FXML
    private StackPane mapStack,
            textDirectionsHolder;

    @FXML
    @Getter
    private StackPane stackPane;

    @FXML
    @Getter
    private GesturePane gpane;

    @FXML
    private JFXTextArea txtAreaStops;

    @FXML
    private Circle pathHead;

    @FXML
    private JFXComboBox<String> findClosestLocation;

    @FXML
    private MapCache mc;

    public static final double COORDINATE_SCALE = 25 / 9.0;
    public static final int MAX_X = 5000;
    public static final int MAX_Y = 3400;

    // Map of category short name to category long name, REST -> Restroom
    private final Map<String, String> categoryNameMap = new HashMap<>();

    // NodeID -> original color of nodes that have benn turned grey
    private final HashMap<String, Color> colors = new HashMap<>();

    private final DatabaseHandler db = DatabaseHandler.getHandler();

    final FileChooser fileChooser = new FileChooser();
    final DirectoryChooser directoryChooser = new DirectoryChooser();

    private final MapCache mapCache = new MapCache();
    private MapDrawer mapDrawer;
    private MapEditorPopupManager mapEditorPopupManager;
    private MapPathPopupManager mapPathPopupManager;
    @Getter
    private FloorSwitcher floorSwitcher;

    private static final Color grey = Color.web("#9A9999");

    private String previousFrom = "";
    private String previousStops = "";
    private String previousTo = "";
    private String previousAlgorithm = "";
    private boolean previousMobility = false;
    private boolean mapInEditMode = false;

    CovidSurveyController covidSurveyController;
    // JavaFX code **************************************************************************************

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        //Add better category names to a hash map
        initCategoriesMap();

        // Disable the find path button
        validateFindPathButton();

        //Adds all the destination names to locationNames and sort the nodes by floor
        mapCache.updateLocations();
        populateTreeView();

        // Class that draws things on the map
        mapDrawer = new MapDrawer(this, mapCache, nodeHolder, mapHolder,
                intermediateNodeHolder, lblError, mapStack, gpane);

        // Class that handles the popups for the map editor
        mapEditorPopupManager = new MapEditorPopupManager(mapDrawer, mapCache, gpane, mapStack);
        mapDrawer.setMapEditorPopupManager(mapEditorPopupManager);

        // Class that handles the popups for the path finding
        mapPathPopupManager = new MapPathPopupManager(mapDrawer, mapCache, txtStartLocation, txtEndLocation, btnRemoveStop, mapStack, gpane, this, nodeHolder, textDirectionsHolder);
        mapDrawer.setMapPathPopupManager(mapPathPopupManager);

        // Set up floor switching
        floorSwitcher = new FloorSwitcher(mapDrawer, mapCache, mapPathPopupManager, map, btnF3, btnF2, btnF1, btnFL1, btnFL2);
        floorSwitcher.switchFloor(FloorSwitcher.floor1ID);
        mapDrawer.setFloorSwitcher(floorSwitcher);

        // Fill in proper fields if the last scene is the covid survey
        checkFromCovidSurvey();

        // Set up map for editing mode if the user is an admin
        if (db.getAuthenticationUser().getAuthenticationLevel().equals(User.AuthenticationLevel.ADMIN))
            initMapForEditing();

        // Set up Load and Save buttons for csv
        btnLoad.setOnAction(event -> loadCSV());
        btnSave.setOnAction(event -> saveCSV());

        // Set up limited mobility toggles
        btnMobility.setOnAction(event -> mapDrawer.setMobility(btnMobility.isSelected()));

        // Disable editing if the user is not an admin
        checkPermissions();

        // Makes sure no illegal characters can't be written in the field
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(" a-zA-Z0-9\\-"))
                txtSearch.setText(newValue.replaceAll("[^ a-zA-Z0-9\\-]", ""));
        });

        // Set up the pathing type combo box
        setUpPathfindingChoices();

        // Set up the find closest location combo box
        setUpClosestLocChoices();

        // Set up control-click functionality for aligning nodes
        gpane.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.CONTROL && !mapDrawer.getAligned().isEmpty()) {
                mapEditorPopupManager.showAlignNodePopup(mapDrawer);
            }
        });

        // Find Closest exit validation if start ID is null
        findClosestLocation.setOnAction(e -> {
            if (txtStartLocation.getText().isEmpty()) {
                lblError.setText("Please choose a starting location");
                lblError.setVisible(true);
            } else {
                lblError.setVisible(false);
                findClosestLocationTo();
            }
        });
    }

    /**
     * Loads node and edges data from csv
     */
    private void loadCSV() {
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
        try {
            db.loadNodesEdges(newNodes, newEdges);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Now delete and refresh the nodes
        nodeHolder.getChildren().remove(mapDrawer.getHead());
        mapDrawer.drawAllElements();
    }

    /**
     * Saves node and edges data to a csv
     */
    private void saveCSV() {
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

    /**
     * Only show edit map buttons if the user has the correct permissions.
     */
    private void checkPermissions() {
        btnEditMap.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
        btnLoad.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
        btnSave.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
        comboPathingType.setVisible(db.getAuthenticationUser().isAtLeast(User.AuthenticationLevel.ADMIN));
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

        if (!selectedItem.getValue().equals("Favorites") && selectedItem.isLeaf()) {
            //Selected item is a valid location

            Node tempLocation = Graph.getGraph().getNodes().get(
                    mapCache.makeLongToIDMap().get(
                            selectedItem.getValue()));

            mapDrawer.removeAllPopups();
            if (mapDrawer.isEditing())
                mapEditorPopupManager.showEditNodePopup(tempLocation, mouseEvent, true);
            else
                mapPathPopupManager.createGraphicalInputPopup(tempLocation);

        } else if (!selectedItem.isLeaf()) {
            mapDrawer.removeAllPopups();
        }

        // Stuff for node coloring
        if (!selectedItem.isLeaf() && !mapDrawer.isEditing()) {

            // Change node color back to original color
            revertAllNodeColors();

            if (selectedItem.getValue().equals("Favorites")) {

                // Put original node color in map and set node color to gray
                colorAllNodesGrey();

                // Change the color of the favorite nodes back to their original color
                // Also change the color of the nodes marked for floor swapping in pathfinding
                revertFavoriteNodeColors();

            } else if (!selectedItem.getValue().equals("Locations") && !selectedItem.getValue().equals("Favorites")) {
                // Put original node color in map and set all node colors to gray
                colorAllNodesGrey();

                // Revert the colors of the nodes that are in the selected category
                revertCategoryNodeColors(selectedItem);
            }

            mapDrawer.redrawNodes();
        }

        validateFindPathButton();
    }

    /**
     * Revert the colors of the nodes that are in the selected category
     *
     * @param selectedItem Selected item in the tree view. Not a leaf
     */
    private void revertCategoryNodeColors(TreeItem<String> selectedItem) {
        String category = selectedItem.getValue();
        Map<String, List<Node>> floorNodes = mapCache.getFloorNodes();
        NodeType nt = NodeType.deprettify(category);

        for (Node node : floorNodes.get(mapCache.getCurrentFloor())) {

            if (!mapCache.getEditedNodes().isEmpty()) {
                // There are nodes on the floor colored for path finding
                for (Node editedNode : mapCache.getEditedNodes()) {

                    if (node.getNodeType().equals(nt.toString()) || editedNode.getNodeID().equals(node.getNodeID())) {

                        if (colors.get(node.getNodeID()) != null) {
                            node.setColor(colors.get(node.getNodeID()));
                            colors.remove(node.getNodeID());

                        }
                    }

                }
            } else {
                // There are no nodes on the floor for path finding
                if (node.getNodeType().equals(nt.toString())) {

                    if (colors.get(node.getNodeID()) != null) {
                        node.setColor(colors.get(node.getNodeID()));
                        colors.remove(node.getNodeID());
                    }
                }
            }
        }
    }

    /**
     * Revert the colors of the favorite nodes to the original colors
     */
    private void revertFavoriteNodeColors() {
        List<String> favorites = null;

        try {
            favorites = DatabaseHandler.getHandler().getFavorites();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Map<String, String> longNames = mapCache.makeLongToIDMap();
        Map<String, List<Node>> floorNodes = mapCache.getFloorNodes();

        // Change the color of the favorite nodes back to their original color
        // Also change the color of the nodes marked for floor swapping in pathfinding
        assert favorites != null;
        for (String location : favorites) {

            String nodeID = longNames.get(location);
            for (Node node : floorNodes.get(mapCache.getCurrentFloor())) {

                if (!mapCache.getEditedNodes().isEmpty()) {
                    // There are colored nodes for swapping floors
                    for (Node editedNode : mapCache.getEditedNodes()) {

                        if (node.getNodeID().equals(nodeID) || editedNode.getNodeID().equals(node.getNodeID())) {

                            if (colors.get(node.getNodeID()) != null) {
                                node.setColor(colors.get(node.getNodeID()));
                                colors.remove(node.getNodeID());

                            }

                        }
                    }
                } else {
                    // There are no colored nodes for swapping floors
                    if (node.getNodeID().equals(nodeID)) {

                        if (colors.get(node.getNodeID()) != null) {
                            node.setColor(colors.get(node.getNodeID()));
                            colors.remove(node.getNodeID());
                        }
                    }
                }

            }
        }
    }

    /**
     * Make all the nodes grey on the map
     */
    private void colorAllNodesGrey() {

        for (Node node : mapCache.getFloorNodes().get(mapCache.getCurrentFloor())) {

            colors.put(node.getNodeID(), node.getColor());
            node.setColor(grey);

        }
    }

    /**
     * Revert the colors of all nodes to their original color
     */
    private void revertAllNodeColors() {
        Map<String, List<Node>> floorNodes = mapCache.getFloorNodes();

        if (!colors.isEmpty()) {

            for (Node node : floorNodes.get(mapCache.getCurrentFloor())) {
                if (colors.containsKey(node.getNodeID())) {
                    node.setColor(colors.get(node.getNodeID()));
                }
            }

            colors.clear();
        }
    }

    /**
     * Input validation for the pathfinding button. Button only enables when both input fields are populated and they
     * are not equal to each other.
     */
    public void validateFindPathButton() {
        btnFindPath.setDisable(mapInEditMode || txtStartLocation.getText().isEmpty() || txtEndLocation.getText().isEmpty() || txtStartLocation.getText().equals(txtEndLocation.getText()));
    }

    /**
     * Button handler for the scene
     *
     * @param e the action event being handled
     */
    @FXML
    public void handleButtonAction(ActionEvent e) {
        JFXButton b = (JFXButton) e.getSource();

        switch (b.getId()) {
            case "btnFindPath":
                if (!previousFrom.equals(txtStartLocation.getText()) || !previousStops.equals(String.join(" ", mapCache.getStopsList())) || !previousTo.equals(txtEndLocation.getText())
                        || !previousAlgorithm.equals(comboPathingType.getSelectionModel().getSelectedItem()) || previousMobility != btnMobility.isSelected()) {
                    mapCache.updateLocations();
                    Map<String, String> longToId = mapCache.makeLongToIDMap();
                    mapPathPopupManager.removeTxtDirPopup();
                    mapDrawer.removeAllEdges();

                    floorSwitcher.switchFloor(DatabaseHandler.getHandler().getNodeById(longToId.get(txtStartLocation.getText())).getFloor());
                    mapDrawer.calculatePath(txtStartLocation.getText(), txtEndLocation.getText());
                    btnTxtDir.setDisable(false);

                    // Set previous ones
                    previousFrom = txtStartLocation.getText();
                    previousStops = String.join(" ", mapCache.getStopsList());
                    previousTo = txtEndLocation.getText();
                    previousAlgorithm = comboPathingType.getSelectionModel().getSelectedItem();
                    previousMobility = btnMobility.isSelected();
                }
                break;
            case "btnEditMap":
                mapPathPopupManager.removeTxtDirPopup();
                mapDrawer.removeAllPopups();
                mapDrawer.removeAllEdges();
                mapInEditMode = !mapInEditMode;
                mapDrawer.setEditing(!mapDrawer.isEditing());
                btnFindPath.setDisable(!btnFindPath.isDisable());

                mapCache.setFinalPath(null);

                mapDrawer.drawAllElements();
                break;
            case "btnF3":
                mapDrawer.removeAllEdges();
                floorSwitcher.switchFloor(FloorSwitcher.floor3ID);
                break;
            case "btnF2":
                mapDrawer.removeAllEdges();
                floorSwitcher.switchFloor(FloorSwitcher.floor2ID);
                break;
            case "btnF1":
                mapDrawer.removeAllEdges();
                floorSwitcher.switchFloor(FloorSwitcher.floor1ID);
                break;
            case "btnFL1":
                mapDrawer.removeAllEdges();
                floorSwitcher.switchFloor(FloorSwitcher.floorL1ID);
                break;
            case "btnFL2":
                mapDrawer.removeAllEdges();
                floorSwitcher.switchFloor(FloorSwitcher.floorL2ID);
                break;
            case "btnRemoveStop":
                mapPathPopupManager.removeTxtDirPopup();
                mapCache.getStopsList().remove(mapCache.getStopsList().size() - 1);
                displayStops(mapCache.getStopsList());

                // Validate button
                btnRemoveStop.setDisable(mapCache.getStopsList().isEmpty());

                break;
            case "btnHelp":
                HelpDialog.loadHelpDialog(stackPane, getHelpDialog());
                break;
            case "btnSearch":
                handleItemSearched();
                break;
            case "btnAbout":
                SceneSwitcher.switchScene("/edu/wpi/cs3733/D21/teamB/views/map/pathfindingMenu.fxml", "/edu/wpi/cs3733/D21/teamB/views/misc/aboutPage.fxml");
                break;
            case "btnBack":
                // Reset all the colors of the nodes
                if (!colors.isEmpty()) {
                    for (String floor : mapCache.getFloorNodes().keySet()) {
                        for (Node n : mapCache.getFloorNodes().get(floor)) {

                            for (String id : colors.keySet()) {
                                colors.get(id);
                                if (n.getNodeID().equals(id))
                                    n.setColor(colors.get(id));
                            }

                        }
                    }
                }

                break;
            case "btnTxtDir":
                mapDrawer.removeAllPopups();
                if (mapCache.getFinalPath() != null) {
                    mapPathPopupManager.createTxtDirPopup(mapCache.getFinalPath());
                }
                break;
        }

        super.handleButtonAction(e);
    }

    /**
     * Display the stops in the textArea
     *
     * @param stopsList List of node longnames
     */
    public void displayStops(List<String> stopsList) {
        StringBuilder stops = new StringBuilder();

        for (String s : stopsList) {
            stops.append(s).append("\n");
        }

        txtAreaStops.setText(stops.toString());
    }

    /**
     * Populates the tree view with nodes and categories
     */
    private void populateTreeView() {
        //Populating TreeView
        TreeItem<String> rootNode = new TreeItem<>("Root");
        treeLocations.setRoot(rootNode);
        treeLocations.setShowRoot(false);

        // Favorites drop down
        favorites = new TreeItem<>("Favorites");
        if (DatabaseHandler.getHandler().getAuthenticationUser().isAtLeast(User.AuthenticationLevel.PATIENT)) {
            favorites.setExpanded(true);
            rootNode.getChildren().add(favorites);
        }

        // Locations drop down
        TreeItem<String> locations = new TreeItem<>("Locations");
        locations.setExpanded(true);
        rootNode.getChildren().add(locations);

        //Adding Categories
        for (String category : mapCache.getCatNameMap().keySet()) {
            TreeItem<String> categoryTreeItem = new TreeItem<>(categoryNameMap.get(category));
            categoryTreeItem.getChildren().addAll(mapCache.getCatNameMap().get(category));
            alphabetize(categoryTreeItem.getChildren());
            locations.getChildren().add(categoryTreeItem);
        }

        alphabetize(locations.getChildren());

        populateFavorites();
    }

    /**
     * alphabetizes the strings to go into the treeview list
     */
    private ObservableList<TreeItem<String>> alphabetize(ObservableList<TreeItem<String>> strings) {
        strings.sort(Comparator.comparing(TreeItem::toString));
        return strings;
    }

    /**
     * Populates the tree view with favorite locations from the database
     */
    public void populateFavorites() {
        try {
            ArrayList<String> savedFavorites = (ArrayList<String>) DatabaseHandler.getHandler().getFavorites();
            for (String favorite : savedFavorites) {
                TreeItem<String> item = new TreeItem<>(favorite);
                favorites.getChildren().add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for favorites
     *
     * @return favorites
     */
    public TreeItem<String> getFavorites() {
        return favorites;
    }

    /**
     * Getter for tree locations
     *
     * @return tree locations
     */
    public JFXTreeView<String> getTreeLocations() {
        return treeLocations;
    }

    /**
     * Shows the add node popup when double clicking on the map.
     */
    private void initMapForEditing() {

        map.setOnMouseClicked(event -> {
            // Show popup on double clicks
            if (event.getClickCount() < 2) return;

            // if in editing mode
            if (mapDrawer.isEditing()) {

                // Only one window open at a time;
                mapDrawer.removeAllPopups();

                mapEditorPopupManager.showAddNodePopup(event);
            }
        });

    }

    /**
     * Shows the help dialog box.
     */
    private String getHelpDialog() {
        String helpText;
        if (!mapDrawer.isEditing() && DatabaseHandler.getHandler().getAuthenticationUser().isAtLeast(User.AuthenticationLevel.PATIENT)) {
            helpText = "Enter your start and end location and any stops graphically or using our menu selector. " +
                    "To use the graphical selection,\nsimply click on the node and click on the set button. " +
                    "To enter a location using the menu, click on the appropriate\ndrop down and choose your location. " +
                    "The node you selected will show up on your map where you can either\nset it to your start or end location or a stop. " +
                    "Once both the start and end nodes are filled in you can press \"Go\" to generate\nyour path. " +
                    "If you want to remove your stops, click on the \"Remove Stop\" button. " +
                    "Favorites can be chosen by clicking\non them in the menu selector and pressing the add button, and removed by pressing the minus button on the node.\n";
        } else if (!mapDrawer.isEditing()) {
            helpText = "Enter your start and end location and any stops graphically or using our menu selector. " +
                    "To use the graphical selection,\nsimply click on the node and click on the set button. " +
                    "To enter a location using the menu, click on the appropriate\ndrop down and choose your location. " +
                    "The node you selected will show up on your map where you can either\nset it to your start or end location or a stop. " +
                    "Once both the start and end nodes are filled in you can press \"Go\" to generate\nyour path. " +
                    "If you want to remove your stops, click on the \"Remove Stop\" button.\n";
        } else {
            helpText = "Double click to add a node. Click on a node or an edge to edit or remove them. To add a new edge click on\n" +
                    "one of the nodes, then \"Add Edge\". Click on another node and click \"Yes\" to add the new edge or \"No\" to cancel it.\n" +
                    "If you control-click on several nodes, then release control, a popup appears to ask if the nodes should be aligned.\n" +
                    "If you select \"Yes\", the nodes will be aligned according to the line of best fit; otherwise, nothing will occur.\n" +
                    "Additionally, if you control-click on an already selected node, it will be deselected.";
        }
        return helpText;
    }

    @FXML
    private void handleKeysPressedSearchBar(KeyEvent e) {
        String regex = "[ a-zA-Z0-9\\-]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(e.getText());
        if (matcher.matches()) handleItemSearched();

        // Check for backspace
        if (e.getCode() == KeyCode.BACK_SPACE) {
            handleItemSearched();
            if (txtSearch.getText().isEmpty() || txtSearch.getText().length() == 1) populateTreeView();
        }
    }

    /**
     * Shows only the nodes similar to what is being searched for
     */
    @FXML
    private void handleItemSearched() {
        //when an item is searched for, have only objects with that phrase populate the treeview
        String searchBar = txtSearch.getText();
        //Populating TreeView
        TreeItem<String> newRoot = new TreeItem<>("Locations");
        newRoot.setExpanded(true);
        treeLocations.setRoot(newRoot);

        //Adding the nodes
        for (Node n : Graph.getGraph().getNodes().values()) {
            if (!n.getNodeType().equals(NodeType.WALK.name()) && !n.getNodeType().equals(NodeType.HALL.name()) && n.getLongName().toLowerCase().contains(searchBar.toLowerCase())) {
                newRoot.getChildren().add(new TreeItem<>(n.getLongName()));
            }
        }
        // If nothing is found, say "None"
        if (newRoot.getChildren().isEmpty()) {
            newRoot.setValue("No results!");
            treeLocations.setShowRoot(true);
        }
        alphabetize(newRoot.getChildren());
    }

    /**
     * Initialize the map of category short names to category long names
     */
    private void initCategoriesMap() {
        categoryNameMap.put("SERV", "Services");
        categoryNameMap.put("REST", "Restrooms");
        categoryNameMap.put("LABS", "Lab Rooms");
        categoryNameMap.put("ELEV", "Elevators");
        categoryNameMap.put("DEPT", "Departments");
        categoryNameMap.put("CONF", "Conference Rooms");
        categoryNameMap.put("INFO", "Information Locations");
        categoryNameMap.put("RETL", "Retail Locations");
        categoryNameMap.put("EXIT", "Entrances");
        categoryNameMap.put("STAI", "Stairs");
        categoryNameMap.put("PARK", "Parking Spots");
    }

    /**
     * Checks if the last scene was the covid survey and fill in proper directions based on results
     */
    private void checkFromCovidSurvey() {
        //test if we came from a failed covid survey
        if (DatabaseHandler.getHandler().getAuthenticationUser().getCovidStatus().equals(User.CovidStatus.DANGEROUS)) {
            List<String> stopsList = mapCache.getStopsList();
            stopsList.add("Emergency Department Entrance");
            displayStops(stopsList);
        }

        Map<String, Request> requests = null;

        try {
            requests = DatabaseHandler.getHandler().getRequests();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (requests != null) {
            for (Request request : requests.values()) {
                if (request.getSubmitter().equals(DatabaseHandler.getHandler().getAuthenticationUser().getUsername())) {
                    txtStartLocation.setText(db.getNodeById(request.getLocation()).getLongName());
                }
            }
        }
    }

    /**
     * Set up the combo boxes to choose between the different pathfinding algorithms
     */
    private void setUpPathfindingChoices() {
        comboPathingType.getItems().add("A*");
        comboPathingType.getItems().add("DFS");
        comboPathingType.getItems().add("BFS");
        comboPathingType.getItems().add("BestFS");
        comboPathingType.getItems().add("Dijkstra");
        comboPathingType.getSelectionModel().select(Graph.getGraph().getPathingTypeIndex());
        comboPathingType.setOnAction(e -> Graph.getGraph().setPathingTypeIndex(comboPathingType.getSelectionModel().getSelectedIndex()));
    }

    /**
     * set up the combo boxes for the different closest locations that we can path find to
     */
    private void setUpClosestLocChoices() {
        findClosestLocation.getItems().add("Restroom");
        findClosestLocation.getItems().add("Food Place");
        findClosestLocation.getItems().add("Service Desk");
        findClosestLocation.getItems().add("Entrance");
    }

    /**
     * finding closest location to the specified category
     */
    @FXML
    private void findClosestLocationTo() {

        String category = findClosestLocation.getSelectionModel().getSelectedItem();
        String startID = mapCache.getMapLongToID().get(txtStartLocation.getText());
        String endID = null;

        if (startID != null) {
            Dijkstra dijkstra = new Dijkstra();
            mapCache.updateLocations();

            switch (category) {
                case "Restroom":

                    List<TreeItem<String>> restrooms = mapCache.getCatNameMap().get("REST");
                    List<String> restroomsList = new ArrayList<>();
                    for (TreeItem<String> restroom : restrooms) {
                        restroomsList.add(mapCache.getMapLongToID().get(restroom.getValue()));
                    }
                    List<String> restroomsPath = dijkstra.findPath(startID, mapDrawer.isMobility(), restroomsList).getPath();
                    if (restroomsPath.isEmpty()) break;
                    endID = restroomsPath.get(restroomsPath.size() - 1);
                    break;

                //all the food places that are not vending machines
                case "Food Place":
                    List<String> foodPlacesList = new ArrayList<>();
                    foodPlacesList.add("ARETL00101");
                    foodPlacesList.add("DRETL00102");
                    foodPlacesList.add("FRETL00201");
                    foodPlacesList.add("HRETL00102");

                    List<String> foodPlacesPath = dijkstra.findPath(startID, mapDrawer.isMobility(), foodPlacesList).getPath();
                    if (foodPlacesPath.isEmpty()) break;
                    endID = foodPlacesPath.get(foodPlacesPath.size() - 1);
                    break;

                //all services desks that are not security desks
                case "Service Desk":
                    List<String> serviceDesksList = new ArrayList<>();
                    serviceDesksList.add("BINFO00102");
                    serviceDesksList.add("BINFO00202");
                    serviceDesksList.add("FINFO00101");
                    serviceDesksList.add("GINFO01902");
                    List<String> serviceDesksPath = dijkstra.findPath(startID, mapDrawer.isMobility(), serviceDesksList).getPath();
                    if (serviceDesksPath.isEmpty()) break;
                    endID = serviceDesksPath.get(serviceDesksPath.size() - 1);
                    break;

                case "Entrance":
                    List<TreeItem<String>> entrances = mapCache.getCatNameMap().get("EXIT");
                    List<String> entrancesList = new ArrayList<>();
                    for (TreeItem<String> entrance : entrances) {
                        entrancesList.add(mapCache.getMapLongToID().get(entrance.getValue()));
                    }
                    List<String> entrancesPath = dijkstra.findPath(startID, mapDrawer.isMobility(), entrancesList).getPath();
                    if (entrancesPath.isEmpty()) break;
                    endID = entrancesPath.get(entrancesPath.size() - 1);
                    break;
            }

            if (endID == null) {
                lblError.setText("Path could not be found between the selected locations!");
                lblError.setVisible(true);
            } else {
                txtEndLocation.setText(Graph.getGraph().getNodes().get(endID).getLongName());
                btnFindPath.setDisable(false);
            }
        }
    }
}
