package org.example.finalproject;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageBoardHandler implements EventHandler<ActionEvent> {
    private final Stage stage; //existing stage
    private final Scene homeScene; //homepage scene
    private final BufferedReader in; //input stream from server
    private final PrintWriter out; //output stream from server

    public MessageBoardHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //create a vbox to hold messageboard messages
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new javafx.geometry.Insets(20)); //add padding around the vbox

        //title
        Label titleLabel = new Label("Message Board");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        //get messsages from the server
        try {
            out.println("fetchMessages"); //request
            String response = in.readLine(); //response
            System.out.println("Received messages:\n" + response); //confirmation log

            if (response != null && !response.isEmpty()) {
                //split the response into each message
                String[] messages = response.split("&&");

                //add each message to box
                for (String message : messages) {
                    Label messageLabel = new Label(message);
                    messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
                    messageLabel.setStyle(
                            "-fx-background-color: #f9f9f9; " + //light gray background
                                    "-fx-padding: 10px; " + //padding inside the box
                                    "-fx-border-color: #cccccc; " + //border color
                                    "-fx-border-width: 1px; " + //border width
                                    "-fx-border-radius: 5px; "  //rounded corners

                    );
                    messageLabel.setWrapText(true); //wrap the text
                    messageLabel.setMaxWidth(Client.WIDTH - 40); //set max width for the label
                    messageLabel.setTextFill(Color.DARKGRAY); //text color
                    vbox.getChildren().add(messageLabel); //add the message label to the vbox
                }
            } else {
                //if no messages are available, display:
                Label noMessagesLabel = new Label("No messages available.");
                noMessagesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");
                vbox.getChildren().add(noMessagesLabel);
            }
        } catch (IOException e) {
            e.printStackTrace();
            //if there's an error fetching messages, display an error message
            Label errorLabel = new Label("Failed to retrieve messages from the server.");
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff0000;");
            vbox.getChildren().add(errorLabel);
        }

        //back button
        BackButtonHandler backButtonHandler = new BackButtonHandler(stage, homeScene);
        Button backButton = backButtonHandler.createStyledBackButton();

        vbox.getChildren().addAll(titleLabel, backButton);

        //create the scene
        Scene messageBoardScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        //set scene to stage
        stage.setScene(messageBoardScene);
        stage.setTitle("Message Board");
    }
}