package com.example.cs4076;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends Application {
    // Server components
    private static ServerSocket serverSocket = null;
    static Lecture[][] lectures = new Lecture[9][5];
    static List<String> messages = new ArrayList<>();
    private static TextArea logArea;
    private static final List<String> logBuffer = new ArrayList<>();
    private static ClientControl clientControl = ClientControl.getInstance();

    // UI Components
    private StackPane rootPane;
    private VBox mainPanel;
    private ScrollPane clientControlPanel;
    private VBox clientListContainer;
    private boolean showingClientPanel = false;

    // Client tracking
    private ConcurrentHashMap<Socket, HBox> clientUiElements = new ConcurrentHashMap<>();

    // Thread synchronization objects
    private static final Object lockMessages = new Object();
    private static boolean isWritingMessages = false;
    private static final Object lockTimetable = new Object();
    private static boolean isWritingTimetable = false;

    public static void main(String[] args) {
        new Thread(Server::setup).start();
        launch(args);
    }

    private static void setup() {
        loadMessagesFromFile();
        loadTimetableFromFile();

        try {
            serverSocket = new ServerSocket(1234);
            logMessage("Server started on port 1234");
            accept();
        } catch (IOException e) {
            logMessage("Server error: " + e.getMessage());
        }
    }

    private static void accept() {
        while (true) {
            try {
                Socket link = serverSocket.accept();
                clientControl.registerClient(link);
                logMessage("New connection: " + link.getInetAddress().getHostAddress());

                // Create and start a new thread for each client
                new Thread(new ClientHandler(link)).start();

            } catch (IOException e) {
                logMessage("Connection error: " + e.getMessage());
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        clientControl.setServerUI(this);

        // Create UI panels
        mainPanel = createMainPanel();
        clientControlPanel = createClientControlPanel();
        clientControlPanel.setVisible(false);

        // Setup root container
        rootPane = new StackPane(mainPanel, clientControlPanel);

        // Configure main window
        Scene scene = new Scene(rootPane, 850, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Lecture Server Control Center");
        primaryStage.show();
    }

    private VBox createMainPanel() {
        // Header
        Label header = new Label("SERVER CONTROL PANEL");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setTextFill(Color.DARKBLUE);

        // Connection status
        Label statusLabel = new Label("Server Status: RUNNING");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Client counter
        Label clientCounter = new Label();
        clientCounter.textProperty().bind(
                clientControl.clientCountProperty().asString("Active Connections: %d")
        );
        clientCounter.setFont(Font.font("Arial", 18));
        clientCounter.setTextFill(Color.GREEN);

        // Log display
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(400);

        // Load buffered logs
        for (String log : logBuffer) {
            logArea.appendText(log + "\n");
        }
        logBuffer.clear();

        // Control buttons
        HBox buttonBox = new HBox(20);
        Button clientBtn = new Button("Client Management");
        clientBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px;");
        clientBtn.setOnAction(e -> toggleClientPanel());

        Button exitBtn = new Button("Shutdown Server");
        exitBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px; -fx-background-color: #ff4444;");
        exitBtn.setOnAction(e -> shutdownServer());

        buttonBox.getChildren().addAll(clientBtn, exitBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Main layout
        VBox panel = new VBox(20, header, statusLabel, clientCounter, logArea, buttonBox);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #f5f5f5;");

        return panel;
    }

    private ScrollPane createClientControlPanel() {
        // Header
        Label header = new Label("CLIENT MANAGEMENT");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setTextFill(Color.DARKBLUE);

        // Connection summary
        Label connectionLabel = new Label();
        connectionLabel.textProperty().bind(
                clientControl.clientCountProperty().asString("ACTIVE CONNECTIONS: %d")
        );
        connectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        connectionLabel.setTextFill(Color.BLUE);

        // Client list container
        clientListContainer = new VBox(5);
        clientListContainer.setPadding(new Insets(10));
        clientListContainer.setStyle("-fx-background-color: white;");

        // Refresh button
        Button refreshBtn = new Button("Refresh List");
        refreshBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px;");
        refreshBtn.setOnAction(e -> refreshClientList());

        // Back button
        Button backBtn = new Button("Back to Main");
        backBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8px 16px;");
        backBtn.setOnAction(e -> toggleClientPanel());

        HBox buttonBox = new HBox(20, refreshBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Main layout
        VBox content = new VBox(15, header, connectionLabel, clientListContainer, buttonBox);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #eef7ff;");

        return scrollPane;
    }

    public void addClientToUi(Socket clientSocket) {
        Platform.runLater(() -> {
            String ipAddress = clientSocket.getInetAddress().getHostAddress();

            // Create UI elements for this client
            Label ipLabel = new Label(ipAddress);
            ipLabel.setFont(Font.font("Arial", 14));
            ipLabel.setPrefWidth(200);

            Button kickBtn = new Button("Disconnect");
            kickBtn.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white;");
            kickBtn.setOnAction(e -> kickClient(clientSocket));

            HBox clientRow = new HBox(15, ipLabel, kickBtn);
            clientRow.setAlignment(Pos.CENTER_LEFT);
            clientRow.setPadding(new Insets(8, 15, 8, 15));
            clientRow.setStyle("-fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");

            clientUiElements.put(clientSocket, clientRow);
            clientListContainer.getChildren().add(clientRow);
        });
    }

    public void removeClientFromUi(Socket clientSocket) {
        Platform.runLater(() -> {
            HBox clientRow = clientUiElements.remove(clientSocket);
            if (clientRow != null) {
                clientListContainer.getChildren().remove(clientRow);
            }
        });
    }

    private void kickClient(Socket clientSocket) {
        try {
            logMessage("Disconnecting client: " + clientSocket.getInetAddress().getHostAddress());
            clientSocket.close();
        } catch (IOException e) {
            logMessage("Error disconnecting client: " + e.getMessage());
        }
    }

    private void toggleClientPanel() {
        showingClientPanel = !showingClientPanel;
        mainPanel.setVisible(!showingClientPanel);
        clientControlPanel.setVisible(showingClientPanel);

        if (showingClientPanel) {
            refreshClientList();
        }
    }

    private void refreshClientList() {
        clientListContainer.getChildren().clear();
        clientUiElements.clear();

        for (Socket clientSocket : clientControl.getConnectedClients()) {
            addClientToUi(clientSocket);
        }
    }

    private void shutdownServer() {
        logMessage("Initiating server shutdown...");
        clientControl.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logMessage("Error during shutdown: " + e.getMessage());
        }
        Platform.exit();
        System.exit(0);
    }

    static void handleClientMessage(String message, PrintWriter out) {
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
        synchronized (lockMessages) {
            while (isWritingMessages) {
                try {
                    lockMessages.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            isWritingMessages = true;

            try (FileWriter writer = new FileWriter("messages.txt")) {
                for (String message : messages) {
                    writer.write(message + System.lineSeparator());
                }
                logMessage("Messages saved to file.");
            } catch (IOException e) {
                logMessage("Error saving messages to file: " + e.getMessage());
            } finally {
                isWritingMessages = false;
                lockMessages.notifyAll();
            }
        }
    }

    private static void loadMessagesFromFile() {
        synchronized (lockMessages) {
            while (isWritingMessages) {
                try {
                    lockMessages.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            File file = new File("messages.txt");
            if (!file.exists()) {
                logMessage("No existing messages file found. Starting with an empty list.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                messages.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    messages.add(line);
                }
                logMessage("Messages loaded from file.");
            } catch (IOException e) {
                logMessage("Error loading messages from file: " + e.getMessage());
            } finally {
                lockMessages.notifyAll();
            }
        }
    }

    private static void saveTimetableToFile() {
        synchronized (lockTimetable) {
            while (isWritingTimetable) {
                try {
                    lockTimetable.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            isWritingTimetable = true;

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
            } finally {
                isWritingTimetable = false;
                lockTimetable.notifyAll();
            }
        }
    }

    private static void loadTimetableFromFile() {
        synchronized (lockTimetable) {
            while (isWritingTimetable) {
                try {
                    lockTimetable.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

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
            } finally {
                lockTimetable.notifyAll();
            }
        }
    }

    private static void optimizeTimetable() {
        Thread[] dayThreads = new Thread[5];

        for (int day = 0; day < 5; day++) {
            final int currentDay = day;
            dayThreads[day] = new Thread(() -> optimizeDay(currentDay));
            dayThreads[day].start();
        }

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
        for (int timeSlot = 0; timeSlot < 9; timeSlot++) {
            Lecture lecture = lectures[timeSlot][day];
            if (lecture != null) {
                for (int earlierSlot = 0; earlierSlot < timeSlot; earlierSlot++) {
                    if (lectures[earlierSlot][day] == null) {
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

    static void logMessage(String msg) {
        System.out.println(msg);
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(msg + "\n"));
        } else {
            logBuffer.add(msg);
        }
    }

    // ClientHandler inner class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    handleClientMessage(inputLine, out);
                    if (inputLine.equals("STOP")) {
                        break;
                    }
                }
            } catch (IOException e) {
                logMessage("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    clientControl.unregisterClient(clientSocket);
                } catch (IOException e) {
                    logMessage("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}

//disconnet from server side doesn't close client, just freezes