package com.koiti.checkpoint;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

import android.database.sqlite.SQLiteDatabase;

import com.koiti.checkpoint.MifareThreads.AuthenticationMifare;
import com.koiti.checkpoint.MifareThreads.Disconnect;
import com.koiti.checkpoint.MifareThreads.ReadMifare;
import com.koiti.checkpoint.MifareThreads.WriteMifare;

import es.dmoral.toasty.Toasty;

public class Entrance extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button car;
    private Button motorbike;
    private Button bike;
    private Boolean active = false, foto = false, placa = false;
    private int parameter;
    private Toast toasty;
    String fixed, fixedDateIn, currentPhotoPath;

    ConfigStorage veh = new ConfigStorage();

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateIn = new SimpleDateFormat("yyyy-MM-dd  HH:mm");

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdYear = new SimpleDateFormat("yy");
    String DateYear;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdMonth = new SimpleDateFormat("MM");
    String DateMonth;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdDay = new SimpleDateFormat("dd");
    String DateDay;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdHour = new SimpleDateFormat("HH");
    String DateHour;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdMinut = new SimpleDateFormat("mm");
    String DateMinut;

    ConfigStorage config = new ConfigStorage();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in);
        setTitle("Entrada");
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        context = this;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final TextView textViewDate = findViewById(R.id.date_Id);

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DateYear = sdYear.format(new Date());
                DateMonth = sdMonth.format(new Date());
                DateDay = sdDay.format(new Date());
                DateHour = sdHour.format(new Date());
                DateMinut = sdMinut.format(new Date());
                textViewDate.setText(dateIn.format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        car = findViewById(R.id.car_Id);
        motorbike = findViewById(R.id.motorbike_Id);
        bike = findViewById(R.id.bike_Id);
        Button exit = findViewById(R.id.exit_Id);

        car.setOnClickListener(mListener);
        motorbike.setOnClickListener(mListener);
        bike.setOnClickListener(mListener);
        exit.setOnClickListener(mListener);

        Boolean carro = veh.getValueBoolean("carro", context);
        Boolean moto = veh.getValueBoolean("moto", context);
        Boolean bicicleta = veh.getValueBoolean("bicicleta", context);
        foto = veh.getValueBoolean("foto", context);
        placa = veh.getValueBoolean("placa", context);

        car.setVisibility(carro ? View.VISIBLE : View.GONE);
        motorbike.setVisibility(moto ? View.VISIBLE : View.GONE);
        bike.setVisibility(bicicleta ? View.VISIBLE : View.GONE);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.car_Id:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        car.setBackground(getDrawable(R.drawable.btn_round_green));//Select Color
                        motorbike.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                        bike.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                    }
                    active = true;
                    parameter = 0;
                    break;

                case R.id.motorbike_Id:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        car.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                        motorbike.setBackground(getDrawable(R.drawable.btn_round_green));//Select Color
                        bike.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                    }
                    active = true;
                    parameter = 1;
                    break;

                case R.id.bike_Id:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        car.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                        motorbike.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                        bike.setBackground(getDrawable(R.drawable.btn_round_green));//Select Color
                    }
                    active = true;
                    parameter = 2;
                    break;

                case R.id.exit_Id:
                    finish();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            return;
        }
        int code = config.getValueInt("code", context);
        int id = config.getValueInt("id", context);
        int consecutive = config.getValueInt("consecutive", context);

        Toasty.Config.getInstance().setTextSize(24).apply();

        Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Mifare mifare = new Mifare(nfcTag);

        byte[] writeDataB0 = new byte[16];

        fixed = Integer.toString(consecutive);
        ArrayList<Integer> dataPark = new ArrayList<>();

        while (consecutive > 0) {
            dataPark.add(consecutive % 10);
            consecutive = consecutive / 10;
        }

        Collections.reverse(dataPark);

        for (int i = 0; i < dataPark.size(); i++) {
            String sDataPark = Integer.toHexString(dataPark.get(i) + 48);
            byte BDataPark = Byte.parseByte(sDataPark, 16);
            writeDataB0[i] = BDataPark;
        }

        for (int j = dataPark.size(); j < 16; j++)
            writeDataB0[j] = (byte) 0;


        if (active) {
            switch (mifare.connectTag()) {
                case Mifare.MIFARE_CONNECTION_SUCCESS:
                    ExecutorService service = Executors.newFixedThreadPool(4);
                    try {
                        if (service.submit(new AuthenticationMifare(mifare, context)).get()) {
                            byte[] datosB1 = service.submit(new ReadMifare(mifare, 1)).get();
                            byte[] datosB2 = service.submit(new ReadMifare(mifare, 2)).get();

                            if (datosB1 != null && datosB2 != null) {
                                if (datosB1[0] == 1 && datosB1[1] == code && datosB1[3] == id) {
                                    if (datosB2[10] == 0 || datosB2[10] == 2) {
                                        byte[] writeData = new byte[16];

                                        System.arraycopy(datosB2, 0, writeData, 0, datosB2.length);//Copia manual del arreglo datosB2 a writeData

                                        int iyear = Integer.parseInt(DateYear);
                                        int imonth = Integer.parseInt(DateMonth);
                                        int iday = Integer.parseInt(DateDay);
                                        int ihour = Integer.parseInt(DateHour);
                                        int iminut = Integer.parseInt(DateMinut);

                                        String sparameter = Integer.toHexString(parameter);
                                        byte Bparameter = Byte.parseByte(sparameter);

                                        writeData[0] = (byte) iyear;
                                        writeData[1] = (byte) imonth;
                                        writeData[2] = (byte) iday;
                                        writeData[3] = (byte) ihour;
                                        writeData[4] = (byte) iminut;
                                        writeData[8] = Bparameter;
                                        writeData[10] = (byte) 1;

                                        DecimalFormat formatter = new DecimalFormat("00");
                                        String read1 = formatter.format(imonth);
                                        String read2 = formatter.format(iday);
                                        String read3 = formatter.format(ihour);
                                        String read4 = formatter.format(iminut);

                                        fixedDateIn = "20" + iyear + "-" + read1 + "-" + read2 + " " + read3 + ":" + read4;

                                        if (service.submit(new WriteMifare(mifare, writeDataB0, 0)).get()) {
                                            if (service.submit(new WriteMifare(mifare, writeData, 2)).get()) {
                                                Toasty.success(Entrance.this, "Escritura Exitosa", Toast.LENGTH_SHORT).show();

                                                int increment = config.getValueInt("consecutive", context);
                                                int consecutivePicture = config.getValueInt("consecutive", context);
                                                config.save(++increment, "consecutive", context);
                                                if (foto && parameter == 2)
                                                    dispatchTakePictureIntent(consecutivePicture);

                                                if (placa && (parameter == 0 || parameter == 1))
                                                    vehiclePlate(consecutivePicture);

                                                addData();
                                            } else {
                                                byte[] writeDataB2 = new byte[16];

                                                for (int i = 0; i < 16; i++) {
                                                    writeDataB2[i] = (byte) 0;
                                                }

                                                mifare.writeMifareTag(1, 0, writeDataB2);
                                                Toasty.error(Entrance.this, "Grabación Incorrecta", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toasty.error(Entrance.this, "Grabación Incorrecta", Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } else {
                                        Toasty.error(Entrance.this, "La tarjeta no posee salida", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toasty.error(getBaseContext(), "" + "Tarjeta no pertenece al" +
                                            " parqueadero.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toasty.error(getBaseContext(), "" + "La lectura ha fallado" +
                                        " por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();
                            }

                        } else
                            Toasty.error(getBaseContext(), "Fallo de autentificación", Toast.LENGTH_LONG).show();

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    new Thread(new Disconnect(mifare)).start();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        car.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                        motorbike.setBackground(getDrawable(R.drawable.btn_round));//Default Color
                        bike.setBackground(getDrawable(R.drawable.btn_round));//Default Color
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
        Intent intent = new Intent(this, Entrance.class).
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

    private void dispatchTakePictureIntent(int consecutive) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.koiti.checkpoint.fileprovider",
                        photoFile);

                veh.save(photoURI.toString(), Integer.toString(consecutive), context);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir("Parqueadero");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void vehiclePlate(final int consecutive) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setTitle("Digite la placa del vehiculo");

        alert.setView(edittext);

        alert.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String editTextValue = edittext.getText().toString();
                config.save(editTextValue, Integer.toString(consecutive), context);
            }
        });

        alert.setCancelable(false);
        alert.show();
    }

    public void addData() {
        String tipo;
        if (parameter == 0) {
            tipo = "Carro";
        } else if (parameter == 1) {
            tipo = "Moto";
        } else {
            tipo = "Bicicleta";
        }

        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProvider.DATABASE_NAME).getWritableDatabase();

        ContentValues register = new ContentValues();
        register.put("veh_id", fixed);
        register.put("veh_fh_entrada", fixedDateIn);
        register.put("veh_estacion", "MOVIL");
        register.put("veh_usuario", "SISTEMA");
        register.put("veh_tipo", "Normal");
        register.put("veh_dir_entrada", "1");
        register.put("veh_fe_entrada", fixedDateIn);
        register.put("veh_clase", tipo);

        db.insert("tb_vehiculos", null, register);
    }

}
