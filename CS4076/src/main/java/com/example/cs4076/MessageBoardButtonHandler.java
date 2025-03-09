package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageBoardButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; //existing stage
    private Scene homeScene; //homepage scene
    private BufferedReader in; //input stream from server
    private PrintWriter out; //output stream to server

    //default constructor
    public MessageBoardButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        System.out.println("View Message Board button clicked!"); //debug statement

        //request messages from server
        out.println("fetchMessages");

        try {
            //read messages from server
            String response = in.readLine();
            System.out.println("Server response: " + response); //debug statement

            //vbox to hold message board content
            VBox messageBox = new VBox(10); //spacing between messages
            messageBox.setAlignment(Pos.CENTER);
            messageBox.setStyle("-fx-padding: 20px; -fx-background-color: #EBF5FB;"); //light blue background

            //title label
            Label titleLabel = new Label("Message Board");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2E86C1;"); //dark blue text
            messageBox.getChildren().add(titleLabel);

            if (response != null && !response.isEmpty()) {
                String[] messages = response.split("&&");

                //get the last 8 messages
                int startIndex = Math.max(0, messages.length - 8); //make sure we don't go out of bounds

                //add last 8 messages to display
                for (int i = startIndex; i < messages.length; i++) {
                    Label messageLabel = new Label(messages[i]);
                    messageLabel.setStyle(
                            "-fx-background-color: #ffffff; " + //white background
                                    "-fx-padding: 15px; " + //padding inside box
                                    "-fx-border-color: #2E86C1; " + //dark blue border
                                    "-fx-border-width: 2px; " + //border width
                                    "-fx-border-radius: 10px; "  //rounded corners

                    );
                    messageLabel.setWrapText(true); //wrap text
                    messageLabel.setMaxWidth(Client.WIDTH - 40); //max width for label
                    messageLabel.setTextFill(Color.BLACK); //black text
                    messageLabel.setStyle(messageLabel.getStyle() + "-fx-font-size: 16px;"); //larger font size
                    messageLabel.setAlignment(Pos.CENTER); //center-align text
                    messageBox.getChildren().add(messageLabel);
                }
            } else {
                //if no messages, show placeholder
                Label noMessagesLabel = new Label("No messages available.");
                noMessagesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2E86C1;"); //dark blue text
                messageBox.getChildren().add(noMessagesLabel);
            }

            //create back button
            BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
            Button backButton = backButtonHandler.createStyledBackButton();

            //add back button to go to homepage
            messageBox.getChildren().add(backButton);

            //create message board scene
            Scene messageScene = new Scene(messageBox, Client.WIDTH, Client.HEIGHT);

            //set scene to stage
            stage.setScene(messageScene);
            stage.setTitle("Message Board");
        } catch (IOException e) {
            System.err.println("Error reading messages from server.");
            e.printStackTrace();

            //show error message
            Label errorLabel = new Label("Failed to retrieve messages from the server.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #d32f2f;"); //red text for errors

            //vbox to hold error message and back button
            VBox errorBox = new VBox(10);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setStyle("-fx-background-color: #EBF5FB;"); //light blue background
            errorBox.getChildren().add(errorLabel);

            //create back button
            BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
            Button backButton = backButtonHandler.createStyledBackButton();
            errorBox.getChildren().add(backButton);

            //create error scene
            Scene errorScene = new Scene(errorBox, Client.WIDTH, Client.HEIGHT);

            //set scene to stage
            stage.setScene(errorScene);
            stage.setTitle("Error");
        }
    }
}