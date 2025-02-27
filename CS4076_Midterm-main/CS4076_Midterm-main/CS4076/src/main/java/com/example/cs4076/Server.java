package com.example.cs4076;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    // Initialising ServerSocket
    private static ServerSocket serverSocket = null;

    // This is a basic arrayList I just made to text things
    static ArrayList<String> arrLst = new ArrayList<>();


    // basic array used to test things
    static Lecture[][] lectures = new Lecture[9][5];

    static ArrayList<String> messageBoard = new ArrayList<>();


    // Utility method for setting up server socket
    private static void setup() {

        // also for testing
        lectures[0][0] = new Lecture("Programming", "FB-028");
        lectures[3][4] = new Lecture("Maths", "FB-029");

        try {
            serverSocket = new ServerSocket(1234);
            System.out.println("Server is running... Waiting for clients...");
            accept();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Utility method for accepting messages
    private static void accept() {
        while (true) {
            try (Socket link = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(link.getInputStream()));
                 PrintWriter out = new PrintWriter(link.getOutputStream(), true)) {

                System.out.println("Client connected: " + link.getInetAddress());

                String message;
                while ((message = in.readLine()) != null) {  // Read messages in a loop
                    System.out.println("Received: " + message);

                    // Split the message into head and body
                    String[] parts = message.split(",");
                    String head = parts[0];
                    String body = parts.length > 1 ? parts[1] : "";  // Handle cases where body might be missing

                    // Handle array request
                    if (head.equals("arrayRequest")) {
                        String output = "";
                        for (int i = 0; i < lectures.length; i++) {
                            for (int j = 0; j < lectures[i].length; j++) {
                                if (lectures[i][j] != null) {
                                    output = output + lectures[i][j].getName() + " " + lectures[i][j].getRoom() + ",";
                                } else {
                                    output = output + "null , ";
                                }
                            }
                        }
                        out.println(output);
                    }

                    else if (head.equals("sendMessage")) {
                        messageBoard.add(0, body); // Add message at the top of the list
                        out.println("Message sent successfully");
                        System.out.println("Message added: " + body); // Debug statement
                    }

                    else if (head.equals("viewMessageBoard")) {
                        StringBuilder messages = new StringBuilder();
                        for (String msg : messageBoard) {
                            messages.append(msg).append("\n"); // Add each message followed by a newline
                        }
                        String messageString = messages.toString();
                        System.out.println("Sending messages:\n" + messageString); // Debug statement
                        out.println(messageString); // Send all messages as a single string
                    }

                    // Handle remove lecture request
                    else if (head.equals("remove")) {
                        String[] arrParts = body.split("-");
                        int row = Integer.parseInt(arrParts[0]);
                        int col = Integer.parseInt(arrParts[1]);
                        lectures[row][col] = null;

                        String output = "";
                        for (int i = 0; i < lectures.length; i++) {
                            for (int j = 0; j < lectures[i].length; j++) {
                                if (lectures[i][j] != null) {
                                    output = output + lectures[i][j].getName() + " " + lectures[i][j].getRoom() + ",";
                                } else {
                                    output = output + "null , ";
                                }
                            }
                        }
                        out.println(output);
                    }
                }

            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }

    }

    public static void main(String[] args) {
        setup();

    }
}

