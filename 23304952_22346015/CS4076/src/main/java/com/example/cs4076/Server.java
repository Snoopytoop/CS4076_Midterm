package com.example.cs4076;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    //server socket
    private static ServerSocket serverSocket = null;

    //array to hold lectures
    static Lecture[][] lectures = new Lecture[9][5];

    //arraylist to store messages
    static List<String> messages = new ArrayList<>();

    //set up the server socket
    private static void setup() {
        //load messages
        loadMessagesFromFile();

        //load timetable
        loadTimetableFromFile();

        try {
            serverSocket = new ServerSocket(1234);
            System.out.println("Server is running... Waiting for clients...");
            accept();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    //accept messages from clients
    private static void accept() {
        while (true) {
            try (Socket link = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                 PrintWriter out = new PrintWriter(link.getOutputStream(), true)) {

                System.out.println("Client connected: " + link.getInetAddress());

                String message = in.readLine(); //read first message before loop

                while (message != null) {
                    //if client is requesting array,
                    if (message.split(",")[0].equals("arrayRequest")) {
                        String output = convertArrayToString();
                        out.println(output);
                    }
                    //if client wants to remove a lecture
                    else if (message.split(",")[0].equals("remove")) {
                        String[] arrParts = message.split(",")[1].split("-");
                        int row = Integer.parseInt(arrParts[0]);
                        int col = Integer.parseInt(arrParts[1]);

                        //remove the lecture
                        lectures[row][col] = null;

                        //save the updated timetable#
                        saveTimetableToFile();

                        //send new timetable to server
                        String output = convertArrayToString();
                        out.println(output);
                    }
                    // client wants to add a lecture
                    else if (message.split(",")[0].equals("add")) {
                        String[] arrParts = message.split(",")[1].split("-");
                        String subject = arrParts[0];
                        String room = arrParts[1];
                        System.out.println("room: " + room);
                        int row = Integer.parseInt(arrParts[2]);
                        int col = Integer.parseInt(arrParts[3]);

                        lectures[row][col] = new Lecture(subject, room);

                        //save the updated timetable
                        saveTimetableToFile();

                        String output = convertArrayToString();
                        out.println(output);
                    }
                    // client trying to send a message to the message board
                    else if (message.startsWith("sendMessage,")) {
                        String newMessage = message.substring(message.indexOf(",") + 1);
                        System.out.println("newMessage: " + newMessage);
                        messages.add(newMessage); //add the message to the messages array
                        saveMessagesToFile(); //save the updated messages
                        out.println("Message sent successfully!");
                    }
                    //check if client is requesting the messages for messageboard
                    else if (message.split(",")[0].equals("fetchMessages")) {
                        System.out.println("Sending Messages to client: " + String.join("&&", messages)); //debug statement
                        out.println(String.join("&&", messages)); //send the messages to the client
                    }

                    //check if "IncorrectActionException" has been thrown
                    else if (message.equals("Error! Incorrect action!")) {
                        System.out.println("Exception of type: IncorrectActionException thrown");
                    }

                    //terminate if client sends stop message
                    else if (message.equals("STOP")) {
                        out.println("TERMINATE");
                    }

                    message = in.readLine();//read next message
                }

                //client disconnected
                System.out.println("Client disconnected: " + link.getInetAddress());

            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
    }

    //convert the lecture array to a string for sending
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

    //store messages in a file (make persistent)
    private static void saveMessagesToFile() {
        try (FileWriter writer = new FileWriter("messages.txt")) {
            for (String message : messages) {
                writer.write(message + System.lineSeparator()); //write each message to a new line
            }
            System.out.println("Messages saved to file.");
        } catch (IOException e) {
            System.err.println("Error saving messages to file: " + e.getMessage());
        }
    }

    //load messages from the file
    private static void loadMessagesFromFile() {
        File file = new File("messages.txt");
        if (!file.exists()) {
            System.out.println("No existing messages file found. Starting with an empty list.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                messages.add(line); //add each line (message) to the messages list
            }
            System.out.println("Messages loaded from file.");
        } catch (IOException e) {
            System.err.println("Error loading messages from file: " + e.getMessage());
        }
    }

    //save timetable to a file (make changes persistent)
    private static void saveTimetableToFile() {
        try (FileWriter writer = new FileWriter("timetable.txt")) {
            for (int i = 0; i < lectures.length; i++) {
                for (int j = 0; j < lectures[i].length; j++) {
                    if (lectures[i][j] != null) {
                        writer.write(i + "," + j + "," + lectures[i][j].getName() + "," + lectures[i][j].getRoom() + System.lineSeparator());
                    }
                }
            }
            System.out.println("Timetable saved to file.");
        } catch (IOException e) {
            System.err.println("Error saving timetable to file: " + e.getMessage());
        }
    }

    //load timetable from file
    private static void loadTimetableFromFile() {
        File file = new File("timetable.txt");
        if (!file.exists()) {
            System.out.println("No existing timetable file found. Starting with an empty timetable.");
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
            System.out.println("Timetable loaded from file.");
        } catch (IOException e) {
            System.err.println("Error loading timetable from file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        setup();
    }
}