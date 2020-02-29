package com.koiti.checkpoint.MifareThreads;

import com.koiti.checkpoint.Mifare;

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
