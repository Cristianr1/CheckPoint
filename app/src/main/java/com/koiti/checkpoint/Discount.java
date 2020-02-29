package com.koiti.checkpoint;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class Discount extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button discount1, discount2, discount3, exit;
    private Boolean active = false, message;
    private int discount, type, discountValue, inpago;

    ConfigStorage config = new ConfigStorage();

    Date date = new Date();
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
    String DateIn = dateIn.format(date);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount);
        setTitle("Descuento");

        Toasty.Config.getInstance().setTextSize(24).apply();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final TextView textViewDate = findViewById(R.id.datediscount_Id);

        //This thread updates the date every minute
        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textViewDate.setText(dateIn.format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;
        inpago = 0;

        message = config.getValueBoolean("mensaje", context);

        Boolean descuento1 = config.getValueBoolean("discountActive1", context);
        Boolean descuento2 = config.getValueBoolean("discountActive2", context);
        Boolean descuento3 = config.getValueBoolean("discountActive3", context);

        String discount1Name = config.getValueString("discount1name", context);
        String discount2Name = config.getValueString("discount2name", context);
        String discount3Name = config.getValueString("discount3name", context);

        discount1 = findViewById(R.id.discount1_Id);
        discount2 = findViewById(R.id.discount2_Id);
        discount3 = findViewById(R.id.discount3_Id);
        exit = findViewById(R.id.exitdisc_Id);

        discount1.setOnClickListener(mListener);
        discount2.setOnClickListener(mListener);
        discount3.setOnClickListener(mListener);
        exit.setOnClickListener(mListener);

        if (!descuento1) {
            discount1.setVisibility(View.GONE);
        } else {
            discount1.setVisibility(View.VISIBLE);
            discount1.setText(discount1Name);
        }

        if (!descuento2) {
            discount2.setVisibility(View.GONE);
        } else {
            discount2.setVisibility(View.VISIBLE);
            discount2.setText(discount2Name);
        }

        if (!descuento3) {
            discount3.setVisibility(View.GONE);
        } else {
            discount3.setVisibility(View.VISIBLE);
            discount3.setText(discount3Name);
        }

    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.discount1_Id:
                    discountData1();
                    discount1.setBackgroundColor(Color.parseColor("#02840A"));//Select Color
                    active = true;
                    break;
                case R.id.discount2_Id:
                    discountData2();
                    discount2.setBackgroundColor(Color.parseColor("#02840A"));//Select Color
                    active = true;
                    break;
                case R.id.discount3_Id:
                    discountData3();
                    discount3.setBackgroundColor(Color.parseColor("#02840A"));//Select Color
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
            for (String s : arrayBinary) {
                sbuilderDiscount.append(s);
            }
            sbuilderInpago.append("000");
        }
        sbuilderInpago.insert(0, type);

        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    if (mifare.authentificationKey(Mifare.ACCESS_KEY1, Mifare.KEY_TYPE_A, 1)) {
                        byte[] datosB1 = mifare.readMifareTagBlock(1, 1);//read data that that belongs to block 1
                        byte[] datosB2 = mifare.readMifareTagBlock(1, 2);//read data that that belongs to block 2
                        byte[] writeData = new byte[16];

                        if (datosB1 != null && datosB2 != null) {

                            if (datosB2[0] == 0 && datosB2[1] == 0) {
                                Toasty.error(getBaseContext(), "Tarjeta no posee ingreso", Toast.LENGTH_LONG).show();
                                discount1.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                                discount2.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                                discount3.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                                active = false;
                                break;
                            }

                            int code = config.getValueInt("code", context);
                            int id = config.getValueInt("id", context);

                            System.arraycopy(datosB2, 0, writeData, 0, 16);

                            writeData[5] = (byte) Integer.parseInt(String.valueOf(sbuilderDiscount), 2);
                            writeData[6] = (byte) discount;
                            writeData[9] = (byte) inpago;

                            if (datosB1[0] == 1 && datosB1[1] == code && datosB1[3] == id) {
                                boolean row2 = mifare.writeMifareTag(1, 2, writeData);
                                if (row2) {
                                    Toasty.success(Discount.this, "Escritura Exitosa", Toast.LENGTH_SHORT).show();

                                    if (type == 0 && message) {
//                                    String part1 = Integer.toBinaryString((datosB2[9]&0xFF));
//                                    String part2 = Integer.toBinaryString((datosB2[5]&0xFF));
                                        String part1 = Integer.toBinaryString(inpago);

                                        String sMinutosRestantes = part1.substring(1, 4);
                                        StringBuilder sbuilderMR = new StringBuilder(sMinutosRestantes); //Binario
                                        sbuilderMR.append(sbuilderDiscount);

                                        DateTime ldtEntrada = new DateTime(datosB2[0] + 2000, datosB2[1], datosB2[2], datosB2[3], datosB2[4]);
                                        DateTime ldtActual = DateTime.now();
                                        DateTime dtDiscount = ldtEntrada.plusMinutes(Integer.parseInt(String.valueOf(sbuilderMR), 2));

                                        int minutosEntradaActual = Minutes.minutesBetween(ldtEntrada, ldtActual).getMinutes();
                                        int minutosEntradaDescuento = Minutes.minutesBetween(ldtEntrada, dtDiscount).getMinutes();
                                        final int minutosRestantes = minutosEntradaDescuento - minutosEntradaActual;

                                        String success = "Quedan " + minutosRestantes + " minutos para salir del parqueadero";
                                        String timeOut = "Descuento aplicado, por favor acerquese a caja";

                                        final String finalMessage;

                                        if (minutosRestantes > 0)
                                            finalMessage = success;
                                        else
                                            finalMessage = timeOut;

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                alertaTiempoSalida(minutosRestantes).show();

                                                AlertDialog builder = new AlertDialog.Builder(context).setTitle("Tiempo para salir")
                                                        .setMessage(finalMessage)
                                                        .setPositiveButton("OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                    }
                                                                }).show();

                                                TextView textView = builder.findViewById(android.R.id.message);
                                                if (textView != null) textView.setTextSize(25);

                                            }
                                        });
                                    }
                                } else
                                    Toasty.error(Discount.this, "Grabación Incorrecta", Toast.LENGTH_SHORT).show();
                            } else
                                Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                        " parqueadero.", Toast.LENGTH_LONG).show();


                            discount1.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                            discount2.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                            discount3.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                            active = false;
                        } else
                            Toasty.error(getBaseContext(), "" + "La lectura ha fallado" +
                                    " por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();
                    } else
                        Toasty.error(getBaseContext(), "Fallo de autentificación", Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toasty.warning(getBaseContext(), "Recuerde presionar un boton para poder hacer operaciones " +
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

    public void discountData1() {
        type = config.getValueInt("typeDiscount1", context);
        discountValue = config.getValueInt("discountValue1", context);
        discount = config.getValueInt("discount1", context);
        dataSetting();
    }

    public void discountData2() {
        type = config.getValueInt("typeDiscount2", context);
        discountValue = config.getValueInt("discountValue2", context);
        discount = config.getValueInt("discount2", context);
        dataSetting();
    }

    public void discountData3() {
        type = config.getValueInt("typeDiscount3", context);
        discountValue = config.getValueInt("discountValue3", context);
        discount = config.getValueInt("discount3", context);
        dataSetting();
    }

    public void dataSetting() {
        if (type == 0) {
            if (discountValue > 2047) {
                discountValue = 0;
            } else if (discountValue > 255 && discountValue < 2047) {
                if (discountValue < 512) inpago = 19; //10011
                if (discountValue > 767 && discountValue < 1024) inpago = 23;//10011
                if (discountValue >= 1024) inpago = 25;//11001
            } else {
                inpago = 17; //10001
            }
        } else {
            inpago = 129;
            if (discountValue > 100) {
                discountValue = 0;
            }
        }
    }
}