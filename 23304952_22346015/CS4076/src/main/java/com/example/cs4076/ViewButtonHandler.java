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

//new window to display uneditable timetable
public class ViewButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; //existing stage
    private Scene homeScene; //homepage scene
    private BufferedReader in; //input stream from server
    private PrintWriter out; //output stream to server

    public ViewButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //request array from server
        out.println("arrayRequest");

        //recieve array from server
        String[] scheduleArray = null;
        try {
            String response = in.readLine();
            if (response != null) {
                scheduleArray = response.split(","); //split the response into parts
            }
        } catch (IOException e) {
            System.err.println("Error reading array from server.");
            e.printStackTrace();
            return;
        }

        //create timetable
        Timetable timetable = new Timetable(out, stage, homeScene, in);
        timetable.populateGridForView(scheduleArray); //view only

        //back button
        BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
        Button backButton = backButtonHandler.createStyledBackButton();

        //vbox to hold the timetable and back button
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().addAll(backButton, timetable.getGridPane());

        //vbox will stretch to fill window
        VBox.setVgrow(timetable.getGridPane(), Priority.ALWAYS);

        //view schedule scene
        Scene viewScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        //set scene to stage
        stage.setScene(viewScene);
        stage.setTitle("View Schedule");
    }
}