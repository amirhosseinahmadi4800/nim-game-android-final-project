package com.example.nimgame;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;


public class GameActivity extends AppCompatActivity {

    public static GameActivity instance;

    private int selectedCount = 0;
    private Integer activeRow = null;
    private boolean isMyTurn = false;

    private TextView tvPlayers;
    private TextView tvPlayers1;

    private ImageView[] images;
    private ImageView btnNext;
    private ImageView ivGameResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        instance = this;

        tvPlayers = findViewById(R.id.tvPlayers);
        tvPlayers.setText("اسم بازیکن : "+nameplyer.playerName);
        tvPlayers1 = findViewById(R.id.tvPlayers1);
        tvPlayers1.setText("نوبت حریف");
        images = new ImageView[]{
                findViewById(R.id.imageView1), findViewById(R.id.imageView2),
                findViewById(R.id.imageView4), findViewById(R.id.imageView5),
                findViewById(R.id.imageView6), findViewById(R.id.imageView7),
                findViewById(R.id.imageView8), findViewById(R.id.imageView9),
                findViewById(R.id.imageView10), findViewById(R.id.imageView11),
                findViewById(R.id.imageView12), findViewById(R.id.imageView13),
                findViewById(R.id.imageView14),
                findViewById(R.id.imageView15), findViewById(R.id.imageView17),
                findViewById(R.id.imageView19), findViewById(R.id.imageView20),
                findViewById(R.id.imageView21), findViewById(R.id.imageView22),
                findViewById(R.id.imageView23), findViewById(R.id.imageView24),
                findViewById(R.id.imageView25), findViewById(R.id.imageView26),
                findViewById(R.id.imageView27), findViewById(R.id.imageView28),
                findViewById(R.id.imageView29),
                findViewById(R.id.imageView30), findViewById(R.id.imageView31),
                findViewById(R.id.imageView32), findViewById(R.id.imageView33),
                findViewById(R.id.imageView34), findViewById(R.id.imageView35),
                findViewById(R.id.imageView36), findViewById(R.id.imageView37),
                findViewById(R.id.imageView38), findViewById(R.id.imageView39),
                findViewById(R.id.imageView40), findViewById(R.id.imageView41),
                findViewById(R.id.imageView42),
                findViewById(R.id.imageView43), findViewById(R.id.imageView44),
                findViewById(R.id.imageView45), findViewById(R.id.imageView46),
                findViewById(R.id.imageView47), findViewById(R.id.imageView48),
                findViewById(R.id.imageView49), findViewById(R.id.imageView50),
                findViewById(R.id.imageView51), findViewById(R.id.imageView52),
                findViewById(R.id.imageView53), findViewById(R.id.imageView54),
                findViewById(R.id.imageView55)
        };

        setRow(0,
                R.id.imageView1, R.id.imageView2, R.id.imageView4, R.id.imageView5,
                R.id.imageView6, R.id.imageView7, R.id.imageView8, R.id.imageView9,
                R.id.imageView10, R.id.imageView11, R.id.imageView12, R.id.imageView13,
                R.id.imageView14
        );

        setRow(1,
                R.id.imageView15, R.id.imageView17, R.id.imageView19, R.id.imageView20,
                R.id.imageView21, R.id.imageView22, R.id.imageView23, R.id.imageView24,
                R.id.imageView25, R.id.imageView26, R.id.imageView27, R.id.imageView28,
                R.id.imageView29
        );

        setRow(2,
                R.id.imageView30, R.id.imageView31, R.id.imageView32, R.id.imageView33,
                R.id.imageView34, R.id.imageView35, R.id.imageView36, R.id.imageView37,
                R.id.imageView38, R.id.imageView39, R.id.imageView40, R.id.imageView41,
                R.id.imageView42
        );

        setRow(3,
                R.id.imageView43, R.id.imageView44, R.id.imageView45, R.id.imageView46,
                R.id.imageView47, R.id.imageView48, R.id.imageView49, R.id.imageView50,
                R.id.imageView51, R.id.imageView52, R.id.imageView53, R.id.imageView54,
                R.id.imageView55
        );

        if ("player1".equals(BluetoothChecker.playerRole)) {
            isMyTurn = true;
            Toast.makeText(this, "نوبت توئه! بازی رو شروع کن", Toast.LENGTH_SHORT).show();
            tvPlayers1 = findViewById(R.id.tvPlayers1);
            tvPlayers1.setText("نوبت توئه! ");

        } else {
            isMyTurn = false;
            Toast.makeText(this, "منتظر نوبت حریف باش", Toast.LENGTH_SHORT).show();
        }

        for (ImageView img : images) {
            img.setOnClickListener(v -> {
                if (isMyTurn) toggleSelect((ImageView) v);
                else Toast.makeText(this, "منتظر نوبت خودت باش", Toast.LENGTH_SHORT).show();
            });
        }

        BluetoothConnectionHolder holder = BluetoothConnectionHolder.getInstance();
        if (holder.hasConnection()) {
            holder.setMessageReceiver(this::handleReceivedBluetoothMessage);
        } else {
            Toast.makeText(this, "اتصال بلوتوث برقرار نیست. از صفحه بلوتوث وارد شوید.", Toast.LENGTH_LONG).show();
        }

        btnNext = findViewById(R.id.imageView18);
        ivGameResult = findViewById(R.id.ivGameResult);

        // مهم: اجازه کلیک روی عکس نتیجه
        ivGameResult.setClickable(true);
        ivGameResult.setFocusable(true);

