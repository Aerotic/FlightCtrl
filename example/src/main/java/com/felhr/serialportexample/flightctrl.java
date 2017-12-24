
/*
* 文件名：flightcontrol
* 描    述：
* 作    者：Aeroman
* 时    间：2017-12-08
* 版    权：
*/
package com.felhr.serialportexample;

//import sun.jvm.hotspot.debugger.posix.elf.ELFException;

import android.support.v4.view.animation.PathInterpolatorCompat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import java.net.PortUnreachableException;
import java.math.*;


public class flightctrl {

    public void flightctrl(){
        mFilter.LPF2pSetCutoffFreq_1(100,10);
    }
    /**
     * METHOD:LIMIT()
     * PARAETER:DATATYPE origin,DATATYPE min,DATATYPE max
     * DATATYPE:float,double,byte,short,int,long,
     * FUNCTION:limit origin to the range between min and max
     */
    public filter mFilter = new filter();
    public float LIMIT(float origin,float min,float max){
        return origin>max?max:(origin<min?min:origin);
    }
    public int LIMIT(int origin,int min,int max){
        return origin>max?max:(origin<min?min:origin);
    }
    public double LIMIT(double origin,double min,double max){
        return origin>max?max:(origin<min?min:origin);
    }
    public short LIMIT(short origin,short min,short max){
        return origin>max?max:(origin<min?min:origin);
    }
    public long LIMIT(long origin,long min,long max){
        return origin>max?max:(origin<min?min:origin);
    }
    public char LIMIT(char origin,char min,char max){
        return origin>max?max:(origin<min?min:origin);
    }
    public byte LIMIT(byte origin,byte min,byte max){
        return origin>max?max:(origin<min?min:origin);
    }


    //传感器相关
    public float[] acc=new float[3];
    public float[] gyro_dps=new float[3];
    public float[] gyro_rps=new float[3];
    public float[] orientation=new float[3];
    public float[] RotationVector = new float[3];

    public void getGyro(float[] mgyro_dps,float[] mgyro_rps){
        gyro_dps = mgyro_dps;
        gyro_rps = mgyro_rps;
    }
    public void getAcc(float[] macc){
        acc=macc;
    }

    public void getOrientation(float[] mangle){
        orientation = mangle;
    }

