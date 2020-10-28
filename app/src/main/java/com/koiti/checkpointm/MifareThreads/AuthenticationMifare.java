package com.koiti.checkpointm.MifareThreads;

import android.content.Context;

import com.koiti.checkpointm.ConfigStorage;
import com.koiti.checkpointm.Mifare;

import java.util.concurrent.Callable;

public class AuthenticationMifare implements Callable<Boolean> {
    private Mifare mifare;
    private int sector;
    private byte[] key;

    public AuthenticationMifare(Mifare mifare, Context context) {
        this.mifare = mifare;
        sector = 1;
        ConfigStorage config = new ConfigStorage();
        int numberKey = config.getValueInt("keyMifare", context);
        if (numberKey == 0) this.key = Mifare.oldKeys[0];
        else this.key = Mifare.newKeys[0];
    }

    public AuthenticationMifare(Mifare mifare, int sector) {
        this.mifare = mifare;
        this.sector = sector;
        this.key = Mifare.STANDAR_KEY;
    }

    @Override
    public Boolean call() {
        return mifare.authentificationKey(key, Mifare.KEY_TYPE_A, sector);
    }
}