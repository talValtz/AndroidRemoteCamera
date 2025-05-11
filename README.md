
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
│   ├── constants.py
│   └── main.py
│
├── server/src/main/java/com/example/remoteapp         # Android app (Java) that runs the socket server and camera control logic
│   ├── MainActivity.java
│   ├── SocketServer.java
│   ├── CameraUtils.java
│   ├── Response.java
│   ├── CommandType.java
│   ├── CommandHandlerRegistry.java
│   ├── Constants.java
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

  ##  How to Run the Project

###  Server Side (Android App)
- Open the `Server` project in **Android Studio**.
- Connect your Android device (preferably a real device with a working camera).
- **Grant Camera permissions** if requested.
- Click **Run** ▶️ to launch the app.
- The app will display "Waiting for client..." and open a server on **port 8888**.
- Make sure the device and the computer are on the same **Wi-Fi network**.

###  Client Side (Python Script)
- Open the `Client` folder.
- Open `constants.py`.
- **Update the IP address** at the top of the file:
  ```python
  HOST = 'YOUR_PHONE_IP_ADDRESS'  # <-- Replace with your Android device's IP address
  PORT = 8888
  ```
  To find your phone IP:
  - Open Wi-Fi settings on your phone → Details → IP address.

- Then run the client:
  ```bash
  python main.py
  ```

  ## Project Structure

### Server (Android / Java)
| Component | Purpose |
|:---|:---|
| `MainActivity.java` | Entry point of the Android app. Initializes the socket server and handles camera permissions and UI messages.|
| `SocketServer.java` | Handles server socket operations, communication with the client, and response management. |
| `CommandHandlerRegistry.java` | Maps each command (via `CommandType`) to a Runnable action. |
| `CameraUtils.java` | Manages camera opening, capturing photos, and permission logic. |
| `GetpropUtils.java` | Retrieves system properties using shell commands or reflection. |
| `Response.java` | Standardizes all responses (text/image/error) sent to the client. |
| `Constants.java` | Stores constant values used across the app, such as server port number and permission request codes.|
| `CommandType.java` | Enum for all valid commands that the client can send. 

### Client (Python)
| File | Purpose |
|:---|:---|
| `main.py` | User interface and command menu. |
| `ClientSocket.py` | Handles TCP communication: sending commands and receiving responses (text, properties, or image files). |
| `constants.py` | Constant variables for use in the project. |

## How to Extend the Project

- **To add a new server command**:
  1. Add a new value in `CommandType.java`.
  2. Create a new entry in `CommandHandlerRegistry.java` to link it to an action.

- **To add a new client command**:
  1. Add a new option in `main.py`.
  2. Implement the sending logic using `ClientSocket.start()`.

- **Response improvements**:
  Extend `Response.java` to support more types if needed (e.g., video responses).




---

##  Notes
- The system currently supports **one client at a time**.
- The Android device must remain unlocked when opening the camera.
- If connection errors occur, double-check that the IP address and port are correct, and that both devices are on the same network.

  








