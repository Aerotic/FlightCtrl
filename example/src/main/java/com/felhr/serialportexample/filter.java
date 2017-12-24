package com.felhr.serialportexample;


import java.math.*;

/**
 * Created by aero on 17-12-24.
 */

public class filter {
    public void filter(){

    }
    private static final double M_PI_F = Math.PI;
    private static double _cutoff_freq1;
    private static double _a11;
    private static double _a21;
    private static double _b01;
    private static double _b11;
    private static double _b21;
    private static double _delay_element_11;        // buffered sample -1
    private static double _delay_element_21;        // buffered sample -2
    public void LPF2pSetCutoffFreq_1(double sample_freq, double cutoff_freq){
        double fr =0.0;
        double ohm =0.0;
        double c =0.0;

        fr= sample_freq/cutoff_freq;
        ohm=Math.tan(M_PI_F/fr);
        c=1.0f+2.0f*Math.cos(M_PI_F/4.0f)*ohm + ohm*ohm;
        _cutoff_freq1 = cutoff_freq;
        if (_cutoff_freq1 > 0.0f)
        {
            _b01 = ohm*ohm/c;
            _b11 = 2.0f*_b01;
            _b21 = _b01;
            _a11 = 2.0f*(ohm*ohm-1.0f)/c;
            _a21 = (1.0f-2.0f*Math.cos(M_PI_F/4.0f)*ohm+ohm*ohm)/c;
        }
    }
    public double LPF2pApply_1(float sample) {

        double delay_element_0 = 0, output=0;
        if (_cutoff_freq1 <= 0.0f) {
            // no filtering
            return sample;
        }
        else
        {
            delay_element_0 = sample - _delay_element_11 * _a11 - _delay_element_21 * _a21;
            // do the filtering
            if (Double.isNaN(delay_element_0)) {
                // don't allow bad values to propogate via the filter
                delay_element_0 = sample;
            }
            output = delay_element_0 * _b01 + _delay_element_11 * _b11 + _delay_element_21 * _b21;

            _delay_element_21 = _delay_element_11;
            _delay_element_11 = delay_element_0;

            // return the value.  Should be no need to check limits
            return output;
        }
    }

    private static double _cutoff_freq2;
    private static double _a12;
    private static double _a22;
    private static double _b02;
    private static double _b12;
    private static double _b22;
    private static double _delay_element_12;        // buffered sample -1
    private static double _delay_element_22;        // buffered sample -2
    void LPF2pSetCutoffFreq_2(double sample_freq, double cutoff_freq){
        double fr =0.0;
        double ohm =0.0;
        double c =0.0;

        fr= sample_freq/cutoff_freq;
        ohm=Math.tan(M_PI_F/fr);
        c=1.0f+2.0f*Math.cos(M_PI_F/4.0f)*ohm + ohm*ohm;
        _cutoff_freq2 = cutoff_freq;
        if (_cutoff_freq2 > 0.0f)
        {
            _b02 = ohm*ohm/c;
            _b12 = 2.0f*_b02;
            _b22 = _b02;
            _a12 = 2.0f*(ohm*ohm-1.0f)/c;
            _a22 = (1.0f-2.0f*Math.cos(M_PI_F/4.0f)*ohm+ohm*ohm)/c;
        }
    }
    public double LPF2pApply_2(float sample) {

        double delay_element_0 = 0, output=0;
        if (_cutoff_freq2 <= 0.0f) {
            // no filtering
            return sample;
        }
        else
        {
            delay_element_0 = sample - _delay_element_12 * _a11 - _delay_element_22 * _a22;
            // do the filtering
            if (Double.isNaN(delay_element_0)) {
                // don't allow bad values to propogate via the filter
                delay_element_0 = sample;
            }
            output = delay_element_0 * _b02 + _delay_element_12 * _b12 + _delay_element_22 * _b22;

            _delay_element_22 = _delay_element_12;
            _delay_element_12 = delay_element_0;

            // return the value.  Should be no need to check limits
            return output;
        }
    }

    private static double _cutoff_freq3;
    private static double _a13;
    private static double _a23;
    private static double _b03;
    private static double _b13;
    private static double _b23;
    private static double _delay_element_13;        // buffered sample -1
    private static double _delay_element_23;        // buffered sample -2
    void LPF2pSetCutoffFreq_3(double sample_freq, double cutoff_freq){
        double fr =0.0;
        double ohm =0.0;
        double c =0.0;

        fr= sample_freq/cutoff_freq;
        ohm=Math.tan(M_PI_F/fr);
        c=1.0f+2.0f*Math.cos(M_PI_F/4.0f)*ohm + ohm*ohm;
        _cutoff_freq3 = cutoff_freq;
        if (_cutoff_freq3 > 0.0f)
        {
            _b03 = ohm*ohm/c;
            _b13 = 2.0f*_b03;
            _b23 = _b03;
            _a13 = 2.0f*(ohm*ohm-1.0f)/c;
            _a23 = (1.0f-2.0f*Math.cos(M_PI_F/4.0f)*ohm+ohm*ohm)/c;
        }
    }
    public double LPF2pApply_3(float sample) {

        double delay_element_0 = 0, output=0;
        if (_cutoff_freq3 <= 0.0f) {
            // no filtering
            return sample;
        }
        else
        {
            delay_element_0 = sample - _delay_element_13 * _a13 - _delay_element_23 * _a23;
            // do the filtering
            if (Double.isNaN(delay_element_0)) {
                // don't allow bad values to propogate via the filter
                delay_element_0 = sample;
            }
            output = delay_element_0 * _b03 + _delay_element_13 * _b13 + _delay_element_23 * _b23;

            _delay_element_23 = _delay_element_13;
            _delay_element_13 = delay_element_0;

            // return the value.  Should be no need to check limits
            return output;
        }
    }
    public double[] getResult(){
        double[] result = new double[5];
        result[0] = _a11;
        result[1] = _a21;
        result[2] = _b01;
        result[3] = _b11;
        result[4] = _b21;
        return result;
    }

}