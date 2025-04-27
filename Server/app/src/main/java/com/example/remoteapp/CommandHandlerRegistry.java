package com.example.remoteapp;

import android.os.Handler;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry that manages mapping between commands (represented as enums) and their corresponding execution logic.
 */
public class CommandHandlerRegistry {

    private final Map<CommandType, Runnable> commandHandlers = new HashMap<>();

    /**
     * Initializes the registry with the given context objects.
     *
     * @param mainActivity The main activity instance.
     * @param handler Handler for posting actions to the main thread if needed.
     * @param socketServer The socket server for sending responses back to the client.
     */
    public CommandHandlerRegistry(MainActivity mainActivity, Handler handler, SocketServer socketServer) {
        initializeHandlers(mainActivity, handler, socketServer);
    }

    /**
     * Sets up the mappings between each CommandType and its corresponding action.
     *
     * - OPEN_CAMERA: Opens the device's camera if permissions are granted.
     * - TAKE_PHOTO: Captures a photo automatically after ensuring camera permissions.
     *     (Optionally, you can switch to saving directly to the Gallery by uncommenting the alternative line.)
     * - GET_PROP: Fetches device properties (brand, model, Android version, etc.) and returns to the client.
     */
    private void initializeHandlers(MainActivity mainActivity, Handler handler, SocketServer socketServer) {
        commandHandlers.put(CommandType.OPEN_CAMERA, () -> {
            String result = CameraUtils.openCamera(mainActivity);
            SocketServer.answerToClient(new Response(Response.ResponseType.TEXT, result));
        });

        commandHandlers.put(CommandType.TAKE_PHOTO, () -> {
            handler.post(() -> mainActivity.requestCameraPermissionIfNeeded(() -> CameraUtils.capturePhotoAutomatically(mainActivity, socketServer)));
            // Alternative:
            // If you prefer saving the captured image directly to the device gallery instead of internal storage,
            // uncomment the following line and comment the automatic capture line above:
            //handler.post(() -> mainActivity.requestCameraPermissionIfNeeded(() -> CameraUtils.capturePhotoToGallery(mainActivity, socketServer)));
        });

        commandHandlers.put(CommandType.GET_PROP, () -> {
            String props = GetpropUtils.getProp();
            Log.d("GetProp", "Properties fetched: " + props);
            SocketServer.answerToClient(new Response(Response.ResponseType.TEXT, props));
        });
    }

    /**
     * Handles a command received as a string.
     * If the command is valid, the corresponding action is executed.
     * If the command is invalid or missing, an error response is sent to the client.
     *
     * @param commandStr The command string received from the client.
     */
    public void handleCommand(String commandStr) {
        CommandType command = CommandType.fromString(commandStr);
        if (command == null) {
            SocketServer.answerToClient(new Response(Response.ResponseType.ERROR, "Unknown command: " + commandStr));
            return;
        }

        Runnable action = commandHandlers.get(command);
        if (action != null) {
            action.run();
        } else {
            SocketServer.answerToClient(new Response(Response.ResponseType.ERROR, "No handler registered for command: " + commandStr));
        }
    }
}
