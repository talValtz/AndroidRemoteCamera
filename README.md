
## AndroidRemoteCamera — Control Android Camera Remotely from a Python Client

A cross-platform mini-project that allows remote control of an Android device's camera over a socket connection.

The project consists of:

An Android server app that listens for commands to open the camera, capture photos, or retrieve device information.

A Python client app that sends commands and handles responses (including receiving images or device properties).

Designed to demonstrate modular coding practices, extensibility for adding new commands, and clean network communication between Android and desktop environments.


# Design Decision


```bash
AndroidRemoteCamera/
│
├── client/         # Python client that sends commands to the Android server
│   ├── ClientSocket.py
│   └── main.py
│
├── server/         # Android app (Java) that runs the socket server and camera control logic
│   ├── MainActivity.java
│   ├── SocketServer.java
│   ├── CameraUtils.java
│   ├── Response.java
│   ├── CommandType.java
│   ├── CommandHandlerRegistry.java
│   └── GetpropUtils.java
│
├── README.md       # Project documentation (this file)
└── (additional standard Android project files)
```
##  Client Side (Python) — Command Handling
I separated the responsibility between sending a command and handling the server's response.

## Server Side (Java)

- **Command Registration System:**  
  I introduced a `CommandHandlerRegistry` class to dynamically map commands to actions, replacing traditional switch-case logic. This improves scalability and adheres to the Open/Closed Principle.

- **Separation of Concerns:**  
  Networking (socket communication) and camera operations are handled by separate classes (`SocketServer` and `CameraUtils`). This clean separation improves maintainability and supports easier future enhancements.

- **CameraUtils- Camera Managment Separation:**
  I moved all camera-related functionalities (opening the camera, requesting permissions,   capturing images, saving to gallery) into a dedicated CameraUtils class. This ensures a clear separation of concerns, making the code more modular, easier to test, and maintain. Any future changes related to the camera (for example, adding video recording) can now be done in one place without touching unrelated parts.
  
- **GetpropUtils - System Property Access Utility:**
    I extracted the logic for fetching Android system properties into GetpropUtils.
    The utility supports two strategies: reading via getprop shell command or using reflection  (SystemProperties class). This gives the server-side flexibility across different Android    versions, where access restrictions might vary, and isolates platform-specific code into one place.





