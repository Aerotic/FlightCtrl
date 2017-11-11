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

public class MainActivity extends AppCompatActivity {

    SensorManager mSensorManager;
    Sensor mSensor;
    float a,b,c;
    static float p;
    private SensorManager sm;
    private Sensor GyroSensor,GravitySensor,PressureSensor,AccelerationSensor,OrientationSensor;
    private StringBuffer sb;
    private TextView tvValue;
    public boolean flag;
    //Raw data from sensors
    public float[] gravity = new float[3];
    public float[] acceleration=new float[3];
    public float[] gyro=new float[3];
    public float[] orientation=new float[3];
    public float baro;
    float oldacc_x;
    public KalmanFilter k;
    public TextView textviewGyro,textviewAcceleration,textviewBaro,textviewDuty;
    public SeekBar seekbarDuty;
    public float[] acc=new float[3];
    public UsbDecode dbus;
    public Timer MyTimer=new Timer();

    public TimerTask SendSensorTask=new TimerTask() {
        @Override
        public void run() {
            if (usbService != null){
                UsartSendFloat();
            }
        }
    };
    public TimerTask SendMotorSpeedTask=new TimerTask() {
        @Override
        public void run() {
            if (usbService != null){
                //usbService.write();
            }
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
    byte[] hextest=new byte[1];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);
        TextView_Config();
        Sensor_Config();
        //Initiallization of send button
        Button sendButton = (Button) findViewById(R.id.buttonSend);
        //Initialization of seekbar
        seekbarDuty=(SeekBar)findViewById(R.id.seekBarDuty);

       // usbService.write("Initialization Successful".getBytes());
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (usbService != null){

                    MyTimer.schedule(SendSensorTask,0,5);
                }

            }
        });
       // seekbarDuty.setOnSeekBarChangeListener();
        seekbarDuty.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*
            * seekbar改变时的事件监听处理
            * */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textviewDuty.setText("Duty is "+progress+"%");
                byte[] tmp=new byte[1];
//                tmp[0]=(byte)progress;
//                if (usbService != null) usbService.write(tmp);
//                else Toast.makeText(MainActivity.this,"No USB Device Connected",Toast.LENGTH_SHORT).show();

                //Log.d("debug",String.valueOf(seekBar.getId()));
            }
            /*
            * 按住seekbar时的事件监听处理
            * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this,"按住seekbar",Toast.LENGTH_SHORT).show();
            }
            /*
            * 放开seekbar时的时间监听处理
            * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(MainActivity.this,"放开seekbar",Toast.LENGTH_SHORT).show();
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

    public void UsartSendFloat(){
        //String stmp=acc[0]+"\n";
        byte[] tmp=float2byte(baro);
        byte[] test=new byte[4];
        test[0]=(byte)0xaa;
        test[1]=(byte)0xaa;
        test[2]=0;
        test[3]=0;
        if (usbService!=null) usbService.write(tmp);


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
    public void TextView_Config(){
        //Initialization of textview widgets
        textviewDuty=(TextView)findViewById(R.id.textViewDuty);
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
        //Pressure
        PressureSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sm.registerListener(new MySensorListener(), PressureSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Gyroscope
        GyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(new MySensorListener(), GyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Gravity
        GravitySensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sm.registerListener(new MySensorListener(), GravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
        //Acceleration
        AccelerationSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sm.registerListener(new MySensorListener(), AccelerationSensor, SensorManager.SENSOR_DELAY_FASTEST);
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
                    acceleration[0]=event.values[0];
                    acceleration[1]=event.values[1];
                    acceleration[2]=event.values[2];
                    tmp="accx is\n"+acceleration[0]+"\n"
                            +"accy is\n"+acceleration[1]+"\n"
                            +"accz is\n"+acceleration[2]+"\n";
                    textviewAcceleration.setText(tmp);
                    break;
                case Sensor.TYPE_PRESSURE:
                    baro = event.values[0];
                    tmp="Pressure is\n"+baro+"\n";
                    textviewBaro.setText(tmp);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyro[0]= event.values[0];
                    gyro[1]=event.values[1];
                    gyro[2]=event.values[2];
                    tmp="gyrox is\n"+gyro[0]+"\n"
                            +"gyroy is\n"+gyro[1]+"\n"
                            +"gyroz is\n"+gyro[2]+"\n";
                    //textviewGyro.setText(tmp);
                    break;
                case Sensor.TYPE_ORIENTATION:
                    orientation[0]= event.values[0];
                    orientation[1]=event.values[1];
                    orientation[2]=event.values[2];
                    break;
            }
            String tmpS;
            tmpS="x is"+a+"\n"+"y is"+b+"\n"+"z is"+c+"\np is+"+p+"\n";
            byte[] aa = new byte[256];
            int cnt;
            for(cnt=0;cnt<256;cnt++)
            {
                aa[cnt]=(byte)cnt;//(byte)(cnt+cnt<<4);
            }
            //tv.setText(tmp);
            //usbService.write("Sensor data acquired\n".getBytes());
            //textviewAcceleration.setText(tmp);
            int arr[]={0xaa,0xbb,0xcc,0xdd,0xee,0xff,0x11,0x22};
            //tmpS="AAAAAAAABBBBBBBB";
           // usbService.write(aa);
        }

    }
}