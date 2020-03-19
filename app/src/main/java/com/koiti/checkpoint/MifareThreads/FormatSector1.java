package com.koiti.checkpoint.MifareThreads;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;

public class FormatSector1 implements Runnable {
    private Tag nfcTag;
    private volatile boolean exit = false;
    private byte[] authenticationKey, keyFormat;
    private boolean isAuthenticated;
    private int code, id;

    /**
     * Constructor
     *
     * @param nfcTag            tag NFC
     * @param authenticationKey key for authentication
     * @param code              code configured in settings
     * @param id                id configured in settings
     * @param keyFormat         key to format
     */
    public FormatSector1(Tag nfcTag, byte[] authenticationKey, int code, int id, byte[] keyFormat) {
        this.nfcTag = nfcTag;
        this.authenticationKey = authenticationKey;
        this.code = code;
        this.id = id;
        this.keyFormat = keyFormat;
    }

    @Override
    public void run() {
        while (!exit) {
            MifareClassic mifareClassic = MifareClassic.get(nfcTag);

            try {
                mifareClassic.connect();

                isAuthenticated = mifareClassic.authenticateSectorWithKeyA(1, authenticationKey);

                if (isAuthenticated) {
                    byte[] writeDataB1 = new byte[16];//data that will be written in block 1
                    byte[] writeDataB2 = new byte[16];//data that will be written in block 2

                    //Initialize the arrays
                    for (int i = 0; i < 16; i++) {
                        writeDataB1[i] = (byte) 0;
                        writeDataB2[i] = (byte) 0;
                    }

                    writeDataB1[0] = (byte) 1;
                    writeDataB1[1] = (byte) code;
                    writeDataB1[3] = (byte) id;

                    mifareClassic.writeBlock(4, writeDataB2);
                    mifareClassic.writeBlock(5, writeDataB1);
                    mifareClassic.writeBlock(6, writeDataB2);
                    mifareClassic.writeBlock(7, keyFormat);
                }

                mifareClassic.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stop();
        }
    }

    private void stop() {
        exit = true;
        Log.d("hilo 1", "detenido");
    }

    public boolean getAuthenticated() {
        return isAuthenticated;
    }

}
