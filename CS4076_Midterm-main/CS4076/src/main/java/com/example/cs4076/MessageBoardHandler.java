package org.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MessageBoardHandler implements EventHandler<ActionEvent> {
    private final Stage stage; // Existing stage
    private final Scene homeScene; // Homepage scene
    private final BufferedReader in; // Input stream from server
    private final PrintWriter out; // Output stream to server

    public MessageBoardHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        // Create a VBox to hold the message board content
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        // Create a label for the message board title
        Label titleLabel = new Label("Message Board");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create a TextArea to display messages
        TextArea messageArea = new TextArea();
        messageArea.setEditable(false); // Make it read-only
        messageArea.setWrapText(true); // Enable text wrapping
        messageArea.setPrefSize(Client.WIDTH - 40, Client.HEIGHT - 100); // Set size based on Client.WIDTH and Client.HEIGHT

        // Fetch messages from the server
        try {
            out.println("viewMessageBoard,"); // Send request to server
            String response = in.readLine(); // Receive response from server
            System.out.println("Received messages:\n" + response); // Debug statement

            if (response != null && !response.isEmpty()) {
                // Display all messages in the TextArea
                messageArea.setText(response);
            } else {
                messageArea.setText("No messages available."); // Handle empty message board
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageArea.setText("Failed to retrieve messages from the server.");
        }

        // Create a back button
        Button backButton = new Button("Go Back");
        backButton.setOnAction(event -> {
            // Switch back to the homepage scene
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });

        // Add components to the VBox
        vbox.getChildren().addAll(titleLabel, messageArea, backButton);

        // Create the message board scene
        Scene messageBoardScene = new Scene(vbox, Client.WIDTH, Client.HEIGHT);

        // Set the message board scene to the existing stage
        stage.setScene(messageBoardScene);
        stage.setTitle("Message Board");
    }
}