        // کلیک روی عکس نتیجه → بازگشت به صفحه اصلی
        ivGameResult.setOnClickListener(v -> {
            if (ivGameResult.getVisibility() == View.VISIBLE) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!isMyTurn) return;
            if (selectedCount == 0) {
                Toast.makeText(this, "حداقل یک چوب انتخاب کن", Toast.LENGTH_SHORT).show();
                return;
            }

            int row = activeRow;
            int count = selectedCount;

            // سوزاندن چوب‌های انتخابی خودم
            for (ImageView img : images) {
                if (img.isSelected()) {
                    img.setImageResource(R.drawable.match_burned);
                    img.setSelected(false);
                    img.setBackground(null);
                    img.setEnabled(false);
                }
            }

            selectedCount = 0;
            activeRow = null;

            // همیشه پیام move را بفرست
            byte[] message = createMoveMessage(row, count);
            sendMessage(message);

            // چک پایان بازی
            showGameResult();

            // اگر بازی تمام نشده، نوبت را تغییر بده
            if (ivGameResult.getVisibility() != View.VISIBLE) {
                isMyTurn = false;
                btnNext.setEnabled(false);
                Toast.makeText(this, "نوبت حریف", Toast.LENGTH_SHORT).show();
                tvPlayers1.setText("نوبت حریف ");
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // هیچی ننویس → دکمه Back غیرفعال می‌شود
            }
        });

    }

    private void sendMessage(byte[] message) {
        BluetoothConnectionHolder holder = BluetoothConnectionHolder.getInstance();
        if (holder.hasConnection() && holder.write(message)) {
            return;
        }
        Toast.makeText(this, "اتصال بلوتوث برقرار نیست. به صفحه بلوتوث برگردید.", Toast.LENGTH_LONG).show();
    }

    private void handleReceivedBluetoothMessage(byte[] data) {
        try {
            String received = new String(data, "UTF-8").trim();
            Log.d("Bluetooth", "دریافت: " + received);
            JSONObject json = new JSONObject(received);
            String type = json.optString("type", "");
            if ("move".equals(type)) {
                int row = json.getInt("row");
                int count = json.getInt("count");
                runOnUiThread(() -> {
                    applyOpponentMove(row, count);
                    showGameResult();
                    if (ivGameResult.getVisibility() != View.VISIBLE) {
                        isMyTurn = true;
                        btnNext.setEnabled(true);
                        Toast.makeText(GameActivity.this, "نوبت توئه!", Toast.LENGTH_SHORT).show();
                        tvPlayers1.setText("نوبت توئه! ");
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Bluetooth", "خطا در تجزیه پیام", e);
        }
    }

    private byte[] createMoveMessage(int row, int count) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "move");
            json.put("row", row);
            json.put("count", count);
            return json.toString().getBytes("UTF-8");
        } catch (Exception e) {
            Log.e("Bluetooth", "خطا در ساخت JSON", e);
            return "{\"type\":\"error\"}".getBytes();
        }
    }

    private void applyOpponentMove(int row, int count) {
        int remaining = count;
        for (ImageView img : images) {
            if (remaining <= 0) break;
            Integer imgRow = (Integer) img.getTag();
            if (imgRow != null && imgRow == row && img.isEnabled()) {
                img.setImageResource(R.drawable.match_burned);
                img.setEnabled(false);
                remaining--;
            }
        }
        if (remaining > 0) {
            Log.w("Game", "هشدار: چوب کافی در ردیف " + row + " نبود");
        }
    }

    private boolean isGameOver() {
        for (ImageView img : images) {
            if (img.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    private void showGameResult() {
        if (!isGameOver() || ivGameResult.getVisibility() == View.VISIBLE) {
            return;
        }

        boolean iLost = isMyTurn;
        int resultImageRes = iLost ? R.drawable.loz : R.drawable.win;

        ivGameResult.setImageResource(resultImageRes);
        ivGameResult.setVisibility(View.VISIBLE);

        btnNext.setEnabled(false);
        for (ImageView img : images) {
            img.setEnabled(false);
            img.setClickable(false);
        }

        String message = iLost ?
                "متأسفانه باختی 😔 آخرین چوب را برداشتی!" :
                "تبریک! برنده شدی 🎉 حریف آخرین چوب را برداشت";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothConnectionHolder.getInstance().setMessageReceiver(null);
    }

    private void setRow(int row, int... ids) {
        for (int id : ids) {
            for (ImageView img : images) {
                if (img.getId() == id) {
                    img.setTag(row);
                    break;
                }
            }
        }
    }

    private void toggleSelect(ImageView img) {
        if (!img.isEnabled()) {
            Toast.makeText(this, "این چوب دیگر قابل انتخاب نیست", Toast.LENGTH_SHORT).show();
            return;
        }

        int row = (int) img.getTag();

        if (img.isSelected()) {
            img.setSelected(false);
            img.setBackground(null);
            selectedCount--;
            if (selectedCount == 0) activeRow = null;
            return;
        }

        if (activeRow != null && row != activeRow) {
            Toast.makeText(this, "فقط از یک ردیف می‌توان انتخاب کرد", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCount >= 4) {
            Toast.makeText(this, "حداکثر ۴ چوب مجاز است", Toast.LENGTH_SHORT).show();
            return;
        }

        img.setSelected(true);
        img.setBackgroundResource(R.drawable.selected_bg);
        selectedCount++;
        if (activeRow == null) activeRow = row;
    }
    // --------------------------------------
// تابع برای double click روی imageView16
// --------------------------------------
    private boolean doubleClickExit = false; // flag برای کلیک دوم

    public void handleImageView16Click(View view) {
        if (doubleClickExit) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();

        } else {
            doubleClickExit = true;
            Toast.makeText(this, "برای خروج یک بار دیگر کلیک کنید", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleClickExit = false, 2000);
        }
    }

}