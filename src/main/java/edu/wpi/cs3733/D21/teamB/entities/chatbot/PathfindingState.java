package edu.wpi.cs3733.D21.teamB.entities.chatbot;

import edu.wpi.cs3733.D21.teamB.util.PageCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PathfindingState implements IState {

    private final Stack<String> messagesSent = new Stack<>();

    @Override
    public List<String> respond(String input) {
        List<String> response = new ArrayList<>();

        if (!messagesSent.isEmpty() && messagesSent.peek().equals("unsure")) {
            messagesSent.pop();
        }

        if (messagesSent.isEmpty() && PageCache.getCurrentPage().equals("/edu/wpi/cs3733/D21/teamB/views/map/pathfindingMenu.fxml")) { // If already on the page
            messagesSent.push("Do you need help navigating the hospital?");
            response.add("Do you need any assistance?");
        } else if (messagesSent.isEmpty()) { // First message
            response.add("Do you need help navigating the hospital?");
        } else if (messagesSent.peek().equals("Do you need help navigating the hospital?")) { // Second message
            if (StateManager.containsAny(input, new String[]{"y", "ye", "yes", "yeah", "yup"})) {
                response.add("Okay! Taking you there now...");
                PageCache.getCachedResponses().add("Do you need any assistance?");
                response.add("/edu/wpi/cs3733/D21/teamB/views/map/pathfindingMenu.fxml");
            } else if (StateManager.containsAny(input, new String[]{"n", "no", "nah", "nope"})) {
                response.add("No worries!");
                response.add("Is there anything I can help you with?");
                response.add("return");
            } else {
                response.add("unsure");
                response.add("Do you need help navigating the hospital?");
            }
        } else if (messagesSent.peek().equals("Do you need any assistance?")) { // Third message in
            if (StateManager.containsAny(input, new String[]{"y", "ye", "yes", "yeah", "yup"})) {
                response.add("Please fill select your destination and starting point from the map.");
                response.add("You may also use the categories on the side as well as adding intermediate stops.");
                response.add("Do you need any assistance?");
            } else if (StateManager.containsAny(input, new String[]{"floor", "floors", "stairs", "elevator", "upstairs","downstairs"})) {
                response.add("You can select different floors using the yellow buttons at the top right. The locations of floor traversals are noted by color coding.");
                response.add("Do you need any assistance?");
            } else if (StateManager.containsAny(input, new String[]{"closest", "bathroom", "food", "exit"})) {
                response.add("The dropdown next to closest location will allow you to find the closest bathroom, food, exit, or help you might need.");
                response.add("Do you need any assistance?");
            } else if (StateManager.containsAny(input, new String[]{"find", "where", "looking", "destination"})) {
                response.add("If you are having trouble finding a specific location you can search for it using the bar on the left.");
                response.add("Do you need any assistance?");
            } else if (StateManager.containsAny(input, new String[]{"n", "no", "nah", "nope",})) {
                response.add("No worries! I'll be here.");
            } else {
                response.add("unsure");
                response.add("Do you need any assistance?");
            }
        } else if (messagesSent.peek().equals("No worries! I'll be here.") || messagesSent.peek().equals("If you have any questions, feel free to ask!")) {
            response.add("Do you need any assistance?");
        } else { // Otherwise, what the heck?
            response.add("unsure");
        }

        for (String s : response)
            messagesSent.push(s);

        if (PageCache.getCachedResponses() != null && !PageCache.getCachedResponses().isEmpty()) {
            for (String s : PageCache.getCachedResponses()) {
                messagesSent.push(s);
            }
        }

        return response;
    }
}