    public void getRotationVector(float[] mRotationVector){
        RotationVector = mRotationVector;
    }

//    //计算姿态相关
//    double q0 = 1, q1 = 0, q2 = 0, q3 = 0;     // quaternion elements representing the estimated orientation
//    double exInt = 0, eyInt = 0, ezInt = 0;    // scaled integral error
//    double Kp=0.6,Ki=  0.1;
//    double ux, uy, uz;
//    double wx, wy, wz;
//    double vx, vy, vz;
//    double norm;
//    double ex, ey, ez;
//    double yaw,roll,pitch;
//    double ANGLE_TO_RAD = 0.01745329;
//    double IMU_INTEGRAL_LIM = 2.0*ANGLE_TO_RAD;
//    double[] AngleFromQuaternion = new double[3];
//    /**
//     * Method: getQuaternion().
//     * Parameter: double half_T,
//     *            double gx,
//     *            double gy,
//     *            double gz,
//     *            double ax,
//     *            double ay,
//     *            double az
//     * Return: void    all the result will be stored in double array orientation
//     * */
//    public void getQuaternion(double half_T, double gx, double gy, double gz, double ax, double ay, double az)
//    {
//        norm = Math.sqrt(ax*ax + ay*ay + az*az);
//        ax = ax / norm;
//        ay = ay / norm;
//        az = az / norm;
//
//        // estimated direction of gravity and flux (v and w)              ¹À¼ÆÖØÁ¦·½ÏòºÍÁ÷Á¿/±äÇ¨
//        vx = 2*(q1*q3 - q0*q2);												//ËÄÔªËØÖÐxyzµÄ±íÊ¾
//        vy = 2*(q0*q1 + q2*q3);
//        vz = 1 - 2*(q1*q1 + q2*q2);
//
//        // error is sum of cross product between reference direction of fields and direction measured by sensors
//        ex = (ay*vz - az*vy) ;                           					 //ÏòÁ¿Íâ»ýÔÚÏà¼õµÃµ½²î·Ö¾ÍÊÇÎó²î
//        ey = (az*vx - ax*vz) ;
//        ez = (ax*vy - ay*vx) ;
//
//        exInt = exInt + ex *Ki *2 *half_T;								  //¶ÔÎó²î½øÐÐ»ý·Ö
//        eyInt = eyInt + ey *Ki *2 *half_T;
//        ezInt = ezInt + ez *Ki *2 *half_T;
//
//        // »ý·ÖÏÞ·ù
//            exInt = LIMIT(exInt, - IMU_INTEGRAL_LIM ,IMU_INTEGRAL_LIM );
//            exInt = LIMIT(exInt, - IMU_INTEGRAL_LIM ,IMU_INTEGRAL_LIM );
//            exInt = LIMIT(exInt, - IMU_INTEGRAL_LIM ,IMU_INTEGRAL_LIM );
//
//        // adjusted gyroscope measurements
//        gx = gx + Kp *(ex + exInt);
//        gy = gy + Kp *(ey + eyInt);
//        gz = gz + Kp *(ez + ezInt);
//
//        // integrate quaternion rate and normalise						   //ËÄÔªËØµÄÎ¢·Ö·½³Ì
//        q0 = q0 + (-q1*gx - q2*gy - q3*gz) *half_T;
//        q1 = q1 + ( q0*gx + q2*gz - q3*gy) *half_T;
//        q2 = q2 + ( q0*gy - q1*gz + q3*gx) *half_T;
//        q3 = q3 + ( q0*gz + q1*gy - q2*gx) *half_T;
//
//        // normalise quaternion
//        double tmp=q0*q0 + q1*q1 + q2*q2 + q3*q3;
//        norm = Math.sqrt(tmp);
//        q0 = q0 / norm;
//        q1 = q1 / norm;
//        q2 = q2 / norm;
//        q3 = q3 / norm;
//
//        AngleFromQuaternion[YAW] = Math.atan2(2*q1*q2+2*q0*q3, -2*q2*q2-2*q3*q3+1) *57.3f;
//        AngleFromQuaternion[ROLL] = Math.atan2(2*q2*q3 + 2*q0*q1, -2*q1*q1 - 2*q2*q2 + 1) *57.3f;
//        AngleFromQuaternion[PITCH] = Math.asin(-2*q1*q3 + 2*q0*q2) *57.3f;
//
//        ux = 1 - 2*(q2*q2 + q3*q3);
//        uy = 2*(q1*q2 - q0*q3);
//        uz = 2*(q1*q3 + q0*q2);
//
//        wx = 2*(q1*q2 + q0*q3);
//        wy = 1-2*(q1*q1 + q3*q3);
//        wz = 2*(q2*q3 - q0*q1);
//    }

    //获取竖直方向加速度   可用
    public double getVerticalAcceleration(){

        float[] quaternion = new float[4];
        SensorManager.getQuaternionFromVector(quaternion,RotationVector);

        double q0q0 = quaternion[0]*quaternion[0];
        double q0q1 = quaternion[1]*quaternion[0];
        double q0q2 = quaternion[0]*quaternion[2];

        double q1q1 = quaternion[1]*quaternion[1];
        double q1q3 = quaternion[1]*quaternion[3];

        double q2q2 = quaternion[2]*quaternion[2];
        double q2q3 = quaternion[2]*quaternion[3];

        double q3q3 = quaternion[3]*quaternion[3];

        double vx,vy,vz;
        vx = 2*(q1q3 - q0q2);
        vy = 2*(q0q1 + q2q3);
        vz = q0q0 - q1q1 -q2q2 +q3q3;

        return vx*acc[0]+vy*acc[1]+vz*acc[2];
    }


    //获取时间相关  可用
    public static final byte ITEM_OUTER_TIMEINTERVAL = 0;
    public static final byte ITEM_INNER_TIMEINTERVAL = 1;
    public static final byte ITEM_HEIGHT_TIMEINTERVAL = 2;
    public static final byte NOW=1;
    public static final byte OLD=0;
    public static final byte NEW=2;
    private static long[][] TIMEINTERVAL=new long[10][3];
    private boolean[] FirstFlag = {true,true,true,true,true,true,true,true,true,true};
    double getTimeInterval(int ITEM){
        TIMEINTERVAL[ITEM][OLD] = TIMEINTERVAL[ITEM][NOW];
        TIMEINTERVAL[ITEM][NOW] = System.nanoTime();
        TIMEINTERVAL[ITEM][NEW] =TIMEINTERVAL[ITEM][NOW] - TIMEINTERVAL[ITEM][OLD];
        if (FirstFlag[ITEM]){
            FirstFlag[ITEM] = false;
            return 0.0;
        }
        else return (double) TIMEINTERVAL[ITEM][NEW]/1000000000.0;
    }

