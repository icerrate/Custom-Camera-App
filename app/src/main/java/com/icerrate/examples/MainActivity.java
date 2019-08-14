package com.icerrate.examples;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.math.MathUtils;

import java.math.MathContext;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    private static final int RC_PERMISSIONS = 100;

    private TextureView viewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.view_finder);
        FloatingActionButton takePhotoFab = findViewById(R.id.take_photo);

        //Check permissions
        if (allPermissionGranted()) {
            viewFinder.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, RC_PERMISSIONS);
        }

        takePhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void startCamera() {
        Point screenSize = getScreenSize();
        int width = screenSize.x;
        int height = screenSize.y;

        //Get real aspect ratio
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getRealMetrics(displayMetrics);
        Rational rational = new Rational(displayMetrics.widthPixels, displayMetrics.heightPixels);

        //Build de camera preview
        PreviewConfig build = new PreviewConfig.Builder()
                .setTargetAspectRatio(rational)
                .setTargetResolution(new Size(width,height))
                .build();
        Preview preview = new Preview(build);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup group = (ViewGroup) viewFinder.getParent();
                group.removeView(viewFinder);
                group.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
            }
        });

        CameraX.bindToLifecycle(this, preview);
    }

    private void takePhoto() {
        Toast.makeText(this, "Shot!",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS) {
            if (allPermissionGranted()) {
                viewFinder.post(new Runnable() {
                    @Override
                    public void run() {
                        startCamera();
                    }
                });
            } else {
                Toast.makeText(this, "Permission not granted",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private Point getScreenSize() {
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}
