package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class EditTimetableButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; // Existing stage
    private Scene homeScene; // Homepage scene
    private BufferedReader in; // Input stream from server
    private PrintWriter out; // Output stream to server

    public EditTimetableButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
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

        // Create a timetable and populate it with lecture data
        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGrid(scheduleArray);

        // Create the back button
        Button back = new Button("Go back");

        // Create a VBox to hold the timetable and back button
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(back, timetable.getGridPane());

        // Make the VBox grow to fill the window
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        // Create the view schedule scene
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT); // Replace with actual width and height if Client.WIDTH and Client.HEIGHT are not defined

        // Set the view scene to the existing stage
        stage.setScene(viewScene);
        stage.setTitle("Edit Timetable");

        // Handle the back button
        back.setOnAction(event -> {
            // Switch back to the homepage scene
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });
    }
}