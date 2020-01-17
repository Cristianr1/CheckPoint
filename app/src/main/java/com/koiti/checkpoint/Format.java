package com.koiti.checkpoint;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

/**
 * Initialize the cards that belong to the parking
 */
public class Format extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button save, exit;
    private Boolean active = false;

    ConfigStorage config = new ConfigStorage();

    Date date = new Date();
    SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd  HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format);
        setTitle("Inicializar");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final TextView textViewDate = findViewById(R.id.date_Id);

        //This thread updates the date every minute
        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textViewDate.setText(dateIn.format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        save = findViewById(R.id.savedisc_Id);
        exit = findViewById(R.id.exitdisc_Id);

        save.setOnClickListener(mListener);
        exit.setOnClickListener(mListener);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.savedisc_Id:
                    save.setBackgroundColor(Color.parseColor("#02840A"));//Select Color
                    active = true;
                    break;

                case R.id.exitdisc_Id:
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            return;
        }
        Toasty.Config.getInstance().setTextSize(24).apply();

        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Mifare mifare = new Mifare(nfcTag);

        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    if (mifare.authentificationKey(Mifare.KOITI_KEY1, Mifare.KEY_TYPE_A, 1)) {

                        byte[] datosB1 = mifare.readMifareTagBlock(1, 1);

                        byte[] writeDataB1 = new byte[16];//data that will be written in block 1
                        byte[] writeDataB2 = new byte[16];//data that will be written in block 2

                        int code = config.getValueInt("code", context);
                        int id = config.getValueInt("id", context);

                        //Initialize the arrays
                        for (int i = 0; i < 16; i++) {
                            writeDataB1[i] = (byte) 0;
                            writeDataB2[i] = (byte) 0;
                        }

                        if (datosB1 != null) {
                            // evaluate if the card belongs to the parking
                            if (datosB1[1] == code && datosB1[3] == id) {
                                writeDataB1[0] = (byte) 1;
                                writeDataB1[1] = (byte) code;
                                writeDataB1[3] = (byte) id;

                                boolean row0 = mifare.writeMifareTag(1, 0, writeDataB2);
                                boolean row1 = mifare.writeMifareTag(1, 1, writeDataB1);
                                boolean row2 = mifare.writeMifareTag(1, 2, writeDataB2);

                                if (row0 && row1 && row2) {
                                    final Toast toasty = Toasty.success(Format.this, "" + "Escritura Exitosa", Toast.LENGTH_LONG);
                                    toasty.show();

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            toasty.cancel();
                                        }
                                    }, 700);
                                } else
                                    Toasty.error(getBaseContext(), "" + "La inicialización " +
                                            "ha fallado  por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();

                            } else {
                                Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                        " parqueadero.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toasty.error(getBaseContext(), "" + "La lectura ha fallado" +
                                    " por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();
                        }
                        save.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                        active = false;
                    } else
                        Toasty.error(getBaseContext(), "Fallo de autentificación", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toasty.warning(getBaseContext(), "Recuerde presionar el boton grabar para poder inicializar " +
                    "la tarjeta", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, Format.class).
                addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        IntentFilter[] intentFilter = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                intentFilter, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }
}