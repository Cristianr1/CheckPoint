package com.koiti.checkpointm.MifareThreads;

import com.koiti.checkpointm.Mifare;

import java.util.concurrent.Callable;

public class ReadMifare implements Callable<byte[]> {
    private Mifare mifare;
    private int block, sector;

    public ReadMifare(Mifare mifare, int block) {
        this.mifare = mifare;
        this.block = block;
        this.sector = 1;
    }

    public ReadMifare(Mifare mifare, int sector, int block) {
        this.mifare = mifare;
        this.block = block;
        this.sector = sector;
    }

    @Override
    public byte[] call() {
        return mifare.readMifareTagBlock(sector, block);
    }
}
