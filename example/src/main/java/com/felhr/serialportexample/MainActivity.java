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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.sqrt;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RecursiveAction;

import java.math.* ;


public class MainActivity extends AppCompatActivity {

    private SensorManager sm;
    private Sensor GyroSensor,GravitySensor,PressureSensor,AccelerationSensor,OrientationSensor;
    private StringBuffer sb;
    private TextView tvValue;
    //Start Flag:start transmission
    boolean StartFlag;
    boolean CalOffsetFlag;
    //Raw data from sensors
//    public float[] gravity = new float[3];
//    public float[] acc=new float[3];
//    public float[] acceleration=new float[3];


//    public float baro;

    //Treatment of accelemeter
    int filterBuffer=100;
    public float [][] acc_ftmp=new float[3][filterBuffer];
    public float [] acc_f=new float[3];
//    public void acc_filter(){
//        int cnt;
//        float tmpsumx=0,tmpsumy=0,tmpsumz=0;
//        for(cnt=filterBuffer-1;cnt>0;cnt--){
//            acc_ftmp[0][cnt]=acc_ftmp[0][cnt-1];
//            acc_ftmp[1][cnt]=acc_ftmp[1][cnt-1];
//            acc_ftmp[2][cnt]=acc_ftmp[2][cnt-1];
//        }
//        acc_ftmp[0][0]=acc[0];
//        acc_ftmp[1][0]=acc[1];
//        acc_ftmp[2][0]=acc[2];
//        for(cnt=0;cnt<filterBuffer;cnt++){
//            tmpsumx+=acc_ftmp[0][cnt];
//            tmpsumy+=acc_ftmp[1][cnt];
//            tmpsumz+=acc_ftmp[2][cnt];
//        }
//        acc_f[0]=tmpsumx/filterBuffer;
//        acc_f[1]=tmpsumy/filterBuffer;
//        acc_f[2]=tmpsumz/filterBuffer;
//    }
    //Treatment of gyrometer
    public float[] gyro_raw=new float[3];
    public float[] gyro=new float[3];
    public float[] gyro_offset=new float[3];
    public float [][] gyro_ftmp=new float[3][100];
    public float [] gyro_f=new float[3];
    public float [] gyro_f_dps=new float[3];
    public void gyro_filter(){
        int cnt;
        float tmpsumx=0,tmpsumy=0,tmpsumz=0;
        for(cnt=99;cnt>0;cnt--){
            gyro_ftmp[0][cnt]=gyro_ftmp[0][cnt-1];
            gyro_ftmp[1][cnt]=gyro_ftmp[1][cnt-1];
            gyro_ftmp[2][cnt]=gyro_ftmp[2][cnt-1];
        }
        gyro_ftmp[0][0]=gyro[0];
        gyro_ftmp[1][0]=gyro[1];
        gyro_ftmp[2][0]=gyro[2];
        for(cnt=0;cnt<100;cnt++){
            tmpsumx+=gyro_ftmp[0][cnt];
            tmpsumy+=gyro_ftmp[1][cnt];
            tmpsumz+=gyro_ftmp[2][cnt];
        }
        gyro_f[0]=tmpsumx/100;
        gyro_f[1]=tmpsumy/100;
        gyro_f[2]=tmpsumz/100;
    }
    public void gyro_to_dps(){
        gyro_f_dps[0]=((float)57.295)*gyro_f[0];
        gyro_f_dps[1]=((float)57.295)*gyro_f[1];
        gyro_f_dps[2]=((float)57.295)*gyro_f[2];
    }
    //Treatment of orientation
    public float[] orientation=new float[3];
    public float[] orientation_offset=new float[3];
    public float[] orientation_raw=new float[3];

