import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class ChatServer {
    final static HashMap<String, ClientHandler> clients = new HashMap<>();

    public static void StartServer(int portNum) {
        ServerSocket servSock = null;
        String username = "";
        Boolean userAccepted;
        Pattern userPattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
        try {
            servSock = new ServerSocket(portNum);
            System.out.println("Server Started");
            System.out.println("Waiting for clients");
        } catch (IOException i) {
            System.out.println(i);
        }
        while(servSock != null) {
            Socket clientSock = null;
            try {
                userAccepted = false;
                clientSock = servSock.accept();
                System.out.println("Client accepted");
                PrintWriter out = new PrintWriter(clientSock.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                while(!userAccepted) {
                    username = in.readLine();
                    System.out.println(username);
                    if (clients.containsKey(username)) {
                        out.println("User already exits, enter a different name...");
                    }
                    else if (userPattern.matcher(username).find()) {
                        out.println("Username cannot contain special characters, enter a different name...");
                    }
                    else if (username == null) {
                        out.println("Username cannot be null, enter a different name...");
                    }
                    else {
                        userAccepted = true;
                    }
                }
                System.out.println("Assigning new thread to " + username);
                ClientHandler clientThread = new ClientHandler(clientSock, in, out, clients, username);
                clients.put(username, clientThread);
                clientThread.start();
                out.println(username + " accepted");
                System.out.println(clients);
                for (ClientHandler globalClient : clients.values()) {
                    globalClient.out.println("[SERVER] " + username + " has joined the chat");
                }
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }

    public static void main(String[] args) {
        StartServer(Integer.parseInt(args[0]));
    }
}

class ClientHandler extends Thread {
    final Socket sock;
    final BufferedReader in;
    final PrintWriter out;
    final HashMap<String, ClientHandler> clients;
    final String username;
    private String pmUser;
    private boolean global;
    public ClientHandler(Socket clientSock, BufferedReader in, PrintWriter out, HashMap<String, ClientHandler> clients, String username) {
        this.sock = clientSock;
        this.in = in;
        this.out = out;
        this.clients = clients;
        this.username = username;
        this.global = true;
        this.pmUser = "";
    }

    @Override
    public void run() {
        String inLine;
        String firstToken;
        try {
            while ((inLine = this.in.readLine()) != null) {
                System.out.println(this.username + ": " + inLine);
                StringTokenizer checkForPM = new StringTokenizer(inLine);
                if ((firstToken = checkForPM.nextToken()).equals("/pm")) {
                    this.pmUser = "";
                    while (checkForPM.hasMoreTokens()) {
                        this.pmUser += checkForPM.nextToken();
                        if (checkForPM.hasMoreTokens()) {
                            this.pmUser += " ";
                        }
                    }
                    System.out.println(this.pmUser);
                    if (this.clients.containsKey(this.pmUser)) {
                        this.global = false;
                        this.out.println("[SERVER] You are now privately messaging with " + this.pmUser);
                        continue;
                    }
                    this.out.println("[ERR] '" + this.pmUser + "' does not exist");
                    continue;
                }
                else if (firstToken.equals("/global")) {
                    this.global = true;
                    this.out.println("[SERVER] You are now messaging globally");
                    continue;
                }
                if (this.global) {
                    for (ClientHandler globalClient : this.clients.values()) {
                        globalClient.out.println(this.username + ": " + inLine);
                    }
                } else {
                    this.clients.get(this.pmUser).out.println("(private) " + this.username + ": " + inLine);
                    this.out.println("(private) " + this.username + ": " + inLine);
                }
            }
        } catch (IOException i) {
            System.out.println(i);
        } finally {
            for (ClientHandler globalClient : this.clients.values()) {
                globalClient.out.println("[SERVER] " + this.username + " has left the chat");
            }
            System.out.println(this.username + " has disconnected");
            this.clients.remove(this.username);
            System.out.println(this.clients);
            try {
                this.sock.close();
                this.in.close();
                this.out.close();
            } catch (IOException i) {
                System.out.println(i);
            }
        }
    }
}