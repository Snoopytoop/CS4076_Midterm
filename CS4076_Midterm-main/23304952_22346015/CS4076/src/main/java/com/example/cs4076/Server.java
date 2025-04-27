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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.BooleanSupplier;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Server extends Application {

    /* ---------- DATA & CONSTANTS ---------- */

    // Network
    private static ServerSocket serverSocket;
    private static final ClientControl clientControl = ClientControl.getInstance();

    // Lecture data
    private static final Lecture[][] lectures = new Lecture[9][5];
    private static final List<String> messages = new ArrayList<>();

    // Logging
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_PATH = Path.of("serverLog.txt");
    private static final Object FILE_LOCK = new Object();
    private static TextArea logArea;                 // UI component
    private static final List<String> logBuffer = new ArrayList<>(); // holds logs before UI exists

    // UI
    private StackPane rootPane;
    private VBox mainPanel;
    private ScrollPane clientControlPanel;
    private VBox clientListContainer;
    private boolean showingClientPanel = false;

    // Client‑row bookkeeping
    private final ConcurrentHashMap<Socket, HBox> clientUiElements = new ConcurrentHashMap<>();

    // Thread‑safe persistence guards
    private static final Object lockMessages  = new Object();
    private static final Object lockTimetable = new Object();
    private static boolean isWritingMessages  = false;
    private static boolean isWritingTimetable = false;

    /* ---------- APPLICATION ENTRY ---------- */

    public static void main(String[] args) {
        new Thread(Server::setup).start();
        launch(args);                      // JavaFX UI
    }

    private static void setup() {
        loadMessagesFromFile();
        loadTimetableFromFile();

        try {
            serverSocket = new ServerSocket(1234);
            logMessage("Server started on port 1234");
            accept();
        } catch (IOException ex) {
            logMessage("Server error: " + ex.getMessage());
        }
    }

    private static void accept() {
        while (true) {
            try {
                Socket link = serverSocket.accept();
                clientControl.registerClient(link);
                logMessage("New connection: " + link.getInetAddress().getHostAddress());

                new Thread(new ClientHandler(link)).start();
            } catch (IOException ex) {
                logMessage("Connection error: " + ex.getMessage());
            }
        }
    }

    /* ---------- JAVAFX UI ---------- */

    @Override
    public void start(Stage primaryStage) {
        clientControl.setServerUI(this);

        mainPanel          = createMainPanel();
        clientControlPanel = createClientControlPanel();
        clientControlPanel.setVisible(false);

        rootPane = new StackPane(mainPanel, clientControlPanel);

        Scene scene = new Scene(rootPane, 850, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Lecture Server Control Center");
        primaryStage.show();
    }

    private VBox createMainPanel() {
        Label header = new Label("SERVER CONTROL PANEL");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setTextFill(Color.DARKBLUE);

        Label statusLabel = new Label("Server Status: RUNNING");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label clientCounter = new Label();
        clientCounter.textProperty().bind(
                clientControl.clientCountProperty().asString("Active Connections: %d"));
        clientCounter.setFont(Font.font("Arial", 18));
        clientCounter.setTextFill(Color.GREEN);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(400);
        logBuffer.forEach(s -> logArea.appendText(s + '\n'));
        logBuffer.clear();

        Button clientBtn = new Button("Client Management");
        clientBtn.setOnAction(e -> toggleClientPanel());

        Button logBtn = new Button("View Server Log");
        logBtn.setOnAction(e -> showServerLog());


        Button exitBtn = new Button("Shutdown Server");
        exitBtn.setStyle("-fx-background-color: #ff4444;");
        exitBtn.setOnAction(e -> shutdownServer());

        HBox buttons = new HBox(20, clientBtn, logBtn, exitBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox panel = new VBox(20, header, statusLabel, clientCounter, logArea, buttons);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #f5f5f5;");
        return panel;
    }

    private ScrollPane createClientControlPanel() {
        Label header = new Label("CLIENT MANAGEMENT");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setTextFill(Color.DARKBLUE);

        Label connectionLabel = new Label();
        connectionLabel.textProperty().bind(
                clientControl.clientCountProperty().asString("ACTIVE CONNECTIONS: %d"));
        connectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        connectionLabel.setTextFill(Color.BLUE);

        clientListContainer = new VBox(5);
        clientListContainer.setPadding(new Insets(10));

        Button refreshBtn = new Button("Refresh List");
        refreshBtn.setOnAction(e -> refreshClientList());

        Button backBtn = new Button("Back to Main");
        backBtn.setOnAction(e -> toggleClientPanel());

        HBox buttonBox = new HBox(20, refreshBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox content = new VBox(15, header, connectionLabel,
                clientListContainer, buttonBox);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #eef7ff;");
        return sp;
    }

    /* ---------- UI HELPERS ---------- */

    public void addClientToUi(Socket client) {
        Platform.runLater(() -> { //forces JavaFX thread execution
            String ip = client.getInetAddress().getHostAddress(); //hashmap

            Label ipLabel = new Label(ip);
            ipLabel.setPrefWidth(200);

            Button kick = new Button("Disconnect");
            kick.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white;");
            kick.setOnAction(e -> kickClient(client));

            HBox row = new HBox(15, ipLabel, kick);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 15, 8, 15));
            row.setStyle("-fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");

            clientUiElements.put(client, row);
            clientListContainer.getChildren().add(row);
        });
    }

    public void removeClientFromUi(Socket client) {
        Platform.runLater(() -> {
            HBox row = clientUiElements.remove(client);
            if (row != null) clientListContainer.getChildren().remove(row);
        });
    }

    private void refreshClientList() {
        clientListContainer.getChildren().clear();
        clientUiElements.clear();
        clientControl.getConnectedClients().forEach(this::addClientToUi);
    }

    /** Opens a modal window showing the full contents of serverLog.txt */
    private void showServerLog() {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_PATH.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) content.append(line).append('\n');
        } catch (IOException ex) {
            content.append("Unable to read log file: ").append(ex.getMessage());
        }

        TextArea ta = new TextArea(content.toString());
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setPrefWidth(700);
        ta.setPrefHeight(500);

        Alert dlg = new Alert(AlertType.INFORMATION);
        dlg.setTitle("Server Log");
        dlg.getDialogPane().setContent(ta);
        dlg.setHeaderText("Server Log – " + LOG_PATH.toAbsolutePath());
        dlg.getDialogPane().setPrefSize(720, 560);
        dlg.showAndWait();
    }


    private void toggleClientPanel() {
        showingClientPanel = !showingClientPanel;
        mainPanel.setVisible(!showingClientPanel);
        clientControlPanel.setVisible(showingClientPanel);
        if (showingClientPanel) refreshClientList();
    }

    private void kickClient(Socket client) {
        try {
            // 1) tell the client to shut itself down
            new PrintWriter(client.getOutputStream(), true).println("TERMINATE");

            // 2) add your log entry
            logMessage("Disconnecting client: " + client.getInetAddress().getHostAddress());

            // 3) close the socket
            client.close();
        } catch (IOException ex) {
            logMessage("Error disconnecting client: " + ex.getMessage());
        }
    }


    private void shutdownServer() {
        logMessage("Initiating server shutdown...");
        clientControl.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ex) {
            logMessage("Error during shutdown: " + ex.getMessage());
        }
        Platform.exit();
        System.exit(0);
    }

    /* ---------- LOGGING ---------- */

    static void logMessage(String msg) {
        String entry = "[" + LocalDateTime.now().format(TS) + "] " + msg;

        // console
        System.out.println(entry);

        // UI
        if (logArea != null) {
            Platform.runLater(() -> logArea.appendText(entry + '\n'));
        } else {
            logBuffer.add(entry);
        }

        // file (append, thread‑safe)
        synchronized (FILE_LOCK) {
            try (FileWriter fw = new FileWriter(LOG_PATH.toFile(), true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter  pw = new PrintWriter(bw))
            {
                pw.println(entry);
            } catch (IOException ex) {
                // fallback to console only — do NOT recurse
                System.err.println("Failed to write log: " + ex.getMessage());
            }
        }
    }

    /* ---------- CLIENT MESSAGE HANDLING ---------- */

    static void handleClientMessage(String message, PrintWriter out) {
        logMessage("Received message: " + message);

        if (message.startsWith("arrayRequest")) {
            out.println(convertArrayToString());

        } else if (message.startsWith("remove")) {
            String[] p = message.split(",")[1].split("-");
            int r = Integer.parseInt(p[0]), c = Integer.parseInt(p[1]);
            lectures[r][c] = null;
            saveTimetableToFile();
            out.println(convertArrayToString());

        } else if (message.startsWith("add")) {
            String[] p = message.split(",")[1].split("-");
            int r = Integer.parseInt(p[2]), c = Integer.parseInt(p[3]);
            lectures[r][c] = new Lecture(p[0], p[1]);
            saveTimetableToFile();
            out.println(convertArrayToString());

        } else if (message.startsWith("sendMessage,")) {
            messages.add(message.substring(message.indexOf(',') + 1));
            saveMessagesToFile();
            out.println("Message sent successfully!");

        } else if (message.startsWith("fetchMessages")) {
            out.println(String.join("&&", messages));

        } else if (message.equals("STOP")) {
            out.println("TERMINATE");

        } else if (message.equals("optimizeTimetable")) {
            optimizeTimetable();
            saveTimetableToFile();
            out.println("optimizationComplete");
        }
    }

    private static String convertArrayToString() {
        StringBuilder sb = new StringBuilder();
        for (Lecture[] row : lectures)
            for (Lecture lec : row)
                sb.append(lec != null ? lec.getName() + ' ' + lec.getRoom() : "null").append(',');
        return sb.toString();
    }

    /* ---------- FILE PERSISTENCE (messages & timetable) ---------- */

    private static void saveMessagesToFile() {
        synchronized (lockMessages) {
            waitWhile(() -> isWritingMessages, lockMessages);
            isWritingMessages = true;
            try (FileWriter w = new FileWriter("messages.txt")) {
                for (String m : messages) w.write(m + System.lineSeparator());
                logMessage("Messages saved to file.");
            } catch (IOException ex) {
                logMessage("Error saving messages: " + ex.getMessage());
            } finally {
                isWritingMessages = false;
                lockMessages.notifyAll();
            }
        }
    }

    private static void loadMessagesFromFile() {
        synchronized (lockMessages) {
            waitWhile(() -> isWritingMessages, lockMessages);
            File f = new File("messages.txt");
            if (!f.exists()) { logMessage("No messages file found."); return; }
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                messages.clear();
                br.lines().forEach(messages::add);
                logMessage("Messages loaded from file.");
            } catch (IOException ex) {
                logMessage("Error loading messages: " + ex.getMessage());
            } finally { lockMessages.notifyAll(); }
        }
    }

    private static void saveTimetableToFile() {
        synchronized (lockTimetable) {
            waitWhile(() -> isWritingTimetable, lockTimetable);
            isWritingTimetable = true;
            try (FileWriter w = new FileWriter("timetable.txt")) {
                for (int r = 0; r < lectures.length; r++)
                    for (int c = 0; c < lectures[r].length; c++)
                        if (lectures[r][c] != null)
                            w.write(r + "," + c + "," +
                                    lectures[r][c].getName() + "," +
                                    lectures[r][c].getRoom() + System.lineSeparator());
                logMessage("Timetable saved to file.");
            } catch (IOException ex) {
                logMessage("Error saving timetable: " + ex.getMessage());
            } finally {
                isWritingTimetable = false;
                lockTimetable.notifyAll();
            }
        }
    }

    private static void loadTimetableFromFile() {
        synchronized (lockTimetable) {
            waitWhile(() -> isWritingTimetable, lockTimetable);
            File f = new File("timetable.txt");
            if (!f.exists()) { logMessage("No timetable file found."); return; }
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    lectures[Integer.parseInt(p[0])]
                            [Integer.parseInt(p[1])] = new Lecture(p[2], p[3]);
                }
                logMessage("Timetable loaded from file.");
            } catch (IOException ex) {
                logMessage("Error loading timetable: " + ex.getMessage());
            } finally { lockTimetable.notifyAll(); }
        }
    }

    /* ---------- TIMETABLE OPTIMISATION ---------- */

    //optimise timetable using fork-join
    private static void optimizeTimetable() {
        //create fork-join pool
        ForkJoinPool pool = new ForkJoinPool();
        //create range 0 - 5 (days of week)
        pool.invoke(new TimetableOptimizer(0, 5));
        //log message
        logMessage("Timetable optimization complete for all days");
    }

    static class TimetableOptimizer extends RecursiveAction {
        private final int start, end;

        //constructor
        TimetableOptimizer(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            //if one day left to process
            if (end - start <= 1) {
                //optimise this single day
                optimizeDay(start);

            } else {
                //split work in half
                //find midpoint to split at
                int mid = (start + end) / 2;
                //fork both tasks so they run in parallel
                invokeAll(
                        //create sub-task for each half
                        new TimetableOptimizer(start, mid),
                        new TimetableOptimizer(mid, end)
                );
            }
        }
    }

    //moves lectures to earliest available slots
    private static void optimizeDay(int day) {
        //iterate through time slots for a given day (0 - 8)
        for (int slot = 0; slot < 9; slot++) {
            Lecture lec = lectures[slot][day];
            if (lec == null) continue;
            //try move this lecture to an earlier available timeslot
            for (int earlier = 0; earlier < slot; earlier++) {
                // if empty lecture slot
                if (lectures[earlier][day] == null) {
                    //move the lecture
                    lectures[earlier][day] = lec;
                    lectures[slot][day]   = null;
                    //log the move
                    logMessage(String.format("Moved %s from %d-%d to %d-%d",
                            lec.getName(), slot, day, earlier, day));
                    break;
                }
            }
        }
    }

    /* ---------- UTILITY ---------- */

    private static void waitWhile(BooleanSupplier cond, Object lock) {
        while (cond.getAsBoolean()) {
            try { lock.wait(); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
        }
    }

    /* ---------- CLIENT HANDLER ---------- */

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        ClientHandler(Socket s) { this.socket = s; }

        @Override public void run() {
            try (BufferedReader in  = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
            {
                String line;
                while ((line = in.readLine()) != null) {
                    handleClientMessage(line, out);
                    if ("STOP".equals(line)) break;
                }
            } catch (IOException ex) {
                logMessage("Error handling client: " + ex.getMessage());
            } finally {
                try { socket.close(); }
                catch (IOException ignored) {}
                clientControl.unregisterClient(socket);
            }
        }
    }
}