    public void push(){

    }
    public void getOrientationOffset(){
        orientation_offset[0]=orientation_raw[0];
        orientation_offset[1]=orientation_raw[1];
        orientation_offset[2]=orientation_raw[2];
    }
    public void getOrientation(){
        orientation[0]=orientation_raw[0]-orientation_offset[0];
        float tmp=-orientation_raw[1]+orientation_offset[1];
        if(tmp>180)
            orientation[1]=tmp-360.0f;
        else
            orientation[1]=tmp;
        orientation[2]=orientation_raw[2]-orientation_offset[2];
    }

    public void limit(){

    }
    //Treatment of barometer
    public Barometer height=new Barometer();
    public double dHeight,OriginHeight;
    public String stmpH;
    public String s;
    //Transmission Buffer
    public byte[] TxBuffer=new byte[32];

    public TextView textviewGyro,textviewAcceleration,textviewBaro,textviewDuty;
//    public SeekBar seekbarDuty;
    public Button sendButton;
    public Button buttonCalOffset;



    //Schedule
    public Timer MyTimer=new Timer();
    public TimerTask Task_1000Hz=new TimerTask() {
        @Override
        public void run() {

        }
    };
    public TimerTask Task_500Hz=new TimerTask() {
        @Override
        public void run() {
            testTxPrepare();
            UsbSendIMU();
        }
    };
    public TimerTask Task_200Hz=new TimerTask() {
        @Override
        public void run() {
//            gyro_filter();
//            gyro_to_dps();
//            getOrientation();
//            TxPrepare();
//            UsbSendIMU();
            testTxPrepare();
            UsbSendIMU();
        }
    };
    public TimerTask Task_100Hz=new TimerTask() {
        @Override
        public void run() {

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
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
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
                if(!CalOffsetFlag) sendButton.setText("Offset Not Cal!");
                else {
                    StartFlag=true;
                    MySchedule();
                    sendButton.setText("Running!");
                }
            }
        });
        buttonCalOffset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrientationOffset();
//                if(orientation_offset[0]>10.0|orientation_offset[1]>10|orientation_offset[2]>10|orientation_offset[0]<-10|orientation_offset[1]<-10|orientation_offset[2]<-10)
//                    buttonCalOffset.setText("Fail!Retry,plz");
//                else
                    CalOffsetFlag=true;

            }
        });
        //MyTimer.schedule(PrintHeight,100,200);
