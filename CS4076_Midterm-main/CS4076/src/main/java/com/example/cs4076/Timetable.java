package org.example.cs4076;
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

    // Days as columns, Times as rows
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

        // Columns represent days
        for (int i = 0; i <= DAYS.length; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setFillWidth(true);
            gridPane.getColumnConstraints().add(column);
        }

        // Rows represent time slots
        for (int i = 0; i <= TIMES.length; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            gridPane.getRowConstraints().add(row);
        }

        // Add time labels (now in first column)
        for (int i = 0; i < TIMES.length; i++) {
            Label timeLabel = new Label(TIMES[i]);
            timeLabel.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #2E86C1;"
            );
            gridPane.add(timeLabel, 0, i + 1);  // Time labels are now in the first column
        }

        // Add day labels (now in the first row)
        for (int j = 0; j < DAYS.length; j++) {
            Label dayLabel = new Label(DAYS[j]);
            dayLabel.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #2E86C1;"
            );
            gridPane.add(dayLabel, j + 1, 0); // Day labels are now in the first row
        }
    }

    private void handleSlotSelection(int dayIndex, int timeIndex) {
        System.out.println("Selected: " + TIMES[timeIndex] + " on " + DAYS[dayIndex]);
    }

    public void populateGrid(String[] scheduleArray) {
        if (scheduleArray == null) return;

        for (int i = 0; i < DAYS.length; i++) {  // Days in COLUMNS
            for (int j = 0; j < TIMES.length; j++) { // Times in ROWS
                int index = i * DAYS.length + j;  // Corrected index calculation
                Button button;

                if (index < scheduleArray.length && !"null".equals(scheduleArray[index].trim())) {
                    String message = "remove," + i + "-" + j;  // Corrected row & column order
                    String[] info = scheduleArray[index].trim().split(" ");
                    button = new Button(info[0] + "\n" + info[1]);
                    button.setStyle("-fx-background-color: #87CEFA;" +
                            "-fx-font-size: 12px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold;");

                    button.setOnAction(event -> {
                        out.println(message);
                        RemoveButtonHandler removeButtonHandler = new RemoveButtonHandler(stage, homeScene, in, out);
                        removeButtonHandler.handle(null);
                    });

                    System.out.println("button added at " + index);

                } else {
                    button = new Button(" ");
                    button.setStyle("-fx-background-color: #ccffda;" +
                            "-fx-font-size: 12px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold;");

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

                gridPane.add(button, i + 1, j + 1);  // Days (i) in columns, Times (j) in rows
            }
        }
    }


    public void addButtonsForNullSlots(String[] scheduleArray, EventHandler<ActionEvent> buttonHandler) {
        if (scheduleArray == null) return;

        for (int i = 0; i < TIMES.length; i++) {
            for (int j = 0; j < DAYS.length; j++) {
                int index = i * DAYS.length + j + 1; // Swapped indexing
                if (index < scheduleArray.length && "null".equals(scheduleArray[index])) {
                    Button slotButton = new Button("Choose this slot");
                    slotButton.setStyle(
                            "-fx-font-size: 12px; " +
                                    "-fx-background-color: #2E86C1; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-weight: bold;"
                    );
                    slotButton.setOnAction(buttonHandler);
                    gridPane.add(slotButton, j + 1, i + 1); // Swapped row and column
                }
            }
        }
    }

    public GridPane getGridPane() {
        return gridPane;
    }
}
