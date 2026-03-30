package com.example.screenrecorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 100;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final int REQUEST_CODE_STORAGE = 102;

    private MediaProjectionManager projectionManager;
    private Button btnRecord;
    private TextView tvStatus;

    public static boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecord = findViewById(R.id.btnRecord);
        tvStatus = findViewById(R.id.tvStatus);

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        btnRecord.setOnClickListener(v -> {
            if (!isRecording) {
                checkPermissionsAndStart();
            } else {
                stopRecording();
            }
        });

        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (isRecording) {
            btnRecord.setText("Kaydı Durdur ■");
            btnRecord.setBackgroundColor(0xFFE53935);
            tvStatus.setText("⏺ Kayıt devam ediyor...");
        } else {
            btnRecord.setText("Kaydı Başlat ●");
            btnRecord.setBackgroundColor(0xFF43A047);
            tvStatus.setText("Hazır");
        }
    }

    private void checkPermissionsAndStart() {
        // Android 10 ve altı için depolama izni gerekli
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO
                        },
                        REQUEST_CODE_PERMISSIONS);
                return;
            }
        } else {
            // Android 11+ için sadece RECORD_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_PERMISSIONS);
                return;
            }
        }

        startScreenCapture();
    }

    private void startScreenCapture() {
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
    }

    private void stopRecording() {
        Intent stopIntent = new Intent(this, RecorderService.class);
        stopIntent.setAction(RecorderService.ACTION_STOP);
        startService(stopIntent);
        isRecording = false;
        updateUI();
        tvStatus.setText("Kayıt durduruldu. Dosya: Movies/ScreenRecorder/");
        Toast.makeText(this, "Kayıt kaydedildi!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent serviceIntent = new Intent(this, RecorderService.class);
                serviceIntent.setAction(RecorderService.ACTION_START);
                serviceIntent.putExtra(RecorderService.EXTRA_RESULT_CODE, resultCode);
                serviceIntent.putExtra(RecorderService.EXTRA_RESULT_DATA, data);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }

                isRecording = true;
                updateUI();
            } else {
                Toast.makeText(this, "Ekran kaydı izni reddedildi.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startScreenCapture();
            } else {
                Toast.makeText(this, "İzinler gereklidir.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
