package com.example.nimgame;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

/**
 * نگه‌دارندهٔ اتصال بلوتوث مشترک بین BluetoothChecker و GameActivity
 */
public class BluetoothConnectionHolder {

    private static BluetoothConnectionHolder instance;
    private BluetoothSocket socket;
    private OutputStream out;
    private Thread readerThread;
    private volatile boolean running;
    private final AtomicReference<MessageReceiver> receiver = new AtomicReference<>();

    public interface MessageReceiver {
        void onMessage(byte[] data);
    }

    public static synchronized BluetoothConnectionHolder getInstance() {
        if (instance == null) {
            instance = new BluetoothConnectionHolder();
        }
        return instance;
    }

    public synchronized void setConnection(BluetoothSocket socket) {
        disconnect();
        this.socket = socket;
        try {
            out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            running = true;
            readerThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytes;
                while (running && in != null) {
                    try {
                        bytes = in.read(buffer);
                        if (bytes > 0) {
                            byte[] data = new byte[bytes];
                            System.arraycopy(buffer, 0, data, 0, bytes);
                            MessageReceiver r = receiver.get();
                            if (r != null) {
                                r.onMessage(data);
                            }
                        }
                    } catch (IOException e) {
                        if (running) Log.e("BluetoothHolder", "read error", e);
                        break;
                    }
                }
            });
            readerThread.start();
        } catch (IOException e) {
            Log.e("BluetoothHolder", "setup error", e);
        }
    }

    public void setMessageReceiver(MessageReceiver r) {
        receiver.set(r);
    }

    public boolean write(byte[] data) {
        if (out == null) return false;
        try {
            out.write(data);
            out.flush();
            return true;
        } catch (IOException e) {
            Log.e("BluetoothHolder", "write error", e);
            return false;
        }
    }

    public boolean hasConnection() {
        return socket != null && socket.isConnected();
    }

    public synchronized void disconnect() {
        running = false;
        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }
        try {
            if (out != null) out.close();
        } catch (IOException ignored) {}
        out = null;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        socket = null;
        receiver.set(null);
    }
}
