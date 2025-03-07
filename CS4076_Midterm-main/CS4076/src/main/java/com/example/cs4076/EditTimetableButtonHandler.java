package org.example.midtermproject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

public class EditTimetableButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage;
    private Scene homeScene;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService executorService; // Shared executor service

    public EditTimetableButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out, ExecutorService executorService) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
        this.executorService = executorService;
    }


    public void handleUpdate(String message) {
        if (!message.isEmpty()) {
            System.out.println("Received update: " + message);
            handle(new ActionEvent());  // Refresh timetable UI
        }
    }


    public void shutdownExecutor() {
        executorService.shutdownNow();
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        out.println("arrayRequest");
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                scheduleArray = response.split(",");
                System.out.println("response: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            return;
        }

        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGrid(scheduleArray);

        Button back = new Button("Go back");
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(back, timetable.getGridPane());

        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);
        stage.setScene(viewScene);
        stage.setTitle("Edit Timetable");

        back.setOnAction(event -> {
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });
    }
}
