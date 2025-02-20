package org.example.javafx;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ViewButtonHandler implements EventHandler<ActionEvent> {
    private PrintWriter out;
    private BufferedReader in;

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        // sending message to server to request array
        out.println("arrayRequest");

        // getting response from server
        try {
            String input = in.readLine();
            System.out.println(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Setting up Stage
        Stage stage = new Stage();
        stage.setTitle("Add a lecture");

        // Creating text
        Text Monday = new Text("Monday");
        Text Tuesday = new Text("Tuesday");
        Text Wednesday = new Text("Wednesday");
        Text Thursday = new Text("Thursday");
        Text Friday = new Text("Friday");

        // Creating times
        Text nineToTen = new Text("9 - 10");
        Text tenToEleven = new Text("10 - 11");
        Text elevenToTwelve = new Text("11 - 12");
        Text twelveToThirteen = new Text("12 - 13");
        Text thirteenToFourteen = new Text("13 - 14");
        Text fourteenToFifteen = new Text("14 - 15");
        Text fifteenToSixteen = new Text("15 - 16");
        Text sixteenToSeventeen = new Text("16 - 17");
        Text seventeenToEighteen = new Text("17 - 18");

        // Setting up scene
        Button back = new Button("Go back");
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);

        // Setting up grid pane
        gridPane.add(Monday, 0, 1);
        gridPane.add(Tuesday, 0, 2);
        gridPane.add(Wednesday, 0, 3);
        gridPane.add(Thursday, 0, 4);
        gridPane.add(Friday, 0, 5);

        gridPane.add(nineToTen, 1, 0);
        gridPane.add(tenToEleven, 2, 0);
        gridPane.add(elevenToTwelve, 3, 0);
        gridPane.add(twelveToThirteen, 4, 0);
        gridPane.add(thirteenToFourteen, 5, 0);
        gridPane.add(fourteenToFifteen, 6, 0);
        gridPane.add(fifteenToSixteen, 7, 0);
        gridPane.add(sixteenToSeventeen, 8, 0);
        gridPane.add(seventeenToEighteen, 9, 0);

        gridPane.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-grid-lines-visible: true;");
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        Scene scene = new Scene(gridPane, 500, 500);
        stage.setScene(scene);
        stage.show();

        // Handling back button
        BackButtonHandler backButtonHandler = new BackButtonHandler();
        backButtonHandler.setStage(stage);
        back.setOnAction(backButtonHandler);
    }
}
