package org.example.midtermproject;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

public class PostMessageButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage;
    private Scene homeScene;
    private BufferedReader in;
    private PrintWriter out;

    //deault constructor
    public PostMessageButtonHandler(Stage stage, Scene homeScene, BufferedReader in, PrintWriter out) {
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        this.out = out;
    }

    @Override
    public void handle(ActionEvent event) {
        //prompt user for message
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Post a Message");
        dialog.setHeaderText("Enter your message:");
        dialog.setContentText("Message:");

        //wait for response
        Optional<String> result = dialog.showAndWait();

        //send message to server
        result.ifPresent(message -> {
            //message being sent
            out.println("sendMessage," + message);
            try {
                //server response
                String response = in.readLine();
                System.out.println("SERVER RESPONSE: " + response);
            } catch (IOException e) {
                System.err.println("Error reading server response.");
                e.printStackTrace();
            }
        });
    }
}