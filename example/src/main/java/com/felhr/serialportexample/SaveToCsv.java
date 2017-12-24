package com.felhr.serialportexample;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by aero on 17-12-24.
 */

public class SaveToCsv {
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