    private static final byte PITCH=0;
    private static final byte YAW=1;
    private static final byte ROLL=2;
    private static final byte X=0;
    private static final byte Y=1;
    private static final byte Z=2;
    private static final byte KP=0;
    private static final byte KI=1;
    private static final byte KD=2;
    private static final byte INC_MAX=2;
    private static final byte ACC=0;
    private static final byte VEL=1;
    private static final byte POS=2;


   //油门相关
    public class THROTTLE{
        public double value;
        public double lpf;
        public double weight;
        public double comp;
        public void Thr_Ctrl(double T){
            if (Unlock){
                value=0.6061*RC[3]+500;
                value=LIMIT(value,READY_THR,MAX_THR);
            }
            else value=0;//飞机上锁油门强制置零

                    /*油门低通滤波*/
            lpf+=10*3.14*T*(value/400.0-lpf);
            weight = LIMIT(lpf,0,1);
        }
    }
    public THROTTLE thr=new THROTTLE();


    //输出相关
    private static final int READY_THR=100;
    private static final int MAX_THR=1000;
    private static final int MAX_INNER_OUT=200;
    public int[] motorout=new int[4];
    private static final double RtA=57.2957795;//  rad to degree
    private static final short MAX_MOTOR_PWM = 8000;
    private static final short MIN_MOTOR_PWM = 4000;
    public short[] output(){
        double[] MotorSpeed=new double[4];
        short[] MotorRPM=new short[4];
        if(!Unlock){
            MotorSpeed[0] = 0;
            MotorSpeed[1] = 0;
            MotorSpeed[2] = 0;
            MotorSpeed[3] = 0;
        }
        else {
            thr.comp = thr.value / Math.cos(orientation[PITCH]/RtA) / Math.cos(orientation[ROLL]/RtA);

            MotorSpeed[0] = thr.comp - pid.pidinner.innerout[X] - pid.pidinner.innerout[Y] - pid.pidinner.innerout[Z] + ( pid.pidhgtctrl.HgtCtrl[ACC][pid.HEIGHT_OUT] +  pid.pidhgtctrl.HgtCtrl[VEL][pid.HEIGHT_OUT] )/ Math.cos(orientation[PITCH]/RtA) / Math.cos(orientation[ROLL]/RtA);
            MotorSpeed[1] = thr.comp + pid.pidinner.innerout[X] - pid.pidinner.innerout[Y] + pid.pidinner.innerout[Z] + ( pid.pidhgtctrl.HgtCtrl[ACC][pid.HEIGHT_OUT] +  pid.pidhgtctrl.HgtCtrl[VEL][pid.HEIGHT_OUT] )/ Math.cos(orientation[PITCH]/RtA) / Math.cos(orientation[ROLL]/RtA);
            MotorSpeed[2] = thr.comp + pid.pidinner.innerout[X] + pid.pidinner.innerout[Y] - pid.pidinner.innerout[Z] + ( pid.pidhgtctrl.HgtCtrl[ACC][pid.HEIGHT_OUT] +  pid.pidhgtctrl.HgtCtrl[VEL][pid.HEIGHT_OUT] )/ Math.cos(orientation[PITCH]/RtA) / Math.cos(orientation[ROLL]/RtA);
            MotorSpeed[3] = thr.comp - pid.pidinner.innerout[X] + pid.pidinner.innerout[Y] + pid.pidinner.innerout[Z] + ( pid.pidhgtctrl.HgtCtrl[ACC][pid.HEIGHT_OUT] +  pid.pidhgtctrl.HgtCtrl[VEL][pid.HEIGHT_OUT] )/ Math.cos(orientation[PITCH]/RtA) / Math.cos(orientation[ROLL]/RtA);

            MotorSpeed[0] = LIMIT(MotorSpeed[0],READY_THR*0.8,MAX_THR*1.1);
            MotorSpeed[1] = LIMIT(MotorSpeed[1],READY_THR*0.8,MAX_THR*1.1);
            MotorSpeed[2] = LIMIT(MotorSpeed[2],READY_THR*0.8,MAX_THR*1.1);
            MotorSpeed[3] = LIMIT(MotorSpeed[3],READY_THR*0.8,MAX_THR*1.1);
        }

        MotorRPM[0]=(short)(MIN_MOTOR_PWM + 4*MotorSpeed[0]);
        MotorRPM[1]=(short)(MIN_MOTOR_PWM + 4*MotorSpeed[1]);
        MotorRPM[2]=(short)(MIN_MOTOR_PWM + 4*MotorSpeed[2]);
        MotorRPM[3]=(short)(MIN_MOTOR_PWM + 4*MotorSpeed[3]);

        return MotorRPM;
        //待改动
    }


