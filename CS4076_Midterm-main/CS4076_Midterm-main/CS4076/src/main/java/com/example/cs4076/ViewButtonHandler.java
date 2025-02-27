package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ViewButtonHandler implements EventHandler<ActionEvent> {
    private final Stage stage; // Existing stage
    private final Scene homeScene; // Homepage scene
    private final BufferedReader in; // Input stream from server
    private final PrintWriter out; // Output stream to server

    public ViewButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        // Send a request to the server to get the array
        out.println("arrayRequest");

        // Retrieve the array from the server
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                scheduleArray = response.split(","); // Assuming the server sends the array as a comma-separated string
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            e.printStackTrace();
            return;
        }

        // Create a timetable and add buttons for null slots
        Timetable timetable = new Timetable();
        timetable.addButtonsForNullSlots(scheduleArray, this::handleSlotSelection);

        // Create the back button
        Button back = new Button("Go back");

        // Create a VBox to hold the timetable and back button
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(back, timetable.getGridPane());

        //make the vbox grow to fill the window
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        // Create the add lecture scene
        Scene addScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT); // Use WIDTH and HEIGHT

        // Set the add scene to the existing stage
        stage.setScene(addScene);
        stage.setTitle("View Schedule");

        // Handle the back button
        back.setOnAction(event -> {
            // Switch back to the homepage scene
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });
    }

    // Handle slot selection
    private void handleSlotSelection(ActionEvent event) {
        Button button = (Button) event.getSource();
        Integer rowIndex = GridPane.getRowIndex(button);
        Integer columnIndex = GridPane.getColumnIndex(button);

        if (rowIndex != null && columnIndex != null) {
            // Send a request to the server to update the array
            out.println("updateSlot:" + (rowIndex - 1) + ":" + (columnIndex - 1));

            // Refresh the add lecture scene to reflect the updated array
            handle(new ActionEvent());
        }
    }
}