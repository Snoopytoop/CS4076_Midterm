package org.example.midtermproject2;

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
//remove a lecture
public class RemoveButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; //existing stage
    private Scene homeScene; //homepage scene
    private BufferedReader in; //input stream from server
    private PrintWriter out; //output stream to server

    //default constructor
    public RemoveButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //request array from server
        out.println("arrayRequest");

        //handle the array
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                //split response into parts
                scheduleArray = response.split(",");
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            e.printStackTrace();
            return;
        }

        //create timetable and fill it with saved data
        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGrid(scheduleArray);

        //create back button
        BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
        Button backButton = backButtonHandler.createStyledBackButton();

        //vbox to hold back button and timetable
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(backButton, timetable.getGridPane());

        //vbox will stretch to fill window
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        //create scene with set width and height
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        //set scene to stage
        stage.setScene(viewScene);
        stage.setTitle("View Schedule");
    }
}