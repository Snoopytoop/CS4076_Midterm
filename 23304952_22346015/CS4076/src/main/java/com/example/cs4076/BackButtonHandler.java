package org.example.midtermproject2;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
//back button to take user to homepage
public class BackButtonHandler implements EventHandler<ActionEvent> {
    private Stage stage; //stage to switch back from
    private Scene homeScene; //homepage scene to return to

    //constructor to set stage and home scene
    public BackButtonHandler(Stage stage, Scene homeScene) {
        this.stage = stage;
        this.homeScene = homeScene;
    }

    @Override
    public void handle(ActionEvent event) {
        //go back to homepage
        goBackToHomepage();
    }

    //switch back to homepage
    private void goBackToHomepage() {
        stage.setScene(homeScene);
        stage.setTitle("Homepage");
    }

    //create a styled green back button
    public Button createStyledBackButton() {
        Button backButton = new Button("Go Back");
        backButton.setStyle(
                "-fx-background-color: #4CAF50; " + //green background
                        "-fx-text-fill: white; " + //white text
                        "-fx-font-size: 14px; " + //font size
                        "-fx-padding: 10px 20px; " + //padding
                        "-fx-border-radius: 5px;" //rounded corners
        );

        //set action for the back button
        backButton.setOnAction(this);

        return backButton;
    }
}