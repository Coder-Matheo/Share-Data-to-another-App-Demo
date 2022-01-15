package com.shareingdatatoanotherappdemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Button shareButton;
    MaterialButton shareTextBtn;
    MaterialButton shareImageBtn;
    MaterialButton shareBothBtn;
    ImageView imageViewShare;
    TextInputEditText textEditText;

    //Picked image uri will be saved in it
    Uri imageUri = null;
    String textToShare = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shareTextBtn = findViewById(R.id.shareTextbtn);
        shareImageBtn = findViewById(R.id.shareImagebtn);
        shareBothBtn = findViewById(R.id.shareBothbtn);

        textEditText = findViewById(R.id.textEditText);
        imageViewShare = findViewById(R.id.imageViewShare);


        imageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });

        //Share Text
        shareTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getText
                textToShare = textEditText.getText().toString().trim();
                //Check if Text is Empty
                if (TextUtils.isEmpty(textToShare)) {
                    showToast("Enter text ...");
                } else {
                    shareText();

                }
            }
        });
        //Share Image
        shareImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and Image is picked or not
                if (imageUri == null) {

                } else {
                    shareImage();
                }
            }
        });

        shareBothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getText
                textToShare = textEditText.getText().toString().trim();
                //Check if Text is Empty or not, and Image is picked or not
                if (TextUtils.isEmpty(textToShare)) {
                    showToast("Enter text ...");
                } else if (imageUri == null) {
                    showToast("Pick image");
                } else {
                    shareBoth();
                }
            }
        });
    }

    private void pickImage() {
        //Intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);

    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //handle the result in both cashes; either image is picked or not
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //Image picked
                        showToast("Image Picked from gallery");
                        //get image uri
                        Intent data = result.getData();
                        imageUri = data.getData();
                        //set image to imageView
                        imageViewShare.setImageURI(imageUri);
                    } else {
                        //cancelled
                        showToast("Cancelled");
                    }

                }
            });


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void shareText() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject HERE");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        startActivity(Intent.createChooser(shareIntent, "Share Via "));
    }

    private void shareImage() {
        Uri contentUri = getContentUri();


        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject HERE");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Via "));
    }

    private void shareBoth() {
        Uri contentUri = getContentUri();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject HERE");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Via "));

    }

    private Uri getContentUri() {
        Bitmap bitmap = null;
        //get bitmap from uri
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("" + e.getMessage());
        }
        //if you want to get bitmap from imageView instead of uri the
        /*BitmapDrawable bitmapDrawable = (BitmapDrawable) imageViewShare.getDrawable();
        bitmap = bitmapDrawable.getBitmap();*/

        File imageFolder = new File(getCacheDir(), "images");
        Uri contentUri = null;
        try {
            imageFolder.mkdirs();//create if not exists
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            stream.flush();
            stream.close();
            contentUri = FileProvider.getUriForFile(this, "com.shareingdatatoanotherappdemo.fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
            showToast("" + e.getMessage());
        }

        return contentUri;
    }
}