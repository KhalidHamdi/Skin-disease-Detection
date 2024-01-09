package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // Define constants for request codes
    static final int REQUEST_IMAGE_SELECTION = 10;
    static final int REQUEST_IMAGE_CAPTURE = 12;
    static final int REQUEST_CAMERA_PERMISSION = 11;

    // Declaring UI elements
    Button selectButton, predictButton, captureButton;
    ImageView imageView;
    Bitmap selectedImage;
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        selectButton = findViewById(R.id.selectButton);
        predictButton = findViewById(R.id.predictButton);
        captureButton = findViewById(R.id.captureButton);
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.diseaseTextView);

        // Set button click listeners
        setButtonClickListener(selectButton, REQUEST_IMAGE_SELECTION);
        setButtonClickListener(predictButton, -1);
        setButtonClickListener(captureButton, REQUEST_IMAGE_CAPTURE);
    }

    private void setButtonClickListener(Button button, int requestCode) {
        button.setOnClickListener(view -> {
            if (requestCode == -1) {
                // Predict button
                performPrediction(selectedImage);
            } else {
                // Select or Capture button
                openImagePicker(requestCode);
            }
        });
    }

    private void openImagePicker(int requestCode) {
        //Selecting images from the gallery.
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //  If the request code is equal to image capture, then intent will switch to the camera.
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        startActivityForResult(intent, requestCode);
    }

    //Request camera permission
    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
        }
    }

    @Override
    //Check the result of a permission request for camera access.
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed
            openImagePicker(requestCode);
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    //The buttons functions determined by the unique requestCode associated with each button
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_SELECTION:
                    handleImageSelection(data.getData());
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    handleImageCapture((Bitmap) data.getExtras().get("data"));
                    break;
            }
        }
    }

    // Handles the selection of an image from a URI, converting it to a Bitmap then displays it in the imageView.
    private void handleImageSelection(Uri uri) {
        try {
            selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            displayImage(selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handles the capture of an image from the camera
    private void handleImageCapture(Bitmap image) {
        selectedImage = image;
        displayImage(selectedImage);
    }
    // This method is for displaying image in imageView
    private void displayImage(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    //Performs the prediction by sending a bitmap image to the server
    private void performPrediction(Bitmap image) {
        //Create an OkHttpClient to handle the request
        OkHttpClient client = new OkHttpClient();
        //Build a multipart request body containing the image
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        // Convert the Bitmap image to a byte array
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            // Add the image byte array to the multipart request body
            multipartBodyBuilder.addFormDataPart("image", "image", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Build the final request body
        RequestBody postBodyImage = multipartBodyBuilder.build();
        // Here we are creating a POST request with the image data and target server URL
        Request request = new Request.Builder().url("http://192.168.1.160:5000/prediction").post(postBodyImage).build();
        //Enqueue the request asynchronously and handle the callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            // Main purpose of this method is to notify the user about connection failure
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    // Update the resultTextView with a message about the unsuccessful server connection
                    resultTextView.setText("Connecting to the server unsuccessfully");
                });
            }

            @Override
            // Displaying the detected disease in the resultTextView
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(() -> resultTextView.setText(responseData));
            }
        });
    }
}
