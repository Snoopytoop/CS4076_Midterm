package com.example.cs4076;

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
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
//requests timetable data from server when clicked
public class EditTimetableButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; //existing stage
    private Scene homeScene; //homepage scene
    private BufferedReader in; //input stream from server
    private PrintWriter out; //output stream to server
    private ExecutorService executorService; //shared executor service
    private Homepage homepage; //reference to homepage for button styling

    public EditTimetableButtonHandler(Stage stage, Scene homeScene, BufferedReader in,
                                      PrintWriter out, ExecutorService executorService,
                                      Homepage homepage) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
        this.executorService = executorService;
        this.homepage = homepage; //store homepage reference
    }

    public void handleUpdate(String message) {
        if (!message.isEmpty()) {
            System.out.println("Received update: " + message);
            handle(new ActionEvent());  //refresh timetable UI
        }
    }

    public void shutdownExecutor() {
        executorService.shutdownNow(); //shut down executor
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //request timetable array from server
        out.println("arrayRequest");

        //handle the array
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                scheduleArray = response.split(","); //split response into parts
                System.out.println("Response: " + response);
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            return;
        }

        //create timetable and fill it with data
        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGrid(scheduleArray);

        //create early timetable button using homepage's styling
        Button earlyTimetableButton = homepage.createStyledButton("Early Timetable", "#00BCD4");
        EarlyLecture earlyLectureHandler = new EarlyLecture(stage, homeScene, in, out,
                executorService, timetable, homepage);
        earlyTimetableButton.setOnAction(earlyLectureHandler);

        //create back button using homepage's styling
        Button backButton = homepage.createStyledButton("Back", "#607D8B");
        backButton.setOnAction(new BackButtonHandler(stage, homeScene));

        //vbox to hold buttons and timetable
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(earlyTimetableButton, backButton, timetable.getGridPane());

        //make the gridPane stretch to fill the window
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        //create scene with set width and height
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        //set scene to stage
        stage.setScene(viewScene);
        stage.setTitle("Edit Timetable");
    }
}