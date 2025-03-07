package org.example.midtermproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Homepage extends Application {
    private static InetAddress IPAddress;
    private static int PORT = 1234;
    private static Socket link = null;
    private static BufferedReader in = null;
    private static PrintWriter out = null;

    private ExecutorService executorService; // Shared executor service for background tasks

    private static void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (link != null) link.close();
        } catch (IOException e) {
            System.err.println("Error closing resources.");
            e.printStackTrace();
        }
    }

    private static void getAddress() {
        try {
            IPAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
    }

    private static void serverConnect() {
        try {
            link = new Socket(IPAddress, PORT);
            System.out.println("Connected to server.");
        } catch (IOException e) {
            System.err.println("Error establishing connection to server.");
            e.printStackTrace();
            return;
        }
    }

    private static void setStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            out = new PrintWriter(link.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up streams.");
            e.printStackTrace();
            closeResources();
        }
    }

    // A single method that both checks for updates and checks for the TERMINATE message
    public void checkForMessagesAndUpdates(EditTimetableButtonHandler editTimetableButtonHandler) {
        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (in.ready()) {
                        String message = in.readLine();
                        if ("TERMINATE".equals(message)) {
                            // TERMINATE received, stop the program
                            Platform.runLater(() -> {
                                editTimetableButtonHandler.shutdownExecutor();
                                executorService.shutdownNow();
                                Platform.exit();
                                System.exit(0);
                            });
                            return;  // Exit the loop and stop the program
                        } else if (message != null && !message.isEmpty()) {
                            // Update received, notify UI to refresh
                            Platform.runLater(() -> editTimetableButtonHandler.handleUpdate(message));
                        }
                    }
                    Thread.sleep(50); // Sleep to avoid excessive CPU usage
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error while checking for messages or updates.");
            }
        });
    }

    // Method to handle incorrect actions and display an error message
    private void handleIncorrectAction() {
        try {
            throw new IncorrectActionException("Error! Incorrect action!");
        } catch (IncorrectActionException ex) {
            Platform.runLater(() -> showErrorPopup(ex.getMessage())); // Show error message in a popup
            out.println(ex.getMessage()); // Send the error message to the server or log it
        }
    }

    // Method to create and display the error popup
    private void showErrorPopup(String message) {
        Label errorLabel = new Label(message);
        Stage errorStage = new Stage();
        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        Scene errorScene = new Scene(errorBox, 300, 100);
        errorStage.setScene(errorScene);
        errorStage.setTitle("Error");
        errorStage.show();
    }

    @Override
    public void start(Stage stage) {
            getAddress();
            serverConnect();
            setStreams();

            executorService = Executors.newSingleThreadExecutor(); // Use single thread executor for background tasks

            Label label = new Label("Would you like to...");
            Button view = new Button("View Timetable");
            Button add = new Button("Edit Timetable");
            Button board = new Button("View Message Board");
            Button post = new Button("Post a message");
            Button throwException = new Button("Throw exception");
            Button stop = new Button("STOP");

            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            box.setSpacing(10);
            box.getChildren().addAll(label, view, add, board, post, throwException, stop);
            Scene homeScene = new Scene(box, Client.WIDTH, Client.HEIGHT);
            stage.setScene(homeScene);
            stage.show();

            EditTimetableButtonHandler editTimetableHandler = new EditTimetableButtonHandler(stage, homeScene, in, out, executorService);

            ViewButtonHandler viewButtonHandler = new ViewButtonHandler(stage, homeScene, in, out);
            MessageBoardButtonHandler messageBoardButtonHandler = new MessageBoardButtonHandler(stage, homeScene, in, out);
            PostMessageButtonHandler postMessageButtonHandler = new PostMessageButtonHandler(stage, homeScene, in, out);

            add.setOnAction(editTimetableHandler);
            view.setOnAction(viewButtonHandler);
            board.setOnAction(messageBoardButtonHandler);
            post.setOnAction(postMessageButtonHandler);
            throwException.setOnAction(e -> handleIncorrectAction());
            stop.setOnAction(e -> out.println("STOP"));

            checkForMessagesAndUpdates(editTimetableHandler); // Start checking for both updates and TERMINATE signal


    }

}
