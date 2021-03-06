package edu.wpi.cs3733.D21.teamB;

import edu.wpi.cs3733.D21.teamB.database.DatabaseHandler;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Edge;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Node;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Path;
import edu.wpi.cs3733.D21.teamB.pathfinding.*;
import edu.wpi.cs3733.D21.teamB.util.CSVHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PathfindingTests {

    final AStar aStar = new AStar();

    @BeforeAll
    static void initDB() {
        DatabaseHandler db = DatabaseHandler.getHandler();
        List<Node> nodes = CSVHandler.loadCSVNodes("/edu/wpi/cs3733/D21/teamB/csvFiles/bwBnodes.csv");
        List<Edge> edges = CSVHandler.loadCSVEdges("/edu/wpi/cs3733/D21/teamB/csvFiles/bwBedges.csv");

        try {
            db.loadNodesEdges(nodes, edges);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        Graph.setGraph(db);
    }

    @BeforeEach
    public void fixGraph() {
        Graph.getGraph().updateGraph();
    }

    @Test
    public void adjNodes() {
        //Testing only one adj node
        List<Node> adjNodes = new ArrayList<>();
        Node bWALK00101 = new Node("bWALK00101", 568, 1894, "1", "Parking", "WALK", "Left Parking Lot Walkway", "Llot Walk");
        adjNodes.add(bWALK00101);
        assertEquals(adjNodes, Graph.getGraph().getAdjNodesById("bPARK00101"));
    }

    @Test
    public void testDist() {
        Node start = new Node("bWALK00301", 3, 0, "1", "Parking", "WALK", "Francis Street Vining Street Intersection", "FrancisViningInt");
        Node end = new Node("bEXIT00101", 0, 4, "1", "Parking", "EXIT", "75 Francis Lobby Entrance", "FrancisLobbyEnt");
        assertEquals(5.0, Graph.dist(start, end));
    }

    @Test
    public void testAStar() {
        LinkedList<String> expectedPath = new LinkedList<>();
        expectedPath.add("bPARK00101");
        expectedPath.add("bWALK00101");
        expectedPath.add("bWALK00201");
        expectedPath.add("bWALK00301");
        expectedPath.add("bWALK00401");
        expectedPath.add("bWALK00501");
        expectedPath.add("bWALK00601");
        expectedPath.add("bWALK00701");
        expectedPath.add("bWALK00801");
        expectedPath.add("bWALK01001");
        expectedPath.add("bWALK01101");
        expectedPath.add("bWALK01201");
        expectedPath.add("bWALK01301");
        expectedPath.add("bWALK01401");
        expectedPath.add("bWALK01501");
        expectedPath.add("bWALK01601");
        expectedPath.add("bPARK02501");

        Path path = aStar.findPath("bPARK00101", "bPARK02501", false);
        assertEquals(expectedPath, path.getPath());
    }

    @Test
    public void testMultipleNodePathfinding() {
        Stack<String> nodeList = new Stack<>();
        nodeList.push("bPARK02501");
        nodeList.push("bEXIT00501");
        nodeList.push("bPARK00101");

        LinkedList<String> expectedPath = new LinkedList<>();
        expectedPath.add("bPARK00101");
        expectedPath.add("bWALK00101");
        expectedPath.add("bWALK00201");
        expectedPath.add("bWALK00301");
        expectedPath.add("bWALK00401");
        expectedPath.add("bWALK00501");
        expectedPath.add("bWALK00601");
        expectedPath.add("bWALK00701");
        expectedPath.add("bWALK00801");
        expectedPath.add("bWALK00901");
        expectedPath.add("bEXIT00501");
        expectedPath.add("bWALK00901");
        expectedPath.add("bWALK00801");
        expectedPath.add("bWALK01001");
        expectedPath.add("bWALK01101");
        expectedPath.add("bWALK01201");
        expectedPath.add("bWALK01301");
        expectedPath.add("bWALK01401");
        expectedPath.add("bWALK01501");
        expectedPath.add("bWALK01601");
        expectedPath.add("bPARK02501");


        Path expectedResult = new Path(expectedPath, 2484.102858858675 + 1954.7029936098086);

        Path returnedResult = aStar.findPath(nodeList, false);

        assertEquals(expectedResult, returnedResult);

        Stack<String> longNodesList = new Stack<>();
        longNodesList.push("FDEPT00301");
        longNodesList.push("FSERV00201");
        longNodesList.push("bEXIT00501");
        longNodesList.push("bPARK00101");


        LinkedList<String> expectedLongPath = new LinkedList<>();
        expectedLongPath.add("bPARK00101");
        expectedLongPath.add("bWALK00101");
        expectedLongPath.add("bWALK00201");
        expectedLongPath.add("bWALK00301");
        expectedLongPath.add("bWALK00401");
        expectedLongPath.add("bWALK00501");
        expectedLongPath.add("bWALK00601");
        expectedLongPath.add("bWALK00701");
        expectedLongPath.add("bWALK00801");
        expectedLongPath.add("bWALK00901");
        expectedLongPath.add("bEXIT00501");
        expectedLongPath.add("bEXIT00401");
        expectedLongPath.add("FEXIT00201");
        expectedLongPath.add("FHALL02801");
        expectedLongPath.add("FHALL02201");
        expectedLongPath.add("FHALL02101");
        expectedLongPath.add("FHALL01901");
        expectedLongPath.add("FHALL01601");
        expectedLongPath.add("FHALL01501");
        expectedLongPath.add("FHALL01401");
        expectedLongPath.add("FSERV00201");
        expectedLongPath.add("FHALL01401");
        expectedLongPath.add("FHALL01501");
        expectedLongPath.add("FHALL01601");
        expectedLongPath.add("FHALL03201");
        expectedLongPath.add("FHALL01801");
        expectedLongPath.add("FHALL01701");
        expectedLongPath.add("FDEPT00301");

        Path expectedLongResult = new Path(expectedLongPath, 2484.102858858675 + 848.2401435306502 + 350.0);

        Path returnedLongResult = aStar.findPath(longNodesList, false);

        assertEquals(expectedLongResult, returnedLongResult);


    }

    @Test
    public void testBFS() {
        LinkedList<String> expectedPath = new LinkedList<>();
        expectedPath.add("bPARK00101");
        expectedPath.add("bWALK00101");
        expectedPath.add("bWALK00201");
        expectedPath.add("bWALK00301");
        expectedPath.add("bWALK00401");
        expectedPath.add("bWALK00501");
        expectedPath.add("bWALK00601");
        expectedPath.add("bWALK00701");
        expectedPath.add("bWALK00801");
        expectedPath.add("bWALK01001");
        expectedPath.add("bWALK01101");
        expectedPath.add("bWALK01201");
        expectedPath.add("bWALK01301");
        expectedPath.add("bWALK01401");
        expectedPath.add("bWALK01501");
        expectedPath.add("bWALK01601");
        expectedPath.add("bPARK02501");

        Path path = new BFS().findPath("bPARK00101", "bPARK02501", false);
        assertEquals(expectedPath, path.getPath());
    }

    @Test
    public void testDijkstra() {
        LinkedList<String> expectedPath = new LinkedList<>();
        expectedPath.add("GHALL02801");
        expectedPath.add("GHALL02101");
        expectedPath.add("GHALL02201");
        expectedPath.add("GSERV02301");
        expectedPath.add("GHALL02401");
        expectedPath.add("GELEV00N01");
        expectedPath.add("GELEV00N02");
        expectedPath.add("GDEPT02402");
        expectedPath.add("GHALL01702");
        expectedPath.add("GINFO01902");

        Path path = new Dijkstra().findPath("GHALL02801", "GINFO01902", false);
       assertEquals(expectedPath, path.getPath());
    }

    @Test
    //tested more on the app itself
    public void testBestFS() {
        LinkedList<String> expectedPath = new LinkedList<>();
        expectedPath.add("bEXIT00401");
        expectedPath.add("bEXIT00501");
        expectedPath.add("FEXIT00301");
        expectedPath.add("FDEPT00501");
        expectedPath.add("FHALL03301");
        expectedPath.add("FHALL02701");
        expectedPath.add("FHALL02601");
        expectedPath.add("FHALL03101");
        expectedPath.add("FHALL00201");
        expectedPath.add("FHALL00101");
        expectedPath.add("WHALL00101");
        expectedPath.add("WHALL00201");
        expectedPath.add("ESTAI00101");
        expectedPath.add("EHALL00301");
        expectedPath.add("EHALL00401");
        expectedPath.add("EHALL00501");
        expectedPath.add("EHALL00601");
        expectedPath.add("EHALL00801");
        expectedPath.add("EHALL00901");
        expectedPath.add("EHALL01101");
        expectedPath.add("EHALL01501");
        expectedPath.add("EHALL01601");
        expectedPath.add("EHALL01801");
        expectedPath.add("WELEV00G01");
        expectedPath.add("EHALL01901");
        expectedPath.add("EHALL02001");
        expectedPath.add("EREST00101");

        Path path = new BestFS().findPath("bEXIT00401", "EREST00101", false);
        assertEquals(expectedPath, path.getPath());
    }

    @Test
    public void testGetFloorPathSegment(){
        Path testPath = new Path();

        List<String> testPathList = new ArrayList<>();

        testPathList.add("bPARK00101");
        testPathList.add("bSTAI00101");
        testPathList.add("bWALK00102");

        testPath.setPath(testPathList);

        List<String> expectedPathList = new ArrayList<>();
        expectedPathList.add("bWALK00102");
        assertEquals(expectedPathList, testPath.getFloorPathSegment("2"));
    }
    @Test
    public void testing180Angles() {
        Node a = new Node("bPARK01501", 3159, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b = new Node("bPARK01501", 3159, 2, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c = new Node("bPARK01501", 3159, 3, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double angle180 = Directions.angleBetweenEdges(a, b, c);
        assertEquals(0, angle180, .5);


        Node a1 = new Node("bPARK01501", 3159, 3159, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b1 = new Node("bPARK01501", 5, 3159, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c1 = new Node("bPARK01501", 1, 3159, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        angle180 = Directions.angleBetweenEdges(a1, b1, c1);
        assertEquals(0, angle180, .5);


        Node a2 = new Node("bPARK01501", 5, 5, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b2 = new Node("bPARK01501", 8, 8, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c2 = new Node("bPARK01501", 13, 13, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        angle180 = Directions.angleBetweenEdges(a2, b2, c2);
        assertEquals(0, angle180, .5);
    }

    @Test
    public void testing0Angles() {
        Node a1 = new Node("bPARK01501", 3159, 3159, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b1 = new Node("bPARK01501", 5, 3159, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c1 = new Node("bPARK01501", 1, 3159, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double angle0 = Directions.angleBetweenEdges(c1, b1, a1);
        assertEquals(0, angle0, .5);
    }

    @Test
    public void testing90and270Angles() {

        //is 270 taking a left
        Node a1 = new Node("bPARK01501", 1, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b1 = new Node("bPARK01501", 1, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c1 = new Node("bPARK01501", 0, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double angle270 = Directions.angleBetweenEdges(a1, b1, c1);
        assertEquals(-90, angle270, 0);

        //is 270
        Node a2 = new Node("bPARK01501", 0, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b2 = new Node("bPARK01501", 0, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c2 = new Node("bPARK01501", 1, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double angle270s = Directions.angleBetweenEdges(a2, b2, c2);
        assertEquals(-90, angle270s, 0);


        //take a right
        Node a = new Node("bPARK01501", 0, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b = new Node("bPARK01501", 0, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c = new Node("bPARK01501", 1, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double angle90 = Directions.angleBetweenEdges(a, b, c);
        assertEquals(90, angle90, 0);

        //take a right 90 degrees this wrong
        Node a3 = new Node("bPARK01501", 1, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b3 = new Node("bPARK01501", 1, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c3 = new Node("bPARK01501", 0, 1, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double angle90s = Directions.angleBetweenEdges(a3, b3, c3);
        assertEquals(90, angle90s, 0);

    }

    @Test
    public void testSlightRights(){
        Node a = new Node("bPARK01501", 5, 5, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b = new Node("bPARK01501", 5, 3, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c = new Node("bPARK01501", 4, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double slightRight = Directions.angleBetweenEdges(a, b, c);
        assertEquals(-20, slightRight, 2);
    }

    @Test
    public void testSlightLeft(){
        Node a = new Node("bPARK01501", 0, 0, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node b = new Node("bPARK01501", 0, 2, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");
        Node c = new Node("bPARK01501", 1, 3, "1", "Parking", "PARK", "Right Parking Lot Spot 5", "RLot5");

        double slightRight = Directions.angleBetweenEdges(a, b, c);
    }

//    @Test
    //tested on app
//    public void DijkstraFindPathCat(){
//        List<String> ids = new ArrayList<>();
//        ids.add("AREST00101");
//
//        Path path = new Dijkstra().findPath("bPARK01501", false, ids);
//    }

//    @Test
//    public void testSimplePath() {
//        Path path = AStar.findPath("FDEPT00101", "FSERV00201");
//        List<String> idExpected = new ArrayList<>();
//        idExpected.add("FDEPT00101"); //1617,825
//        idExpected.add("FHALL01301"); //1627,825
//        idExpected.add("FHALL01401"); //1627,1029
//        idExpected.add("FSERV00201"); //1605,1029
//
//        Path pathCheck = new Path();
//        pathCheck.setPath(idExpected);
//        pathCheck.setTotalPathCost(path.getTotalPathCost());
//        List<String> simplePath = Directions.simplifyPath(path);
//        assertEquals(idExpected, simplePath);
//
//        Path path1 = AStar.findPath("FINFO00101", "WELEV00L01");
//        List<String> idExpected1 = new ArrayList<>();
//        idExpected1.add("FINFO00101");
//        idExpected1.add("FHALL02901");
//        idExpected1.add("FHALL02201");
//        idExpected1.add("FHALL00701");
//        idExpected1.add("WELEV00L01");
//
//        Path pathCheck1 = new Path();
//        pathCheck1.setPath(idExpected1);
//        pathCheck1.setTotalPathCost(path1.getTotalPathCost());
//        List<String> simplePath1 = Directions.simplifyPath(pathCheck1);
//        assertEquals(idExpected1, simplePath1);
//    }

    //tested txtDirections by using the UI

}
