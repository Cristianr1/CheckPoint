package com.koiti.checkpoint.MifareThreads;

import com.koiti.checkpoint.Mifare;

import java.util.concurrent.Callable;

public class ReadMifare implements Callable<byte[]> {
    private Mifare mifare;
    private int block;

    public ReadMifare(Mifare mifare, int block) {
        this.mifare = mifare;
        this.block = block;
    }

    @Override
    public byte[] call() {
        return mifare.readMifareTagBlock(1, block);
    }
}
