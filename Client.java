import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

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

    // Utility method for getting IP address of local host
    private static void getAddress() {
        try
        {
            // setting IP address to address of local host
            IPAddress = InetAddress.getLocalHost();
        }
        // catching exception if host not found
        catch(UnknownHostException e)
        {
            System.out.println("Host ID not found!");
            System.exit(1);
        }
    }

    // Utility method for connecting to server
    private static void serverConnect() {
        try {
            link = new Socket(IPAddress, PORT); // Step 1.
            System.out.println("Connected to server.");
        } catch (IOException e) {
            System.err.println("Error establishing connection to server.");
            e.printStackTrace();
            return; // Exit if connection fails
        }
    }

    // Utility method for setting up input & output streams
    private static void setStreams() {
        try {
            in = new BufferedReader(new InputStreamReader(link.getInputStream())); // Step 2.
            out = new PrintWriter(link.getOutputStream(), true); // Step 2.
            userEntry = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            System.err.println("Error setting up streams.");
            e.printStackTrace();
            closeResources(link, in, out, userEntry);
            return;
        }
    }

    // Utility method for sending message
    private static void sendMessage() {
        try {
            System.out.println("Enter message to be sent to server: ");
            String message = userEntry.readLine();
            out.println(message); // Step 3.

            String response = in.readLine(); // Step 3.
            System.out.println("\nSERVER RESPONSE> " + response);
        } catch (IOException e) {
            System.err.println("Error during communication with server.");
            e.printStackTrace();
        } finally {
            closeResources(link, in, out, userEntry);
        }
    }

    public static void main(String[] args) {
       getAddress();
       serverConnect();
       setStreams();
       sendMessage();
    }
}


