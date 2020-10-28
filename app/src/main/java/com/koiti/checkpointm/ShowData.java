package com.koiti.checkpointm;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.koiti.checkpointm.MifareThreads.AuthenticationMifare;
import com.koiti.checkpointm.MifareThreads.Disconnect;
import com.koiti.checkpointm.MifareThreads.ReadMifare;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.dmoral.toasty.Toasty;

public class ShowData extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button read, exit;
    private Boolean active = false;
    private Toast toasty;
    private TextView idTV, trade, codParq, idParq, dateInput, veh, apb, dateOutput, classCard, inPay, dateLiquidation, dateRegMonthly, dateExpMonthly;

    ConfigStorage config = new ConfigStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showdata);
        setTitle("Lectura Tarjeta");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        read = findViewById(R.id.read_Id);
        exit = findViewById(R.id.exitdisc_Id);

        idTV = findViewById(R.id.vehid_Id);
        trade = findViewById(R.id.trade_Id);
        codParq = findViewById(R.id.codeParq_Id);
        idParq = findViewById(R.id.idParq_Id);
        dateInput = findViewById(R.id.dateInput_Id);
        veh = findViewById(R.id.veh_Id);
        apb = findViewById(R.id.apb_Id);
        dateOutput = findViewById(R.id.dateOutput_Id);
        classCard = findViewById(R.id.classCard_Id);
        inPay = findViewById(R.id.inpay_Id);
        dateLiquidation = findViewById(R.id.dateLiquidation_Id);
        dateRegMonthly = findViewById(R.id.dateRegMonthly_Id);
        dateExpMonthly = findViewById(R.id.dateExpMonthly_Id);

        read.setOnClickListener(mListener);
        exit.setOnClickListener(mListener);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.read_Id:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        read.setBackground(getDrawable(R.drawable.btn_round_green));//Select Color
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


    @SuppressLint("SetTextI18n")
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
                    ExecutorService service = Executors.newFixedThreadPool(4);
                    try {
                        String sUid = "";
                        StringBuilder sbUid = new StringBuilder();
                        byte[] uid = mifare.getUid();
                        for (int i = uid.length - 1; i >= 0; i--) {
                            if (i < 2) {
                                sUid = String.format("%02X", uid[i] & 0xFF);
                                sbUid.append(sUid);
                            }
                        }

                        if (service.submit(new AuthenticationMifare(mifare, context)).get()) {

                            byte[] datosB0 = service.submit(new ReadMifare(mifare, 0)).get();
                            byte[] datosB1 = service.submit(new ReadMifare(mifare, 1)).get();
                            byte[] datosB2 = service.submit(new ReadMifare(mifare, 2)).get();

                            int code = config.getValueInt("code", context);
                            int id = config.getValueInt("id", context);

                            if (datosB0 != null && datosB1 != null && datosB2 != null) {
                                // evaluate if the card belongs to the parking
//                                if (datosB1[1] == code && datosB1[3] == id) {

                                    String sRead = new String(datosB0);
                                    String fixed = sRead.replaceAll("[^\\x20-\\x7e]", "");

                                    idTV.setText(fixed);

                                    int uidDecimal = Integer.parseInt(sbUid.toString(), 16);

                                    trade.setText((uid[2] & 0xFF) + "-" + uidDecimal);
                                    codParq.setText(datosB1[1] + "");
                                    idParq.setText(datosB1[3] + "");
                                    dateInput.setText(2000 + datosB2[0] + "-" + datosB2[1] + "-" + datosB2[2] + " " + datosB2[3] + ":" + datosB2[4]);

                                    String sveh;
                                    if (datosB2[8] == 0)
                                        sveh = "Carro";
                                    else if (datosB2[8] == 1)
                                        sveh = "Moto";
                                    else
                                        sveh = "Bicicleta";
                                    veh.setText(sveh);

                                    String antipassback;
                                    if (datosB2[10] == 0)
                                        antipassback = "Inicializado";
                                    else if (datosB2[10] == 1)
                                        antipassback = "IN";
                                    else
                                        antipassback = "OUT";
                                    apb.setText(antipassback);

                                    dateOutput.setText(2000 + datosB2[11] + "-" + datosB2[12] + "-" + datosB2[13] + " " + datosB2[14] + ":" + datosB2[15]);
                                    classCard.setText("De rotación normal");
                                    inPay.setText(datosB2[9] + "");
                                    dateLiquidation.setText(2000 + datosB1[11] + "-" + datosB1[12] + "-" + datosB1[13] + " " + datosB1[14] + ":" + datosB1[15]);

                                    String sRegMonthly = datosB1[5] != 0 ? 2000 + datosB1[5] + "-" + datosB1[6] + "-" + datosB1[7] : "Sin fecha";
                                    dateRegMonthly.setText(sRegMonthly);

                                    String sExpMonthly = datosB1[8] != 0 ? 2000 + datosB1[8] + "-" + datosB1[9] + "-" + datosB1[10] : "Sin fecha";
                                    dateExpMonthly.setText(sExpMonthly);

                                    final Toast toasty = Toasty.success(ShowData.this, "" + "Lectura Exitosa", Toast.LENGTH_LONG);
                                    toasty.show();

                                    sbUid.delete(0, sbUid.length());

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            toasty.cancel();
                                        }
                                    }, 700);
//                                } else
//                                    Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
//                                            " parqueadero.", Toast.LENGTH_LONG).show();
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

                    new Thread(new Disconnect(mifare)).start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        read.setBackground(getDrawable(R.drawable.btn_round));//Default Color
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
        Intent intent = new Intent(this, ShowData.class).
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
