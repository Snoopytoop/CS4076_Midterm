package com.example.cs4076;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientControl {
    private static final ClientControl instance = new ClientControl();
    private final IntegerProperty clientCount = new SimpleIntegerProperty(0);
    private final Set<Socket> connectedClients = new HashSet<>();
    private final AtomicBoolean monitoringActive = new AtomicBoolean(false);
    private Thread monitoringThread;
    private Server serverUI; // Reference to server UI for updates

    private ClientControl() {
        startMonitoringThread();
    }

    public static ClientControl getInstance() {
        return instance;
    }

    public void setServerUI(Server server) {
        this.serverUI = server;
    }

    public Set<Socket> getConnectedClients() {
        synchronized (connectedClients) {
            return new HashSet<>(connectedClients);
        }
    }

    private void startMonitoringThread() {
        if (monitoringActive.compareAndSet(false, true)) {
            monitoringThread = new Thread(() -> {
                while (monitoringActive.get()) {
                    try {
                        // Check for disconnected clients
                        synchronized (connectedClients) {
                            connectedClients.removeIf(socket -> {
                                boolean isDisconnected = socket.isClosed() || !socket.isConnected();
                                if (isDisconnected) {
                                    Platform.runLater(() -> {
                                        clientCount.set(clientCount.get() - 1);
                                        if (serverUI != null) {
                                            serverUI.removeClientFromUi(socket);
                                        }
                                    });
                                }
                                return isDisconnected;
                            });
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.err.println("Monitoring error: " + e.getMessage());
                    }
                }
            });
            monitoringThread.setDaemon(true);
            monitoringThread.setName("ClientMonitoringThread");
            monitoringThread.start();
        }
    }

    public void registerClient(Socket clientSocket) {
        synchronized (connectedClients) {
            connectedClients.add(clientSocket);
            Platform.runLater(() -> {
                clientCount.set(clientCount.get() + 1);
                if (serverUI != null) {
                    serverUI.addClientToUi(clientSocket);
                }
            });
        }
    }

    public void unregisterClient(Socket clientSocket) {
        synchronized (connectedClients) {
            if (connectedClients.remove(clientSocket)) {
                Platform.runLater(() -> {
                    clientCount.set(clientCount.get() - 1);
                    if (serverUI != null) {
                        serverUI.removeClientFromUi(clientSocket);
                    }
                });
            }
        }
    }

    public void shutdown() {
        monitoringActive.set(false);
        if (monitoringThread != null) {
            monitoringThread.interrupt();
        }
        synchronized (connectedClients) {
            connectedClients.clear();
            Platform.runLater(() -> clientCount.set(0));
        }
    }

    public IntegerProperty clientCountProperty() {
        return clientCount;
    }

    public int getClientCount() {
        return clientCount.get();
    }
}