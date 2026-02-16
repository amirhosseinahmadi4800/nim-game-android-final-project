package com.example.nimgame;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//عمودی ماندن صفحه
        Toast.makeText(this, "Welcome to NIM \uD83C\uDFAE\n", Toast.LENGTH_SHORT).show();

    }

    public void StartGame(View view){//باتن bluetooth
        Intent intent = new Intent(MainActivity.this, nameplyer.class);
        startActivity(intent);
    }


    public void aboutgame(View view){//باتن about
        Intent intent = new Intent(MainActivity.this, GameAbout.class);
        startActivity(intent);
    }

    public void Developer(View view){
        Intent intent = new Intent(MainActivity.this, Developer.class);
        startActivity(intent);
    }

    public void exitApp(View view) {//exitApp
        finishAffinity();   // بستن همه Activity ها
        System.exit(0);     // خروج کامل از برنامه
    }
}