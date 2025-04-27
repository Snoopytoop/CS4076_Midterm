package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
//post a message to the message board
public class PostMessageButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; //existing stage
    private Scene homeScene; //homepage scene
    private BufferedReader in; //input stream from server
    private PrintWriter out; //output stream to server

    //default constructor
    public PostMessageButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent event) {
        //ask user for message
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Post a Message");
        dialog.setHeaderText("Enter your message:");
        dialog.setContentText("Message:");

        //wait for user input
        Optional<String> result = dialog.showAndWait();

        //send message to server
        result.ifPresent(message -> {
            //send message
            out.println("sendMessage," + message);
            try {
                //get server response
                String response = in.readLine();
                System.out.println("SERVER RESPONSE: " + response);

                //show confirmation message and back button
                Label confirmationLabel = new Label("Message posted successfully!");
                confirmationLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50;");

                //create back button
                BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
                Button backButton = backButtonHandler.createStyledBackButton();

                //vbox to hold confirmation message and back button
                VBox vbox = new VBox(10);
                vbox.setAlignment(Pos.CENTER);
                vbox.getChildren().addAll(confirmationLabel, backButton);

                //create message confirmation scene
                Scene confirmationScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

                //set scene to stage
                stage.setScene(confirmationScene);
                stage.setTitle("Message Posted");
            } catch (IOException e) {
                System.err.println("Error reading server response.");
                e.printStackTrace();

                //show error message and back button
                Label errorLabel = new Label("Failed to post message. Please try again.");
                errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff0000;");

                //create back button
                BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
                Button backButton = backButtonHandler.createStyledBackButton();

                //vbox to hold error message and back button
                VBox vbox = new VBox(10);
                vbox.setAlignment(Pos.CENTER);
                vbox.getChildren().addAll(errorLabel, backButton);

                //create error scene
                Scene errorScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

                //set scene to stage
                stage.setScene(errorScene);
                stage.setTitle("Error");
            }
        });
    }
}