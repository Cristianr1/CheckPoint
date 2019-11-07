package com.koiti.checkpoint;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.jumpmind.symmetric.android.SQLiteOpenHelperRegistry;
import org.jumpmind.symmetric.android.SymmetricService;
import org.jumpmind.symmetric.common.ParameterConstants;

import java.util.Objects;
import java.util.Properties;

public class DbProvider extends ContentProvider {

//    "http://104.236.57.82:31415/sync/movil"

    private final String REGISTRATION_URL = "http://209.97.160.237:31415/sync/movil";
    private final String NODE_ID = "android";
    private final String NODE_GROUP = "android";

//    private final String REGISTRATION_URL = "null";
//    private final String NODE_ID = "null";
//    private final String NODE_GROUP = "null";

    private static final String TABLE_NAME = "tb_vehiculos";   // Table Name
    private static final String VEHID = "veh_id";     // Column 1 (Primary Key)
    private static final String VEHENTRADA = "veh_fh_entrada";    //Column 2
    private static final String VEHTIPOID = "veh_tipo_id";    // Column 3
    private static final String VEHUSUARIO = "veh_usuario";    // Column 4
    private static final String VEHESTACION = "veh_estacion";    // Column 5
    private static final String VEHCODIGO = "veh_codigo";    // Column 6
    private static final String VEHTIPO = "veh_tipo";    // Column 7
    private static final String VEHSALIDA = "veh_fh_salida";    // Column 8
    private static final String VEHROBADO = "veh_robado";    // Column 9
    private static final String VEHFEENTRADA = "veh_fe_entrada";    //Column 10
    private static final String VEHFESALIDA = "veh_fe_salida";    //Column 11
    private static final String VEHSITIOENTRADA = "veh_sitio_entrada";    //Column 12
    private static final String VEHSITIOSALIDA = "veh_sitio_salida";    //Column 13
    private static final String VEHDIRENTRADA = "veh_dir_entrada";    //Column 14
    private static final String VEHDIRSALIDA = "veh_dir_salida";    //Column 15
    private static final String VEHPURGADO = "veh_purgado";    // Column 16
    private static final String VEHPURGAESTACION = "veh_purga_ses_estacion";    // Column 17
    private static final String VEHPURGAUSUARIO = "veh_purga_ses_usuario";    // Column 18
    private static final String VEHPURGAINICIO = "veh_purga_ses_fh_inicio";    // Column 19
    private static final String VEHPURGAFECHAHORA = "veh_purga_fecha_hora";    // Column 20
    private static final String VEHCLASE = "veh_clase";    // Column 21
    private static final String VEHLOTE = "veh_lote";    // Column 22
    private static final String VEHPLACA = "veh_placa";    // Column 23
    private static final String VEHDOCUMENTO = "veh_documento";    // Column 24
    private static final String VEHNOMBRE = "veh_nombre";    // Column 25
    private static final String VEHMINUTOS = "veh_minutos";    // Column 26
    private static final String VEHROTACION = "veh_rotacion";    // Column 27

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS  " + TABLE_NAME +
            " (" + VEHID + " INT PRIMARY KEY, " + VEHENTRADA + " VARCHAR(255) ," + VEHTIPOID + " VARCHAR(255) ,"
            + VEHUSUARIO + " VARCHAR(255) ," + VEHESTACION + " VARCHAR(255) ," + VEHCODIGO + " VARCHAR(225),"
            + VEHTIPO + " VARCHAR(255) ," + VEHSALIDA + " VARCHAR(255) ," + VEHROBADO + " VARCHAR(255) ,"
            + VEHFEENTRADA + " VARCHAR(255) ," + VEHFESALIDA + " VARCHAR(255) ," + VEHSITIOENTRADA + " VARCHAR(255) ,"
            + VEHSITIOSALIDA + " VARCHAR(255) ," + VEHDIRENTRADA + " VARCHAR(255) ," + VEHDIRSALIDA + " VARCHAR(255) ,"
            + VEHPURGADO + " VARCHAR(255) ," + VEHPURGAESTACION + " VARCHAR(255) ," + VEHPURGAUSUARIO + " VARCHAR(255) ,"
            + VEHPURGAINICIO + " VARCHAR(255) ," + VEHPURGAFECHAHORA + " VARCHAR(255) ," + VEHCLASE + " VARCHAR(255) ,"
            + VEHLOTE + " VARCHAR(255) ," + VEHPLACA + " VARCHAR(255) ," + VEHDOCUMENTO + " VARCHAR(255) ,"
            + VEHNOMBRE + " VARCHAR(255) ," + VEHMINUTOS + " VARCHAR(255) ," + VEHROTACION + " VARCHAR(255));";

    public static final String DATABASE_NAME = "checkpoint.db";

    // Handle to a new DatabaseHelper.
    private DatabaseHelper mOpenHelper;

    /**
     * This class helps open, create, and upgrade the database file. Set to package visibility
     * for testing purposes.
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            // calls the super constructor, requesting the default cursor factory.
            super(context, DATABASE_NAME, null, 2);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    /**
     * Initializes the provider by creating a new DatabaseHelper. onCreate() is called
     * automatically when Android creates the provider in response to a resolver request from a
     * client.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onCreate() {
        ConfigStorage config = new ConfigStorage();

        String ip, node, nodeGroup;
        // Creates a new helper object. Note that the database itself isn't opened until
        // something tries to access it, and it's only created if it doesn't already exist.
        mOpenHelper = new DatabaseHelper(getContext());

        // Init the DB here
        mOpenHelper.getWritableDatabase().execSQL(CREATE_TABLE);

        // Register the database helper, so it can be shared with the SymmetricService
        SQLiteOpenHelperRegistry.register(DATABASE_NAME, mOpenHelper);

        Intent intent = new Intent(getContext(), SymmetricService.class);

        ip = config.getValueString("url", Objects.requireNonNull(getContext()));
        node = config.getValueString("node", getContext());
        nodeGroup = config.getValueString("group", getContext());

//        if (ip.isEmpty() && node.isEmpty() && nodeGroup.isEmpty())
//            ip = node = nodeGroup = "null";

        intent.putExtra(SymmetricService.INTENTKEY_SQLITEOPENHELPER_REGISTRY_KEY, DATABASE_NAME);
        intent.putExtra(SymmetricService.INTENTKEY_REGISTRATION_URL, REGISTRATION_URL);
        intent.putExtra(SymmetricService.INTENTKEY_EXTERNAL_ID, NODE_ID);
        intent.putExtra(SymmetricService.INTENTKEY_NODE_GROUP_ID, NODE_GROUP);
        intent.putExtra(SymmetricService.INTENTKEY_START_IN_BACKGROUND, true);

        Properties properties = new Properties();
        properties.put(ParameterConstants.FILE_SYNC_ENABLE, "true");
        properties.put("start.file.sync.tracker.job", "true");
        properties.put("start.file.sync.push.job", "true");
        properties.put("start.file.sync.pull.job", "true");
        properties.put("job.file.sync.pull.period.time.ms", "10000");

        intent.putExtra(SymmetricService.INTENTKEY_PROPERTIES, properties);
        getContext().startService(intent);

        // Assumes that any failures will be reported by a thrown exception.
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    public String getType(Uri uri) {
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}