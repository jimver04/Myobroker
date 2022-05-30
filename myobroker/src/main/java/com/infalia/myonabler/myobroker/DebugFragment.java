/*
 * Android Myo library by darken
 * Matthias Urhahn (matthias.urhahn@rwth-aachen.de)
 * mHealth - Uniklinik RWTH-Aachen.
 */
package com.infalia.myonabler.myobroker;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.infalia.myonabler.BaseMyo;
import com.infalia.myonabler.Myo;
import com.infalia.myonabler.MyoCmds;
import com.infalia.myonabler.MyoConnector;
import com.infalia.myonabler.tools.Logy;

/**
 * Test class for various library and Myo functions.
 * Tries to display all found Myo devices with all their available data.
 */
public class DebugFragment extends Fragment implements BaseMyo.ConnectionListener {
    private static final String TAG = "MyoLib:DebugFragment";
    private Map<Myo, MyoInfoView> mMyoViewMap = new HashMap<>();

    private ViewGroup mContainer;
    private boolean mScanning = false;
    private MyoConnector mMyoConnector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logy.sLoglevel = Logy.VERBOSE;
        Log.w("Myonabler", "onCreate DebugFrament");


        //Toast.makeText((Context)getActivity(), "DebugFrament", Toast.LENGTH_LONG ).show();

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_debug, container, false);
        Log.w("Myonabler", "DebugFrament onCreateView");
        mContainer = layout.findViewById(R.id.container);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mMyoConnector = new MyoConnector(getActivity());
        mMyoConnector.scan(2000, mScannerCallback);

        super.onActivityCreated(savedInstanceState);

        // COARSE Location is needed for Bluetooth data
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    private MyoConnector.ScannerCallback mScannerCallback = new MyoConnector.ScannerCallback() {
        @Override
        public void onScanFinished(final List<Myo> myos) {

            Log.d("Myonabler", "DebugFragment ScannerCallback 1");

            if (mContainer.getHandler() == null)
                return;

            Log.d("Myonabler", "DebugFragment ScannerCallback 2");

            Log.d(TAG, "MYOS:" + myos.size());

            mContainer.post(new Runnable() {
                @Override
                public void run() {

                    if (getView() == null)
                        return;

                    for (final Myo myo : myos) {
                        if (!mMyoViewMap.containsKey(myo)) {
                            MyoInfoView infoView = (MyoInfoView) LayoutInflater.from(getActivity()).inflate(R.layout.view_myoinfo, mContainer, false);
                            mMyoViewMap.put(myo, infoView);
                            infoView.setContextCustom(getActivity());


                            Log.d("Myonabler", "Found");

                            myo.addConnectionListener(DebugFragment.this);
                            myo.connect();
                            myo.setConnectionSpeed(BaseMyo.ConnectionSpeed.HIGH);
                            myo.writeSleepMode(MyoCmds.SleepMode.NEVER, null);
                            myo.writeMode(MyoCmds.EmgMode.FILTERED, MyoCmds.ImuMode.RAW, MyoCmds.ClassifierMode.DISABLED, null);
                            myo.writeUnlock(MyoCmds.UnlockType.HOLD, null);
                            infoView.setMyo(myo);
                            mContainer.addView(infoView);
                        }
                    }

                    if (mScanning) {
                        mMyoConnector.scan(2000, mScannerCallback);
                    }
                }
            });

        }
    };


    @Override
    public void onResume() {
        mScanning = true;
        Log.d("Myonabler", "DebugFragment OnResume");



        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("Myonabler", "DebugFragment Pause");

        mScanning = false;

        for (Myo myo : mMyoViewMap.keySet()) {
            myo.removeConnectionListener(this);
            myo.setConnectionSpeed(BaseMyo.ConnectionSpeed.BALANCED);
            myo.writeSleepMode(MyoCmds.SleepMode.NORMAL, null);
            myo.writeMode(MyoCmds.EmgMode.NONE, MyoCmds.ImuMode.NONE, MyoCmds.ClassifierMode.DISABLED, null);
            myo.disconnect();
        }

        mContainer.removeAllViews();
        mMyoViewMap.clear();
        super.onPause();
    }

    @Override
    public void onConnectionStateChanged(final BaseMyo myo, BaseMyo.ConnectionState state) {

        Log.d("Myonabler", "DebugFragment onConnectionStateChanged");

        if (getView() == null)
            return;
        if (state == BaseMyo.ConnectionState.DISCONNECTED) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    mContainer.removeView(mMyoViewMap.get(myo));
                }
            });
        }
    }
}


