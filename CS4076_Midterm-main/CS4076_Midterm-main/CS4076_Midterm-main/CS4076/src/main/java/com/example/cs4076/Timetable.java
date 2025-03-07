package com.example.cs4076;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

public class Timetable {
    private final GridPane gridPane;

    // Days of the week
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    // Timeslots
    private static final String[] TIMES = {"9-10", "10-11", "11-12", "12-13", "13-14", "14-15", "15-16", "16-17", "17-18"};

    public Timetable() {
        gridPane = new GridPane();
        setupGrid();
    }

    // Set up the grid layout and styling
    private void setupGrid() {
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(20, 20, 20, 20)); // Increased padding
        gridPane.setVgap(10); // Increased vertical gap
        gridPane.setHgap(10); // Increased horizontal gap
        gridPane.setStyle(
                "-fx-border-color: #2E86C1; " + // Border color
                        "-fx-border-width: 2; " +
                        "-fx-background-color: #EBF5FB; " + // Light blue background
                        "-fx-padding: 20;"
        );

        // Make columns and rows resizable
        for (int i = 0; i <= TIMES.length; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS); // Allow columns to grow
            column.setFillWidth(true); // Fill available width
            gridPane.getColumnConstraints().add(column);
        }

        for (int i = 0; i <= DAYS.length; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS); // Allow rows to grow
            row.setFillHeight(true); // Fill available height
            gridPane.getRowConstraints().add(row);
        }

        // Add day labels to the grid
        for (int i = 0; i < DAYS.length; i++) {
            Label dayLabel = new Label(DAYS[i]);
            dayLabel.setStyle(
                    "-fx-font-size: 14px; " + // Larger font size
                            "-fx-font-weight: bold; " + // Bold text
                            "-fx-text-fill: #2E86C1;" // Blue text color
            );
            gridPane.add(dayLabel, 0, i + 1);
        }

        // Add time labels to the grid
        for (int j = 0; j < TIMES.length; j++) {
            Label timeLabel = new Label(TIMES[j]);
            timeLabel.setStyle(
                    "-fx-font-size: 14px; " + // Larger font size
                            "-fx-font-weight: bold; " + // Bold text
                            "-fx-text-fill: #2E86C1;" // Blue text color
            );
            gridPane.add(timeLabel, j + 1, 0);
        }
    }

    // Method to handle button click
    private void handleSlotSelection(int dayIndex, int timeIndex) {
        System.out.println("Selected: " + DAYS[dayIndex] + " " + TIMES[timeIndex]);
    }


    // Populate the grid with lecture data
    public void populateGrid(String[] scheduleArray) {
        if (scheduleArray == null) return;

        for (int i = 0; i < DAYS.length; i++) {
            for (int j = 0; j < TIMES.length; j++) {
                int index = i * TIMES.length + j;
                Button button;

                if (index < scheduleArray.length && !"null".equals(scheduleArray[index].trim())) {
                    String[] info = scheduleArray[index].trim().split(" ");
                    button = new Button(info[0] + "\n" + info[1]);
                    button.setStyle("-fx-background-color: #87CEFA;" +
                            "-fx-font-size: 12px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold;");
                } else {
                    button = new Button(" "); // Empty button for null slots
                    button.setStyle("-fx-background-color: #ccffda;" +
                            "-fx-font-size: 12px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold;");
                }

                // Set uniform size for all buttons
                button.setMinSize(80, 40);  // Minimum width and height
                button.setPrefSize(100, 50); // Preferred width and height
                button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Expand to fill cell

                // Style the button
                /*
                button.setStyle(
                        "-fx-font-size: 12px; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold;"
                );

                 */

                // Make the button expand fully inside the grid cell
                GridPane.setHgrow(button, Priority.ALWAYS);
                GridPane.setVgrow(button, Priority.ALWAYS);

                gridPane.add(button, j + 1, i + 1);
            }
        }
    }


    // Add buttons for null slots (for the "Add Lecture" view)
    public void addButtonsForNullSlots(String[] scheduleArray, EventHandler<ActionEvent> buttonHandler) {
        if (scheduleArray == null) return;

        for (int i = 0; i < DAYS.length; i++) {
            for (int j = 0; j < TIMES.length; j++) {
                int index = i * TIMES.length + j;
                if (index < scheduleArray.length && "null".equals(scheduleArray[index])) {
                    Button slotButton = new Button("Choose this slot");
                    slotButton.setStyle(
                            "-fx-font-size: 12px; " + // Smaller font size for buttons
                                    "-fx-background-color: #2E86C1; " + // Blue background
                                    "-fx-text-fill: white; " + // White text
                                    "-fx-font-weight: bold;" // Bold text
                    );
                    slotButton.setOnAction(buttonHandler);
                    gridPane.add(slotButton, j + 1, i + 1);
                }
            }
        }
    }

    // Get the grid pane
    public GridPane getGridPane() {
        return gridPane;
    }
}