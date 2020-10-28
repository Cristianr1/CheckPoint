package com.koiti.checkpointm;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.jumpmind.symmetric.android.SymmetricService;


public class MainActivity extends AppCompatActivity {
    private Context context;
    private Button config, in, out, disc, initialize, format, readCard;
    ConfigStorage configuration = new ConfigStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        context = this;
        // Capture our button from layout
        config = findViewById(R.id.config_Id);
        in = findViewById(R.id.in_Id);
        out = findViewById(R.id.out_Id);
        disc = findViewById(R.id.disc_Id);
        initialize = findViewById(R.id.initialize_Id);
        format = findViewById(R.id.format_Id);
        readCard = findViewById(R.id.showdata_Id);
        // Register the onClick listener with the implementation above
        in.setOnClickListener(mListener);
        config.setOnClickListener(mListener);
        disc.setOnClickListener(mListener);
        initialize.setOnClickListener(mListener);
        readCard.setOnClickListener(mListener);
        format.setOnClickListener(mListener);
        out.setOnClickListener(mListener);

        Boolean entrada = configuration.getValueBoolean("entrada", context);
        Boolean salida = configuration.getValueBoolean("salida", context);
        Boolean descuento1 = configuration.getValueBoolean("discountActive1", context);
        Boolean descuento2 = configuration.getValueBoolean("discountActive2", context);
        Boolean descuento3 = configuration.getValueBoolean("discountActive3", context);
        Boolean lectura = configuration.getValueBoolean("lectura", context);
        Boolean inicializar = configuration.getValueBoolean("inicializar", context);
        Boolean formatear = configuration.getValueBoolean("formatear", context);

        in.setVisibility(entrada ? View.VISIBLE : View.GONE);
        out.setVisibility(salida ? View.VISIBLE : View.GONE);
        disc.setVisibility(descuento1 || descuento2 || descuento3 ? View.VISIBLE : View.GONE);
        initialize.setVisibility(inicializar ? View.VISIBLE : View.GONE);
        readCard.setVisibility(lectura ? View.VISIBLE : View.GONE);
        format.setVisibility(formatear ? View.VISIBLE : View.GONE);
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        public void onClick(View v) {
            // do something when the button is clicked

            Intent intentNav = null;
            switch (v.getId()) {
                case R.id.config_Id:
                    intentNav = new Intent(context, Password.class);
                    break;
                case R.id.in_Id:
                    intentNav = new Intent(context, Entrance.class);
                    break;
                case R.id.disc_Id:
                    intentNav = new Intent(context, Discount.class);
                    break;
                case R.id.initialize_Id:
                    intentNav = new Intent(context, Initialize.class);
                    break;
                case R.id.format_Id:
                    intentNav = new Intent(context, Format.class);
                    break;
                case R.id.showdata_Id:
                    intentNav = new Intent(context, ShowData.class);
                    break;
                case R.id.out_Id:
                    intentNav = new Intent(context, Exit.class);
                    break;

            }
            startActivity(intentNav);
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();

        Boolean entrada = configuration.getValueBoolean("entrada", context);
        Boolean salida = configuration.getValueBoolean("salida", context);
        Boolean descuento1 = configuration.getValueBoolean("discountActive1", context);
        Boolean descuento2 = configuration.getValueBoolean("discountActive2", context);
        Boolean descuento3 = configuration.getValueBoolean("discountActive3", context);
        Boolean inicializar = configuration.getValueBoolean("inicializar", context);
        Boolean lectura = configuration.getValueBoolean("lectura", context);
        Boolean formatear = configuration.getValueBoolean("formatear", context);

        in.setVisibility(entrada ? View.VISIBLE : View.GONE);
        out.setVisibility(salida ? View.VISIBLE : View.GONE);
        disc.setVisibility(descuento1 || descuento2 || descuento3 ? View.VISIBLE : View.GONE);
        initialize.setVisibility(inicializar ? View.VISIBLE : View.GONE);
        readCard.setVisibility(lectura ? View.VISIBLE : View.GONE);
        format.setVisibility(formatear ? View.VISIBLE : View.GONE);

        startService(new Intent(this, SymmetricService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(context, "Symmetric se ha detenido", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, SymmetricService.class);
        stopService(intent);
    }
}
