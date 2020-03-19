package com.koiti.checkpoint;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
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

import com.koiti.checkpoint.MifareThreads.FormatSector1;
import com.koiti.checkpoint.MifareThreads.FormatSector2;
import com.koiti.checkpoint.MifareThreads.FormatSector3;

import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;


/**
 * Reset mifareclassic 1K card and save a new key in sector 1, 2 and 3.
 */
public class Format extends AppCompatActivity {

    private Context context;
    private NfcAdapter nfcAdapter;
    private Button save;
    private Boolean active = false;
    private Toast toasty;
    private int numberKey, code, id;
    private byte[] authenticationKey, keyFormat1, keyFormat2;

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

        numberKey = config.getValueInt("keyMifare", context);
        code = config.getValueInt("code", context);
        id = config.getValueInt("id", context);

        if (numberKey == 0) {
            keyFormat1 = Keys.OLD_KEYS_FORMAT[0];
            keyFormat2 = Keys.OLD_KEYS_FORMAT[1];
        } else{
            keyFormat1 = Keys.NEW_KEYS_FORMAT[0];
            keyFormat2 = Keys.NEW_KEYS_FORMAT[1];
        }

        save = findViewById(R.id.savedisc_Id);
        Button exit = findViewById(R.id.exitdisc_Id);

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

        boolean isAuthenticatedSector1, isAuthenticatedSector2, isAuthenticatedSector3;

        if (active) {
            FormatSector1 formatSector1 = new FormatSector1(nfcTag, Keys.STANDAR_KEY, code, id, keyFormat1);
            Thread t1 = new Thread(formatSector1, "T1");
            t1.start();
            try {
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isAuthenticatedSector1 = formatSector1.getAuthenticated();

            if (!isAuthenticatedSector1 && numberKey == 1) {
                formatSector1 = new FormatSector1(nfcTag, Keys.OLD_KEYS[0], code, id, keyFormat1);
                t1 = new Thread(formatSector1, "T1");
                t1.start();
                try {
                    t1.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isAuthenticatedSector1 = formatSector1.getAuthenticated();
            }

            Log.d("datos", "1" + isAuthenticatedSector1);

            //-----------------------------------------------------------------------------------------------------------------------

            FormatSector2 formatSector2 = new FormatSector2(nfcTag, Keys.STANDAR_KEY, keyFormat2);
            Thread t2 = new Thread(formatSector2, "T2");
            t2.start();
            try {
                t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isAuthenticatedSector2 = formatSector2.getAuthenticated();

            if (!isAuthenticatedSector2 && numberKey == 1) {
                formatSector2 = new FormatSector2(nfcTag, Keys.OLD_KEYS[1], keyFormat2);
                t2 = new Thread(formatSector2, "T2");
                t2.start();
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isAuthenticatedSector2 = formatSector2.getAuthenticated();
            }

            Log.d("datos", "2" + isAuthenticatedSector2);

            //-----------------------------------------------------------------------------------------------------------------------

            FormatSector3 formatSector3 = new FormatSector3(nfcTag, Keys.STANDAR_KEY, keyFormat1);
            Thread t3 = new Thread(formatSector3, "T3");
            t3.start();
            try {
                t3.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isAuthenticatedSector3 = formatSector3.getAuthenticated();

            if (!isAuthenticatedSector3 && numberKey == 1) {
                formatSector3 = new FormatSector3(nfcTag, Keys.OLD_KEYS[0], keyFormat1);
                t3 = new Thread(formatSector3, "T3");
                t3.start();
                try {
                    t3.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isAuthenticatedSector3 = formatSector3.getAuthenticated();
            }

            Log.d("datos", "3" + isAuthenticatedSector3);

            //-----------------------------------------------------------------------------------------------------------------------

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                save.setBackground(getDrawable(R.drawable.btn_round));//Default Color
            }

            if ((isAuthenticatedSector1 && isAuthenticatedSector2 && isAuthenticatedSector3)) {
                final Toast toasty = Toasty.success(Format.this, "" + "Formateo Exitoso", Toast.LENGTH_LONG);
                toasty.show();

            } else Toasty.error(getBaseContext(), "" + "El formateo " +
                    "ha fallado  por favor vuelva a intentarlo.", Toast.LENGTH_LONG).show();

            active = false;
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