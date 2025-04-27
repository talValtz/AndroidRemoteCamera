package com.example.remoteapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple TCP server that listens for incoming client connections,
 * handles commands using a CommandHandlerRegistry, and sends responses back to clients.
 */
public class SocketServer {

    private final TextView messageTextView;
    private final MainActivity mainActivity;
    private CommandHandlerRegistry commandHandlerRegistry;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter latestWriter;

    /**
     * Constructs the SocketServer.
     *
     * @param mainActivity The main activity instance.
     * @param messageTextView The TextView to display server status messages.
     */
    public SocketServer(MainActivity mainActivity, TextView messageTextView) {
        this.mainActivity = mainActivity;
        this.messageTextView = messageTextView;

    }

    /**
     * Starts the server asynchronously.
     */
    public void start() {
        executorService.execute(this::startServer);
    }

    /**
     * Initializes the server socket and waits for incoming client connections.
     */
    private void startServer() {
        try {
            updateUI("Server started on port " );
            serverSocket = new ServerSocket(Constants.SERVER_PORT);
            updateUI("Server started on port " + Constants.SERVER_PORT);
            commandHandlerRegistry = new CommandHandlerRegistry(mainActivity, mainHandler, this);

            while (true) {
                acceptClient();
            }
        } catch (IOException e) {
            updateUI("Server error: " + e.getMessage());
            Log.e("SocketServer", "Server error: " + e.getMessage(), e);
        }
    }

    /**
     * Accepts a new client connection and processes the incoming command.
     */
    private void acceptClient() {
        try {
            clientSocket = serverSocket.accept();
            logAndUpdateUI("Client connected: " + clientSocket.getInetAddress());
            latestWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = in.readLine();
            if (message != null) {
                commandHandlerRegistry.handleCommand(message);
            }
            updateUI("Message received: " + message);
        } catch (IOException e) {
            logAndUpdateUI("Error handling client: " + e.getMessage());
        }
    }


    /**
     * Sends a Response object to the currently connected client asynchronously.
     *
     * @param response The response to send (TEXT, ERROR, or IMAGE).
     */
    public static void answerToClient(Response response) {
        new Thread(() -> {
            if (latestWriter != null) {
                try {
                    OutputStream out = clientSocket.getOutputStream();
                    PrintWriter writer = new PrintWriter(out, true);

                    writer.println(response.getType().name());
                    writer.flush();

                    if (response.getType() == Response.ResponseType.TEXT || response.getType() == Response.ResponseType.ERROR) {
                        sendTextResponse(response,out,writer);
                    } else if (response.getType() == Response.ResponseType.IMAGE) {
                        sendImageFile(response.getImageFile(), out, writer);
                    }

                } catch (IOException e) {
                    Log.e("SocketServer", "Error sending response: " + e.getMessage(), e);
                } finally {
                    closeLatestClient();
                }
            }
        }).start();
    }

    /**
     * Sends a text or error payload to the client.
     *
     * @param response The text or error response to send.
     * @param out The output stream to the client.
     * @param writer The PrintWriter for simple text sending.
     * @throws IOException If sending fails.
     */
    private static void sendTextResponse(Response response, OutputStream out, PrintWriter writer) throws IOException {
        String payload = response.getPayload();
        byte[] payloadBytes = payload.getBytes("UTF-8");
        writer.println(payloadBytes.length);
        writer.flush();
        out.write(payloadBytes);
        out.flush();
    }

    /**
     * Sends an image file to the client.
     *
     * @param imageFile The image file to send.
     * @param out The output stream to the client.
     * @param writer The PrintWriter for sending initial file size.
     * @throws IOException If file reading or writing fails.
     */
    private static void sendImageFile(File imageFile, OutputStream out, PrintWriter writer) throws IOException {
        if (imageFile != null && imageFile.exists()) {
            writer.println(imageFile.length());
            writer.flush();

            try (FileInputStream fis = new FileInputStream(imageFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            Log.d("PhotoStatus", " Image sent successfully.");
        } else {
            Log.e("PhotoStatus", " Image file does not exist.");
        }
    }


    /**
     * Closes the currently connected client socket cleanly.
     */
    public static void closeLatestClient() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
                Log.d("CameraX","latestWriter=null");
                latestWriter=null;
            }
        } catch (IOException e) {
            Log.e("SocketServer", "Error closing client socket: " + e.getMessage(), e);
        }
    }
    /**
     * Updates the UI text safely from the main thread.
     *
     * @param message The message to display in the TextView.
     */
    private void updateUI(String message) {
        mainHandler.post(() -> messageTextView.setText(message));
    }
    /**
     * Stops the server and shuts down the executor service cleanly.
     */
    public void stop() {
        executorService.shutdownNow();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.e("SocketServer", "Error closing server socket: " + e.getMessage(), e);
        }
    }
    /**
     * Logs a message and updates the UI with it.
     *
     * @param message The message to log and display.
     */
    private void logAndUpdateUI(String message) {
        Log.d("SocketServer", message);
        updateUI(message);
    }


}
