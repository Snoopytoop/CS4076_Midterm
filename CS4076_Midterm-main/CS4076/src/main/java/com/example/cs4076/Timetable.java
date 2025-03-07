package org.example.midtermproject;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.io.PrintWriter;

public class Timetable {
    private final GridPane gridPane;
    private final PrintWriter out;
    private final Stage stage;
    private final Scene homeScene;
    private final BufferedReader in;

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final String[] TIMES = {"9-10", "10-11", "11-12", "12-13", "13-14", "14-15", "15-16", "16-17", "17-18"};

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

        for (int i = 0; i <= DAYS.length; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            gridPane.getColumnConstraints().add(column);
        }

        for (int i = 0; i <= TIMES.length; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            gridPane.getRowConstraints().add(row);
        }

        for (int i = 0; i < TIMES.length; i++) {
            Label timeLabel = new Label(TIMES[i]);
            timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E86C1;");
            gridPane.add(timeLabel, 0, i + 1);
        }

        for (int j = 0; j < DAYS.length; j++) {
            Label dayLabel = new Label(DAYS[j]);
            dayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E86C1;");
            gridPane.add(dayLabel, j + 1, 0);
        }
    }

    private void handleSlotSelection(int dayIndex, int timeIndex) {
        System.out.println("Selected: " + TIMES[timeIndex] + " on " + DAYS[dayIndex]);
    }

    public void populateGrid(String[] scheduleArray) {
        if (scheduleArray == null) return;

        for (int index = 0; index < scheduleArray.length; index++) {
            Button button;
            int j = index % 5; // Day (column)
            int i = index / 5; // Time slot (row)

            if (scheduleArray[index] != null && !"null".equals(scheduleArray[index].trim())) {
                String message = "remove," + i + "-" + j;
                String[] info = scheduleArray[index].trim().split(" ");
                button = new Button(info[0] + "\n" + info[1] + " " + info[2]);
                button.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");

                button.setOnAction(event -> {
                    out.println(message);
                    RemoveButtonHandler removeButtonHandler = new RemoveButtonHandler(stage, homeScene, in, out);
                    removeButtonHandler.handle(null);
                });

                System.out.println("Button added at " + index);
            } else {
                button = new Button(" ");
                button.setStyle("-fx-background-color: #ccffda; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");

                String position = i + "-" + j;
                button.setOnAction(event -> {
                    AddWindow addWindow = new AddWindow(out, position);
                    addWindow.showWindow(stage);
                });
            }

            button.setMinSize(80, 40);
            button.setPrefSize(100, 50);
            button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);

            gridPane.add(button, j + 1, i + 1); // Fix: Days (j) as columns, Times (i) as rows
        }
    }

    public void populateGridForView(String[] scheduleArray) {
        if (scheduleArray == null) return;

        for (int index = 0; index < scheduleArray.length; index++) {
            int j = index % 5; // Day (column)
            int i = index / 5; // Time slot (row)

            Label label;
            if (scheduleArray[index] != null && !"null".equals(scheduleArray[index].trim())) {
                // If the slot has data, display it in a label
                String[] info = scheduleArray[index].trim().split(" ");
                label = new Label(info[0] + "\n" + info[1] + " " + info[2]);
                label.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                // If the slot is empty, display an empty label
                label = new Label(" ");
                label.setStyle("-fx-background-color: #ccffda; -fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");
            }

            label.setMinSize(80, 40);
            label.setPrefSize(100, 50);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            GridPane.setHgrow(label, Priority.ALWAYS);
            GridPane.setVgrow(label, Priority.ALWAYS);

            gridPane.add(label, j + 1, i + 1); // Add the label to the grid
        }
    }

    public GridPane getGridPane() {
        return gridPane;
    }
}
