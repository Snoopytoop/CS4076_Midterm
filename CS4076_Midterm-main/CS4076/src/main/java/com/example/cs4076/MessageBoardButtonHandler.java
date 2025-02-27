package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageBoardButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; // Existing stage
    private Scene homeScene; // Homepage scene
    private BufferedReader in; // Input stream from server
    private PrintWriter out; // Output stream to server

    public MessageBoardButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        // Request messages from the server
        out.println("fetchMessages");

        try {
            // Read the messages from the server
            String response = in.readLine();
            if (response != null) {
                String[] messages = response.split(",");

                // Create a new scene to display the messages
                VBox messageBox = new VBox(10);
                messageBox.setAlignment(Pos.CENTER);

                // Add each message to the VBox
                for (String message : messages) {
                    Label messageLabel = new Label(message);
                    messageBox.getChildren().add(messageLabel);
                }

                // Add a back button
                Button backButton = new Button("Go Back");
                backButton.setOnAction(event -> stage.setScene(homeScene));
                messageBox.getChildren().add(backButton);

                // Create and set the new scene
                Scene messageScene = new Scene(messageBox, Client.WIDTH, Client.HEIGHT);
                stage.setScene(messageScene);
                stage.setTitle("Message Board");
            }
        } catch (IOException e) {
            System.err.println("Error reading messages from server.");
            e.printStackTrace();
        }
    }
}