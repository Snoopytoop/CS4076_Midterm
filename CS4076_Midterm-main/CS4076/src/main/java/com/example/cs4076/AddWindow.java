package com.example.cs4076;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;

public class AddWindow {
    private final PrintWriter out;
    private String position;

    AddWindow(PrintWriter out, String position) {
        this.out = out;
        this.position = position;
    }

    public void showWindow(Stage parentStage) {
        // Create a new stage (window)
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with other windows
        window.setTitle("Add Lecture");
        window.setMinWidth(300);

        // Create dropdown menus
        ComboBox<String> subject = new ComboBox<>();
        subject.getItems().addAll("Discrete Maths", "Programming", "Computer Hardware", "DSA", "Computer Graphics");
        subject.setPromptText("Select Module");

        ComboBox<String> room = new ComboBox<>();
        room.getItems().addAll("Room 1", "Room 2", "Room 3", "Room 4", "Room 5", "Room 6");
        room.setPromptText("Select Room");

        // Create a button
        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(event -> {
            String selection1 = subject.getValue();
            String selection2 = room.getValue();
            String message = "add,"+selection1+"-"+selection2+"-"+position;
            out.println(message);

            if (selection1 != null && selection2 != null) {
                System.out.println("Selected: " + selection1 + " - " + selection2);
                window.close(); // Close the window after selection
            }
        });

        // Layout (VBox)
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(subject, room, confirmButton);

        // Scene setup
        Scene scene = new Scene(layout);
        window.setScene(scene);

        // Show the window
        window.showAndWait();
    }
}

