package com.felhr.serialportexample;

/**
 * Created by Aero on 2017/8/22.
 */

public class KalmanFilter {
    /**Kalman Filter*/
    private float predict;
    private float current;
    private float estimate;
    private double pdelt;
    private double mdelt;
    private double Gauss;
    private double kalmanGain;
    private final static double Q = 0.00001;
    private final static double R = 0.1;

    public void initial(){
        pdelt = 4.0;    //相当于系统测量误差
        mdelt = 3.0;
    }
    public float KF(float oldValue,float value){
        predict = oldValue;     //(1)
        current = value;        //
        Gauss = Math.sqrt(pdelt * pdelt + mdelt * mdelt) + Q;         //(2)
        kalmanGain = Math.sqrt((Gauss * Gauss)/(Gauss * Gauss + pdelt * pdelt)) + R;    //(4)
        estimate = (float) (kalmanGain * (current - predict) + predict);       //(3)
        mdelt = Math.sqrt((1-kalmanGain) * Gauss * Gauss);      //(5)
        return estimate;
    }
}