    //PID相关
    public boolean Unlock = false;

    public double MAX_CTRL_ASPEED = 220.0;
    public double MAX_CTRL_ANGLE = 25.0f ;
    public double ANGLE_TO_MAX_AS = 30.0f;
    public double MAX_CTRL_YAW_SPEED = 150.0f;
    public double CTRL_OUTER_INC_LIMIT = 0.5f *MAX_CTRL_ANGLE;
    public double CTRL_INNER_INC_LIMIT = 0.5f *MAX_CTRL_ASPEED;
    public class PID{





            //PID参数
            public class PARAMETER{
                public double[][] outer=new double[3][3];
                public double[][] inner=new double[3][3];
                public double innerFB;
                public double[][] heightctrl=new double[3][3];
                public void PARAMETER(){
                    //姿态外环参数
                    outer[PITCH][KP]=0.8;
                    outer[PITCH][KI]=0.11;
                    outer[PITCH][KD]=2.0;
                    outer[ROLL][KP]=0.8;
                    outer[ROLL][KI]=0.11;
                    outer[ROLL][KD]=2.0;
                    outer[YAW][KP]=1.0;
                    outer[YAW][KI]=0.0;
                    outer[YAW][KD]=0.0;

                    //姿态内环参数
                    inner[PITCH][KP]=0.40;
                    inner[PITCH][KI]=0.0;
                    inner[PITCH][KD]=0.02;
                    inner[ROLL][KP]=0.40;
                    inner[ROLL][KI]=0.0;
                    inner[ROLL][KD]=0.02;
                    inner[YAW][KP]=0.6;
                    inner[YAW][KI]=0.0;
                    inner[YAW][KD]=0.035;
                    innerFB=0.06;

                    //高度环
                    heightctrl[ACC][KP]=0.35;
                    heightctrl[VEL][KP]=2.5;
                    heightctrl[POS][KP]=1.0;
                    heightctrl[POS][KI]=0.10;
                    heightctrl[POS][INC_MAX]=10.0;

                }
            }

