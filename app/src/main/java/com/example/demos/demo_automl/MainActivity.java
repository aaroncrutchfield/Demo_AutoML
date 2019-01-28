package com.example.demos.demo_automl;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private int GALLERY = 1;
    private ImageView imageView;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUploadImage = findViewById(R.id.uploadImageButton);
        imageView = findViewById(R.id.imageView);
        tvResult = findViewById(R.id.result_textview);

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureDialog();
            }
        });
    }

    private void showPictureDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Select Action");

        String[] items = {
                "Select photo from gallery",
                "Capture from camera"
        };

        dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    choosePhotoFromGallery();
                }
            }
        });

        dialogBuilder.create().show();
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
                    Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();

                    imageView.setImageBitmap(bitmap);

                    String imageString = Base64.encodeToString(
                            getBytesFromBitmap(bitmap),
                            Base64.NO_WRAP
                    );

                    Network.ModelRequestBody modelRequestBody = new Network.ModelRequestBody(new Network.PayloadRequest(new Network.ModelImage(imageString)));

                    String baseUrl = "https://automl.googleapis.com/vbeta1/";
                    Network network = new Network(baseUrl, true);

                    network.getRetroFitClient().create(Network.Endpoint.class).classifyImage(modelRequestBody).enqueue(new Callback<Network.PayloadRequest>() {
                        @Override
                        public void onResponse(Call<Network.PayloadRequest> call, Response<Network.PayloadRequest> response) {
                            if (response.isSuccessful()) {
                                Log.d("MainActivity", "onResponse: " + response.body().toString());
                                String text = response.body().toString();
                                tvResult.setText(text);
                            }
                        }

                        @Override
                        public void onFailure(Call<Network.PayloadRequest> call, Throwable t) {
                            Log.d("MainActivity", "onFailure: " + t.getMessage());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("MainActivity", "onActivityResult: Failed");
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        return outputStream.toByteArray();
    }
}
