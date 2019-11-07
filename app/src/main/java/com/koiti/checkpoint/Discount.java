package com.koiti.checkpoint;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Discount extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button save, exit;
    private Boolean active = false;
    private int discount, type, discountValue, inpago;

    ConfigStorage config = new ConfigStorage();

    Date date = new Date();
    SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
    String DateIn = dateIn.format(date);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount);
        setTitle("Descuento");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView textViewDate = findViewById(R.id.datediscount_Id);
        textViewDate.setText(DateIn);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;
        inpago = 0;

        save = findViewById(R.id.savedisc_Id);
        exit = findViewById(R.id.exitdisc_Id);

        save.setOnClickListener(mListener);
        exit.setOnClickListener(mListener);

        type = config.getValueInt("tipo", context);
        discountValue = config.getValueInt("discountValue", context);
        discount = config.getValueInt("discount", context);

        if (type == 0) {
            if (discountValue > 2047) {
                discountValue = 0;
            } else if (discountValue > 255 && discountValue < 2047) {
                if (discountValue < 512) inpago = 17;
                if (discountValue >= 512 && discountValue < 1024) inpago = 27;
                if (discountValue >= 1024) inpago = 31;
            } else {
                inpago = 1;
            }
        } else {
            inpago = 129;
            if (discountValue > 100) {
                discountValue = 0;
            }
        }
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
        String action = intent.getAction();

        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Mifare mifare = new Mifare(nfcTag);

        /**
         Estructuración del valor de descuento, cambio entre byte a binario
         */
        String discountBinary = Integer.toBinaryString(discountValue);
        String[] arrayBinary = discountBinary.split("(?!\\A)");
        StringBuilder sbuilderDiscount = new StringBuilder(); //Binario
        StringBuilder sbuilderInpago = new StringBuilder(); //Binario

        int j = 0;

        if (discountValue > 255) {
            for (int i = arrayBinary.length - 8; i < arrayBinary.length; i++) {
                sbuilderDiscount.append(arrayBinary[i]);
            }
            while (j < arrayBinary.length - 8) {
                sbuilderInpago.append(arrayBinary[j]);
                j++;
            }
        } else {
            for (int i = 0; i < arrayBinary.length; i++) {
                sbuilderDiscount.append(arrayBinary[i]);
            }
            sbuilderInpago.append("000");
        }
        sbuilderInpago.insert(0, type);

        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    if (mifare.authentificationKey(Mifare.KOITI_KEY1, Mifare.KEY_TYPE_A, 1)) {
                        byte[] datosB1 = mifare.readMifareTagBlock(1, 1);
                        byte[] datosB2 = mifare.readMifareTagBlock(1, 2);
                        byte[] writeData = new byte[16];

                        int code = config.getValueInt("code", context);
                        int id = config.getValueInt("id", context);
                        int consecutive = config.getValueInt("consecutive", context);

                        for (int i = 0; i < 16; i++) {
                            writeData[i] = datosB2[i];
                        }

                        writeData[5] = (byte) Integer.parseInt(String.valueOf(sbuilderDiscount), 2);
                        writeData[6] = (byte) discount;
                        writeData[9] = (byte) inpago;

                        Log.d("stringbuilder Descuento", sbuilderDiscount + "--" + sbuilderInpago + "--");

                        if (datosB1[0] == 1 && datosB1[1] == code && datosB1[3] == id) {
                            mifare.writeMifareTag(1, 2, writeData);
                            Toast.makeText(getBaseContext(), "" + "Escritura Exitosa", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                    " parqueadero.", Toast.LENGTH_LONG).show();
                        }

                        save.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                        active = false;
                    } else
                        Toast.makeText(getBaseContext(), "Fallo de autentificación", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toast.makeText(getBaseContext(), "Recuerde presionar un boton para poder hacer operaciones " +
                    "en la tarjeta", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, Discount.class).
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