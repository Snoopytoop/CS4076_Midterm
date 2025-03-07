package org.example.midtermproject;

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
    private Stage stage;
    private Scene homeScene;
    private BufferedReader in;
    private PrintWriter out;

    //default constructor
    public MessageBoardButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        //request messages array from server
        out.println("fetchMessages");

        try {
            //read the messages array
            String response = in.readLine();
            if (response != null) {
                String[] messages = response.split("&&");

                //create a new display to view the messages in the messages array
                VBox messageBox = new VBox(10);
                messageBox.setAlignment(Pos.CENTER);

                //add the messages to the new display
                for (String message : messages) {
                    Label messageLabel = new Label(message);
                    messageBox.getChildren().add(messageLabel);
                }

                //include the back button to go to homepage
                Button backButton = new Button("Go Back");
                backButton.setOnAction(event -> stage.setScene(homeScene));
                messageBox.getChildren().add(backButton);

                //new scene info
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