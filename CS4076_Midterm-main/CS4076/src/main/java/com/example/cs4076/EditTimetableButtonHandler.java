package org.example.cs4076;

import javafx.application.Platform;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditTimetableButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; // Existing stage
    private Scene homeScene; // Homepage scene
    private BufferedReader in; // Input stream from server
    private PrintWriter out; // Output stream to server
    private ExecutorService executorService; // Executor for managing background tasks

    public EditTimetableButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
        this.executorService = Executors.newSingleThreadExecutor(); // Executor for handling background reads
    }

    // Method to continuously check for updates in the BufferedReader without blocking the main thread
    public void checkForUpdates() {
        // Run the background task on a separate thread
        executorService.submit(() -> {
            try {
                while (true) {
                    // Use the BufferedReader to read data asynchronously without blocking
                    if (in.ready()) {  // Non-blocking check if data is available
                        String line = in.readLine(); // Only read if data is available
                        if (line != null && !line.isEmpty()) {
                            // New data received. Notify the UI thread to refresh.
                            Platform.runLater(() -> {
                                handle(new ActionEvent()); // Call the handle() method to update the timetable
                            });
                        }
                    }
                    Thread.sleep(50); // Sleep briefly to avoid overloading the CPU with constant checking
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error while listening for updates from server.");
                e.printStackTrace();
            }
        });
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
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

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
