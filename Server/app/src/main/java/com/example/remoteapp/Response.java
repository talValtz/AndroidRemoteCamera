package com.example.remoteapp;

import java.io.File;

/**
 * Represents a response to be sent to the client, either a text message, an image, or an error.
 */
public class Response {

    public enum ResponseType {
        TEXT,
        IMAGE,
        ERROR
    }

    private final ResponseType type;
    private final String payload;   // For text or error messages
    private final File imageFile;    // For image responses

    /**
     * Constructor for a text or error response.
     *
     * @param type The type of the response (TEXT or ERROR).
     * @param payload The textual content of the response.
     */
    public Response(ResponseType type, String payload) {
        this.type = type;
        this.payload = payload;
        this.imageFile = null;
    }

    /**
     * Constructor for an image response.
     *
     * @param type The type of the response (should be IMAGE).
     * @param imageFile The file containing the image to send.
     */
    public Response(ResponseType type, File imageFile) {
        this.type = type;
        this.payload = null;
        this.imageFile = imageFile;
    }

    /**
     * Returns the type of the response.
     */
    public ResponseType getType() {
        return type;
    }

    /**
     * Returns the text payload (only for TEXT or ERROR types).
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Returns the image file (only for IMAGE type).
     */
    public File getImageFile() {
        return imageFile;
    }

    /**
     * Serializes the response into a string format that can be transmitted over the network.
     * For TEXT and ERROR, includes the payload. For IMAGE, only includes a placeholder.
     */
    public String serialize() {
        if (type == ResponseType.TEXT || type == ResponseType.ERROR) {
            return type.name() + "|" + (payload != null ? payload : "");
        } else if (type == ResponseType.IMAGE) {
            return type.name() + "|IMAGE_FILE";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Serializes only the header part of the response (the type).
     */
    public String serializeHeader() {
        return type.name();
    }
}
