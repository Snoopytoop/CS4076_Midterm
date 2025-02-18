package org.example.javafx;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Homepage extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // setting up children
        // setting up text
        Label label = new Label("Would you like to...");
        // setting up buttons
        Button add = new Button("Add a lecture");
        Button remove = new Button("Remove a lecture");
        Button view = new Button("View schedule");

        //adding event handler for buttons
        add.setOnAction(new addButtonHandler());
        remove.setOnAction(new RemoveButtonHandler());
        view.setOnAction(new ViewButtonHandler());


        // setting up stage and scene
        stage.setTitle("Homepage");
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
        box.getChildren().addAll(label, add, remove, view);
        Scene scene = new Scene(box, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

}
