package com.example.nimgame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class nameplyer extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 100;

    EditText editTextName;
    public static String playerName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.namepalyer);

        // پیدا کردن EditText
        editTextName = findViewById(R.id.editTextName);

        // پیام اولیه به کاربر
        Toast.makeText(this,
                "اسم خود را وارد کنید 🎮",
                Toast.LENGTH_LONG
        ).show();
    }

    // این متد روی ImageView nextbluetooth صدا زده میشه
    public void goToBluetoothChecker(View view) {

        // گرفتن متن از EditText و حذف فاصله‌های اضافی
        String name = editTextName.getText().toString().trim();

        // اگر فیلد خالی بود
        if (name.isEmpty()) {
            Toast.makeText(this,
                    "نباید فیلد اسم خالی باشد ❗",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        playerName = name;

        // روی اندروید ۱۲ به بالا: اول اجازه بلوتوث بگیر
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
        }

        openBluetoothScreen();
    }

    private void openBluetoothScreen() {
        startActivity(new Intent(nameplyer.this, BluetoothChecker.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            boolean allGranted = grantResults.length > 0;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "دسترسی بلوتوث تایید شد ✓", Toast.LENGTH_SHORT).show();
                openBluetoothScreen();
            } else {
                Toast.makeText(this,
                        "بدون اجازه بلوتوث بازی دو نفره کار نمی‌کند",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
