package com.koiti.checkpoint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;
import org.jumpmind.symmetric.android.SymmetricService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Configuration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private Button out, delete, sync;
    private CheckBox checkBoxEntrada, checkBoxSalida, checkBoxDescuento, checkBoxCarro, checkBoxMoto, checkBoxBicicleta,
            checkBoxFoto, checkBoxTdg, checkBoxPay, checkBoxFormat;
    EditText codeInput, idInput, consecutiveInput, tdgInput, descInput, discountValueInput, ipInput, nodeInput, nodegroupInput, publicadorInput;

    private String ip = "";
    private String node = "";
    private String publicador = "";
    private String nodeGroup = "";
    private DbProvider.DatabaseHelper mOpenHelper;


    ConfigStorage config = new ConfigStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        context = this;
        setTitle("Configuración");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        checkBoxEntrada = findViewById(R.id.entradaId);
        checkBoxCarro = findViewById(R.id.carroId);
        checkBoxMoto = findViewById(R.id.motoId);
        checkBoxBicicleta = findViewById(R.id.bicicletaId);
        checkBoxFoto = findViewById(R.id.fotoId);

        checkBoxSalida = findViewById(R.id.salidaId);
        checkBoxTdg = findViewById(R.id.tdgId);
        checkBoxPay = findViewById(R.id.payId);

        checkBoxDescuento = findViewById(R.id.DescId);

        codeInput = findViewById(R.id.codeInput);
        idInput = findViewById(R.id.idInput);
        consecutiveInput = findViewById(R.id.consecutiveInput);
        tdgInput = findViewById(R.id.tdgTextId);
        descInput = findViewById(R.id.descInput);
        discountValueInput = findViewById(R.id.discountValueInput);
        ipInput = findViewById(R.id.ipInput);
        nodeInput = findViewById(R.id.nodeIdInput);
        nodegroupInput = findViewById(R.id.nodeGroupInput);
        publicadorInput = findViewById(R.id.publicadorInput);

        checkBoxFormat = findViewById(R.id.FormatId);

        delete = findViewById(R.id.delete_Id);
        sync = findViewById(R.id.sync_Id);
        out = findViewById(R.id.exit_Id);


        Boolean entrada = config.getValueBoolean("entrada", context);
        Boolean carro = config.getValueBoolean("carro", context);
        Boolean moto = config.getValueBoolean("moto", context);
        Boolean bicicleta = config.getValueBoolean("bicicleta", context);
        Boolean foto = config.getValueBoolean("foto", context);

        Boolean salida = config.getValueBoolean("salida", context);
        Boolean tdgcheck = config.getValueBoolean("tdgcheck", context);
        Boolean pago = config.getValueBoolean("pago", context);

        Boolean descuento = config.getValueBoolean("descuento", context);

        Boolean formatear = config.getValueBoolean("formatear", context);

        int code = config.getValueInt("code", context);
        int id = config.getValueInt("id", context);
        int consecutive = config.getValueInt("consecutive", context);
        int tdg = config.getValueInt("tdg", context);
        int discount = config.getValueInt("discount", context);
        int discountValue = config.getValueInt("discountValue", context);

        ip = config.getValueString("ip", context);
        node = config.getValueString("node", context);
        publicador = config.getValueString("publicador", context);
        nodeGroup = config.getValueString("group", context);

        codeInput.setText(Integer.toString(code));
        idInput.setText(Integer.toString(id));
        consecutiveInput.setText(Integer.toString(consecutive));
        tdgInput.setText(Integer.toString(tdg));
        descInput.setText(Integer.toString(discount));
        discountValueInput.setText(Integer.toString(discountValue));

        ipInput.setText(ip);
        nodeInput.setText(node);
        publicadorInput.setText(publicador);
        nodegroupInput.setText(nodeGroup);

        checkBoxEntrada.setChecked(entrada);
        checkBoxCarro.setChecked(carro);
        checkBoxMoto.setChecked(moto);
        checkBoxBicicleta.setChecked(bicicleta);
        checkBoxFoto.setChecked(foto);

        checkBoxSalida.setChecked(salida);
        checkBoxTdg.setChecked(tdgcheck);
        checkBoxPay.setChecked(pago);

        checkBoxDescuento.setChecked(descuento);

        checkBoxFormat.setChecked(formatear);

        codeInput.addTextChangedListener(listenerCode);
        idInput.addTextChangedListener(listenerId);
        consecutiveInput.addTextChangedListener(listenerConsecutive);
        tdgInput.addTextChangedListener(listenerTDG);
        descInput.addTextChangedListener(listenerDiscount);
        discountValueInput.addTextChangedListener(listenerDiscountValue);
        ipInput.addTextChangedListener(listenerIp);
        nodeInput.addTextChangedListener(listenerNode);
        nodegroupInput.addTextChangedListener(listenerGroup);
        publicadorInput.addTextChangedListener(listenerPublicador);

        delete.setOnClickListener(mListener);
        sync.setOnClickListener(mListener);
        out.setOnClickListener(mListener);

        Spinner spinner = findViewById(R.id.tipo_spinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setSelection(config.getValueInt("tipo", context));

        addListenerOnIn();
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked

            switch (v.getId()) {
                case R.id.exit_Id:
                    finish();
                    break;
                case R.id.sync_Id:
                    sincronizar();
                    break;
                case R.id.delete_Id:
                    alert().show();
                    break;
            }
        }
    };

    private final TextWatcher listenerCode = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }
            config.save(value, "code", context);
        }
    };

    private final TextWatcher listenerId = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }
            config.save(value, "id", context);
        }
    };

    private final TextWatcher listenerConsecutive = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }
            config.save(value, "consecutive", context);
        }
    };

    private final TextWatcher listenerTDG = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }
            config.save(value, "tdg", context);
        }
    };

    private final TextWatcher listenerDiscount = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }
            if (descInput.getText().toString().trim().equalsIgnoreCase("")) {
                descInput.setError("El valor debe estar entre 0 y 255");
            } else if (Integer.parseInt(descInput.getText().toString().trim()) > 255) {
                descInput.setError("El valor debe estar entre 0 y 255");
            } else {
                config.save(value, "discount", context);
            }
        }
    };

    private final TextWatcher listenerDiscountValue = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int type = config.getValueInt("tipo", context);
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }

            if (type == 1) {
                if (discountValueInput.getText().toString().trim().equalsIgnoreCase("")) {
                    discountValueInput.setError("El valor debe estar en el rango de 0-100%");
                } else if (Integer.parseInt(discountValueInput.getText().toString().trim()) > 100) {
                    discountValueInput.setError("No puede exceder el 100%");
                } else {
                    config.save(value, "discountValue", context);
                }
            } else {
                if (discountValueInput.getText().toString().trim().equalsIgnoreCase("")) {
                    discountValueInput.setError("El valor debe estar en el rango de 0-2047 minutos");
                } else if (Integer.parseInt(discountValueInput.getText().toString().trim()) > 2047) {
                    discountValueInput.setError("No puede exceder los 2047 minutos");
                } else {
                    config.save(value, "discountValue", context);
                }
            }
        }
    };

    private final TextWatcher listenerIp = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            config.save(text, "ip", context);
        }
    };

    private final TextWatcher listenerNode = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            config.save(text, "node", context);
        }
    };

    private final TextWatcher listenerGroup = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            config.save(text, "group", context);
        }
    };

    private final TextWatcher listenerPublicador = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            config.save(text, "publicador", context);
        }
    };

