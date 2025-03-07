package com.example.cs4076;

import javafx.application.Application;
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

public class Homepage extends Application {

    // initialising IP address
    private static InetAddress IPAddress;
    // setting port number
    private static int PORT = 1234;
    // initialising Socket
    private static Socket link = null;
    // initialising BufferedReaders
    private static BufferedReader in = null;
    private static BufferedReader userEntry = null;
    // initialising Printwriter
    private static PrintWriter out = null;

    // Utility method for closing resources
    private static void closeResources(Socket socket, BufferedReader in, PrintWriter out, BufferedReader userEntry) {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (userEntry != null) userEntry.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources.");
            e.printStackTrace();
        }
    }

    // get local host IP address (server)
    private static void getAddress() {
        try {
            //set ip to localhost ip
            IPAddress = InetAddress.getLocalHost();
        }
        // catching exception if host not found
        catch (UnknownHostException e) {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
    }

    //connect to server
    private static void serverConnect() {
        try {
            link = new Socket(IPAddress, PORT);
            System.out.println("Connected to server.");
        } catch (IOException e) {
            System.err.println("Error establishing connection to server.");
            e.printStackTrace();
            return; //exits if connection fails
        }
    }

    //setup input and output for client
    private static void setStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(link.getInputStream()));
            out = new PrintWriter(link.getOutputStream(), true);
            userEntry = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            System.err.println("Error setting up streams.");
            e.printStackTrace();
            closeResources(link, in, out, userEntry);
            return;
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        //connect to server and set up input & output
        getAddress();
        serverConnect();
        setStreams();

        //GUI header and buttons
        Label label = new Label("Would you like to...");
        Button view = new Button("View Timetable");
        Button add = new Button("Edit Timetable");
        Button board = new Button("View Message Board");
        Button post = new Button("Post a message");

        //creating stage, scene, width and height
        //used in other windows to keep consistent
        stage.setTitle("Homepage");
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
        box.getChildren().addAll(label, view, add, board, post);
        Scene homeScene = new Scene(box, Client.WIDTH, Client.HEIGHT);
        stage.setScene(homeScene);
        stage.show();

        //handlers that take the window information
        EditTimetableButtonHandler editTimetableHandler = new EditTimetableButtonHandler(stage, homeScene, in, out);
        editTimetableHandler.checkForUpdates();


        ViewButtonHandler viewButtonHandler = new ViewButtonHandler(stage, homeScene, in, out);
        MessageBoardButtonHandler messageBoardButtonHandler = new MessageBoardButtonHandler(stage, homeScene, in, out);
        PostMessageButtonHandler postMessageButtonHandler = new PostMessageButtonHandler(stage, homeScene, in, out);

        //button handlers
        add.setOnAction(editTimetableHandler);
        view.setOnAction(viewButtonHandler);
        board.setOnAction(messageBoardButtonHandler);
        post.setOnAction(postMessageButtonHandler);
    }
}