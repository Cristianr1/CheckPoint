package com.koiti.checkpointm;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class UploadData implements Runnable {
    private Context context;
    private String uid, dateIn, dateOut;
    private boolean entry;

    UploadData(Context context, int uid, String dateIn, boolean entry) {
        this.context = context;
        this.uid = String.valueOf(uid);
        this.dateIn = dateIn;
        this.entry = entry;
    }

    UploadData(Context context, int uid, String dateIn, String dateOut, boolean entry) {
        this.context = context;
        this.uid = String.valueOf(uid);
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.entry = entry;
    }

    @Override
    public void run() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://157.245.253.235/parkline/public/api/vehiculos";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response is: ", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response is:", "false" + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("parqueadero", "El Carmelo");
                params.put("veh_id", uid);
                params.put("veh_fh_entrada", dateIn);
                params.put("veh_estacion", "MOVIL");
                params.put("veh_usuario", "SISTEMA");
                params.put("veh_tipo", "Mensual");
                params.put("veh_dir_entrada", "1");
                params.put("veh_fe_entrada", dateIn);
                params.put("veh_clase", "Carro");
                if (!entry) {
                    params.put("veh_fh_salida", dateOut);
                    params.put("veh_fe_salida", dateOut);
                    params.put("veh_dir_salida", "1");
                }
                return params;
            }
        };

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
