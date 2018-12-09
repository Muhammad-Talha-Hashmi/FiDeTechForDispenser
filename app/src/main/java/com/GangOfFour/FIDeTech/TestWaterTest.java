package com.GangOfFour.FIDeTech;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class TestWaterTest extends AppCompatActivity {
    final String[] lastValue = new String[1];
    private static final long START_TIME_IN_MILLIS =10000;
    //Timmer things
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    private long mEndTime;
    //    private final String DEVICE_NAME="MyBTBee";
//    private final String DEVICE_ADDRESS="20:13:10:15:33:66";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button btnStartTest, btnConnect2BT,clearButton,btnStop;
    TextView tvDataPanal,tvTimer,tvResult,tvSeconds,tvSensorData,tvStatus;
    boolean deviceConnected=false;
    byte buffer[];
    boolean stopThread;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_water_test);
        btnStartTest =findViewById(R.id.btnGo);
        tvDataPanal =findViewById(R.id.textView);
        tvTimer=findViewById(R.id.tvTimer);
        tvDataPanal.setMovementMethod(new ScrollingMovementMethod());
        tvResult=findViewById(R.id.tvResult);
        tvSeconds=findViewById(R.id.tvSeconds);

        tvSensorData=findViewById(R.id.tvSensorData);
        tvStatus=findViewById(R.id.tvStatus);
        tvStatus.setTextColor(Color.RED);
        setUI(false);
        Toast.makeText(getApplicationContext(),"Searching for FIDeTech bluetooth connection.",Toast.LENGTH_SHORT).show();

//        Typeface custom_font = ResourcesCompat.getFont(TestWaterTest.this, R.font.crystal);
//        tvTimer.setTypeface(custom_font);

        if(BTinit())
        {
            if(BTconnect())
            {

                deviceConnected=true;
                tvStatus.setText("Device Connected!");
                tvStatus.setTextColor(Color.GREEN);
                btnStartTest.setTextColor(Color.GREEN);
                Toast.makeText(getApplicationContext(),"Sensor Connected!",Toast.LENGTH_SHORT).show();
            }

        }


    }


    private void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerrStopped();
            }
        }.start();
        beginListenForData();
        mTimerRunning = true;

    }

    private void updateCountDownText() {
        //int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d",  seconds);
        tvTimer.setText(timeLeftFormatted);
    }
    private void setUI(boolean visible){
        if (!visible){
            btnStartTest.setVisibility(View.VISIBLE);
            tvDataPanal.setVisibility(View.INVISIBLE);
            tvTimer.setVisibility(View.INVISIBLE);
            tvSensorData.setVisibility(View.INVISIBLE);
            tvSeconds.setVisibility(View.INVISIBLE);
            tvResult.setVisibility(View.INVISIBLE);

        }else if (visible){
            btnStartTest.setVisibility(View.INVISIBLE);
//            btnConnect2BT.setVisibility(View.VISIBLE);
            tvDataPanal.setVisibility(View.VISIBLE);
            tvTimer.setVisibility(View.VISIBLE);
            tvSensorData.setVisibility(View.VISIBLE);
            tvSeconds.setVisibility(View.VISIBLE);
            tvResult.setVisibility(View.VISIBLE);
        }


    }
    public boolean BTinit()
    {

        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesn't Support Bluetooth"+bluetoothAdapter,Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {


            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getName().equals("HC-05"))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {

            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public void onClickStart(View view) {

        if(deviceConnected)
        {

            setUI(true);
            startTimer();

        }


    }
    void timerrStopped(){

        mTimerRunning = false;
        btnStartTest.setEnabled(true);
        stopThread=true;
//        tvDataPanal.setText(lastValue[0]);
    }


    void beginListenForData()
    {
        final String string;
        final Handler handler = new Handler();

        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread && mTimerRunning)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");

                            handler.post(new Runnable() {
                                public void run()
                                {
                                    System.out.println(string);
                                    tvDataPanal.setText(string);
                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public void onClickSend(View view) {



    }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        deviceConnected=false;

    }

    public void onClickClear(View view) {
        tvDataPanal.setText("");
    }

}
