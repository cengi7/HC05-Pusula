package com.cengiz.hc05pusula;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQ_PERM = 100;

    private TextView tvYon, tvDerece, tvDurum;
    private Button btnBaglan, btnKes;
    private PusulaView pusula;

    private BluetoothAdapter btAdapter;
    private BluetoothSocket socket;
    private Thread okumaThread;
    private volatile boolean bagli = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvYon = findViewById(R.id.tvYon);
        tvDerece = findViewById(R.id.tvDerece);
        tvDurum = findViewById(R.id.tvDurum);
        btnBaglan = findViewById(R.id.btnBaglan);
        btnKes = findViewById(R.id.btnKes);
        pusula = findViewById(R.id.pusula);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            tvDurum.setText("Bu cihazda Bluetooth yok");
            btnBaglan.setEnabled(false);
            return;
        }

        btnBaglan.setOnClickListener(v -> izinIsteVeBaglan());
        btnKes.setOnClickListener(v -> baglantiyiKes());
    }

    private void izinIsteVeBaglan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN}, REQ_PERM);
                return;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERM);
                return;
            }
        }
        cihazSecVeBaglan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERM) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cihazSecVeBaglan();
            } else {
                Toast.makeText(this, "İzin verilmedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void cihazSecVeBaglan() {
        if (!btAdapter.isEnabled()) {
            tvDurum.setText("Bluetooth kapalı, lütfen açın");
            return;
        }
        Set<BluetoothDevice> eslesmis = btAdapter.getBondedDevices();
        if (eslesmis.isEmpty()) {
            tvDurum.setText("Eşleşmiş cihaz yok. Önce ayarlardan HC-05'i eşleştirin (PIN 1234).");
            return;
        }

        String[] adlar = new String[eslesmis.size()];
        BluetoothDevice[] cihazlar = new BluetoothDevice[eslesmis.size()];
        int i = 0;
        for (BluetoothDevice d : eslesmis) {
            cihazlar[i] = d;
            adlar[i] = d.getName() + "\n" + d.getAddress();
            i++;
        }

        new AlertDialog.Builder(this)
                .setTitle("HC-05 seç")
                .setItems(adlar, (dialog, which) -> baglan(cihazlar[which]))
                .show();
    }

    @SuppressLint("MissingPermission")
    private void baglan(BluetoothDevice device) {
        tvDurum.setText("Bağlanıyor: " + device.getName());
        new Thread(() -> {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                btAdapter.cancelDiscovery();
                socket.connect();
                bagli = true;

                runOnUiThread(() -> {
                    tvDurum.setText("Bağlı: " + device.getName());
                    btnBaglan.setEnabled(false);
                    btnKes.setEnabled(true);
                });

                okumaBaslat();
            } catch (IOException e) {
                runOnUiThread(() -> tvDurum.setText("Bağlantı hatası: " + e.getMessage()));
                closeSocket();
            }
        }).start();
    }

    private void okumaBaslat() {
        okumaThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                String satir;
                while (bagli && (satir = reader.readLine()) != null) {
                    isleSatir(satir.trim());
                }
            } catch (IOException e) {
                // bağlantı kesilmiş olabilir
            } finally {
                runOnUiThread(() -> {
                    if (bagli) tvDurum.setText("Bağlantı koptu");
                    btnBaglan.setEnabled(true);
                    btnKes.setEnabled(false);
                });
                bagli = false;
            }
        });
        okumaThread.start();
    }

    private void isleSatir(String satir) {
        String[] parca = satir.split(",");
        if (parca.length != 2) return;

        String yon = parca[0].trim();
        double derece;
        try {
            derece = Double.parseDouble(parca[1].trim());
        } catch (NumberFormatException e) {
            return;
        }

        runOnUiThread(() -> {
            tvYon.setText(yon);
            tvDerece.setText(String.format(java.util.Locale.US, "%.1f°", derece));
            pusula.setAci((float) derece);
        });
    }

    private void baglantiyiKes() {
        bagli = false;
        closeSocket();
        tvDurum.setText("Bağlantı kesildi");
        btnBaglan.setEnabled(true);
        btnKes.setEnabled(false);
    }

    private void closeSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) { }
        socket = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bagli = false;
        closeSocket();
    }
}