            //PID内环
            public class INNER{
                private double[] innerexp = new double[3];//角速度期望值
                private double[]  innererror=new double[3];//角速度误差
                private double[] innererror_last=new double[3];//上次误差
                private double[] innererr_weight=new double[3];//角速度误差权重
                private double[] innererr_dif=new double[3];//角速度误差微分
                private double[] innererr_inc =new double[3];//角速度误差积分
                private double[] innererr_eliminate_i=new double[3];//角速度误差积分分离
                private double[] innerout=new double[3];//角速度PID输出值
                public void INNER(){
//                    innerexp=new double[3];
//                    innererror=new double[3];
//                    innererr_dif=new double[3];
//                    innererr_eliminate_i=new double[3];
//                    innererr_inc =new double[3];
//                    innererr_weight=new double[3];
//                    innererror_last=new double[3];
//                    innerout=new double[3];
                }
                public double[] run(double T){
                    //给定期望角速度
                    innerexp[X] = MAX_CTRL_ASPEED*(outresult[X]/ANGLE_TO_MAX_AS);
                    innerexp[Y] = MAX_CTRL_ASPEED*(outresult[Y]/ANGLE_TO_MAX_AS);
                    innerexp[Z] = MAX_CTRL_ASPEED*(outresult[Z]/ANGLE_TO_MAX_AS);

                    //期望角速度限幅
                    innerexp[X]=LIMIT(innerexp[X],-MAX_CTRL_ASPEED,MAX_CTRL_ASPEED);
                    innerexp[Y]=LIMIT(innerexp[Y],-MAX_CTRL_ASPEED,MAX_CTRL_ASPEED);
                    innerexp[Z]=LIMIT(innerexp[Z],-MAX_CTRL_ASPEED,MAX_CTRL_ASPEED);

                    //得到角速度误差
                    innererror[X]=innerexp[X]-gyro_dps[X];
                    innererror[Y]=innerexp[Y]-gyro_dps[Y];
                    innererror[Z]=innerexp[Z]-gyro_dps[Z];

                    //计算角速度误差权重
                    innererr_weight[X]=Math.abs(innererror[X])/MAX_CTRL_ASPEED;
                    innererr_weight[Y]=Math.abs(innererror[Y])/MAX_CTRL_ASPEED;
                    innererr_weight[Z]=Math.abs(innererror[Z])/MAX_CTRL_YAW_SPEED;

                    //角度误差微分
                    innererr_dif[X]=-pidparameter.inner[ROLL][KD]*(gyro_dps[X]-innererror_last[X])/T;
                    innererr_dif[Y]=-pidparameter.inner[PITCH][KD]*(gyro_dps[Y]-innererror_last[Y])/T;
                    innererr_dif[Z]=-pidparameter.inner[YAW][KD]*(gyro_dps[Z]-innererror_last[Z])/T;

                    //角速度误差积分
                    innererr_inc[X]+=pidparameter.inner[ROLL][KI]*(innererror[X]-innererror_last[X])*T;
                    innererr_inc[Y]+=pidparameter.inner[PITCH][KI]*(innererror[Y]-innererror_last[Y])*T;
                    innererr_inc[Z]+=pidparameter.inner[YAW][KI]*(innererror[Z]-innererror_last[Z])*T;
                    //误差积分分离
                    innererr_eliminate_i[X]=thr.weight*CTRL_OUTER_INC_LIMIT;
                    innererr_eliminate_i[Y]=thr.weight*CTRL_OUTER_INC_LIMIT;
                    innererr_eliminate_i[Z]=thr.weight*CTRL_OUTER_INC_LIMIT;
                    //误差积分限幅
                    innererr_inc[X]=LIMIT(innererr_inc[X],-innererr_eliminate_i[X],innererr_eliminate_i[X]);
                    innererr_inc[Y]=LIMIT(innererr_inc[Y],-innererr_eliminate_i[Y],innererr_eliminate_i[Y]);
                    innererr_inc[Z]=LIMIT(innererr_inc[Z],-innererr_eliminate_i[Z],innererr_eliminate_i[Z]);

                    //角速度PID输出
                    innerout[X]=3*(pidparameter.innerFB*LIMIT((0.45+0.55*innererr_weight[X]),0,1)*pidouter.exp[X]
                            +(1-pidparameter.innerFB)*pidparameter.inner[ROLL][KP]*(innererror[X]+innererr_dif[X]+innererr_inc[X]));
                    innerout[Y]=3*(pidparameter.innerFB*LIMIT((0.45+0.55*innererr_weight[Y]),0,1)*pidouter.exp[Y]
                            +(1-pidparameter.innerFB)*pidparameter.inner[PITCH][KP]*(innererror[Y]+innererr_dif[Y]+innererr_inc[Y]));
                    innerout[Z]=3*(pidparameter.innerFB*LIMIT((0.45+0.55*innererr_weight[Z]),0,1)*pidouter.exp[Z]
                            +(1-pidparameter.innerFB)*pidparameter.inner[YAW][KP]*(innererror[Z]+innererr_dif[Z]+innererr_inc[Z]));

                    //内环输出限幅
                    innerout[X]=LIMIT(innerout[X],-MAX_INNER_OUT,MAX_INNER_OUT);
                    innerout[Y]=LIMIT(innerout[Y],-MAX_INNER_OUT,MAX_INNER_OUT);
                    innerout[Z]=LIMIT(innerout[Z],-MAX_INNER_OUT,MAX_INNER_OUT);

                    //记录历史数据
                    innererror_last[X]=gyro_dps[X];
                    innererror_last[Y]=gyro_dps[Y];
                    innererror_last[Z]=gyro_dps[Z];

                    return innerout;
                }

        }

