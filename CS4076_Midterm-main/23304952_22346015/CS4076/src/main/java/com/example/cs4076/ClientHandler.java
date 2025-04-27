package com.example.cs4076;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.example.cs4076.Server.handleClientMessage;
import static com.example.cs4076.Server.logMessage;


class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                logMessage("Received from " + clientSocket.getInetAddress() + ": " + message);
                handleClientMessage(message, out);
            }

        } catch (IOException e) {
            logMessage("Error handling client: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            logMessage("Client disconnected: " + clientSocket.getInetAddress());
        } catch (IOException e) {
            logMessage("Error closing client connection: " + e.getMessage());
        }
    }
}
