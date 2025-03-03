package org.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

// class to go back to main page
public class BackButtonHandler implements EventHandler<ActionEvent> {
    public Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void handle(ActionEvent event) {
        stage.close();
    }
}

