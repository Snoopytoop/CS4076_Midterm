package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RemoveButtonHandler implements EventHandler<ActionEvent> {
    private final Stage stage; // Existing stage
    private final Scene homeScene; // Homepage scene

    public RemoveButtonHandler(Stage stage, Scene homeScene) {
        this.stage = stage;
        this.homeScene = homeScene;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        // Setting up scene for removing lectures
        Label label = new Label("Page for removing lectures");
        Button back = new Button("Go back");
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(label, back);
        Scene removeScene = new Scene(box, Client.WIDTH, Client.HEIGHT); // Use WIDTH and HEIGHT

        // Set the remove scene to the existing stage
        stage.setScene(removeScene);
        stage.setTitle("Remove a Lecture");

        // Handle the back button
        back.setOnAction(event -> {
            // Switch back to the homepage scene
            stage.setScene(homeScene);
            stage.setTitle("Homepage");
        });
    }
}