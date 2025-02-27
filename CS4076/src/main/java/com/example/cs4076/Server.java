package org.example.javafx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    // Initializing ServerSocket
    private static ServerSocket serverSocket = null;

    // Basic array used to test things
    static Lecture[][] lectures = new Lecture[9][5];

    // Utility method for setting up server socket
    private static void setup() {
        // Initial test values
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

                String message = in.readLine(); // Read first message before loop

                while (message != null) {
                    // Condition to send array
                    if (message.split(",")[0].equals("arrayRequest")) {
                        String output = convertArrayToString();
                        out.println(output);
                    }
                    // Condition to remove a lecture
                    else if (message.split(",")[0].equals("remove")) {

                        String[] arrParts = message.split(",")[1].split("-");
                        int row = Integer.parseInt(arrParts[0]);
                        int col = Integer.parseInt(arrParts[1]);

                        // Remove the lecture
                        lectures[row][col] = null;

                        // Send updated array
                        String output = convertArrayToString();
                        out.println(output);
                    }

                    //condition to add a lecture (needs to be added)
                    else if (message.split(",")[0].equals("add")) {
                        String[] arrParts = message.split(",")[1].split("-");
                        String subject = arrParts[0];
                        String room = arrParts[1];
                        int row = Integer.parseInt(arrParts[2]);
                        int col = Integer.parseInt(arrParts[3]);

                        lectures[row][col] = new Lecture(subject, room);
                        String output = convertArrayToString();
                        out.println(output);
                    }

                    message = in.readLine();  // Read next message
                }

            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            }
        }
    }

    // Helper method to convert the lecture array to a string for sending
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

    public static void main(String[] args) {
        setup();
    }
}
