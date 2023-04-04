package com.example.codeclause1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView originalImageIv;
    private Button detectFacesBtn;

    private ImageView croppedImageIv;

    private static final String TAG = "FACE_DETECT_TAG";

    private static final int SCALING_FACTOR = 10;

    private FaceDetector detector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        originalImageIv = findViewById(R.id.originalImageIv);
        detectFacesBtn = findViewById(R.id.detectFacesBtn);
        croppedImageIv = findViewById(R.id.croppedImageIv);

        FaceDetectorOptions realTimeFdo =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build();

        detector = FaceDetection.getClient(realTimeFdo);
        detectFacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic1);

                /*Uri imageUri = null;
                try {
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                /*BitmapDrawable bitmapDrawable = (BitmapDrawable) originalImageIv.getDrawable();
                Bitmap bitmap1 = bitmapDrawable.getBitmap();*/

                analyzePhoto(bitmap);
            }
            });
    }
    private void analyzePhoto(Bitmap bitmap){
        Log.d(TAG, "analyzePhoto: ");

        Bitmap smallerBitmap = Bitmap.createScaledBitmap(
                bitmap,
                bitmap.getWidth()/SCALING_FACTOR,
                bitmap.getHeight()/SCALING_FACTOR,
        false);

        InputImage inputImage = InputImage.fromBitmap(smallerBitmap,0);


        detector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        Log.d(TAG, "onSuccess: No of Faces Detected"+faces.size());
                        for (Face face: faces){
                            Rect rect = face.getBoundingBox();
                            rect.set(rect.left*SCALING_FACTOR,
                                    rect.top*(SCALING_FACTOR-1),
                                    rect.right*SCALING_FACTOR,
                                    (rect.bottom*SCALING_FACTOR)+98
                                    );
                        }

                        croppedDetectedFaces(bitmap, faces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG, "onFailure: ",e);
                        Toast.makeText(MainActivity.this, "Detection Failed Due To"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void croppedDetectedFaces(Bitmap bitmap, List<Face> faces) {

        Log.d(TAG, "croppedDetectedFaces: ");
        Rect rect = faces.get(1).getBoundingBox();

        int x = Math.max(rect.left,0);
        int y = Math.max(rect.top,0);
        int width = rect.width();
        int height = rect.height();

        Bitmap croppedBitmap = Bitmap.createBitmap(
                bitmap,
                x,
                y,
                (x+width> bitmap.getWidth()) ? bitmap.getWidth() - x : width,
                (y+height> bitmap.getHeight()) ? bitmap.getHeight() -y : height
        );

        croppedImageIv.setImageBitmap(croppedBitmap);
    }
}