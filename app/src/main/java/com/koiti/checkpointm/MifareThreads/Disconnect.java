package com.koiti.checkpointm.MifareThreads;

import com.koiti.checkpointm.Mifare;

public class Disconnect implements Runnable {
    private Mifare mifare;

    public Disconnect(Mifare mifare) {
        this.mifare = mifare;
    }

    @Override
    public void run() {
        mifare.disconnectTag();
    }
}
