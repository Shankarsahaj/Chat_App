import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Set<ClientHandler> clientHandlers;

    public ClientHandler(Socket socket, Set<ClientHandler> clientHandlers) throws IOException {
        this.socket = socket;
        this.clientHandlers = clientHandlers;

        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String msg;

            while ((msg = in.readLine()) != null) {
                broadcast(msg);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            clientHandlers.remove(this);
        }
    }

    private void broadcast(String msg) {
        for (ClientHandler client : clientHandlers) {
            client.out.println(msg);
        }
    }
}