package com.example.remoteapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

/*MainActivity acts as the entry point of the app,
  responsible for starting the socket server and handling permissions.*/
public class MainActivity extends AppCompatActivity {
    private SocketServer socketServer;
    private TextView messageTextView;

    /**
     * Called when the activity is first created. Initializes the UI and starts the socket server.
      *@param savedInstanceState Previously saved state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageTextView = findViewById(R.id.messageTextView);
        messageTextView.setText(R.string.waiting_connection);
        socketServer = new SocketServer(MainActivity.this,messageTextView);
        socketServer.start();
    }
    /**
     * Called when the activity is about to be destroyed.
     * Stops the socket server to release resources.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketServer != null) {
            socketServer.stop();
        }
    }

    /**
     * Handles the result of permission requests.
     *
     * @param requestCode  The code identifying the permission request.
     * @param permissions  The requested permissions.
     * @param grantResults The results for the requested permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION) {
            handleOpenCameraPermissionResult(grantResults);
        }
        else if(requestCode==Constants.CAMERA_PERMISSION_CODE){
            handleTakePhotoPermissionResult(grantResults);
        }
    }

    /**
     * Handles the result for the "open camera" permission request.
     *
     * @param grantResults The results for the camera permission request.
     */
    private void handleOpenCameraPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CameraUtils.openCamera(this);
            SocketServer.answerToClient(new Response(Response.ResponseType.TEXT, "Permission granted. Camera opened."));
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            SocketServer.answerToClient(new Response(Response.ResponseType.ERROR, "Camera permission denied."));
        }
    }
    /**
     * Handles the result for the "take photo" permission request.
     *
     * @param grantResults The results for the camera permission request.
     */
    private void handleTakePhotoPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Log.d("Permissions log", "Camera permission granted.");
            if (CameraUtils.getPendingActionAfterPermission() != null) {
                CameraUtils.runPendingActionAfterPermission();
                CameraUtils.setPendingActionAfterPermission(null);
            }
        } else {
            Log.e("Permissions log", "Camera permission denied.");
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            SocketServer.answerToClient(new Response(Response.ResponseType.ERROR, "Camera permission denied."));
        }
    }
    /**
     * Requests camera permission if not already granted.
     * If permission is already granted, runs the provided action immediately.
     *
     * @param onGranted A Runnable to execute if permission is granted.
     */
    public void requestCameraPermissionIfNeeded(Runnable onGranted) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},Constants.CAMERA_PERMISSION_CODE);
            CameraUtils.setPendingActionAfterPermission(onGranted);
           // Log.d("CameraX", "requestCameraPermissionIfNeeded called!");
        } else {
           // Log.d("Permissions log", "CAMERA permission already granted");
            onGranted.run();
        }
    }

}