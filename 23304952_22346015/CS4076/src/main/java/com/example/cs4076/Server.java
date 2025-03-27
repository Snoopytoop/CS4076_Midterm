package com.example.cs4076;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Application {
    private static ServerSocket serverSocket = null;
    static Lecture[][] lectures = new Lecture[9][5];
    static List<String> messages = new ArrayList<>();
    private static TextArea logArea; // UI Log Display
    private static final List<String> logBuffer = new ArrayList<>(); // Store logs before UI initializes

    public static void main(String[] args) {
        // Start server in a background thread
        new Thread(Server::setup).start();

        // Start JavaFX application
        launch(args);
    }

    private static void setup() {
        loadMessagesFromFile();
        loadTimetableFromFile();

        try {
            serverSocket = new ServerSocket(1234);
            logMessage("Server is running... Waiting for clients...");
            accept();
        } catch (IOException e) {
            logMessage("Server error: " + e.getMessage());
        }
    }

    private static void accept() {
        while (true) {
            try (Socket link = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                 PrintWriter out = new PrintWriter(link.getOutputStream(), true)) {

                logMessage("Client connected: " + link.getInetAddress());

                String message = in.readLine();
                while (message != null) {
                    handleClientMessage(message, out);
                    message = in.readLine();
                }

                logMessage("Client disconnected: " + link.getInetAddress());

            } catch (IOException e) {
                logMessage("Connection error: " + e.getMessage());
            }
        }
    }

    private static void handleClientMessage(String message, PrintWriter out) {
        logMessage("Received message: " + message);

        if (message.startsWith("arrayRequest")) {
            out.println(convertArrayToString());
        } else if (message.startsWith("remove")) {
            String[] parts = message.split(",")[1].split("-");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            lectures[row][col] = null;
            saveTimetableToFile();
            out.println(convertArrayToString());
        } else if (message.startsWith("add")) {
            String[] parts = message.split(",")[1].split("-");
            String subject = parts[0];
            String room = parts[1];
            int row = Integer.parseInt(parts[2]);
            int col = Integer.parseInt(parts[3]);
            lectures[row][col] = new Lecture(subject, room);
            saveTimetableToFile();
            out.println(convertArrayToString());
        } else if (message.startsWith("sendMessage,")) {
            String newMessage = message.substring(message.indexOf(",") + 1);
            messages.add(newMessage);
            saveMessagesToFile();
            out.println("Message sent successfully!");
        } else if (message.startsWith("fetchMessages")) {
            out.println(String.join("&&", messages));
        } else if (message.equals("Error! Incorrect action!")) {
            logMessage("Exception of type: IncorrectActionException thrown");
        } else if (message.equals("STOP")) {
            out.println("TERMINATE");
        } else if (message.equals("optimizeTimetable")) {
            optimizeTimetable();
            out.println("optimizationComplete");
            saveTimetableToFile();
        }
    }

    private static String convertArrayToString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < lectures.length; i++) {
            for (int j = 0; j < lectures[i].length; j++) {
                if (lectures[i][j] != null) {
                    output.append(lectures[i][j].getName()).append(" ").append(lectures[i][j].getRoom()).append(",");
                } else {
                    output.append("null,");
                }
            }
        }
        return output.toString();
    }

    private static void saveMessagesToFile() {
        try (FileWriter writer = new FileWriter("messages.txt")) {
            for (String message : messages) {
                writer.write(message + System.lineSeparator());
            }
            logMessage("Messages saved to file.");
        } catch (IOException e) {
            logMessage("Error saving messages to file: " + e.getMessage());
        }
    }

    private static void loadMessagesFromFile() {
        File file = new File("messages.txt");
        if (!file.exists()) {
            logMessage("No existing messages file found. Starting with an empty list.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                messages.add(line);
            }
            logMessage("Messages loaded from file.");
        } catch (IOException e) {
            logMessage("Error loading messages from file: " + e.getMessage());
        }
    }

    private static void saveTimetableToFile() {
        try (FileWriter writer = new FileWriter("timetable.txt")) {
            for (int i = 0; i < lectures.length; i++) {
                for (int j = 0; j < lectures[i].length; j++) {
                    if (lectures[i][j] != null) {
                        writer.write(i + "," + j + "," + lectures[i][j].getName() + "," + lectures[i][j].getRoom() + System.lineSeparator());
                    }
                }
            }
            logMessage("Timetable saved to file.");
        } catch (IOException e) {
            logMessage("Error saving timetable to file: " + e.getMessage());
        }
    }

    private static void loadTimetableFromFile() {
        File file = new File("timetable.txt");
        if (!file.exists()) {
            logMessage("No existing timetable file found. Starting with an empty timetable.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                String subject = parts[2];
                String room = parts[3];
                lectures[row][col] = new Lecture(subject, room);
            }
            logMessage("Timetable loaded from file.");
        } catch (IOException e) {
            logMessage("Error loading timetable from file: " + e.getMessage());
        }
    }

    private static void optimizeTimetable() {
        // Create a thread for each day
        Thread[] dayThreads = new Thread[5];

        for (int day = 0; day < 5; day++) {
            final int currentDay = day;
            dayThreads[day] = new Thread(() -> optimizeDay(currentDay));
            dayThreads[day].start();
        }

        // Wait for all threads to complete
        for (Thread thread : dayThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                logMessage("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        logMessage("Timetable optimization complete for all days");
    }

    private static void optimizeDay(int day) {
        // Process each time slot for this day
        for (int timeSlot = 0; timeSlot < 9; timeSlot++) {
            Lecture lecture = lectures[timeSlot][day];
            if (lecture != null) {
                // Find earliest available slot for this lecture
                for (int earlierSlot = 0; earlierSlot < timeSlot; earlierSlot++) {
                    if (lectures[earlierSlot][day] == null) {
                        // Move lecture to earlier slot
                        lectures[earlierSlot][day] = lecture;
                        lectures[timeSlot][day] = null;
                        logMessage(String.format("Moved %s from %d-%d to %d-%d",
                                lecture.getName(), timeSlot, day, earlierSlot, day));
                        break;
                    }
                }
            }
        }
    }


    @Override
    public void start(Stage stage) {
        Label label = new Label("Lecture Server is Running...");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setTextFill(Color.web("#333333"));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(200);

        // Load buffered logs into UI
        for (String log : logBuffer) {
            logArea.appendText(log + "\n");
        }
        logBuffer.clear(); // Clear buffer after loading

        Button closeButton = new Button("Stop Server");
        closeButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        VBox box = new VBox(15, label, logArea, closeButton);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f0f0f0;");

        Scene homeScene = new Scene(box, 500, 400);
        stage.setScene(homeScene);
        stage.setTitle("Lecture Server");
        stage.show();
    }

    private static void logMessage(String msg) {
        System.out.println(msg);
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(msg + "\n"));
        } else {
            logBuffer.add(msg); // Store logs temporarily
        }
    }
}