            //PID外环
            double[] outresult = new double[3];
            public class OUTER{
                private double[] exp = new double[3];//角度期望值
                private double[] error = new double[3];//角速度误差
                private double[] error_last=new double[3];//上次误差
                private double[] err_weight=new double[3];//角速度误差权重
                private double[] err_dif = new double[3];//角速度误差微分
                private double[] err_inc =new double[3];//角速度误差积分
                private double[] err_eliminate_i = new double[3];//角速度误差积分分离
                private double[] out = new double[3];//角速度PID输出值
                public void OUTER(){
////                    exp=new double[3];
//                    error=new double[3];
//                    err_dif=new double[3];
//                    err_eliminate_i=new double[3];
//                    err_inc =new double[3];
//                    err_weight=new double[3];
//                    error_last=new double[3];
//                    out=new double[3];
                }
                public double[] run(double T){
                    if (thr.value>240) exp[Z]+=-RC[2]*0.0006;
                    if (exp[Z]>180) exp[Z]-=360;
                    if (exp[Z]<-180) exp[Z]+=360;

                    exp[X]=RC[0]*MAX_CTRL_ANGLE/660;
                    exp[Y]=RC[1]*MAX_CTRL_ANGLE/660;

                    //得到角度误差
                    error[X]=exp[X]-orientation[ROLL];
                    error[Y]=exp[Y]-orientation[PITCH];
                    error[Z]=exp[Z]-orientation[YAW];

                    //计算角度误差权重
                    err_weight[X]=Math.abs(error[X])/ANGLE_TO_MAX_AS;
                    err_weight[Y]=Math.abs(error[Y])/ANGLE_TO_MAX_AS;
                    err_weight[Z]=Math.abs(error[Z])/ANGLE_TO_MAX_AS;

                    //角度误差微分
                    err_dif[X]=10*pidparameter.outer[ROLL][KD]*(-orientation[ROLL] -error_last[X]+RCDIF[0]*T*MAX_CTRL_ANGLE/600)*(0.005/T)*( 0.65f + 0.35f *err_weight[Z] );
                    err_dif[Y]=10*pidparameter.outer[PITCH][KD]*(-orientation[PITCH]-error_last[Y]+RCDIF[1]*T*MAX_CTRL_ANGLE/600)*(0.005/T)*( 0.65f + 0.35f *err_weight[Z] );
                    err_dif[Z]=10*pidparameter.outer[YAW][KD]*(error[Z]-error_last[Z])*(0.005/T)*( 0.65f + 0.35f *err_weight[Z] );

                    //角度误差积分
                    err_inc[X]+=pidparameter.outer[ROLL][KI]*error[X]*T;
                    err_inc[Y]+=pidparameter.outer[PITCH][KI]*error[Y]*T;
                    err_inc[Z]+=pidparameter.outer[YAW][KI]*error[Z]*T;
                    //误差积分分离
                    err_eliminate_i[X]=thr.weight*CTRL_OUTER_INC_LIMIT;
                    err_eliminate_i[Y]=thr.weight*CTRL_OUTER_INC_LIMIT;
                    err_eliminate_i[Z]=thr.weight*CTRL_OUTER_INC_LIMIT;
                    //误差积分限幅
                    err_inc[X]=LIMIT(err_inc[X],-err_eliminate_i[X],err_eliminate_i[X]);
                    err_inc[Y]=LIMIT(err_inc[Y],-err_eliminate_i[Y],err_eliminate_i[Y]);
                    err_inc[Z]=LIMIT(err_inc[Z],-err_eliminate_i[Z],err_eliminate_i[Z]);

                    //用于计算比例项输出的角度误差限幅
                    error[X]=LIMIT(error[X],-90,90);
                    error[Y]=LIMIT(error[Y],-90,90);
                    error[Z]=LIMIT(error[Z],-90,90);

                    //PID输出
                    out[X]=pidparameter.outer[ROLL][KP]*(error[X]+err_dif[X]+err_inc[X]);
                    out[Y]=pidparameter.outer[PITCH][KP]*(error[Y]+err_dif[Y]+err_inc[Y]);
                    out[Z]=pidparameter.outer[YAW][KP]*(error[Z]+err_dif[Z]+err_inc[Z]);

                    //记录历史数据
                    error_last[X]=-orientation[ROLL];
                    error_last[Y]=-orientation[PITCH];
                    error_last[Z]=-orientation[YAW];
                    outresult = out;
                    return out;
                }

        }

