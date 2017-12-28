package com.felhr.serialportexample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.concurrent.RecursiveAction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import java.math.* ;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    public flightctrl mFC = new flightctrl();

    private SensorManager sm;
    private Sensor GyroSensor,GravitySensor,PressureSensor,AccelerationSensor,OrientationSensor,RotationVectorSensor;
    private StringBuffer sb;
    private TextView tvValue;
    //Start Flag:start transmission
    boolean StartFlag;
    boolean CalOffsetFlag;
    public MyTestClass1 csv=new MyTestClass1();
    //Raw data from sensors
    public float[] acc=new float[3];
//    public float[] acceleration=new float[3];


//    public float baro;

    //Treatment of accelemeter
    int filterBuffer=100;
    public float [][] acc_ftmp=new float[3][filterBuffer];
    public float [] acc_f=new float[3];
    public void acc_filter(){
        int cnt;
        float tmpsumx=0,tmpsumy=0,tmpsumz=0;
        for(cnt=filterBuffer-1;cnt>0;cnt--){
            acc_ftmp[0][cnt]=acc_ftmp[0][cnt-1];
            acc_ftmp[1][cnt]=acc_ftmp[1][cnt-1];
            acc_ftmp[2][cnt]=acc_ftmp[2][cnt-1];
        }
        acc_ftmp[0][0]=acc[0];
        acc_ftmp[1][0]=acc[1];
        acc_ftmp[2][0]=acc[2];
        for(cnt=0;cnt<filterBuffer;cnt++){
            tmpsumx+=acc_ftmp[0][cnt];
            tmpsumy+=acc_ftmp[1][cnt];
            tmpsumz+=acc_ftmp[2][cnt];
        }
        acc_f[0]=tmpsumx/(float) filterBuffer;
        acc_f[1]=tmpsumy/(float)filterBuffer;
        acc_f[2]=tmpsumz/(float)filterBuffer;
    }
    //Treatment of gyrometer
    public float[] gyro_raw=new float[3];
    public float[] gyro=new float[3];
    public float[] gyro_offset=new float[3];
    public float [][] gyro_ftmp=new float[3][100];
    public float [] gyro_f=new float[3];
    public float [] gyro_dps=new float[3];
    public void gyro_to_dps(){
        gyro_dps[0]=((float)57.295)*gyro[0];
        gyro_dps[1]=((float)57.295)*gyro[1];
        gyro_dps[2]=((float)57.295)*gyro[2];
    }
    //Treatment of orientation
    public float[] orientation=new float[3];
    public float[] orientation_offset=new float[3];
    public float[] orientation_raw=new float[3];

    public void getOrientationOffset(){
        orientation_offset[0]=orientation_raw[0];
        orientation_offset[1]=orientation_raw[1];
        orientation_offset[2]=orientation_raw[2];
    }


    //Treatment of barometer

    //Transmission Buffer
    public byte[] TxBuffer=new byte[32];

    public TextView textviewGyro,textviewAcceleration,textviewBaro,textviewDuty;
    //    public SeekBar seekbarDuty;
    public Button sendButton,sendacc;
    public Button buttonCalOffset;



    //Schedule
    public Timer MyTimer=new Timer();
    public boolean FreFlag;
    public short[] MotorRPM = new short[4];
    public void FCLoop(){
        int tmpi;
        for (tmpi=0;tmpi<10;tmpi++) mFC.getTimeInterval(tmpi);
        MyTimer.schedule(Task_Schedule,0,1);
    }
    public TimerTask Task_Schedule = new TimerTask() {
        @Override
        public void run() {
            cnt_20hz++;
            cnt_50hz++;
            cnt_100hz++;
            cnt_200hz++;
            cnt_500hz++;
            cnt_1000hz++;
            MySchedule();
        }
        private int cnt_1000hz, cnt_500hz, cnt_200hz, cnt_100hz, cnt_50hz, cnt_20hz;
        public void MySchedule(){

            if (cnt_1000hz > 0){
                Task_1000Hz();
                cnt_1000hz = 0;
            }

            if (cnt_500hz > 1){
                Task_500Hz();
                cnt_500hz = 0;
            }

            if (cnt_200hz > 4){
                Task_200Hz();
                cnt_200hz = 0;
            }

            if (cnt_100hz > 9){
                Task_100Hz();
                cnt_100hz = 0;
            }

            if (cnt_50hz > 19){
                Task_50Hz();
                cnt_100hz = 0;
            }

            if (cnt_20hz > 49){
                Task_20Hz();
                short[] rcdata = {0,0,0,200};
                mFC.getRcData(rcdata,true);
                cnt_100hz = 0;
            }

        }
        public void Task_1000Hz(){

            mFC.getRotationVector(RotationVector);
            mFC.getAcc(acc);
            mFC.getOrientation(orientation_raw);
            mFC.getGyro(gyro_dps,gyro);
            Log.e(TAG, "Task_1000Hz");
        }
        public void Task_500Hz(){

            double loop_time_500hz = mFC.getTimeInterval(0);
            mFC.pid.pidinner.run(loop_time_500hz);
            MotorRPM = mFC.output();
            Log.e(TAG, "Task_500Hz");
        }
        public void Task_200Hz(){
            double loop_time_200hz = mFC.getTimeInterval(1);
            mFC.pid.pidouter.run(loop_time_200hz);
            mFC.pid.pidinertialnav.Update_Velocities_Positions_Z(loop_time_200hz);
            Log.e(TAG, "Task_200Hz");
        }
        public void Task_100Hz(){
            double loop_time_100hz = mFC.getTimeInterval(2);
            mFC.pid.pidhgtctrl.Height_Acceleration_Control(loop_time_100hz);
            Log.e(TAG, "Task_100Hz");
        }
        public void Task_50Hz(){
            double loop_time_50hz = mFC.getTimeInterval(3);
            mFC.pid.pidhgtctrl.Height_Velocity_Control(loop_time_50hz);
            Log.e(TAG, "Task_50Hz");
        }
        public void Task_20Hz(){
            double loop_time_20hz = mFC.getTimeInterval(4);
            mFC.pid.pidhgtctrl.Height_Position_Control(loop_time_20hz);
            Log.e(TAG, "Task_20Hz");
//            Log.e(TAG, "is" + MotorRPM[0] +" "+ MotorRPM[1] + " "+MotorRPM[2] + " "+ MotorRPM[3]);
//            Log.e(TAG, "angle" + mFC.orientation[0] +" "+ mFC.orientation[1] + " "+mFC.orientation[2]);
        }
    };




    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "UAV Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No UAV connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "UAV disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    public EditText editText;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);
        InitialonCreate();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csv.flush();
                if(!CalOffsetFlag) sendButton.setText("Offset Not Cal!");
                else {

                    if (!StartFlag){
                        StartFlag=true;
                        FreFlag=false;

                        sendButton.setText("Running!");

                    }
                }
            }
        });
        sendacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //csv.flush();
                if(!CalOffsetFlag) sendButton.setText("Offset Not Cal!");
                else {
                    StartFlag=true;
                    accflag=true;
                    FreFlag=false;
                    FCLoop();
                    sendacc.setText("ACC Running!");
                    TxPrepare();
                    UsbSendIMU();
                    // MySchedule();

                }
            }
        });
        buttonCalOffset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrientationOffset();
                byte[] BeginSignal = {(byte)0xff,(byte)0xaa,(byte)0xaa,(byte)0xaa,(byte)0xaa,(byte)0xaa,(byte)0xaa,(byte)0xaa,(byte)0xaa};
                if (usbService!=null)
                    usbService.write(BeginSignal);
                CalOffsetFlag=true;

            }
        });






    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    public void UsbSendIMU(){
        if (usbService!=null)
            usbService.write(TxBuffer);
    }
    public boolean accflag;
    public void TxPrepare(){
        byte[] tmp ={(byte)0xaa};
        TxBuffer= tmp; //new String("abcdefghijklmnopqrstuvwxyz").getBytes();
        Log.e(TAG, "TxPrepare: " + TxBuffer.length);
    }



    public byte[] ByteArraryMerge(byte[] a,byte[] b){
        byte[] tmp=new byte[a.length+b.length];
        System.arraycopy(a,0,tmp,0,a.length);
        System.arraycopy(b,0,tmp,a.length,b.length);
        return tmp;
    }
    public  byte[] float2byte(float f) {

        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        // 翻转数组
        int len = b.length;
        // 建立一个与源数组元素类型相同的数组
        byte[] dest = new byte[len];
        // 为了防止修改源数组，将源数组拷贝一份副本
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        // 将顺位第i个与倒数第i个交换
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;

    }
    public  float byte2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }
    public void TextView_Config(){
        //Initialization of textview widgets

        textviewBaro=(TextView) findViewById(R.id.textviewGravity);
        textviewAcceleration=(TextView) findViewById(R.id.textviewAcceleration);
        textviewGyro=(TextView) findViewById(R.id.textviewGyro);
        display=(TextView)findViewById(R.id.textView1);
    }
    public void Sensor_Config(){
        //Configuration of Sensors


        //获取SensorManager对象
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        //获取Sensor对象
        //Orientation
        OrientationSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sm.registerListener(new MySensorListener(), OrientationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //RotationVector
        RotationVectorSensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sm.registerListener(new MySensorListener(), RotationVectorSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Gyroscope
        GyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(new MySensorListener(), GyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Acceleration
        AccelerationSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(new MySensorListener(), AccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }
    public void InitialonCreate(){
        TextView_Config();
        Sensor_Config();
        StartFlag=false;
        CalOffsetFlag=false;
        //Initiallization of send button
        sendButton = (Button) findViewById(R.id.buttonSend);
        sendacc = (Button) findViewById(R.id.send_acc);
        buttonCalOffset = (Button) findViewById(R.id.buttonCalOffset);

    }


    public long[] timinterval=new long[3];
    byte NOW=1,OLD=0,NEW=2;
    double getTimeInterval(){
        timinterval[OLD]=timinterval[NOW];
        timinterval[NOW]=System.nanoTime();
        timinterval[NEW]= timinterval[NOW] - timinterval[OLD] ;
        return (double)timinterval[NEW]/1000000000.0;
    }




    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        //solve msgs from uart
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    int[] test=new int[200];
                    byte[] rawdata = (byte[]) msg.obj;
//                    short shorttmp = (short) ((rawdata[0]<<8) | rawdata[1]);
                    Log.e(TAG, "ch1 is" + rawdata[0] +"  " +rawdata.length);
                    //if (rawdata.length != 0) Log.e(TAG, "handleMessage: "+rawdata[0] );
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
    //Acquire Sensor Data
    public float[] RotationVector = new float[4];
    public class MySensorListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent event) {
            String tmp="";
            switch (event.sensor.getType())
            {
                case Sensor.TYPE_ACCELEROMETER:
                    acc[0]= event.values[0];
                    acc[1]=event.values[1];
                    acc[2]=event.values[2];
//                    csv.writeCsv(acc[0]+"",acc[1]+"",acc[2]+"");

                    break;
                case Sensor.TYPE_ROTATION_VECTOR:

                    RotationVector[0] = event.values[0];
                    RotationVector[1] = event.values[1];
                    RotationVector[2] = event.values[2];
                    RotationVector[3] = event.values[3];

                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyro[0]= event.values[0];
                    gyro[1]=event.values[1];
                    gyro[2]=event.values[2];
                    gyro_to_dps();

                    break;
                case Sensor.TYPE_ORIENTATION:
                    orientation_raw[0]= event.values[0];
                    orientation_raw[1]=event.values[1];
                    orientation_raw[2]=event.values[2];
                    break;
            }
        }

    }


    public class MyTestClass1 {

        public static final String mComma = ",";
        private  StringBuilder mStringBuilder = null, macc, mgyro = null;
        private  String mFileName = null,acc =null,gyro = null;
        String path;
        public  void open() {
            String folderName = null;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                path = Environment.getExternalStorageDirectory().getAbsolutePath();

                if (path != null) {
                    folderName = path ;//+"/CSV/";
                }
            }

            File fileRobo = new File(folderName);
            if(!fileRobo.exists()){
                fileRobo.mkdir();
            }
            mFileName =  "acc.csv";
            gyro = "gyro.csv";
            mgyro = new StringBuilder();
            mgyro.append("gyrox");
            mgyro.append(mComma);
            mgyro.append("gyroy");
            mgyro.append(mComma);
            mgyro.append("gyroz");
//            mStringBuilder.append(mComma);
//            mStringBuilder.append("gyrox");
//            mStringBuilder.append(mComma);
//            mStringBuilder.append("gyroy");
//            mStringBuilder.append(mComma);
//            mStringBuilder.append("gyroz");
            mgyro.append("\n");
            mStringBuilder = new StringBuilder();
            mStringBuilder.append("accx");
            mStringBuilder.append(mComma);
            mStringBuilder.append("accy");
            mStringBuilder.append(mComma);
            mStringBuilder.append("accz");
//            mStringBuilder.append(mComma);
//            mStringBuilder.append("gyrox");
//            mStringBuilder.append(mComma);
//            mStringBuilder.append("gyroy");
//            mStringBuilder.append(mComma);
//            mStringBuilder.append("gyroz");
            mStringBuilder.append("\n");
        }

        public void writeCsv(String value1, String value2, String value3) {
            mStringBuilder.append(value1);
            mStringBuilder.append(mComma);
            mStringBuilder.append(value2);
            mStringBuilder.append(mComma);
            mStringBuilder.append(value3);
            mStringBuilder.append("\n");
        }

        public void writegyro(String value1, String value2, String value3) {
            mgyro.append(value1);
            mgyro.append(mComma);
            mgyro.append(value2);
            mgyro.append(mComma);
            mgyro.append(value3);
            mgyro.append("\n");
        }
        public  void flush() {
            int cnt23=0;
            if (mFileName != null) {
                try {
                    File file = new File(path,mFileName);
                    File file1 = new File(path,gyro);
//                    File file2 = new File(path,mFileName);
                    FileOutputStream fos = new FileOutputStream(file, false);
                    FileOutputStream fos1 = new FileOutputStream(file1, false);
                    if(fos != null)
                        cnt23=1;
                    fos.write(mStringBuilder.toString().getBytes());
                    fos.flush();
                    fos.close();
                    if(mgyro != null)
                    {
                        fos1.write(mgyro.toString().getBytes());
                        fos1.flush();
                        fos1.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new RuntimeException("You should call open() before flush()");
            }
        }
    }
}