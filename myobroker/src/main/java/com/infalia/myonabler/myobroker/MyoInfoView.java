/*
 * Android Myo library by darken
 * Matthias Urhahn (matthias.urhahn@rwth-aachen.de)
 * mHealth - Uniklinik RWTH-Aachen.
 */
package com.infalia.myonabler.myobroker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infalia.myonabler.Myo;
import com.infalia.myonabler.MyoCmds;
import com.infalia.myonabler.msgs.MyoMsg;
import com.infalia.myonabler.processor.classifier.ClassifierEvent;
import com.infalia.myonabler.processor.classifier.ClassifierProcessor;
import com.infalia.myonabler.processor.emg.EmgData;
import com.infalia.myonabler.processor.emg.EmgProcessor;
import com.infalia.myonabler.processor.imu.ImuData;
import com.infalia.myonabler.processor.imu.ImuProcessor;
import com.infalia.myonabler.processor.imu.MotionEvent;
import com.infalia.myonabler.processor.imu.MotionProcessor;

import java.util.Locale;

/**
 * Debug view that takes a {@link Myo} object and displays it's data.
 */
public class MyoInfoView extends RelativeLayout implements
        Myo.BatteryCallback,
        Myo.FirmwareCallback,
        EmgProcessor.EmgDataListener,
        ImuProcessor.ImuDataListener,
        ClassifierProcessor.ClassifierEventListener,
        Myo.ReadDeviceNameCallback, MotionProcessor.MotionEventListener {

    private Myo mMyo;
    private TextView mBatteryLevel, mFirmware, mSerialNumber, mAddress;
    private TextView mEmgData, mOrientationData, mGyroData, mAcclData;
    private boolean mAttached = false;
    private boolean isShowing = true;
    private EmgProcessor mEmgProcessor;
    private ImuProcessor mImuProcessor;
    private ClassifierProcessor mClassifierProcessor;
    private MotionProcessor mMotionProcessor;
    Context ctx;
    private TextToSpeech ttobj2;

    public MyoInfoView(Context context) {
        super(context);
        //ctx = get ; // this.getContext();
    }

    public MyoInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyoInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyoInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {

        mBatteryLevel = findViewById(R.id.tv_batterylevel);
        mFirmware = findViewById(R.id.tv_firmware);
        mSerialNumber = findViewById(R.id.tv_serialnumber);
        mAddress = findViewById(R.id.tv_address);
        mEmgData = findViewById(R.id.tv_emg);
        mGyroData = findViewById(R.id.tv_gyro);
        mAcclData = findViewById(R.id.tv_accl);
        mOrientationData = findViewById(R.id.tv_orientation);
        super.onFinishInflate();
    }

    // Speech success
    void speakSuccess(){
        String succStr= ctx.getString(R.string.success_myo_setup).toString();
        ttobj2.speak(succStr, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onAttachedToWindow() {

        if (isInEditMode())
            return;
        mAttached = true;
        post(new Runnable() {
            @Override
            public void run() {
                if (mAttached) {
                    if (mMyo != null && mMyo.isRunning()) {
                        mMyo.readDeviceName(MyoInfoView.this);
                        mAddress.setText(mMyo.getBluetoothDevice().getAddress());
                        mMyo.readBatteryLevel(MyoInfoView.this);
                        mMyo.readFirmware(MyoInfoView.this);
                    }
                    postDelayed(this, 500);
                }
            }
        });
        mEmgProcessor = new EmgProcessor();
        mEmgProcessor.addListener(this);
        mMyo.addProcessor(mEmgProcessor);
        mImuProcessor = new ImuProcessor();
        mImuProcessor.addListener(this);
        mMyo.addProcessor(mImuProcessor);
        mClassifierProcessor = new ClassifierProcessor();
        mClassifierProcessor.addListener(this);
        mMyo.addProcessor(mClassifierProcessor);
        mMotionProcessor = new MotionProcessor();
        mMotionProcessor.addListener(this);
        mMyo.addProcessor(mMotionProcessor);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyo.writeVibrate(MyoCmds.VibrateType.SHORT, null);
            }
        });
        mBatteryLevel.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMyo.writeDeepSleep(null);
                return true;
            }
        });







        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (isInEditMode())
            return;
        mAttached = false;
        mMyo.removeProcessor(mEmgProcessor);
        mMyo.removeProcessor(mImuProcessor);
        mMyo.removeProcessor(mClassifierProcessor);
        super.onDetachedFromWindow();
    }

    public Myo getMyo() {
        return mMyo;
    }

    public void setMyo(Myo myo) {
        mMyo = myo;
    }

    @Override
    public void onBatteryLevelRead(Myo myo, MyoMsg msg, final int batteryLevel) {
        if (getHandler() != null)
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mBatteryLevel.setText(batteryLevel + "%");
                }
            });
    }

    @Override
    public void onFirmwareRead(Myo myo, MyoMsg msg, final String version) {
        if (getHandler() != null)
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    mFirmware.setText("Firmware: " + version);
                }
            });
    }

    private long mLastEmgUpdate = 0;

    @Override
    public void onNewEmgData(final EmgData emgData) {
        if (System.currentTimeMillis() - mLastEmgUpdate > 10) {
            if (getHandler() != null)
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mEmgData.setText("Emg:\n" + emgData.toString() + "\n" + (mEmgProcessor.getPacketCounter() * 2) + " EMG/s");

                        final Intent i= new Intent();
                        i.putExtra("emgdata", emgData.toString());
                        i.setAction("com.infalia.myonabler.myobroker.sendemgdata");
                        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        ctx.sendBroadcast(i);

                        if (isShowing) {

                            //Toast.makeText(ctx, ctx.getText(R.string.success_myo_setup), Toast.LENGTH_LONG).show();

                            // Speech Success
                            ttobj2 = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    if (status == TextToSpeech.SUCCESS) {
                                        speakSuccess();
                                    }
                                }
                            });

 //                           Toast.makeText(ctx,  "Myo setup will close automatically in 3 secs.", Toast.LENGTH_LONG).show();

                            getHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Start Myo app
                                    Intent launchIntent = null;
                                    try{
                                        launchIntent = ctx.getPackageManager().getLaunchIntentForPackage("com.infalia.myonabler");
                                        ctx.startActivity(launchIntent);
                                    } catch (Exception ignored) {}
                                }
                            }, 3000);
                            isShowing = false;
                        }

                    }
                });
            mLastEmgUpdate = System.currentTimeMillis();
        }






    }

    private long mLastImuUpdate = 0;

    @Override
    public void onNewImuData(final ImuData imuData) {
        if (System.currentTimeMillis() - mLastImuUpdate > 500) {
            if (getHandler() != null)
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mOrientationData.setText("Orient: " + ImuData.format(imuData.getOrientationData()) + "\n" + mImuProcessor.getPacketCounter() + " IMU/s");
                        mAcclData.setText("Accel: " + ImuData.format(imuData.getAccelerometerData()) + "\n" + mImuProcessor.getPacketCounter() + " IMU/s");
                        mGyroData.setText("Gyro: " + ImuData.format(imuData.getGyroData()) + "\n" + mImuProcessor.getPacketCounter() + " IMU/s");
                    }
                });
            mLastImuUpdate = System.currentTimeMillis();
        }
    }

    @Override
    public void onClassifierEvent(ClassifierEvent classifierEvent) {

    }

    @Override
    public void onMotionEvent(MotionEvent motionEvent) {

    }


    @Override
    public void onDeviceNameRead(Myo myo, MyoMsg msg, final String deviceName) {
        if (getHandler() != null)
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    //mTitle.setText(deviceName);
                }
            });
    }

    public void setContextCustom(Activity activity) {
        ctx = activity;
    }
}
