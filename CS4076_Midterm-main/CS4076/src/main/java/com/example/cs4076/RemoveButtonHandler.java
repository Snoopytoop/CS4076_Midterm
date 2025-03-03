package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class RemoveButtonHandler implements EventHandler<ActionEvent> {
    private  Stage stage;
    private  Scene homeScene;
    private  BufferedReader in;
    private  PrintWriter out;

    //default constructor
    public RemoveButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //request array from the server
        out.println("arrayRequest");

        //handle the array
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                //recieving array with "," seperating data pieces
                scheduleArray = response.split(",");
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            e.printStackTrace();
            return;
        }

        //create the timetable and add the saved data to it
        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGrid(scheduleArray);

        //add back button to homepage
        Button back = new Button("Go back");

        //Vbox
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(back, timetable.getGridPane());

        //Vbox will adjust dynamically
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        //create the scene using predetermined width and height
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        //change view to this stage
        stage.setScene(viewScene);
        stage.setTitle("View Schedule");

        //back button handler
        back.setOnAction(event -> {
            //go back to homepage scene
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });
    }
}