package com.example.cs4076;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
//make timetable layout
public class Timetable {
    private final GridPane gridPane; //grid for timetable
    private final PrintWriter out; //output stream to server
    private final Stage stage; //existing stage
    private final Scene homeScene; //homepage scene
    private final BufferedReader in; //input stream from server

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"}; //days of the week
    private static final String[] TIMES = {"9-10", "10-11", "11-12", "12-13", "13-14", "14-15", "15-16", "16-17", "17-18"}; //time slots

    public Timetable(PrintWriter out, Stage stage, Scene homeScene, BufferedReader in) {
        gridPane = new GridPane();
        this.out = out;
        this.stage = stage;
        this.homeScene = homeScene;
        this.in = in;
        setupGrid();
    }

    private void setupGrid() {
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setStyle(
                "-fx-border-color: #2E86C1; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-color: #EBF5FB; " +
                        "-fx-padding: 20;"
        );

        //set up columns
        for (int i = 0; i <= DAYS.length; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            gridPane.getColumnConstraints().add(column);
        }

        //set up rows
        for (int i = 0; i <= TIMES.length; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            gridPane.getRowConstraints().add(row);
        }

        //add time labels
        for (int i = 0; i < TIMES.length; i++) {
            Label timeLabel = new Label(TIMES[i]);
            timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E86C1;");
            gridPane.add(timeLabel, 0, i + 1);
        }

        //add day labels
        for (int j = 0; j < DAYS.length; j++) {
            Label dayLabel = new Label(DAYS[j]);
            dayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E86C1;");
            gridPane.add(dayLabel, j + 1, 0);
        }
    }

    public void populateGrid(String[] scheduleArray) {
        if (scheduleArray == null) return;

        for (int index = 0; index < scheduleArray.length; index++) {
            int j = index % 5; //day (column)
            int i = index / 5; //time slot (row)

            Button button;
            if (scheduleArray[index] != null && !"null".equals(scheduleArray[index].trim())) {
                //if slot has data, display it in a button
                String[] info = scheduleArray[index].trim().split(" ");
                button = new Button(info[0] + "\n" + info[1] + " " + info[2]);
                button.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");

                //set up remove action
                String removeMessage = "remove," + i + "-" + j;
                button.setOnAction(event -> {
                    out.println(removeMessage); //send remove request to server
                    //refresh timetable after removal
                    out.println("arrayRequest");
                    try {
                        String response = in.readLine();
                        if (response != null) {
                            String[] updatedArray = response.split(",");
                            populateGrid(updatedArray); //refresh the grid
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading updated array from server.");
                    }
                });
            } else {
                //if slot is empty, display an empty button
                button = new Button(" ");
                button.setStyle("-fx-background-color: #ccffda; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");

                //set up add action
                String position = i + "-" + j;
                button.setOnAction(event -> {
                    AddWindow addWindow = new AddWindow(out, position);
                    addWindow.showWindow(stage);
                    //refresh timetable after adding
                    out.println("arrayRequest");
                    try {
                        String response = in.readLine();
                        if (response != null) {
                            String[] updatedArray = response.split(",");
                            populateGrid(updatedArray); //refresh the grid
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading updated array from server.");
                    }
                });
            }

            button.setMinSize(80, 40);
            button.setPrefSize(100, 50);
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            final int currentIndex = index;

            //add hover effect
            button.setOnMouseEntered(event -> {
                button.setStyle(button.getStyle() + "-fx-border-color: black; -fx-border-width: 2;");
            });

            button.setOnMouseExited(event -> {
                if (scheduleArray[currentIndex] != null && !"null".equals(scheduleArray[currentIndex].trim())) {
                    button.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                } else {
                    button.setStyle("-fx-background-color: #ccffda; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                }
            });

            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);

            gridPane.add(button, j + 1, i + 1); //add label to grid
        }
    }

    public void populateGridForView(String[] scheduleArray) {
        if (scheduleArray == null) return;

        for (int index = 0; index < scheduleArray.length; index++) {
            int j = index % 5; //day (column)
            int i = index / 5; //time slot (row)

            Label label;
            if (scheduleArray[index] != null && !"null".equals(scheduleArray[index].trim())) {
                //if slot has data, display it in a label
                String[] info = scheduleArray[index].trim().split(" ");
                label = new Label(info[0] + "\n" + info[1] + " " + info[2]);
                label.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
            } else {
                //if slot is empty, display an empty label
                label = new Label(" ");
                label.setStyle("-fx-background-color: #ccffda; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
            }

            label.setMinSize(80, 40);
            label.setPrefSize(100, 50);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Create a final copy of the index variable for use in the lambda
            final int currentIndex = index;

            //add hover effect
            label.setOnMouseEntered(event -> {
                label.setStyle(label.getStyle() + "-fx-border-color: black; -fx-border-width: 2;");
            });

            label.setOnMouseExited(event -> {
                if (scheduleArray[currentIndex] != null && !"null".equals(scheduleArray[currentIndex].trim())) {
                    label.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                } else {
                    label.setStyle("-fx-background-color: #ccffda; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center;");
                }
            });

            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setVgrow(label, Priority.ALWAYS);

            gridPane.add(label, j + 1, i + 1); //add label to grid
        }
    }

    public GridPane getGridPane() {
        return gridPane; //return the grid
    }
}