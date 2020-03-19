package com.koiti.checkpoint.MifareThreads;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;

public class FormatSector2 implements Runnable {
    private Tag nfcTag;
    private volatile boolean exit = false;
    private byte[] authenticationKey, keyFormat;
    private boolean isAuthenticated;

    /**
     * Constructor
     *
     * @param nfcTag            tag NFC
     * @param authenticationKey key for authentication
     * @param keyFormat         key to format
     */
    public FormatSector2(Tag nfcTag, byte[] authenticationKey, byte[] keyFormat) {
        this.nfcTag = nfcTag;
        this.authenticationKey = authenticationKey;
        this.keyFormat = keyFormat;
    }

    @Override
    public void run() {
        while (!exit) {
            MifareClassic mifareClassic = MifareClassic.get(nfcTag);

            try {
                mifareClassic.connect();

                isAuthenticated = mifareClassic.authenticateSectorWithKeyA(2, authenticationKey);

                if (isAuthenticated)
                    mifareClassic.writeBlock(11, keyFormat);

                mifareClassic.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stop();
        }
    }

    private void stop() {
        exit = true;
        Log.d("hilo 2", "detenido");
    }

    public boolean getAuthenticated() {
        return isAuthenticated;
    }

}

