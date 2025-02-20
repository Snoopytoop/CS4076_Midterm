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
                message = in.readLine();  // Read the first message before entering the loop
                while (message != null) {
                    System.out.println("Received: " + message);
                    //out.println("Echo: " + message);

                   // String messageHead = message.split("-")[0];
                    //String messageBody = message.split("-")[1];

                    // method for testing
                    //if (messageHead.equals("x")) {
                    //    arrLst.add(messageBody);
                    //    System.out.println(arrLst.getLast());
                    //}

                    // condition to send array
                    if (message.equals("arrayRequest")) {
                        //System.out.println(Arrays.deepToString(lectures));
                        out.println(Arrays.deepToString(lectures));
                        //System.out.println("request recieved");
                    }

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

