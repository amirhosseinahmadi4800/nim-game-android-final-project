package com.example.nimgame;

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

public class Developer extends AppCompatActivity {

    private ImageView back;
    private boolean isAnimating = false;

    private Spinner spinner;
    private TextView textView;

    private String faText, enText, arText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer);
        // نگه داشتن صفحه در حالت عمودی
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinner = findViewById(R.id.languageSpinner1);
        textView = findViewById(R.id.textView2);
        back = findViewById(R.id.back_developer);

        // متن‌ها
        faText =
                "👨‍💻 توسعه‌دهنده بازی\n\n" +
                        "این بازی توسط امیرحسین احمدی، دانشجوی کارشناسی مهندسی کامپیوتر " +
                        "دانشگاه شهید چمران اهواز ساخته شده است.\n\n" +
                        "📞 راه‌های ارتباط با توسعه‌دهنده:\n" +
                        "شماره تماس: \n" +
                        "آیدی تلگرام: @chillamirx";

        enText =
                "👨‍💻 Game Developer\n\n" +
                        "This game was developed by Amirhossein Ahmadi, " +
                        "a Bachelor's student in Computer Engineering at " +
                        "Shahid Chamran University of Ahvaz.\n\n" +
                        "📞 Contact Information:\n" +
                        "Phone Number: \n" +
                        "Telegram ID: @chillamirx";

        arText =
                "👨‍💻 مطوّر اللعبة\n\n" +
                        "تم تطوير هذه اللعبة بواسطة أمير حسين أحمدي، " +
                        "طالب بكالوريوس هندسة الحاسوب في " +
                        "جامعة شهيد جمران في الأهواز.\n\n" +
                        "📞 وسائل التواصل مع المطوّر:\n" +
                        "رقم الهاتف: \n" +
                        "معرّف تيليغرام: @chillamirx";

        // ست کردن adapter برای اسپینر
        String[] languages = {"فارسی", "English", "عربی"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // listener برای تغییر متن بر اساس انتخاب اسپینر
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

        // شروع انیمیشن برای دکمه back (فقط در صورتی که view موجود باشد)
        if (back != null) startButtonAnimation();

        // کلیک برای بازگشت به صفحهٔ قبلی
        back.setOnClickListener(v -> {
            finish();
            // انیمیشن ورود/خروج صفحه (دلخواه) - می‌تونی از منابع سفارشی هم استفاده کنی.
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    // شروع انیمیشن (فلاش جلو/عقب)
    private void startButtonAnimation() {
        isAnimating = true;
        animateNext();
    }

    private void animateNext() {
        if (!isAnimating || back == null) return;

        back.animate()
                .translationX(-30f)
                .setDuration(350)
                .setInterpolator(new LinearInterpolator())
                .withEndAction(() ->
                        back.animate()
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
        if (back != null) {
            back.animate().cancel();
            back.setTranslationX(0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // در صورت قطع شدن انیمیشن در onPause، دوباره شروعش کن
        if (!isAnimating && back != null) startButtonAnimation();
    }
}