//        seekbarDuty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            /*
//            * seekbar改变时的事件监听处理
//            * */
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                textviewDuty.setText("Duty is "+progress+"%");
//                byte[] tmp=new byte[1];
////                tmp[0]=(byte)progress;
////                if (usbService != null) usbService.write(tmp);
////                else Toast.makeText(MainActivity.this,"No USB Device Connected",Toast.LENGTH_SHORT).show();
//
//                //Log.d("debug",String.valueOf(seekBar.getId()));
//            }
//            /*
//            * 按住seekbar时的事件监听处理
//            * */
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                //Toast.makeText(MainActivity.this,"按住seekbar",Toast.LENGTH_SHORT).show();
//            }
//            /*
//            * 放开seekbar时的时间监听处理
//            * */
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                //Toast.makeText(MainActivity.this,"放开seekbar",Toast.LENGTH_SHORT).show();
//            }
//        });

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

    public void TxPrepare(){
        byte[] tmp;
        //tmp=float2byte(acc_f[0]);
        //set the first as 1.2333 for test
        tmp=float2byte(orientation[0]);
        tmp=ByteArraryMerge(tmp,float2byte(orientation[1]));
        tmp=ByteArraryMerge(tmp,float2byte(orientation[2]));
//        tmp=ByteArraryMerge(tmp,float2byte(gyro_f[0]));
//        tmp=ByteArraryMerge(tmp,float2byte(gyro_f[1]));
//        tmp=ByteArraryMerge(tmp,float2byte(gyro_f[2]));
        tmp=ByteArraryMerge(tmp,float2byte(gyro_f_dps[0]));
        tmp=ByteArraryMerge(tmp,float2byte(gyro_f_dps[1]));
        tmp=ByteArraryMerge(tmp,float2byte(gyro_f_dps[2]));
        TxBuffer=tmp;
    }
    public void testTxPrepare(){
        byte[] tmp;
        //tmp=float2byte(acc_f[0]);
        //set the first as 1.2333 for test
        tmp=float2byte(1.0f);
        tmp=ByteArraryMerge(tmp,float2byte(2.0f));
        tmp=ByteArraryMerge(tmp,float2byte(3.0f));
//        tmp=ByteArraryMerge(tmp,float2byte(gyro_f[0]));
//        tmp=ByteArraryMerge(tmp,float2byte(gyro_f[1]));
//        tmp=ByteArraryMerge(tmp,float2byte(gyro_f[2]));
        tmp=ByteArraryMerge(tmp,float2byte(4.0f));
        tmp=ByteArraryMerge(tmp,float2byte(5.0f));
        tmp=ByteArraryMerge(tmp,float2byte(6.0f));
        TxBuffer=tmp;
    }
    public void test(){
        byte[][] tmp=new byte[6][4];
        int i,j,cnt=0;
        for(i=0;i<6;i++) for (j=0;j<4;j++) tmp[i][j]=TxBuffer[cnt++];
        float[] f=new float[6];
        for (i=0;i<6;i++) f[i]= byte2float(tmp[i],0);
        String stmp="\tacc\tx\ty\tz\n"+"\t"+f[0]+"\t"+f[1]+"\t"+f[2]+"\n"+
                    "\tgyro\tx\ty\tz\n"+"\t"+f[3]+"\t"+f[4]+"\t"+f[5]+"\n";
        System.out.println(stmp);
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
        //textviewDuty=(TextView)findViewById(R.id.textViewDuty);
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
//        //Pressure
//        PressureSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
//        sm.registerListener(new MySensorListener(), PressureSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Gyroscope
        GyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(new MySensorListener(), GyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Gravity
//        GravitySensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
//        sm.registerListener(new MySensorListener(), GravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Acceleration
//        AccelerationSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        sm.registerListener(new MySensorListener(), AccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    public void InitialonCreate(){
        TextView_Config();
        Sensor_Config();
        StartFlag=false;
        CalOffsetFlag=false;
        //Initiallization of send button
        sendButton = (Button) findViewById(R.id.buttonSend);
        buttonCalOffset = (Button) findViewById(R.id.buttonCalOffset);

    }
    public void MySchedule(){
        if(StartFlag){
            MyTimer.schedule(Task_1000Hz,0,1);
            MyTimer.schedule(Task_500Hz,0,2);
            MyTimer.schedule(Task_200Hz,0,5);
            MyTimer.schedule(Task_100Hz,0,10);
        }
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
                    String stmp=rawdata[0]+"*\n";
                    mActivity.get().display.append(stmp);
//                    byte[] raw=rawdata.getBytes();
//                    if (mActivity.get().flag){
//                        mActivity.get().flag=false;
//                        tmp[1]=raw[0];
//                        int decode=0;
//                        if (tmp[0]!=0)
//                            decode+=128;
//
//
//                        decode+=tmp[1];
//                        mActivity.get().display.append("Data recieved is "+decode+"\n");
//                    }
//                    else {
//                        mActivity.get().flag=true;
//                        tmp[0]=raw[0];
//                    }
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
    public class MySensorListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent event) {
            String tmp="";
            switch (event.sensor.getType())
            {
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    break;
                case Sensor.TYPE_PRESSURE:
//                    baro = event.values[0];
//                    //tmp="Height is\n"+dHeight+"\n";
//                    //textviewBaro.setText(tmp);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyro[0]= event.values[0];
                    gyro[1]=event.values[1];
                    gyro[2]=event.values[2];
                    break;
                case Sensor.TYPE_ORIENTATION:
                    orientation_raw[0]= event.values[0];
                    orientation_raw[1]=event.values[1];
                    orientation_raw[2]=event.values[2];
//                    textviewGyro.setText(orientation[0]+"\n"+orientation[1]+"\n"+orientation[2]+"\n");
                    break;
            }
        }

    }
}