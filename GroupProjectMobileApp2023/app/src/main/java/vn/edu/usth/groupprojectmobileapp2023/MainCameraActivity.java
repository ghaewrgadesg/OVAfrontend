package vn.edu.usth.groupprojectmobileapp2023;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;

import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainCameraActivity extends AppCompatActivity {

    private static final String TAG= "MainCameraActivity";
    private PreviewView previewView;
    private ImageView cameraButton, imageView, flashButton;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_camera);
        ImageView infoButton = findViewById(R.id.infoBtn);
        imageView = findViewById(R.id.gallery);
        previewView = findViewById(R.id.mainScreen);
        cameraButton = findViewById(R.id.cam_btn);
        flashButton = findViewById(R.id.flashBtn);

        RelativeLayout middlePart = findViewById(R.id.middlePart);
        if (ContextCompat.checkSelfPermission(MainCameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            middlePart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    middlePart.removeView(findViewById(R.id.startCamCheck));
                    startCamera(cameraFacing);
                }
            });

        }
        if (ContextCompat.checkSelfPermission(MainCameraActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            activityResultLauncher.launch(Manifest.permission.INTERNET);
        }

        imageView.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Log.i(TAG, "GALLERY CLICKED");
            if (ContextCompat.checkSelfPermission(MainCameraActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                activityResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                ImagePicker.with(MainCameraActivity.this)
                        .galleryOnly()
                        .start();;
                }

            }
        });


        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                cameraButton.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){

                        Log.i(TAG, "CAMERA CLICKED");
                        takePicture(imageCapture);
                    }
                });

                flashButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setFlashIcon(camera);
                    }
                });
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void takePicture(ImageCapture imageCapture) {
        // Get a stable reference of the modifiable image capture use case
        if (imageCapture == null) {
            return;
        }
        // Create time-stamped name and MediaStore entry
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        // Set up image capture listener, which is triggered after the photo has been taken
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        String msg = "Photo capture succeeded: " + output.getSavedUri();
//                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        openImageResult(output.getSavedUri());
                    }
                }
        );
    }

    private void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                camera.getCameraControl().enableTorch(true);
                flashButton.setImageResource(R.drawable.noflash);
            } else {
                camera.getCameraControl().enableTorch(false);
                flashButton.setImageResource(R.drawable.flash);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Flash is not available currently", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            Uri uri = data.getData();
            ImageView imageView = (ImageView) findViewById(R.id.gallery);
            openImageResult(uri);
        }
    }

    private void openImageResult(Uri imageUri) {
        Intent intent = new Intent(this, ImageResultActivity.class);

        // Pass the image file path and recognized objects to the new Activity
        intent.putExtra("imageUri", imageUri);
        finish();
        startActivity(intent);

    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.i(TAG,"Started");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "Resumed");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "Paused");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "Stopped");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "Destroyed");
    }
}