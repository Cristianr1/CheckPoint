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
import com.koiti.checkpoint.MifareThreads.Disconnect;
import com.koiti.checkpoint.MifareThreads.ReadMifare;
import com.koiti.checkpoint.MifareThreads.WriteMifare;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.dmoral.toasty.Toasty;

/**
 * Initialize the cards that belong to the parking
 */
public class Initialize extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button save, exit;
    private Boolean active = false;
    private Toast toasty;

    ConfigStorage config = new ConfigStorage();

    private int code, id;
    byte[] writeDataB1 = new byte[16];//data that will be written in block 1
    byte[] writeDataB2 = new byte[16];//data that will be written in block 2

    Date date = new Date();
    SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd  HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize);
        setTitle("Inicializar");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        code = config.getValueInt("code", context);
        id = config.getValueInt("id", context);


        for (int i = 0; i < 16; i++) {
            writeDataB1[i] = (byte) 0;
            writeDataB2[i] = (byte) 0;
        }

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

        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    ExecutorService service = Executors.newFixedThreadPool(5);
                    try {
                        if (service.submit(new AuthenticationMifare(mifare, context)).get()) {

                            Future<byte[]> data = service.submit(new ReadMifare(mifare, 1));

                            byte[] datosB1 = data.get();

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

                                    Future<Boolean> writeMifare0 = service.submit(new WriteMifare(mifare, writeDataB2, 0));
                                    Future<Boolean> writeMifare1 = service.submit(new WriteMifare(mifare, writeDataB1, 1));
                                    Future<Boolean> writeMifare2 = service.submit(new WriteMifare(mifare, writeDataB2, 2));


                                    if (writeMifare0.get() && writeMifare1.get() && writeMifare2.get()) {
                                        final Toast toasty = Toasty.success(Initialize.this, "" + "Escritura Exitosa", Toast.LENGTH_LONG);
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

                                } else
                                    Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                            " parqueadero.", Toast.LENGTH_LONG).show();
                            } else
                                Toasty.error(getBaseContext(), "" + "La lectura ha fallado" +
                                        " por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();

                        } else
                            Toasty.error(getBaseContext(), "Fallo de autentificación", Toast.LENGTH_LONG).show();

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    service.shutdown();

                    new Thread(new Disconnect(mifare)).start();
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
        Intent intent = new Intent(this, Initialize.class).
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

    public class Write implements Runnable {
        private MifareClassic mifareClassic;
        private int block;
        private byte[] data;

        Write(MifareClassic mifareClassic, int block, byte[] data) {
            this.mifareClassic = mifareClassic;
            this.block = block;
            this.data = data;
        }

        @Override
        public void run() {
            try {
                mifareClassic.writeBlock(block, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}