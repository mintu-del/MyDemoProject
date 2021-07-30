package com.example.uploaddocuments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
  Button button1;
    Button button2;
    TextView textbox;
    Uri pdfURI;
    ProgressDialog progressDialog;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    button1=findViewById(R.id.button);
    button2=findViewById(R.id.button2);
    textbox=findViewById(R.id.textView);


        mDatabase = FirebaseDatabase.getInstance();

        mStorage = FirebaseStorage.getInstance();

    button1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectPDF();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
            }

        }   });

    button2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(pdfURI!=null){
                uploadPDF(pdfURI);
            }
            else{
                Toast.makeText(MainActivity.this,"Select a File",Toast.LENGTH_SHORT).show();
            }
        }
    });
    }

    private void uploadPDF(Uri pdfUri) {

        progressDialog = new ProgressDialog(MainActivity.this);

        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setTitle("Uploading File...");

        progressDialog.setProgress(0);

        progressDialog.show();

        final String fileName = System.currentTimeMillis()+" ";

        StorageReference mySto = mStorage.getReference();

        mySto.child("Uploads").child(fileName).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        Log.d("Hello",task.toString());

                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String url = uri.toString();

                                DatabaseReference myRef = mDatabase.getReference();

                                myRef.child(fileName).setValue(url)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {


                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    Toast.makeText(MainActivity.this, "File is uploaded", Toast.LENGTH_SHORT).show();

                                                    progressDialog.dismiss();

                                                } else {

                                                    Toast.makeText(MainActivity.this, "File upload failed", Toast.LENGTH_SHORT).show();

                                                    progressDialog.dismiss();

                                                }

                                            }
                                        });

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this, "ERROR! Something went wrong try again.", Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                progressDialog.setProgress(currentProgress);

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
   if(requestCode==9 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
       selectPDF();
   }
   else{
       Toast.makeText(MainActivity.this,"please provide permission",Toast.LENGTH_SHORT).show();
   }
    }

    private void selectPDF() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent ,86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==86 && resultCode == RESULT_OK && data != null) {
            pdfURI = data.getData();
            Log.d("result", data.getData().toString());
        } else {
            Toast.makeText(MainActivity.this, "Please select proper format file", Toast.LENGTH_SHORT).show();
        }
    }
}