            //PID高度环
            private static final byte HEIGHT_EXP=0;
            private static final byte HEIGHT_ERR=1;
            private static final byte HEIGHT_ERR_LAST=2;
            private static final byte HEIGHT_ERR_DIF=3;
            private static final byte HEIGHT_ERR_INC=4;
            private static final byte HEIGHT_INC_MAX=5;
            private static final byte HEIGHT_OUT_P=6;
            private static final byte HEIGHT_OUT_I=7;
            private static final byte HEIGHT_OUT_D=8;
            private static final byte HEIGHT_OUT=9;
            private static final byte HEIGHT_KP=10;
            private static final byte HEIGHT_KI=11;
            private static final byte HEIGHT_KD=12;

            public class HEIGHTCTRL{

                public double[][] HgtCtrl=new double[3][13];
                public void Height_Acceleration_Control(double T){
                    //待改动
                    //数值加速度低通滤波
                    double accel_ef_lpf = mFilter.LPF2pApply_1((float) Z_EF[ACCZ][BASE]); //  待改动
                    HgtCtrl[ACC][HEIGHT_OUT_P] = -accel_ef_lpf * pidparameter.heightctrl[ACC][KP];
                    HgtCtrl[ACC][HEIGHT_OUT] = HgtCtrl[ACC][HEIGHT_OUT_P];
                }

                public void Height_Velocity_Control(double T)
                {
                    HgtCtrl[VEL][HEIGHT_ERR] = HgtCtrl[POS][HEIGHT_OUT] - Z_EF[VELZ][BASE];
                    HgtCtrl[VEL][HEIGHT_ERR] = LIMIT(HgtCtrl[VEL][HEIGHT_ERR],-150.0,150.0);
                    HgtCtrl[VEL][HEIGHT_OUT_P] =  HgtCtrl[VEL][HEIGHT_ERR] * pidparameter.heightctrl[VEL][KP];
                    HgtCtrl[VEL][HEIGHT_OUT] = HgtCtrl[VEL][HEIGHT_OUT_P];
                }
                public void Height_Position_Control(double T){
                    HgtCtrl[POS][HEIGHT_OUT_I] = 0;//
                    HgtCtrl[POS][HEIGHT_OUT] = (thr.value-450)*0.5f;
                    HgtCtrl[POS][HEIGHT_OUT] = LIMIT( HgtCtrl[POS][HEIGHT_OUT],-250.0f,250.0f);
                }
            }


/**
 * 以下为未完成的
 *
 * */


            //惯性导航
            private static final double InertialGravity = 972.665;

            private static final byte ACCZ=0;
            private static final byte VELZ=1;
            private static final byte POSZ=2;
            private static final byte CORRECTION=0;
            private static final byte INCREASE=1;
            private static final byte BASE=2;
            public double[][] Z_EF=new double[3][3];
            public class InertialNavigation{
                public void Update_Velocities_Positions_Z(double T){
                    //获得地球坐标系下竖直方向上的加速度
                    Z_EF[ACCZ][BASE] = 100*getVerticalAcceleration() - InertialGravity;//待改动
                    Z_EF[VELZ][INCREASE] = Z_EF[ACCZ][BASE] * T;//删除了气压校准
                    Z_EF[POSZ][BASE] += (Z_EF[VELZ][BASE]  + Z_EF[VELZ][INCREASE]*0.5f) * T;
                    Z_EF[VELZ][BASE] += Z_EF[VELZ][INCREASE];
                }
            }


            public PARAMETER pidparameter=new PARAMETER();
            public INNER pidinner=new INNER();
            public OUTER pidouter=new OUTER();
            public HEIGHTCTRL pidhgtctrl=new HEIGHTCTRL();
            public InertialNavigation pidinertialnav = new InertialNavigation();




    }
    public PID pid=new PID();


    //遥控器相关

    public short[] RC=new short[6];
    public static short[] last_RC=new short[6];
    public double[] RCDIF=new double[6];
    public void getRcData(short[] rc,boolean lock){
        double T = getTimeInterval(6);
        RC = rc;
        RCDIF[0] = Math.min((RC[0] - last_RC[0])/T , 1000);
        RCDIF[1] = Math.min((RC[1] - last_RC[1])/T , 1000);
        RCDIF[2] = Math.min((RC[2] - last_RC[2])/T , 1000);
        RCDIF[3] = Math.min((RC[3] - last_RC[3])/T , 1000);
        last_RC = RC;
        Unlock = lock;
    }

}


