package com.felhr.serialportexample;

/**
 * Created by Aero on 2017/10/20.
 */

public class UsbDecode {
    int[] dus={0,0,0,0,0,0,0,0,0};
    int cnt;

    public int[] Decode(byte[] raw){
        dus[cnt++]=raw[cnt++];
        if (cnt>8){
            cnt=0;
            dus[8]=1;
        }
        return dus;
    }
}
