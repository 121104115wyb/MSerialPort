package com.example.mserialport;


import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kongqw.serialportlibrary.Device;
import com.kongqw.serialportlibrary.SerialPort;
import com.kongqw.serialportlibrary.SerialPortFinder;
import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity---";

//    private SerialPortFinder mSerialPortFinder;
//    private SerialPort mSerialPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
//        initWidget();
//
//        mSerialPortFinder = new SerialPortFinder();
//        String[] entryValues = mSerialPortFinder.getAllDevicesPath();  // 得到所有设备文件地址的数组
//        try {
//            mSerialPort = new SerialPort(new File("/dev/ttyS1"), 115200, 0);
//        } catch (IOException e) {
//            System.out.println("找不到该设备文件");
//            e.printStackTrace();
//        }
//
//        final InputStream inputStream = mSerialPort.getInputStream();
//
//        /* 开启一个线程进行读取 */
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    byte[] buffer = new byte[1024];
//                    int size = inputStream.read(buffer);
//                    byte[] readBytes = new byte[size];
//                    System.arraycopy(buffer, 0, readBytes, 0, size);
//
//                    System.out.println("received data => " + new String(readBytes));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

//    private void initWidget() {
//        Button button = findViewById(R.id.btn_send);
//        button.setOnClickListener(this);
//    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_send:
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String content = "Hello World";
//                        byte[] bytes = content.getBytes();
//                        OutputStream out = mSerialPort.getOutputStream();
//                        try {
//                            out.write(bytes);
//                            out.flush();
//                            out.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                break;
//        }
//    }

    EditText editText;
    TextView connectPort, receiveData;
    boolean openSerialPort = false;

    void initViews() {
        editText = findViewById(R.id.inputText);
        connectPort = findViewById(R.id.connectPort);
        receiveData = findViewById(R.id.receiveData);
        Button btn_send = findViewById(R.id.btn_send);
        SerialPortFinder serialPortFinder = new SerialPortFinder();
        ArrayList<Device> devices = serialPortFinder.getDevices();

        SerialPortManager mSerialPortManager = new SerialPortManager();
        if (devices.size() > 0) {
            for (Device device : devices) {
                if ("ttyMT1".equals(device.getName())) {
                    openSerialPort = mSerialPortManager.openSerialPort(device.getFile(), 9600);
                    connectPort.setText(openSerialPort ? "ttyMT1:" + device.getFile() : "连接失败");
                    break;
                }
            }
            Log.d(TAG, "initSeriPort: -----devices.size:" + devices.size());
        }
        //boolean openSerialPort = mSerialPortManager.openSerialPort(device.getFile(), 115200);

        mSerialPortManager.setOnOpenSerialPortListener(new OnOpenSerialPortListener() {
            @Override
            public void onSuccess(File device) {
                Log.d(TAG, "onSuccess: 连接串口设备成功：名称" + device.getName());
            }

            @Override
            public void onFail(File device, Status status) {
                Log.d(TAG, "onSuccess: 连接串口设备是被：\n名称:" + device.getName() + "错误信息：" + status.name());
            }
        });

        mSerialPortManager.setOnSerialPortDataListener(new OnSerialPortDataListener() {
            @Override
            public void onDataReceived(byte[] bytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bytes.length > 0) {
                            receiveData.setText(byteArrayToHex(bytes));
                            Log.d(TAG, "onDataReceived Success: " + receiveData.getText().toString().trim());
                        } else {
                            Log.d(TAG, "onDataReceived failed: " + bytes.length);
                        }
                    }
                });
            }

            @Override
            public void onDataSent(byte[] bytes) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onDataSent Success: " + byteArrayToHex(bytes));
                    }
                });
            }
        });

        btn_send.setOnClickListener(v -> {
            String inputCommand = editText.getText().toString().trim();
            if (openSerialPort && !TextUtils.isEmpty(inputCommand)) {
                boolean sendBytes = mSerialPortManager.sendBytes(hexToByteArray(inputCommand));
                Log.d(TAG, "initViews: ----发送：" + (sendBytes ? "成功" : "失败"));
                Toast.makeText(this, "发送" + (sendBytes ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
//                mSerialPortManager.closeSerialPort();
            } else {
                Toast.makeText(this, "串口连接错误或者命令为空", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public static byte[] hexToByteArray(String var0) {
        if (var0 != null && var0.length() != 0) {
            if (var0.length() % 2 == 1) {
                var0 = "0".concat(String.valueOf(var0));
            }

            String[] var1;
            byte[] var2 = new byte[(var1 = new String[var0.length() / 2]).length];

            for (int var3 = 0; var3 < var1.length; ++var3) {
                var1[var3] = var0.substring(2 * var3, 2 * (var3 + 1));
                var2[var3] = (byte) Integer.parseInt(var1[var3], 16);
            }

            return var2;
        } else {
            return null;
        }
    }

    public static String byteArrayToHex(byte[] var0) {
        if (var0 != null && var0.length != 0) {
            StringBuilder var1 = new StringBuilder(var0.length);

            for (int var2 = 0; var2 < var0.length; ++var2) {
                var1.append(String.format("%02X", var0[var2]));
                if (var2 < var0.length - 1) {
                    var1.append(' ');
                }
            }

            return var1.toString();
        } else {
            return "";
        }
    }


}