//    @Override
//    public void finish() {

//        ContentValues registerServer = new ContentValues();
//        ContentValues registerAndroid = new ContentValues();

//        registerServer.put("sync_url", url);
//
//        registerAndroid.put("node_id", node);
//        registerAndroid.put("external_id", node);
//        registerAndroid.put("node_group_id", nodeGroup);
//
//
//        String updateSentenceServer = "database_type = " + "'MySQL'";
//        String updateSentenceAndroid = "database_type = " + "'sqlite'";
//
//        db.update("sym_node", registerServer, updateSentenceServer, null);
//        db.update("sym_node", registerAndroid, updateSentenceAndroid, null);

//        super.finish();
//    }

    public void sincronizar(){
        ip = config.getValueString("ip", context);
        node = config.getValueString("node", context);
        publicador = config.getValueString("publicador", context);
        nodeGroup = config.getValueString("group", context);


        String url = "http://" + ip + ":31415/sync/" + publicador;

        config.save(url, "url", context);

        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProvider.DATABASE_NAME).getWritableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tablas = new ArrayList<>();

        while (c.moveToNext()) {
            tablas.add(c.getString(0));
        }

        tablas.remove("sqlite_sequence");
        tablas.remove("android_metadata");

        c.close();

        for (String tabla : tablas) {
            String dropQuery = "DROP TABLE IF EXISTS " + tabla;
            db.execSQL(dropQuery);
            Log.d("ipSync", tabla);
        }

        Toast.makeText(context, "Sincronizando...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Configuration.this, SymmetricService.class);
        stopService(intent);

        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        finish();
        System.exit(0);
    }

    public void addListenerOnIn() {

        checkBoxEntrada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean in = checkBoxEntrada.isChecked();
                config.save(in, "entrada", context);
            }
        });

        checkBoxCarro.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean carro = checkBoxCarro.isChecked();
                config.save(carro, "carro", context);
            }
        });

        checkBoxMoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean moto = checkBoxMoto.isChecked();
                config.save(moto, "moto", context);
            }
        });

        checkBoxBicicleta.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean bicileta = checkBoxBicicleta.isChecked();
                config.save(bicileta, "bicicleta", context);
            }
        });

        checkBoxFoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean foto = checkBoxFoto.isChecked();
                config.save(foto, "foto", context);
            }
        });

        checkBoxSalida.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean out = checkBoxSalida.isChecked();
                config.save(out, "salida", context);
            }
        });

        checkBoxTdg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean tdg = checkBoxTdg.isChecked();
                config.save(tdg, "tdgcheck", context);
            }
        });

        checkBoxPay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean pago = checkBoxPay.isChecked();
                config.save(pago, "pago", context);
            }
        });

        checkBoxDescuento.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean desc = checkBoxDescuento.isChecked();
                config.save(desc, "descuento", context);
            }
        });

        checkBoxFormat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean desc = checkBoxFormat.isChecked();
                config.save(desc, "formatear", context);
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        config.save(pos, "tipo", context);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public AlertDialog alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Configuration.this);

        final SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProvider.DATABASE_NAME).getWritableDatabase();


        builder.setTitle("Advertencia")
                .setMessage("¿En verdad desea borrar los datos de la tabla vehiculos?")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.delete("tb_vehiculos", null, null);
                            }
                        }).setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }
}
