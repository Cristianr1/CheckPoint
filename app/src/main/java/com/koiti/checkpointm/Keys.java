package com.koiti.checkpointm;

/**
 * Instance variables correspond to the keys for operations with MifareClassic
 */
public class Keys {
    //Old keys used to format
    public static final byte[][] OLD_KEYS_FORMAT = {{(byte) 0x33, (byte) 0x56, (byte) 0x30, (byte) 0x70, (byte) 0x34, (byte) 0x72,
            (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
            (byte) 0x33, (byte) 0x56, (byte) 0x30, (byte) 0x70, (byte) 0x34, (byte) 0x72},

            {(byte) 0x41, (byte) 0x63, (byte) 0x53, (byte) 0x45, (byte) 0x76, (byte) 0x50,
                    (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
                    (byte) 0x41, (byte) 0x63, (byte) 0x53, (byte) 0x45, (byte) 0x76, (byte) 0x50}};

    //New keys used to format
    public static final byte[][] NEW_KEYS_FORMAT = {{(byte) 0x4E, (byte) 0x65, (byte) 0x77, (byte) 0x50, (byte) 0x4C, (byte) 0x31,
            (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
            (byte) 0x4E, (byte) 0x65, (byte) 0x77, (byte) 0x50, (byte) 0x4C, (byte) 0x31},

            {(byte) 0x4E, (byte) 0x65, (byte) 0x77, (byte) 0x50, (byte) 0x4C, (byte) 0x32,
                    (byte) 0xFF, 7, (byte) 0x80, (byte) 0x69,
                    (byte) 0x4E, (byte) 0x65, (byte) 0x77, (byte) 0x50, (byte) 0x4C, (byte) 0x32}};

    //New keys used to authenticate
    static final byte[][] NEW_KEYS = {
            {(byte) 0x4E, (byte) 0x65, (byte) 0x77, (byte) 0x50, (byte) 0x4C, (byte) 0x31},//NewPL1
            {(byte) 0x4E, (byte) 0x65, (byte) 0x77, (byte) 0x50, (byte) 0x4C, (byte) 0x32}//NewPL2
    };

    //Old keys used to authenticate
    public static final byte[][] OLD_KEYS = {
            {(byte) 0x33, (byte) 0x56, (byte) 0x30, (byte) 0x70, (byte) 0x34, (byte) 0x72},//3V0p4r
            {(byte) 0x41, (byte) 0x63, (byte) 0x53, (byte) 0x45, (byte) 0x76, (byte) 0x50}//AcSEvP
    };

    public static final byte[] STANDAR_KEY = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
}
