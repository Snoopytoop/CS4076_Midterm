package com.example.cs4076;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.PrintWriter;

//create dro down menus for adding lectures and rooms
public class AddWindow {
    private final PrintWriter out;
    private String position;

    AddWindow(PrintWriter out, String position) {
        this.out = out;
        this.position = position;
    }

    //show the add window
    public void showWindow(Stage parentStage) {
        //create stage
        Stage window = new Stage();
        //can't interact with other windows
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Add Lecture");
        window.setMinWidth(300);

        //dropdown menus
        ComboBox<String> subject = new ComboBox<>();
        subject.getItems().addAll("Maths", "Programming", "OS", "DSA", "Graphics");
        subject.setPromptText("Select Module");

        ComboBox<String> room = new ComboBox<>();
        room.getItems().addAll("Room 1", "Room 2", "Room 3", "Room 4", "Room 5", "Room 6");
        room.setPromptText("Select Room");

        //button
        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(event -> {
            String selection1 = subject.getValue();
            String selection2 = room.getValue();
            String message = "add,"+selection1+"-"+selection2+"-"+position;
            out.println(message);
            System.out.println(message);

            if (selection1 != null && selection2 != null) {
                System.out.println("Selected: " + selection1 + " - " + selection2 + " - " + position);
                //close the window once selections have been picked
                window.close();

            }
        });

        //vbox setup
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(subject, room, confirmButton);

        //scene setup
        Scene scene = new Scene(layout);
        window.setScene(scene);
        //keep window up until finished
        window.showAndWait();
    }
}