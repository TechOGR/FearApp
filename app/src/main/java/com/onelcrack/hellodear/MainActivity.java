package com.onelcrack.hellodear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Map<String, Integer> codeData;

    private static final String SERVER_IP = "192.168.0.183";
    private static final int SERVER_PORT = 5000;

    private Button btnStartFear, button_qr_cam, btn_no_touch;
    private TextView textBubble;

    private String[] ipPuerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initComponents();
    }



    public void initComponents(){

        codeData = new HashMap<>();
        textBubble = findViewById(R.id.bubble_text);
        textBubble.setVisibility(TextView.INVISIBLE);

        MediaPlayer player = MediaPlayer.create(this, R.raw.grito);

        btnStartFear = findViewById(R.id.btnStartFear);
        btnStartFear.setVisibility(View.INVISIBLE);
        btnStartFear.setEnabled(false);
        btnStartFear.setOnClickListener(this);

        btn_no_touch = findViewById(R.id.btn_no_touch);
        btn_no_touch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
                Intent intent = new Intent(MainActivity.this, FearActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.animation_scale, 0);
            }
        });

        button_qr_cam = findViewById(R.id.button_qr_scan);
        button_qr_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST_CODE);
                } else {
                    startQRScanner();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

        SendMessage sendMessage = new SendMessage();

        if(view == btnStartFear) {
            sendMessage.execute(ipPuerto[0], Integer.parseInt(ipPuerto[1]), "Activar");
        }
    }



    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el código QR");
        integrator.setCameraId(0); // Cámara trasera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(CustomScannerActivity.class);
        integrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
            } else {
                handleQRResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startContinuousAnimation() {
        // Crear animación de movimiento hacia abajo y hacia arriba
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 80); // Moverse 100 píxeles hacia abajo
        animation.setDuration(800); // Duración de cada ciclo
        animation.setRepeatCount(Animation.INFINITE); // Repetir indefinidamente
        animation.setRepeatMode(Animation.REVERSE); // Hacer que vuelva hacia arriba en lugar de reiniciar
        animation.setFillAfter(true); // Mantener la posición final al terminar cada ciclo

        // Iniciar la animación en el botón
        btnStartFear.startAnimation(animation);

        // Agregar listener para mostrar la burbuja cuando el botón suba
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // No hacer nada
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // No hacer nada, ya que es una animación infinita
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Mostrar la burbuja de chat cuando el botón suba
                if (textBubble.getVisibility() == View.GONE) {
                    textBubble.setVisibility(View.VISIBLE);
                } else {
                    textBubble.setVisibility(View.GONE);
                }
            }
        });
    }

    private void handleQRResult(String qrData) {
        // Procesa el resultado del QR (ejemplo: validar si contiene IP:PUERTO)
        if (qrData.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}:\\d{1,5}\\b")) {

            // Realizar acción con los datos
            ipPuerto = qrData.split(":");
            codeData.put(ipPuerto[0], Integer.parseInt(ipPuerto[1]));
            Toast.makeText(this, "Datos del QR: " + String.valueOf(codeData), Toast.LENGTH_LONG).show();
            btnStartFear.setEnabled(true);
            btnStartFear.setVisibility(View.VISIBLE);
            button_qr_cam.setVisibility(View.INVISIBLE);
            button_qr_cam.setEnabled(false);
            startContinuousAnimation();

        } else {
            // Si no es un QR válido
            Toast.makeText(this, "Código QR no válido", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
