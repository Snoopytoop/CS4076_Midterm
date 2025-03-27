package com.example.cs4076;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private static InetAddress IPAddress; //server IP address
    private static int PORT = 1234; //server port
    private static Socket link = null; //connection to server
    private static BufferedReader in = null; //input stream from server
    private static PrintWriter out = null; //output stream to server

    private ExecutorService executorService; //background tasks executor

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
            IPAddress = InetAddress.getLocalHost(); //get server address
        } catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
    }

    private static void serverConnect() {
        try {
            link = new Socket(IPAddress, PORT); //connect to server
            System.out.println("Connected to server.");
        } catch (IOException e) {
            System.err.println("Error establishing connection to server.");
            e.printStackTrace();
            return;
        }
    }

    private static void setStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(link.getInputStream())); //set up input stream
            out = new PrintWriter(link.getOutputStream(), true); //set up output stream
        } catch (IOException e) {
            System.err.println("Error setting up streams.");
            e.printStackTrace();
            closeResources();
        }
    }

    //check for updates and TERMINATE message
    public void checkForMessagesAndUpdates(EditTimetableButtonHandler editTimetableButtonHandler) {
        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (in.ready()) {
                        String message = in.readLine();
                        if ("TERMINATE".equals(message)) {
                            //TERMINATE received, stop the program
                            Platform.runLater(() -> {
                                editTimetableButtonHandler.shutdownExecutor();
                                executorService.shutdownNow();
                                Platform.exit();
                                System.exit(0);
                            });
                            return;  //exit the loop and stop the program
                        } else if (message != null && !message.isEmpty()) {
                            //update received, refresh UI
                            Platform.runLater(() -> editTimetableButtonHandler.handleUpdate(message));
                        }
                    }
                    Thread.sleep(50); //sleep to avoid excessive CPU usage
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error while checking for messages or updates.");
            }
        });
    }

    //handle incorrect actions and show error message
    private void handleIncorrectAction() {
        try {
            throw new IncorrectActionException("Error! Incorrect action!");
        } catch (IncorrectActionException ex) {
            Platform.runLater(() -> showErrorPopup(ex.getMessage())); //show error message in popup
            out.println(ex.getMessage()); //send error message to server
        }
    }

    //create and show error popup
    private void showErrorPopup(String message) {
        Label errorLabel = new Label(message);
        Stage errorStage = new Stage();
        VBox errorBox = new VBox(errorLabel);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setStyle("-fx-background-color: #ffcccc; -fx-padding: 20;");
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

        executorService = Executors.newSingleThreadExecutor(); //single thread executor for background tasks

        //title
        Label label = new Label("Would you like to...");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setTextFill(Color.web("#333333"));

        //give each button different style
        Button view = createStyledButton("View Timetable", "#4CAF50");
        Button add = createStyledButton("Edit Timetable", "#2196F3");
        Button board = createStyledButton("View Message Board", "#FF9800");
        Button post = createStyledButton("Post a message", "#9C27B0");
        Button throwException = createStyledButton("Throw exception", "#F44336");
        Button stop = createStyledButton("STOP", "#607D8B");

        //vbox
        VBox box = new VBox(15); //add space between buttons
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20)); //padding for vbox
        box.setStyle("-fx-background-color: #f0f0f0;"); //background
        box.getChildren().addAll(label, view, add, board, post, throwException, stop);

        Scene homeScene = new Scene(box, Client.WIDTH, Client.HEIGHT);
        stage.setScene(homeScene);
        stage.setTitle("Homepage");
        stage.show();

        EditTimetableButtonHandler editTimetableHandler = new EditTimetableButtonHandler(stage, homeScene, in, out, executorService, this);
        ViewButtonHandler viewButtonHandler = new ViewButtonHandler(stage, homeScene, in, out);
        MessageBoardButtonHandler messageBoardButtonHandler = new MessageBoardButtonHandler(stage, homeScene, in, out);
        PostMessageButtonHandler postMessageButtonHandler = new PostMessageButtonHandler(stage, homeScene, in, out);

        add.setOnAction(editTimetableHandler);
        view.setOnAction(viewButtonHandler);
        board.setOnAction(messageBoardButtonHandler);
        post.setOnAction(postMessageButtonHandler);
        throwException.setOnAction(e -> handleIncorrectAction());
        stop.setOnAction(e -> out.println("STOP"));

        checkForMessagesAndUpdates(editTimetableHandler); //start checking for updates and TERMINATE signal
    }

    //create buttons
    Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 1);"
        );
        button.setMinWidth(200); //make all buttons same width
        button.setAlignment(Pos.CENTER);
        return button;
    }
}