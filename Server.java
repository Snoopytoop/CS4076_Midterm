import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    // Initialising ServerSocket
    private static ServerSocket serverSocket = null;

    // Utility method for setting up server socket
    private static void setup() {
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
                message = in.readLine();  // Read the first message before entering the loop
                while (message != null) {
                    System.out.println("Received: " + message);
                    out.println("Echo: " + message);

                    message = in.readLine();  // Read the next message
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

