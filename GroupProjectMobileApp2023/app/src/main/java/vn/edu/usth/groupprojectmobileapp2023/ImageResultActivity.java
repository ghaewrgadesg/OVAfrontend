package vn.edu.usth.groupprojectmobileapp2023;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;


import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.LayerDrawable;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImageResultActivity extends AppCompatActivity {
    ImageView previousButton, nextButton, speakerButton, translate,frame, mainImageView;
    TextView topText, bottomText;
    int currentIndex, jsonIndex;
    TextToSpeech textToSpeech;
    ArrayList<ArrayList<String>> recognitionResult;
    List<String> engLabel = Arrays.asList("cat", "dog", "tree", "lion");
    List<String> vnLabel = Arrays.asList("mèo", "chó", "cây", "sư tử");
    List<String> frLabel = Arrays.asList("chat", "chien", "arbre", "lion" );
    String languageCode = "EN";
    static final String API_URL = "http://10.0.2.2:8000/api/v1/detection";
    JSONObject jsonObject;
    private Handler mHandler;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int test = 2;
        Log.i("TEST", "The very beginning");
        setContentView(R.layout.activity_image_result);
        TextView backButton = findViewById(R.id.backBtn);
        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        previousButton = findViewById(R.id.previousObject);
        nextButton = findViewById(R.id.nextObject);
        frame = findViewById(R.id.objFrame);
        topText = findViewById((R.id.topResultText));
        bottomText = findViewById(R.id.botResultText);
        mainImageView = findViewById(R.id.imageResult);
        speakerButton = findViewById(R.id.speakerIcon);
        mainImageView.setImageURI(imageUri);
        translate = findViewById(R.id.translateIcon);


        recognitionResult = new ArrayList<>();
        Thread worker2 = new Thread(new Runnable() {
            @Override
            public void run() {
                postImage(API_URL, imageUri);
                Log.i("TEST","PAIN N SUFFERING");
                return;

            }
        });

        Log.i("URI",imageUri.toString());
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i("TTS", "something is wrong");
                    } else {
                        // TTS is successfully initialized
                        Log.i("TTS", "Initialization successful");
                    }
                } else {
                    // TTS initialization failed
                    Log.i("TTS", "Initialization failed");
                }
            }
        });
        try {
            Log.i("TEST", "Before the slaughter");
            worker2.start();
            countDownLatch.await();

            Log.i("TEST","Await ended");
            try{
                if (jsonObject == null){
                    Log.i("Json", "JSON OBJECT IS NULL");
                    jsonObject = new JSONObject();

                    // Add description field
                    jsonObject.put("description", "Detected objects");

                    // Create a JSON array for predictions
                    JSONArray predictionsArray = new JSONArray();

                    // Create a prediction object
                    JSONObject predictionObject = new JSONObject();

                    // Add bbox field to the prediction object
                    JSONObject bboxObject = new JSONObject();
                    bboxObject.put("x1", 442);
                    bboxObject.put("x2", 982);
                    bboxObject.put("y1", 199);
                    bboxObject.put("y2", 1270);
                    predictionObject.put("bbox", bboxObject);

                    // Add label and score fields to the prediction object
                    predictionObject.put("label", "cat");
                    predictionObject.put("score", "0.93");

                    // Add the prediction object to the predictions array
                    predictionsArray.put(predictionObject);

                    // Add the predictions array to the JSON object
                }
                else{
                    Log.i("JSON", jsonObject.toString());
                }
                String description = jsonObject.getString("description");
                Log.i("TEST", "After the slaughter");
                Log.i("RESULT", description);
                if (description.contains("Detected objects")) {
                    ArrayList<String> temp;
                    JSONArray predictionsArray = jsonObject.getJSONArray("predictions");
                    Log.i("RESULT", "Into the array");
                    for (int i = 0; i < predictionsArray.length(); i++) {
                        temp = new ArrayList<String>();
                        JSONObject predictionObject = predictionsArray.getJSONObject(i);
                        JSONObject bboxObject = predictionObject.getJSONObject("bbox");

                        String x1 = bboxObject.getString("x1");
                        String x2 = bboxObject.getString("x2");
                        String y1 = bboxObject.getString("y1");
                        String y2 = bboxObject.getString("y2");
                        String label = predictionObject.getString("label");
                        String score = predictionObject.getString("score");
                        temp.add(x1);
                        temp.add(x2);
                        temp.add(y1);
                        temp.add(y2);
                        temp.add(label);
                        temp.add(score);
                        recognitionResult.add(temp);
                    }
                    currentIndex = 1;
                    topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
                    bottomText.setText(currentIndex + "/" + recognitionResult.size());
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

                    ViewGroup.LayoutParams layoutParams = frame.getLayoutParams();
                    int frameWidth = Integer.parseInt(recognitionResult.get(currentIndex - 1).get(1)) - Integer.parseInt(recognitionResult.get(currentIndex - 1).get(0));
                    int frameHeight = Integer.parseInt(recognitionResult.get(currentIndex - 1).get(3)) - Integer.parseInt(recognitionResult.get(currentIndex - 1).get(2));

                    layoutParams.height = frameHeight;
                    layoutParams.width = frameWidth;
                    frame.setLayoutParams(layoutParams);
                    frame.requestLayout();

                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) frame.getLayoutParams();
                    marginLayoutParams.setMargins(Integer.parseInt(recognitionResult.get(currentIndex - 1).get(0)), Integer.parseInt(recognitionResult.get(currentIndex - 1).get(2)), 0, 0);
                    frame.setLayoutParams(marginLayoutParams);
                    frame.requestLayout();

                } else {
                    Log.i("RESULT", "out of the array");
                    topText.setText("No object detected");
                    bottomText.setText("0/0");
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }

        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
        }

    backButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            returnToCamera();
        }
    });

        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                nextObject(recognitionResult);
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousObject(recognitionResult);
            }
        });

        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (jsonObject.getString("description").contains("Detected objects")) {
                        String textToRead = recognitionResult.get(currentIndex - 1).get(4).toString();
                        Toast.makeText(ImageResultActivity.this, "Trying: " + textToRead, Toast.LENGTH_SHORT).show();
                        textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else{
                        Toast.makeText(ImageResultActivity.this, "No object", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PopupMenu and attach it to the button
                PopupMenu popupMenu = new PopupMenu(ImageResultActivity.this, translate);
                popupMenu.getMenuInflater().inflate(R.menu.language_menu, popupMenu.getMenu());

                // Set a click listener for menu items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.action_english) {
                            languageCode = "EN";
                            String label = recognitionResult.get(currentIndex - 1).get(4);
                            label = engLabel.get(getIndex(label));
                            textToSpeech.setLanguage(Locale.US);
                            recognitionResult.get(currentIndex - 1).set(4, label);
                            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
                            return true;
                        }
                        if (id == R.id.action_vietnam) {
                            String label = recognitionResult.get(currentIndex - 1).get(4);
                            label = vnLabel.get(getIndex(label));
                            textToSpeech.setLanguage(Locale.US);
                            recognitionResult.get(currentIndex - 1).set(4, label);
                            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
                            languageCode = "VN";
                            return true;
                        }
                        if (id == R.id.action_french) {
                            languageCode = "FR";
                            String label = recognitionResult.get(currentIndex - 1).get(4);
                            label = frLabel.get(getIndex(label));
                            textToSpeech.setLanguage(Locale.FRENCH);
                            recognitionResult.get(currentIndex - 1).set(4, label);
                            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
                            return true;
                        }
                        return false;
                        }
                });

                // Show the PopupMenu
                popupMenu.show();
            }
        });
    }

    private void nextObject(ArrayList<ArrayList<String>> recognitionResult ) {

        if (currentIndex < recognitionResult.size()) {
            currentIndex++;
            String label = recognitionResult.get(currentIndex - 1).get(4);
            recognitionResult.get(currentIndex - 1).set(4, label);
            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
            bottomText.setText(currentIndex + "/" + recognitionResult.size());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            ViewGroup.LayoutParams layoutParams = frame.getLayoutParams();
            ViewGroup.LayoutParams layoutParams2 = mainImageView.getLayoutParams();
            int frameWidth = Integer.parseInt(recognitionResult.get(currentIndex - 1).get(1)) - Integer.parseInt(recognitionResult.get(currentIndex - 1).get(0));
            int frameHeight = Integer.parseInt(recognitionResult.get(currentIndex - 1).get(3)) - Integer.parseInt(recognitionResult.get(currentIndex - 1).get(2));

            layoutParams.height = frameHeight;
            layoutParams.width = frameWidth;
            frame.setLayoutParams(layoutParams);
            frame.requestLayout();

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) frame.getLayoutParams();
            marginLayoutParams.setMargins(Integer.parseInt(recognitionResult.get(currentIndex - 1).get(0)), Integer.parseInt(recognitionResult.get(currentIndex - 1).get(2)), 0, 0);
            frame.setLayoutParams(marginLayoutParams);
            frame.requestLayout();

        }
        else {
            Log.i("NEXTOBJ", "No more to forward");
        }
    }

    private void previousObject(ArrayList<ArrayList<String>> recognitionResult){

        if (currentIndex > 1) {
            currentIndex--;
            String label = recognitionResult.get(currentIndex - 1).get(4);
            recognitionResult.get(currentIndex - 1).set(4, label);
            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
            bottomText.setText(currentIndex + "/" + recognitionResult.size());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            ViewGroup.LayoutParams layoutParams = frame.getLayoutParams();
            ViewGroup.LayoutParams layoutParams2 = mainImageView.getLayoutParams();
            int frameWidth = Integer.parseInt(recognitionResult.get(currentIndex - 1).get(1)) - Integer.parseInt(recognitionResult.get(currentIndex - 1).get(0));
            int frameHeight = Integer.parseInt(recognitionResult.get(currentIndex - 1).get(3)) - Integer.parseInt(recognitionResult.get(currentIndex - 1).get(2));


            layoutParams.height = frameHeight;
            layoutParams.width = frameWidth;
            frame.setLayoutParams(layoutParams);
            frame.requestLayout();

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) frame.getLayoutParams();
            marginLayoutParams.setMargins(Integer.parseInt(recognitionResult.get(currentIndex - 1).get(0)), Integer.parseInt(recognitionResult.get(currentIndex - 1).get(2)), 0, 0);
            frame.setLayoutParams(marginLayoutParams);
            frame.requestLayout();
        }
        else{
            Log.i("PREVOBJ", "Too low");
        }
    }

    private void returnToCamera(){
        Log.i("RESULT", "RETURNING");
        Intent intent = new Intent(this, MainCameraActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private int getIndex(String item){
        switch (item){
            case "mèo":
            case "chat":
            case "cat":
                return 0;
            case "dog":
            case "chien":
            case "chó":
                return 1;
            case "tree":
            case "cây":
            case "arbre":
                return 2;
            case "lion":
            case "sư tử":
                return 3;
            default:
                return 4;

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_english) {
            languageCode = "EN";
            String label = recognitionResult.get(currentIndex - 1).get(4);
            label = engLabel.get(getIndex(label));
            textToSpeech.setLanguage(Locale.US);
            recognitionResult.get(currentIndex - 1).set(4, label);
            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
            return true;
        }
        if (id == R.id.action_vietnam) {
            String label = recognitionResult.get(currentIndex - 1).get(4);
            label = vnLabel.get(getIndex(label));
            textToSpeech.setLanguage(Locale.US);
            recognitionResult.get(currentIndex - 1).set(4, label);
            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
            languageCode = "VN";
            return true;
        }
        if (id == R.id.action_french) {
            languageCode = "FR";
            String label = recognitionResult.get(currentIndex - 1).get(4);
            label = frLabel.get(getIndex(label));
            textToSpeech.setLanguage(Locale.FRENCH);
            recognitionResult.get(currentIndex - 1).set(4, label);
            topText.setText(recognitionResult.get(currentIndex - 1).get(4) + " - " + recognitionResult.get(currentIndex - 1).get(5));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String getRealPathFromURI(Uri contentUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Log.i("GETPATH", "Got data");
        Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null);
        Log.i("GETPATH", "GOT RESOLVER");
        if (cursor == null) {
            Log.i("GETPATH", "CURSOR IS NULLED");
            return contentUri.getPath();
        } else {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(column_index);
            cursor.close();
            if (filePath != null){
                Log.i("PATH", filePath);
            }
            else{
                filePath = contentUri.getPath();
                Log.i("PATH", "No Path" + filePath);
            }

            return filePath;
        }
    }
    private void postImage(String url, Uri imageUri) {
        Log.i("TEST", "getPath");
        Log.i("TEST", imageUri.toString());
        File imageFile = new File(getRealPathFromURI(imageUri));
        Log.i("TEST", "HTTP");
        OkHttpClient client = new OkHttpClient();
        Log.i("TEST", "REQUESTBODY");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "yolov4")
                .addFormDataPart("image", "image.jpeg",
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();
        Log.i("TEST", "new REQUEST");
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.i("TEST", "new call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("RESPONSE", "Failure");
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response == null){
                    Log.i("RESPONSE", "NULL RESPONSE");
                }
                if (response.isSuccessful()) {
                    Log.i("REPSPONSE", "not null" );
                    final String responseBody = response.body().string();
                    Log.i("RESPONSE",  responseBody);
                    try{
                        jsonObject = new JSONObject(responseBody);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    countDownLatch.countDown();
                } else {
                    Log.i("RESPONSE" , response.code() + " " + response.message() + " " + response.body().string());
                    countDownLatch.countDown();
                }
            }
        });

    }


}