package edu.wpi.cs3733.D21.teamB.pathfinding;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Node;
import edu.wpi.cs3733.D21.teamB.entities.map.data.Path;
import edu.wpi.cs3733.D21.teamB.entities.map.Coord;

import java.util.ArrayList;
import java.util.List;

public class Directions {

    /**
     * Takes in two coordinates and returns the distance euclidean distance between them
     * @param a the first coordinate
     * @param b the second coordinate
     * @return the distance between them
     */
    public static double dist(Coord a, Coord b) {
        return Math.sqrt(Math.pow((a.getX() - b.getX()), 2) + Math.pow((a.getY() - b.getY()), 2));
    }

    /**
     * Takes in 3 coordinates and calculates the angle of deviance between the line created by the prev and curr
     * and the line created by the curr and next
     * @param prev the users previous node
     * @param curr the node the user is standing on now
     * @param next the node the user is going to go to
     * @return the angle change from their straight path
     */
    public static double angleBetweenEdges(Node prev, Node curr, Node next) {

        Coord prevC = new Coord(prev.getXCoord(), -prev.getYCoord());
        Coord currC = new Coord(curr.getXCoord(), -curr.getYCoord());
        Coord nextC = new Coord(next.getXCoord(), -next.getYCoord());

        double aX = currC.getX() - prevC.getX();
        double aY = currC.getY() - prevC.getY();
        double firstAngle;

        if(aX != 0){
            firstAngle = java.lang.Math.atan(aY / aX) * (180 / Math.PI);
            if(aX<0){
                firstAngle += 180;
            }
        }
        else{
            if(aY>0){
                firstAngle = 90;
            }
            else{
                firstAngle = -90;
            }
        }

        double bX = nextC.getX() - currC.getX();
        double bY = nextC.getY() - currC.getY();
        double secondAngle;
        if(bX != 0 ){
            secondAngle = java.lang.Math.atan(bY / bX) * (180 / Math.PI);
            if(bX<0){
                secondAngle += 180;
            }
        }
        else{
            if(bY>0){
                secondAngle = 90;
            }
            else{
                secondAngle = -90;
            }
        }

        //deviation of the straight line
        double remainder = (firstAngle - secondAngle)%360.0;

        if(remainder < -180){
            remainder += 360;
        }

        if (remainder == 180){
            remainder = 0;
        }


        return remainder;
    }

    /**
     * Takes in a path and generates ids at where the user has made a turn
     * @param path the path that we want to simplify
     * @return the List of strings at where the user changes direction 
     */
    public static List<String> simplifyPath(Path path) {

        List<String> pathID = path.getPath();
        List<String> pathIDCopy = new ArrayList<>(pathID);
        Graph graph = Graph.getGraph();

        Node prevNode = null;
        Node currNode = null;

        for (String nextNodeID : pathID) {

            Node nextNode = graph.getNodes().get(nextNodeID);

            if (prevNode != null) {
                if ((angleBetweenEdges(prevNode, currNode, nextNode) >= -30) && (angleBetweenEdges(prevNode, currNode, nextNode) <= 30)) {
                    pathIDCopy.remove(currNode.getNodeID());
                }
            }
            prevNode = currNode;
            currNode = nextNode;
        }

        return pathIDCopy;
    }

    /**ElEV, STAI
     * The list of instructions the user has to get through to
     * go to their destination
     *
     * @param path the path we want instructions for
     * @return a list of strings where each element in the list is one instruction
     */
    public static List<String> instructions(Path path) {
        List<String> simplePath = simplifyPath(path);

        String idEnd = simplePath.get(simplePath.size()-1);
        Graph graph = Graph.getGraph();

        String endloc = graph.getNodes().get(idEnd).getLongName();
        String startLoc = graph.getNodes().get(simplePath.get(0)).getLongName();

        List<String> directions = new ArrayList<>();
        directions.add("Starting route to " + endloc + " from "+ startLoc);

        double FT_CONST = 5000/1635;

        Node prev = null;
        Node curr = null;

        double distance;
        String dir = "";
        for (String id : simplePath) {

            Node next = graph.getNodes().get(id);

            if (curr != null && prev == null) {
                Coord currCoord = new Coord(curr.getXCoord(), curr.getYCoord());
                Coord nextCoord = new Coord(next.getXCoord(), next.getYCoord());
                distance = dist(currCoord, nextCoord)*FT_CONST;
                //starting directions get the distance between the current and next and convert to feet
                dir += "Walk " + (int)distance + " feet towards " + next.getLongName();
                directions.add(dir);
            }

            if(prev != null) {
                dir = "";
                Coord currCoord = new Coord(curr.getXCoord(), curr.getYCoord());
                Coord nextCoord = new Coord(next.getXCoord(), next.getYCoord());
                distance = dist(currCoord, nextCoord)*FT_CONST;
                //get turn then get the dist between c and next turn blah and walk dist

                double turn = angleBetweenEdges(prev, curr, next);
                if(id.contains("STAI") || id.contains("ELEV")){
                    if(!curr.getLongName().contains("ELEV") || !id.contains("STAI")){
                        String floorNext = graph.getNodes().get(id).getFloor();
                        dir+= "Walk to floor " + floorNext;
                        directions.add(dir);
                    }

                }
                else if (turn > 70 && turn <= 110) {
                    //take a right
                    dir += "Take a right and walk " + (int)distance + " feet towards " + next.getLongName();
                    directions.add(dir);
                } else if (turn > 110 && turn <= 160) {
                    //sharp right
                    dir += "Take a sharp right and walk " + (int)distance + " feet towards " + next.getLongName();
                    directions.add(dir);
                }
                else if(turn<= 70 && turn > 30){
                    dir += "Take a slight right and walk " + (int)distance + " feet towards " + next.getLongName();
                    directions.add(dir);
                }
                else if (turn < -70 && turn >= -110) {
                    //take a left
                    dir += "Take a left and walk " + (int)distance + " feet towards " + next.getLongName();
                    directions.add(dir);
                } else if (turn < -110 && turn >= -160){
                    dir += "Take a sharp left and walk " + (int)distance + " feet towards " + next.getLongName();
                    directions.add(dir);
                }
                else if(turn >= -70){
                    dir += "Take a slight left and walk " + (int)distance + " feet towards " + next.getLongName();
                    directions.add(dir);
                }
            }

            prev = curr;
            curr = next;
        }
        //add that you have reached your destination
        directions.add("You have reached your destination");
        return directions;
    }
}