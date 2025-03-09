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

public class AddButtonHandler implements EventHandler<ActionEvent> {
    private final Stage stage; //existing stage
    private final Scene homeScene; //homepage scene
    private final BufferedReader in; //input stream from server
    private final PrintWriter out; //output stream to server

    public AddButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //request array from server
        out.println("arrayRequest");

        //get array from server
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                scheduleArray = response.split(","); //split response into parts
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            e.printStackTrace();
            return;
        }

        //create timetable and fill it with data
        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGrid(scheduleArray);

        //create back button
        Button back = new Button("Go back");

        //vbox to hold timetable and back button
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(back, timetable.getGridPane());

        //make vbox stretch to fill window
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        //create view schedule scene
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        //set scene to stage
        stage.setScene(viewScene);
        stage.setTitle("View Schedule");

        //handle back button
        back.setOnAction(event -> {
            //go back to homepage
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });
    }
}
