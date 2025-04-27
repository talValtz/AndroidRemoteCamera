package com.example.remoteapp;
/**
 * Represents the types of commands that the server can handle.
 */
public enum CommandType {
    OPEN_CAMERA,
    TAKE_PHOTO,
    GET_PROP;

    /**
    * Converts a string into a corresponding CommandType.
    */
    public static CommandType fromString(String command) {
        try {
            return CommandType.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

