package com.example.cs4076;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

public class EarlyLecture implements EventHandler<ActionEvent> {
    private Stage stage;
    private Scene homeScene;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executorService;
    private Timetable timetable;
    private Homepage homepage;

    public EarlyLecture(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out,
                        ExecutorService executorService, Timetable timetable, Homepage homepage) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
        this.executorService = executorService;
        this.timetable = timetable;
        this.homepage = homepage;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Early Timetable");
        alert.setHeaderText("Reschedule all lectures to earliest available slots?");
        alert.setContentText("This will move all lectures to the earliest available time slots on their respective days.");

        alert.showAndWait().ifPresent(buttonResponse -> {
            if (buttonResponse == ButtonType.OK) {
                // Send request to server to optimize timetable
                out.println("optimizeTimetable");

                // Wait for server response
                try {
                    String serverResponse = in.readLine();
                    if (serverResponse != null && serverResponse.equals("optimizationComplete")) {
                        // Refresh the timetable display
                        out.println("arrayRequest");
                        String updatedArray = in.readLine();
                        Platform.runLater(() -> {
                            timetable.populateGrid(updatedArray.split(","));
                            new Alert(Alert.AlertType.INFORMATION,
                                    "Timetable optimized successfully!").show();
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR,
                            "Error optimizing timetable: " + e.getMessage()).show());
                }
            }
        });
    }
}