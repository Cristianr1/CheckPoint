package com.koiti.checkpoint;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class Password extends AppCompatActivity {

    private Context context;

    private EditText password;
    private Button btnSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        addListenerOnButton();
    }

    public void addListenerOnButton() {

        password = findViewById(R.id.txtPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String pass = password.getText().toString();

                Intent intent = null;
                switch (pass) {
                    case "B800530*":
                        intent = new Intent(context, ConfigurationMINI.class);
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        incorrect();
                        break;
                }
            }

        });
    }

    public void incorrect() {
        Toast t = Toasty.error(this, "Contraseña incorrecta", Toast.LENGTH_SHORT);
        t.show();
    }
}

