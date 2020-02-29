package com.koiti.checkpoint;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.koiti.checkpoint.MifareThreads.AuthenticationMifare;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.dmoral.toasty.Toasty;

/**
 * Reset a new Mifare card and save the key of accesspar
 */
public class Format extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button save, exit;
    private Boolean active = false;
    private Toast toasty;
    private byte[] key1 = new byte[]{(byte) 0x33, (byte) 0x56, (byte) 0x30, (byte) 0x70, (byte) 0x34, (byte) 0x72,
            (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
            (byte) 0x33, (byte) 0x56, (byte) 0x30, (byte) 0x70, (byte) 0x34, (byte) 0x72};
    private byte[] key2 = new byte[]{(byte) 0x41, (byte) 0x63, (byte) 0x53, (byte) 0x45, (byte) 0x76, (byte) 0x50,
            (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
            (byte) 0x41, (byte) 0x63, (byte) 0x53, (byte) 0x45, (byte) 0x76, (byte) 0x50};
    private byte[] standardKey = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    ConfigStorage config = new ConfigStorage();

    SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd  HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format);
        setTitle("Formatear");

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        save.setBackground(getDrawable(R.drawable.btn_round_green));//Select Color
                    }
                    active = true;
                    if (toasty != null)
                        toasty.cancel();
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

        boolean isAuthenticateSector1 = false, isAuthenticateSector2 = false, isAuthenticateSector3 = false;

        boolean isFormatSector1 = false, isFormatSector2 = false, isFormatSector3 = false;

//        if (active) {
//            MifareClassic mifareClassic = MifareClassic.get(nfcTag);
//            try {
//                mifareClassic.connect();
//
//                if (mifareClassic.authenticateSectorWithKeyA(3, key1)) {
//                    mifareClassic.writeBlock(15, standardKey);
//                    Log.d("datos", "si");
//                } else
//                    Log.d("datos", "no");
//
////                for (int i = 1; i < 4; i++) {
////                    if (mifareClassic.authenticateSectorWithKeyA(i, key1)) {
////                        mifareClassic.writeBlock((4 * i) + 3, standardKey);
////                        Log.d("datos", "ok");
////                    } else {
////                        if (mifareClassic.authenticateSectorWithKeyA(i, key2)) {
////                            mifareClassic.writeBlock((4 * i) + 3, standardKey);
////                            Log.d("datos", "ok2");
////                        }
////                    }
////                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            toasty = Toasty.warning(getBaseContext(), "Recuerde presionar el boton grabar para poder inicializar " +
//                    "la tarjeta", Toast.LENGTH_LONG);
//            toasty.show();
//        }

        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:

                    ExecutorService service = Executors.newFixedThreadPool(5);
                    try {
                        if (service.submit(new AuthenticationMifare(mifare, 1)).get()) {
                            isAuthenticateSector1 = true;
                            byte[] writeDataB1 = new byte[16];//data that will be written in block 1
                            byte[] writeDataB2 = new byte[16];//data that will be written in block 2

                            int code = config.getValueInt("code", context);
                            int id = config.getValueInt("id", context);

                            //Initialize the arrays
                            for (int i = 0; i < 16; i++) {
                                writeDataB1[i] = (byte) 0;
                                writeDataB2[i] = (byte) 0;
                            }

                            writeDataB1[0] = (byte) 1;
                            writeDataB1[1] = (byte) code;
                            writeDataB1[3] = (byte) id;

                            Log.d("datos", id + "---" + code);

                            boolean row0 = mifare.writeMifareTag(1, 0, writeDataB2);
                            boolean row1 = mifare.writeMifareTag(1, 1, writeDataB1);
                            boolean row2 = mifare.writeMifareTag(1, 2, writeDataB2);
                            boolean s1row3 = mifare.writeMifareTag(1, 3, key1);

                            if (row0 && row1 && row2 && s1row3)
                                isFormatSector1 = true;
                        }

                        if (service.submit(new AuthenticationMifare(mifare, 2)).get()) {
                            isAuthenticateSector2 = true;
                            boolean s2row3 = mifare.writeMifareTag(2, 3, key2);

                            if (s2row3)
                                isFormatSector2 = true;
                        }

                        if (service.submit(new AuthenticationMifare(mifare, 3)).get()) {
                            isAuthenticateSector3 = true;
                            boolean s3row3 = mifare.writeMifareTag(3, 3, key1);

                            if (s3row3)
                                isFormatSector3 = true;
                        }

                        if ((isFormatSector1 && isFormatSector2 && isFormatSector3) || (!isAuthenticateSector1 && !isAuthenticateSector2 && !isAuthenticateSector3)) {
                            final Toast toasty = Toasty.success(Format.this, "" + "Formateo Exitoso", Toast.LENGTH_LONG);
                            toasty.show();

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    toasty.cancel();
                                }
                            }, 700);
                        } else Toasty.error(getBaseContext(), "" + "El formateo " +
                                "ha fallado  por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    service.shutdown();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        save.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                    }
                    active = false;
                    break;
            }
        } else {
            toasty = Toasty.warning(getBaseContext(), "Recuerde presionar el boton grabar para poder inicializar " +
                    "la tarjeta", Toast.LENGTH_LONG);
            toasty.show();
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