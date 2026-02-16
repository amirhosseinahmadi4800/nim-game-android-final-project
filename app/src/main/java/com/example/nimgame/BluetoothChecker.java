package com.example.nimgame;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothChecker extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    public static String playerRole;
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothAdapter bluetoothAdapter;
    TextView bluetoothStatus;
    Button buttonFirstPlayer;
    ImageView nextButton;
    Spinner spinnerDevices;

    private final ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();

    AcceptThread acceptThread;
    ConnectThread connectThread;
    ConnectedThread connectedThread;

    BluetoothDevice targetDevice;

    private boolean isConnected = false;           // ← کلید اصلی تغییر
    private boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        playerRole = "player2";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothview);

        bluetoothStatus = findViewById(R.id.bluetoothStatus);
        buttonFirstPlayer = findViewById(R.id.button);
        nextButton = findViewById(R.id.imageView9);
        spinnerDevices = findViewById(R.id.spinnerDevices);

        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= pairedDevicesList.size()) {
                    targetDevice = pairedDevicesList.get(position - 1);
                } else {
                    targetDevice = null;
                }
                updateBluetoothStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                targetDevice = null;
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            bluetoothStatus.setText("دستگاه شما بلوتوث ندارد");
            return;
        }

        checkPermissions();
        updateBluetoothStatus();

        // دکمه نفر اول (اتصال دهنده) — از Spinner دستگاه انتخاب شده رو می‌گیره
        buttonFirstPlayer.setOnClickListener(v -> {
            BluetoothDevice toConnect = getSelectedDevice();
            if (toConnect == null) {
                bluetoothStatus.setText("یک دستگاه را انتخاب کنید");
                Toast.makeText(this, "لطفاً دستگاه حریف (گوشی دیگر) را از لیست انتخاب کنید", Toast.LENGTH_LONG).show();
                return;
            }
            if (isConnected) {
                bluetoothStatus.setText("قبلاً متصل شدید!");
                return;
            }

            playerRole = "player1";
            bluetoothStatus.setText("در حال اتصال به " + getDeviceName(toConnect) + "...");

            new Thread(() -> connectToDevice(toConnect)).start();
        });
    }

    private void updateBluetoothStatus() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothStatus.setText("بلوتوث خاموش است\nبه صفحه قبل برگردید و مراحل را انجام بدید.");
            buttonFirstPlayer.setEnabled(false);
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
            stopNextAnimation();
            return;
        }

        if (pairedDevicesList.isEmpty()) {
            bluetoothStatus.setText("هیچ دستگاه جفت‌شده‌ای پیدا نشد\nدر تنظیمات بلوتوث دستگاه حریف را جفت کن");
            buttonFirstPlayer.setEnabled(false);
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
            stopNextAnimation();
            return;
        }

        if (targetDevice == null) {
            bluetoothStatus.setText("دستگاه حریف را از لیست انتخاب کن\n(گوشی که اپ Nim رو روش باز داره)");
            buttonFirstPlayer.setEnabled(false);
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
            stopNextAnimation();
            return;
        }

        if (isConnected) {
            bluetoothStatus.setText("اتصال برقرار شد ✓\nآماده شروع بازی");
            buttonFirstPlayer.setEnabled(false);  // دیگه نیازی به اتصال مجدد نیست
            nextButton.setEnabled(true);
            nextButton.setAlpha(1f);
            startNextAnimation();   // فقط اینجا انیمیشن شروع می‌شود
        } else {
            bluetoothStatus.setText("بلوتوث روشن است\nمنتظر اتصال به بازیکن دیگر...");
            buttonFirstPlayer.setEnabled(true);
            nextButton.setEnabled(false);
            nextButton.setAlpha(0.5f);
            stopNextAnimation();
        }
    }

    private void checkPermissions() {
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
            startBluetoothAfterPermission();
        } else {
            startBluetoothAfterPermission();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    private void startBluetoothAfterPermission() {
        startAcceptThread();
        populatePairedDevice();
    }

    private BluetoothDevice getSelectedDevice() {
        int pos = spinnerDevices.getSelectedItemPosition();
        if (pos > 0 && pos <= pairedDevicesList.size()) {
            return pairedDevicesList.get(pos - 1);
        }
        return pairedDevicesList.isEmpty() ? null : pairedDevicesList.get(0);
    }

    private String getDeviceName(BluetoothDevice d) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            return d.getAddress();
        }
        String name = d.getName();
        return (name != null && !name.isEmpty()) ? name : d.getAddress();
    }

    private void populatePairedDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "BLUETOOTH_CONNECT permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        pairedDevicesList.clear();
        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        ArrayList<String> names = new ArrayList<>();
        names.add("-- دستگاه حریف را انتخاب کنید --");
        for (BluetoothDevice d : paired) {
            pairedDevicesList.add(d);
            names.add(getDeviceName(d) + " (" + d.getAddress().substring(Math.max(0, d.getAddress().length() - 8)) + ")");
        }
        if (!pairedDevicesList.isEmpty()) {
            targetDevice = pairedDevicesList.get(0);
        } else {
            targetDevice = null;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(adapter);
        updateBluetoothStatus();
    }

    // ──────────────────────────────────────────────
    // AcceptThread (گوش دادن برای اتصال ورودی)
    // ──────────────────────────────────────────────
    private void startAcceptThread() {
        if (acceptThread != null) acceptThread.cancel();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        AcceptThread() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(BluetoothChecker.this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("NimGame", MY_UUID);
                } else {
                    serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("NimGame", MY_UUID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if (serverSocket == null) return;
            BluetoothSocket socket;
            while (!isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    if (socket != null) {
                        manageConnectedSocket(socket);
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void cancel() {
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException ignored) {}
        }
    }

    // ──────────────────────────────────────────────
    // ConnectThread (اتصال به دستگاه دیگر)
    // ──────────────────────────────────────────────
    private void connectToDevice(BluetoothDevice device) {
        if (connectThread != null) connectThread.cancel();
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;

        ConnectThread(BluetoothDevice device) {
            this.device = device;
        }

        public void run() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(BluetoothChecker.this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) return;

                bluetoothAdapter.cancelDiscovery();
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    } else {
                        socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    }
                    socket.connect();
                } catch (IOException e) {
                    socket = null;
                    Method m;
                    try {
                        m = device.getClass().getMethod("createRfcommSocket", int.class);
                    } catch (NoSuchMethodException nsme) {
                        throw new IOException("اتصال شکست خورد.", e);
                    }
                    for (int channel = 1; channel <= 10; channel++) {
                        try {
                            socket = (BluetoothSocket) m.invoke(device, channel);
                            if (socket != null) {
                                socket.connect();
                                break;
                            }
                        } catch (Exception ex) {
                            try { if (socket != null) socket.close(); } catch (IOException ignored) {}
                            socket = null;
                            if (channel == 10) throw new IOException("اتصال شکست خورد. هر دو دستگاه باید اپ باز و روی صفحه بلوتوث باشند.", ex);
                        }
                    }
                }

                manageConnectedSocket(socket);

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(BluetoothChecker.this, "اتصال موفق نبود", Toast.LENGTH_SHORT).show();
                    bluetoothStatus.setText("اتصال ناموفق بود. دوباره تلاش کنید.");
                });
                try {
                    if (socket != null) socket.close();
                } catch (IOException ignored) {}
            }
        }

        void cancel() {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    // ──────────────────────────────────────────────
    // اتصال → ذخیره در BluetoothConnectionHolder برای استفاده در GameActivity
    // ──────────────────────────────────────────────
    private void manageConnectedSocket(BluetoothSocket socket) {
        if (connectedThread != null) connectedThread.cancel();

        BluetoothConnectionHolder holder = BluetoothConnectionHolder.getInstance();
        holder.setConnection(socket);
        holder.setMessageReceiver(data -> runOnUiThread(() -> {
            String msg = new String(data).trim();
            if (!msg.isEmpty()) bluetoothStatus.setText("پیام: " + msg);
        }));

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        isConnected = true;

        runOnUiThread(() -> {
            updateBluetoothStatus();
            bluetoothStatus.setText("اتصال موفق ✓\nمی‌تونی بازی رو شروع کنی");
        });

        holder.write("اتصال موفق ✓\nمی‌تونی بازی رو شروع کنی".getBytes());
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private volatile boolean running = true;

        ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        public void run() {
            while (running) {
                try {
                    if (!socket.isConnected()) break;
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        public void cancel() {
            running = false;
            runOnUiThread(() -> {
                isConnected = false;
                BluetoothConnectionHolder.getInstance().disconnect();
                updateBluetoothStatus();
                bluetoothStatus.setText("اتصال قطع شد");
            });
        }
    }

    // ──────────────────────────────────────────────
    // انیمیشن nextButton
    // ──────────────────────────────────────────────
    private void startNextAnimation() {
        if (isAnimating || !isConnected) return;
        isAnimating = true;
        animateNext();
    }

    private void animateNext() {
        if (!isAnimating) return;

        nextButton.animate()
                .translationXBy(30)
                .setDuration(350)
                .withEndAction(() ->
                        nextButton.animate()
                                .translationXBy(-30)
                                .setDuration(350)
                                .withEndAction(this::animateNext)
                                .start()
                )
                .start();

        nextButton.setOnClickListener(v -> {
            stopNextAnimation();
            startActivity(new Intent(BluetoothChecker.this, GameActivity.class));
        });
    }

    private void stopNextAnimation() {
        isAnimating = false;
        nextButton.animate().cancel();
        nextButton.setTranslationX(0);
    }

    // ──────────────────────────────────────────────
    // Lifecycle و Permissions
    // ──────────────────────────────────────────────
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
            if (allGranted && grantResults.length > 0) {
                startBluetoothAfterPermission();
            } else {
                Toast.makeText(this,
                        "برای اجرای بازی باید دسترسی بلوتوث  فعال باشد",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            updateBluetoothStatus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (acceptThread != null) acceptThread.cancel();
        if (connectThread != null) connectThread.cancel();
        if (connectedThread != null) connectedThread.cancel();
    }
}