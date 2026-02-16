package com.example.nimgame;

import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameAbout extends AppCompatActivity {

    private ImageView nextButton;
    private boolean isAnimating = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Spinner spinner = findViewById(R.id.languageSpinner);
        TextView textView = findViewById(R.id.textView);
        nextButton = findViewById(R.id.imageView25 );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//عمودی ماندن صفحه

        startButtonAnimation();

        // زبان‌ها
        String[] languages = {"فارسی", "English", "عربی"};

        // اتصال زبان‌ها به Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languages
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // متن‌ها
        String faText =
                "🎮 معرفی بازی معکوس Nim\n\n" +
                        "بازی Nim معکوس یک بازی فکری و استراتژیکه که بین دو بازیکن انجام می‌شه. " +
                        "در این بازی چند تا ردیف چوب کبریت وجود داره و هر بازیکن در نوبت خودش " +
                        "می‌تونه هر تعداد چوب کبریت که خواست از فقط یکی از ردیف‌ها برداره.\n\n" +
                        "❗ قانون اصلی\n" +
                        "برنده در این بازی کسیه که آخرین مهره را برندارد! یعنی هر بازیکنی که " +
                        "مجبور بشه آخرین مهره روی زمین رو برداره، بازنده حساب می‌شه.\n\n" +
                        "🎯 هدف بازی\n" +
                        "طوری مهره بردار که حریف در نهایت مجبور بشه آخرین مهره را برداره.";

        String enText =
                "🎮 Introduction to Reverse Nim Game 🎮\n\n" +
                        "Reverse Nim is a strategic and logical game played between two players. " +
                        "In this game, there are several rows of matchsticks, and on each turn " +
                        "a player may remove any number of matchsticks, but only from one row.\n\n" +
                        "❗ Main Rule\n" +
                        "The winner of the game is the player who does NOT take the last matchstick! " +
                        "In other words, the player who is forced to take the final matchstick loses.\n\n" +
                        "🎯 Game Objective\n" +
                        "Remove the matchsticks in a way that eventually forces your opponent " +
                        "to take the last one.";

        String arText =
                "🎮 مقدمة لعبة نيم العكسية\n\n" +
                        "لعبة نيم العكسية هي لعبة ذهنية واستراتيجية تُلعب بين لاعبين. " +
                        "في هذه اللعبة توجد عدة صفوف من أعواد الثقاب، وفي كل دور يمكن للاعب " +
                        "أن يزيل أي عدد من الأعواد ولكن من صف واحد فقط.\n\n" +
                        "❗ القاعدة الأساسية\n" +
                        "الفائز في هذه اللعبة هو اللاعب الذي لا يأخذ آخر قطعة! " +
                        "أي أن اللاعب الذي يُجبر على أخذ آخر عود ثقاب يُعتبر خاسراً.\n\n" +
                        "🎯 هدف اللعبة\n" +
                        "قم بإزالة الأعواد بطريقة تجعل خصمك في النهاية مُجبراً على أخذ آخر قطعة.";

        // تغییر متن با انتخاب زبان
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    textView.setText(faText);
                } else if (position == 1) {
                    textView.setText(enText);
                } else if (position == 2) {
                    textView.setText(arText);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void startButtonAnimation() {
        isAnimating = true;
        animateNext();
    }

    private void animateNext() {
        if (!isAnimating) return;

        nextButton.animate()
                .translationX(-30f)
                .setDuration(350)
                .setInterpolator(new LinearInterpolator())
                .withEndAction(() ->
                        nextButton.animate()
                                .translationX(0f)
                                .setDuration(350)
                                .setInterpolator(new LinearInterpolator())
                                .withEndAction(this::animateNext)
                                .start()
                )
                .start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAnimating = false;
        if (nextButton != null) {
            nextButton.animate().cancel();
            nextButton.setTranslationX(0f);
        }
    }

    public void goBack(View view) {
        finish();
    }
}
