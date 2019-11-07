package com.koiti.checkpoint;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import es.dmoral.toasty.Toasty;


@RequiresApi(api = Build.VERSION_CODES.O)
public class Exit extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private TextView cardreadershow;
    private Button save, exit;
    private Boolean active = false, photo = false, photoCorrect = false;
    private ImageView imageView, alertView;
    private Uri photoURIG;
    String fixed, fixedDateIn, fixedDateOut, fixedDateNow;

    ConfigStorage config = new ConfigStorage();

    Date date = new Date();
    SimpleDateFormat dateOut = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    String DateOut;


    SimpleDateFormat sdYear = new SimpleDateFormat("yyyy");
    String DateYear;

    SimpleDateFormat sdMonth = new SimpleDateFormat("MM");
    String DateMonth;

    SimpleDateFormat sdDay = new SimpleDateFormat("dd");
    String DateDay;

    SimpleDateFormat sdHour = new SimpleDateFormat("HH");
    String DateHour;

    SimpleDateFormat sdMinut = new SimpleDateFormat("mm");
    String DateMinut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out);
        setTitle("Salida");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final TextView textViewDate = findViewById(R.id.date_Id);

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DateOut = dateOut.format(new Date());
                DateYear = sdYear.format(new Date());
                DateMonth = sdMonth.format(new Date());
                DateDay = sdDay.format(new Date());
                DateHour = sdHour.format(new Date());
                DateMinut = sdMinut.format(new Date());
                textViewDate.setText(dateOut.format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);


        save = findViewById(R.id.save_Id);
        exit = findViewById(R.id.exit_Id);

        save.setOnClickListener(mListener);
        exit.setOnClickListener(mListener);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            stringBuilder = new StringBuilder();
            switch (v.getId()) {

                case R.id.save_Id:
                    save.setBackgroundColor(Color.parseColor("#02840A"));//Select Color
                    active = true;
                    break;

                case R.id.exit_Id:
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

        Toasty.Config.getInstance().setTextSize(24).apply();

        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Mifare mifare = new Mifare(nfcTag);

        int code = config.getValueInt("code", context);
        int id = config.getValueInt("id", context);

        Boolean tdgcheck = config.getValueBoolean("tdgcheck", context);
        Boolean pay = config.getValueBoolean("pago", context);
        photo = config.getValueBoolean("foto", context);
        int tdg = config.getValueInt("tdg", context);

        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    if (mifare.authentificationKey(Mifare.KOITI_KEY1, Mifare.KEY_TYPE_A, 1)) {
                        byte[] datosB0 = mifare.readMifareTagBlock(1, 0);
                        byte[] datosB1 = mifare.readMifareTagBlock(1, 1);
                        byte[] datosB2 = mifare.readMifareTagBlock(1, 2);
                        byte[] writeData = new byte[16];

                        if (datosB0 != null && datosB1 != null && datosB2 != null) {

                            if (datosB0[0] == 0 && datosB0[1] == 0) {
                                Toasty.error(getBaseContext(), "Tarjeta no posee ingreso", Toast.LENGTH_LONG).show();
                                save.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                                active = false;
                                break;
                            }

                            System.arraycopy(datosB2, 0, writeData, 0, datosB2.length);//Copia manual del arreglo datosB2 a writeData

                            writeData[10] = (byte) 2;

                            int iyear = Integer.parseInt(DateYear);
                            int imonth = Integer.parseInt(DateMonth);
                            int iday = Integer.parseInt(DateDay);
                            int ihour = Integer.parseInt(DateHour);
                            int iminut = Integer.parseInt(DateMinut);

                            LocalDateTime ldtEntrada = LocalDateTime.of(datosB2[0] + 2000, datosB2[1], datosB2[2], datosB2[3], datosB2[4]);
                            LocalDateTime ldtActual = LocalDateTime.of(iyear, imonth, iday, ihour, iminut);

                            String sRead = new String(datosB0);
                            fixed = sRead.replaceAll("[^\\x20-\\x7e]", "");

                            if (photo && !photoCorrect) {
                                SharedPreferences settings = getSharedPreferences("KEY_DATA", 0);
                                String sphoto = settings.getString(fixed, "");
                                photoURIG = Uri.parse(sphoto);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertImage();
                                    }
                                });
                            } else {
                                photoCorrect = true;
                            }

                            if (photoCorrect) {
                                if (datosB1[0] == 1 && datosB1[1] == code && datosB1[3] == id) {
                                    if (datosB2[10] == 0 || datosB2[10] == 1) {
                                        if (!pay) {
                                            if (!tdgcheck) {

                                                if (datosB2[11] != 0 && datosB2[12] != 0 && datosB2[13] != 0) {
                                                    LocalDateTime ldtMaxSalida = LocalDateTime.of(datosB2[11] + 2000, datosB2[12], datosB2[13], datosB2[14], datosB2[15]);

                                                    if (ldtActual.isBefore(ldtMaxSalida)) {
                                                        boolean row2 = mifare.writeMifareTag(1, 2, writeData);
                                                        if (row2) {
                                                            Toasty.success(Exit.this, "Escritura Exitosa", Toast.LENGTH_SHORT).show();
                                                            addData();
                                                        } else {
                                                            Toasty.error(Exit.this, "Grabación Incorrecta", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toasty.error(getBaseContext(), "La fecha de salida es menor que la fecha" +
                                                                " actual \n" + fixedDateOut, Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    Toasty.error(getBaseContext(), "No registra pago", Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                LocalDateTime tdgEntrada = ldtEntrada.plusMinutes(tdg);

                                                if (ldtActual.isBefore(tdgEntrada)) {
                                                    boolean row2 = mifare.writeMifareTag(1, 2, writeData);
                                                    if (row2) {
                                                        Toasty.success(Exit.this, "Escritura Exitosa", Toast.LENGTH_SHORT).show();
                                                        photoCorrect = false;
                                                        addData();
                                                    } else {
                                                        Toasty.error(Exit.this, "Grabación Incorrecta", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toasty.error(getBaseContext(), "Excede tiempo de gracia", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        } else {
                                            boolean row2 = mifare.writeMifareTag(1, 2, writeData);
                                            if (row2) {
                                                Toasty.success(Exit.this, "Escritura Exitosa", Toast.LENGTH_SHORT).show();
                                                photoCorrect = false;
                                                addData();
                                            } else {
                                                Toasty.error(Exit.this, "Grabación Incorrecta", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Toasty.error(getBaseContext(), "Tarjeta no posee ingreso", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                            " parqueadero.", Toast.LENGTH_LONG).show();
                                }
                                save.setBackgroundColor(Color.parseColor("#296DBA"));//Default Color
                                active = false;
                            }
                        } else {
                            Toasty.error(getBaseContext(), "" + "La lectura ha fallado" +
                                    " por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();
                        }
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
        Intent intent = new Intent(this, Exit.class).
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

    public void alertImage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Exit.this);
        LayoutInflater image = LayoutInflater.from(Exit.this);
        final View view = image.inflate(R.layout.alert_image, null);
        // Setting Alert Dialog Title
        alertDialogBuilder.setTitle("Alerta de confirmación");
        // Icon Of Alert Dialog
        // Setting Alert Dialog Message
        alertDialogBuilder.setMessage("¿Las imágenes coinciden?");
//        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Sí           ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                photoCorrect = true;
            }
        });

        alertDialogBuilder.setNeutralButton("         No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Se canceló la operación", Toast.LENGTH_SHORT).show();
            }
        });

        alertView = view.findViewById(R.id.alertImage);
        alertView.setImageURI(photoURIG);
//        alertView.setRotation(alertView.getRotation() + 90);

        alertDialogBuilder.setView(view);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void addData() {
        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProvider.DATABASE_NAME).getWritableDatabase();

        ContentValues register = new ContentValues();

        register.put("veh_fh_salida", DateOut);
        register.put("veh_fe_salida", DateOut);
        register.put("veh_dir_salida", "1");

        String updateSentence = "veh_id = " + fixed;

        db.update("tb_vehiculos", register, updateSentence, null);
    }

}