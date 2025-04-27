package com.example.remoteapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/** Utility class for managing camera operations like opening the native camera,
 taking photos automatically, and saving images to the gallery. */
public class CameraUtils {

    private static Runnable pendingActionAfterPermission;
    /** Opens the default device camera app if permission is granted.
    @param mainActivity The MainActivity context.
    @return A status message indicating success or error.*/
    public static String openCamera(MainActivity mainActivity) {

        if (!mainActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(mainActivity, "No camera found", Toast.LENGTH_SHORT).show();
            return "Camera does not exist";
        }
        if (ContextCompat.checkSelfPermission(mainActivity, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity,
                    new String[]{android.Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
            return "Camera access not authorized";
        }
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mainActivity.startActivity(intent);
            return "Camera opened successfully.";
        } catch (Exception e) {
            Log.d("CameraX", "Failed to open camera: " + e.getMessage());
            return "Failed to open camera: " + e.getMessage();
        }
    }
    /**     *Gets the pending action to execute after camera permission is granted.
     @return A Runnable representing the pending action, or null if none exists.
     */
    public static Runnable getPendingActionAfterPermission(){
        return pendingActionAfterPermission;
    }
    /**
     * Executes the pending action if it exists (after permission is granted).
     */
    public static void runPendingActionAfterPermission() {
        if (pendingActionAfterPermission != null) {
            pendingActionAfterPermission.run();
        }
    }
    /**
     * Sets a new pending action to be executed after permission is granted.
     *
     * @param r The Runnable action to save.
     */
    public static void setPendingActionAfterPermission(Runnable r){
        pendingActionAfterPermission=r;
    }

    /**
     * Captures a photo automatically and saves it to the app's private storage.
     *
     * @param mainActivity The MainActivity context.
     * @param socketServer The SocketServer to send the image back to the client.
     */

    public static void capturePhotoAutomatically(MainActivity mainActivity,SocketServer socketServer) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File outputFile = new File(mainActivity.getExternalFilesDir("photos"), "captured_auto_"+timeStamp+".jpg");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mainActivity);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                ImageCapture imageCapture = new ImageCapture.Builder().setTargetRotation(mainActivity.getWindowManager().getDefaultDisplay().getRotation()).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(mainActivity, cameraSelector, imageCapture);
                ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(mainActivity), new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Log.d("CameraX", " onImageSaved called!");
                                Log.d("CameraX", "Image captured: " + outputFile.getAbsolutePath());
                                SocketServer.answerToClient(new Response(Response.ResponseType.IMAGE, outputFile));
                            }
                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Log.e("CameraX", "Failed: " + exception.getMessage());
                            }
                        }
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Failed to initialize CameraProvider", e);
            }
        }, ContextCompat.getMainExecutor(mainActivity));
    }

    /**
     * Captures a photo and saves it directly into the device's public gallery.
     *
     * @param mainActivity The MainActivity context.
     * @param socketServer The SocketServer to notify the client after saving the photo.
     */

    public static void capturePhotoToGallery(MainActivity mainActivity, SocketServer socketServer) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(mainActivity);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                ImageCapture imageCapture = new ImageCapture.Builder().setTargetRotation(mainActivity.getWindowManager().getDefaultDisplay().getRotation()).build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(mainActivity, cameraSelector, imageCapture);
                ContentValues contentValues = new ContentValues();
                String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM);

                ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(mainActivity.getContentResolver(),
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
                imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(mainActivity), new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Log.d("CameraX", "Image saved to gallery!");
                                SocketServer.answerToClient(new Response(Response.ResponseType.TEXT, "Image saved to gallery!"));
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Log.e("CameraX", " Failed to save image: " + exception.getMessage());
                            }
                        }
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Failed to initialize CameraProvider for gallery save", e);

            }
        }, ContextCompat.getMainExecutor(mainActivity));
    }


}