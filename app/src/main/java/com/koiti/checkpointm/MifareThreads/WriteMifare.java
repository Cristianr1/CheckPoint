package com.koiti.checkpointm.MifareThreads;

import com.koiti.checkpointm.Mifare;

import java.util.concurrent.Callable;

public class WriteMifare implements Callable<Boolean> {
    private Mifare mifare;
    private byte[] writeData; //data that will be written in block 2
    private int block;
    private int sector;

    public WriteMifare(Mifare mifare, byte[] writeData, int block) {
        this.mifare = mifare;
        this.writeData = writeData;
        this.block = block;
        this.sector = 1;
    }

    public WriteMifare(Mifare mifare, byte[] writeData, int block, int sector) {
        this.mifare = mifare;
        this.writeData = writeData;
        this.block = block;
        this.sector = sector;
    }

    @Override
    public Boolean call() {
        return mifare.writeMifareTag(sector, block, writeData);
    }
}
