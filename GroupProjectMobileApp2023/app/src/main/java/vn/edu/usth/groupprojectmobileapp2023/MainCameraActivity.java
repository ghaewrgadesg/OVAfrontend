package vn.edu.usth.groupprojectmobileapp2023;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainCameraActivity extends AppCompatActivity {

    private static final String TAG= "MainCameraActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_camera);
        ImageView imageView = (ImageView) findViewById(R.id.gallery);
        ImageView mainScreen = (ImageView) findViewById(R.id.mainScreen);
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        mainScreen.setImageURI(uri);
                        imageView.setImageURI(uri);
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });
        imageView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                // Launch the photo picker and let the user choose images and videos.
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
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