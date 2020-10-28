package com.koiti.checkpointm;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import java.util.List;


public class Configuration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private CheckBox checkBoxEntrada, checkBoxSalida, checkBoxDescuento1, checkBoxDescuento2, checkBoxDescuento3, checkBoxCarro, checkBoxMoto, checkBoxBicicleta,
            checkBoxFoto, checkBoxTdg, checkBoxPay, checkBoxFormat;
    EditText codeInput, idInput, consecutiveInput, tdgInput, ipInput, nodeInput, nodegroupInput, publicadorInput;
    EditText desc1Input, discountValue1Input, desc2Input, discountValue2Input, desc3Input, discountValue3Input;
    EditText discount1Name, discount2Name, discount3Name;
    Spinner spinnerDiscount1, spinnerDiscount2, spinnerDiscount3;

    private String ip = "";
    private String node = "";
    private String publicador = "";
    private String nodeGroup = "";

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

        checkBoxDescuento1 = findViewById(R.id.Desc1Id);
        checkBoxDescuento2 = findViewById(R.id.Desc2Id);
        checkBoxDescuento3 = findViewById(R.id.Desc3Id);

        codeInput = findViewById(R.id.codeInput);
        idInput = findViewById(R.id.idInput);
        consecutiveInput = findViewById(R.id.consecutiveInput);
        tdgInput = findViewById(R.id.tdgTextId);
        discount1Name = findViewById(R.id.discount1_name_Id);
        discount2Name = findViewById(R.id.discount2_name_Id);
        discount3Name = findViewById(R.id.discount3_name_Id);
        desc1Input = findViewById(R.id.desc1Input);
        desc2Input = findViewById(R.id.desc2Input);
        desc3Input = findViewById(R.id.desc3Input);
        discountValue1Input = findViewById(R.id.discountValue1Input);
        discountValue2Input = findViewById(R.id.discountValue2Input);
        discountValue3Input = findViewById(R.id.discountValue3Input);
        ipInput = findViewById(R.id.ipInput);
        nodeInput = findViewById(R.id.nodeIdInput);
        nodegroupInput = findViewById(R.id.nodeGroupInput);
        publicadorInput = findViewById(R.id.publicadorInput);

        checkBoxFormat = findViewById(R.id.FormatId);

        Button delete = findViewById(R.id.delete_Id);
        Button sync = findViewById(R.id.sync_Id);
        Button out = findViewById(R.id.exit_Id);

        new Thread(new ThreadCod()).start();
        new Thread(new ThreadIn()).start();
        new Thread(new ThreadOut()).start();
        new Thread(new ThreadDisc()).start();
        new Thread(new ThreadSync()).start();

        codeInput.addTextChangedListener(numberTextWatcher);
        idInput.addTextChangedListener(numberTextWatcher);
        consecutiveInput.addTextChangedListener(numberTextWatcher);
        tdgInput.addTextChangedListener(numberTextWatcher);
        desc1Input.addTextChangedListener(numberTextWatcher);
        desc2Input.addTextChangedListener(numberTextWatcher);
        desc3Input.addTextChangedListener(numberTextWatcher);
        discountValue1Input.addTextChangedListener(numberTextWatcher);
        discountValue2Input.addTextChangedListener(numberTextWatcher);
        discountValue3Input.addTextChangedListener(numberTextWatcher);
        discount1Name.addTextChangedListener(stringTextWatcher);
        discount2Name.addTextChangedListener(stringTextWatcher);
        discount3Name.addTextChangedListener(stringTextWatcher);
        ipInput.addTextChangedListener(stringTextWatcher);
        nodeInput.addTextChangedListener(stringTextWatcher);
        nodeInput.addTextChangedListener(stringTextWatcher);
        nodegroupInput.addTextChangedListener(stringTextWatcher);
        publicadorInput.addTextChangedListener(stringTextWatcher);

        delete.setOnClickListener(mListener);
        sync.setOnClickListener(mListener);
        out.setOnClickListener(mListener);

        spinnerDiscount1 = findViewById(R.id.spinner_discount1);
        spinnerDiscount1.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterDiscount1 = ArrayAdapter.createFromResource(this,
                R.array.type_discount, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterDiscount1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerDiscount1.setAdapter(adapterDiscount1);
        spinnerDiscount1.setSelection(config.getValueInt("typeDiscount1", context));


        spinnerDiscount2 = findViewById(R.id.spinner_discount2);
        spinnerDiscount2.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterDiscount2 = ArrayAdapter.createFromResource(this,
                R.array.type_discount, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterDiscount2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerDiscount2.setAdapter(adapterDiscount2);
        spinnerDiscount2.setSelection(config.getValueInt("typeDiscount2", context));


        spinnerDiscount3 = findViewById(R.id.spinner_discount3);
        spinnerDiscount3.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterDiscount3 = ArrayAdapter.createFromResource(this,
                R.array.type_discount, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterDiscount3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerDiscount3.setAdapter(adapterDiscount3);
        spinnerDiscount3.setSelection(config.getValueInt("typeDiscount3", context));

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

    private final TextWatcher numberTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int type1 = config.getValueInt("typeDiscount1", context);
            int type2 = config.getValueInt("typeDiscount2", context);
            int type3 = config.getValueInt("typeDiscount3", context);
            int value = 0;
            String text = s.toString();
            if (!text.equals("")) {
                value = Integer.parseInt(text);
            }

            if (codeInput.getText().hashCode() == s.hashCode())
                config.save(value, "code", context);
            else if (idInput.getText().hashCode() == s.hashCode())
                config.save(value, "id", context);
            else if (consecutiveInput.getText().hashCode() == s.hashCode())
                config.save(value, "consecutive", context);
            else if (tdgInput.getText().hashCode() == s.hashCode())
                config.save(value, "tdg", context);
            else if (desc1Input.getText().hashCode() == s.hashCode()) {
                if (desc1Input.getText().toString().trim().equalsIgnoreCase("")) {
                    desc1Input.setError("El valor debe estar entre 0 y 255");
                } else if (Integer.parseInt(desc1Input.getText().toString().trim()) > 255) {
                    desc1Input.setError("El valor debe estar entre 0 y 255");
                } else {
                    config.save(value, "discount1", context);
                }
            } else if (desc2Input.getText().hashCode() == s.hashCode()) {
                if (desc2Input.getText().toString().trim().equalsIgnoreCase("")) {
                    desc2Input.setError("El valor debe estar entre 0 y 255");
                } else if (Integer.parseInt(desc2Input.getText().toString().trim()) > 255) {
                    desc2Input.setError("El valor debe estar entre 0 y 255");
                } else {
                    config.save(value, "discount2", context);
                }
            } else if (desc3Input.getText().hashCode() == s.hashCode()) {
                if (desc3Input.getText().toString().trim().equalsIgnoreCase("")) {
                    desc3Input.setError("El valor debe estar entre 0 y 255");
                } else if (Integer.parseInt(desc3Input.getText().toString().trim()) > 255) {
                    desc3Input.setError("El valor debe estar entre 0 y 255");
                } else {
                    config.save(value, "discount3", context);
                }
            } else if (discountValue1Input.getText().hashCode() == s.hashCode()) {
                if (type1 == 1) {
                    if (discountValue1Input.getText().toString().trim().equalsIgnoreCase("")) {
                        discountValue1Input.setError("El valor debe estar en el rango de 0-100%");
                    } else if (Integer.parseInt(discountValue1Input.getText().toString().trim()) > 100) {
                        discountValue1Input.setError("No puede exceder el 100%");
                    } else {
                        config.save(value, "discountValue1", context);
                    }
                } else {
                    if (discountValue1Input.getText().toString().trim().equalsIgnoreCase("")) {
                        discountValue1Input.setError("El valor debe estar en el rango de 0-2047 minutos");
                    } else if (Integer.parseInt(discountValue1Input.getText().toString().trim()) > 2047) {
                        discountValue1Input.setError("No puede exceder los 2047 minutos");
                    } else {
                        config.save(value, "discountValue1", context);
                    }
                }
            } else if (discountValue2Input.getText().hashCode() == s.hashCode()) {
                if (type2 == 1) {
                    if (discountValue2Input.getText().toString().trim().equalsIgnoreCase("")) {
                        discountValue2Input.setError("El valor debe estar en el rango de 0-100%");
                    } else if (Integer.parseInt(discountValue2Input.getText().toString().trim()) > 100) {
                        discountValue2Input.setError("No puede exceder el 100%");
                    } else {
                        config.save(value, "discountValue2", context);
                    }
                } else {
                    if (discountValue2Input.getText().toString().trim().equalsIgnoreCase("")) {
                        discountValue2Input.setError("El valor debe estar en el rango de 0-2047 minutos");
                    } else if (Integer.parseInt(discountValue2Input.getText().toString().trim()) > 2047) {
                        discountValue2Input.setError("No puede exceder los 2047 minutos");
                    } else {
                        config.save(value, "discountValue2", context);
                    }
                }
            } else if (discountValue3Input.getText().hashCode() == s.hashCode()) {
                if (type3 == 1) {
                    if (discountValue3Input.getText().toString().trim().equalsIgnoreCase("")) {
                        discountValue3Input.setError("El valor debe estar en el rango de 0-100%");
                    } else if (Integer.parseInt(discountValue3Input.getText().toString().trim()) > 100) {
                        discountValue3Input.setError("No puede exceder el 100%");
                    } else {
                        config.save(value, "discountValue3", context);
                    }
                } else {
                    if (discountValue3Input.getText().toString().trim().equalsIgnoreCase("")) {
                        discountValue3Input.setError("El valor debe estar en el rango de 0-2047 minutos");
                    } else if (Integer.parseInt(discountValue3Input.getText().toString().trim()) > 2047) {
                        discountValue3Input.setError("No puede exceder los 2047 minutos");
                    } else {
                        config.save(value, "discountValue3", context);
                    }
                }
            }
        }
    };

    private final TextWatcher stringTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();

            if (ipInput.getText().hashCode() == s.hashCode())
                config.save(text, "ip", context);
            else if (nodeInput.getText().hashCode() == s.hashCode())
                config.save(text, "node", context);
            else if (nodegroupInput.getText().hashCode() == s.hashCode())
                config.save(text, "group", context);
            else if (publicadorInput.getText().hashCode() == s.hashCode())
                config.save(text, "publicador", context);
            else if (discount1Name.getText().hashCode() == s.hashCode())
                config.save(text, "discount1name", context);
            else if (discount2Name.getText().hashCode() == s.hashCode())
                config.save(text, "discount2name", context);
            else if (discount3Name.getText().hashCode() == s.hashCode())
                config.save(text, "discount3name", context);
        }
    };

    public void sincronizar() {
        ip = config.getValueString("ip", context);
        node = config.getValueString("node", context);
        publicador = config.getValueString("publicador", context);
        nodeGroup = config.getValueString("group", context);


        String url = "http://" + ip + ":31415/sync/" + publicador;

        config.save(url, "url", context);

        SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProviderM.DATABASE_NAME).getWritableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tablas = new ArrayList<>();

        while (c.moveToNext()) {
            tablas.add(c.getString(0));
        }

        tablas.remove("sqlite_sequence");
        tablas.remove("android_metadata");
        tablas.remove("tb_vehiculos");
        tablas.remove("tb_mensuales");

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
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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

        checkBoxDescuento1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean desc = checkBoxDescuento1.isChecked();
                config.save(desc, "discountActive1", context);
            }
        });

        checkBoxDescuento2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean desc = checkBoxDescuento2.isChecked();
                config.save(desc, "discountActive2", context);
            }
        });

        checkBoxDescuento3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Boolean desc = checkBoxDescuento3.isChecked();
                config.save(desc, "discountActive3", context);
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

        if (spinnerDiscount1.hashCode() == parent.hashCode())
            config.save(pos, "typeDiscount1", context);
        else if (spinnerDiscount2.hashCode() == parent.hashCode())
            config.save(pos, "typeDiscount2", context);
        else if (spinnerDiscount3.hashCode() == parent.hashCode())
            config.save(pos, "typeDiscount3", context);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public AlertDialog alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Configuration.this);

        final SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProviderM.DATABASE_NAME).getWritableDatabase();


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

    private class ThreadCod implements Runnable {
        @Override
        public void run() {
            int code = config.getValueInt("code", context);
            int id = config.getValueInt("id", context);

            codeInput.setText(Integer.toString(code));
            idInput.setText(Integer.toString(id));
        }
    }

    private class ThreadIn implements Runnable {
        @Override
        public void run() {
            Boolean entrada = config.getValueBoolean("entrada", context);
            int consecutive = config.getValueInt("consecutive", context);
            Boolean carro = config.getValueBoolean("carro", context);
            Boolean moto = config.getValueBoolean("moto", context);
            Boolean bicicleta = config.getValueBoolean("bicicleta", context);
            Boolean foto = config.getValueBoolean("foto", context);

            checkBoxEntrada.setChecked(entrada);
            consecutiveInput.setText(Integer.toString(consecutive));
            checkBoxCarro.setChecked(carro);
            checkBoxMoto.setChecked(moto);
            checkBoxBicicleta.setChecked(bicicleta);
            checkBoxFoto.setChecked(foto);
        }
    }

    private class ThreadOut implements Runnable {
        @Override
        public void run() {
            Boolean salida = config.getValueBoolean("salida", context);
            Boolean tdgcheck = config.getValueBoolean("tdgcheck", context);
            int tdg = config.getValueInt("tdg", context);
            Boolean pago = config.getValueBoolean("pago", context);
            Boolean formatear = config.getValueBoolean("formatear", context);

            checkBoxSalida.setChecked(salida);
            checkBoxTdg.setChecked(tdgcheck);
            tdgInput.setText(Integer.toString(tdg));
            checkBoxPay.setChecked(pago);
            checkBoxFormat.setChecked(formatear);
        }
    }

    private class ThreadDisc implements Runnable {
        @Override
        public void run() {
            new Thread(new ThreadDisc1()).start();
            new Thread(new ThreadDisc2()).start();
            new Thread(new ThreadDisc3()).start();
        }
    }

    private class ThreadDisc1 implements Runnable {
        @Override
        public void run() {
            Boolean descuento = config.getValueBoolean("discountActive1", context);
            String discountName = config.getValueString("discount1name", context);
            int discount = config.getValueInt("discount1", context);
            int discountValue = config.getValueInt("discountValue1", context);

            discount1Name.setText(discountName);
            desc1Input.setText(Integer.toString(discount));
            discountValue1Input.setText(Integer.toString(discountValue));
            checkBoxDescuento1.setChecked(descuento);
        }
    }

    private class ThreadDisc2 implements Runnable {
        @Override
        public void run() {
            Boolean descuento = config.getValueBoolean("discountActive2", context);
            String discountName = config.getValueString("discount2name", context);
            int discount = config.getValueInt("discount2", context);
            int discountValue = config.getValueInt("discountValue2", context);

            discount2Name.setText(discountName);
            desc2Input.setText(Integer.toString(discount));
            discountValue2Input.setText(Integer.toString(discountValue));
            checkBoxDescuento2.setChecked(descuento);
        }
    }

    private class ThreadDisc3 implements Runnable {
        @Override
        public void run() {
            Boolean descuento = config.getValueBoolean("discountActive3", context);
            String discountName = config.getValueString("discount3name", context);
            int discount = config.getValueInt("discount3", context);
            int discountValue = config.getValueInt("discountValue3", context);

            discount3Name.setText(discountName);
            desc3Input.setText(Integer.toString(discount));
            discountValue3Input.setText(Integer.toString(discountValue));
            checkBoxDescuento3.setChecked(descuento);
        }
    }

    private class ThreadSync implements Runnable {
        @Override
        public void run() {
            ip = config.getValueString("ip", context);
            node = config.getValueString("node", context);
            publicador = config.getValueString("publicador", context);
            nodeGroup = config.getValueString("group", context);

            ipInput.setText(ip);
            nodeInput.setText(node);
            publicadorInput.setText(publicador);
            nodegroupInput.setText(nodeGroup);
        }
    }
}
