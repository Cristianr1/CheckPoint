package com.koiti.checkpointm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;

public class Mensual {
    private SQLiteDatabase db = SQLiteOpenHelperRegistry.lookup(DbProviderM.DATABASE_NAME).getReadableDatabase();
    private String vehicleId, message;
    private LocalDate currentDate;
    private boolean locked = false, withRegistration = false;

    Mensual(int vehicleId, LocalDate currentDate) {
        this.vehicleId = String.valueOf(vehicleId);
        this.currentDate = currentDate;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Evaluate if the monthly payment has expired and if there is a block on the card
     *
     * @return TRUE if there is a blockage or the date expired
     */
    boolean dateUntil() {
        String menUntil = "1-1-1";
        String menLocked = "N";

        Cursor cursor = db.query(
                "tb_mensuales",
                new String[]{"men_hasta", "men_bloqueado"},
                "men_id" + " LIKE ?",
                new String[]{vehicleId},
                null,
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            menUntil = cursor.getString(cursor.getColumnIndex("men_hasta"));
            menUntil = menUntil.substring(0, 10);
            menLocked = cursor.getString(cursor.getColumnIndex("men_bloqueado"));
            Log.d("mensual", String.valueOf(menUntil));
            withRegistration = true;
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime dt = formatter.parseDateTime(menUntil);

        LocalDate expirationDate = dt.toLocalDate();

        if (currentDate.isAfter(expirationDate) || menLocked.equals("S"))
            locked = true;

        cursor.close();

        if (!withRegistration)
            message = "Sin registro en la base de datos";
        else {
            if (menLocked.equals("S"))
                message = "Tarjeta bloqueada";
            else
                message = "Mensualidad vencida";
        }
        return locked;
    }

}
