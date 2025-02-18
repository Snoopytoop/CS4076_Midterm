package org.example.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewButtonHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
        // Setting up Stage
        Stage stage = new Stage();
        stage.setTitle("Add a lecture");

        // Setting up scene
        Label label = new Label("Page for viewing schedule");
        Button back = new Button("Go back");
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(label, back);
        Scene scene = new Scene(box, 500, 500);
        stage.setScene(scene);
        stage.show();

        // Handling back button
        BackButtonHandler backButtonHandler = new BackButtonHandler();
        backButtonHandler.setStage(stage);
        back.setOnAction(backButtonHandler);
    }
}
