package com.koiti.checkpoint.MifareThreads;

import com.koiti.checkpoint.Mifare;

import java.util.concurrent.Callable;

public class WriteMifare implements Callable<Boolean> {
    private Mifare mifare;
    private byte[] writeData; //data that will be written in block 2
    private int block;

    public WriteMifare(Mifare mifare, byte[] writeData, int block) {
        this.mifare = mifare;
        this.writeData = writeData;
        this.block = block;
    }

    @Override
    public Boolean call() {
        return mifare.writeMifareTag(1, block, writeData);
    }
}
