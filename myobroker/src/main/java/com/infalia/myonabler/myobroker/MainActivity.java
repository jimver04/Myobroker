package com.infalia.myonabler.myobroker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    //private TextToSpeech ttobj;
    private MediaPlayer mediaPlayer;

    // Automatic Text-to-Speech is available only in mobile devices but not in OculusQuest 2
    // So we have to synthesize in mobile devices and extracted manually from
    // /data/data/com.infalia / instructions.wav
    // and play it with media player

    void auto_speakInit(){

        //        // --------------- Manually set language -------
        //        Configuration conf = getResources().getConfiguration();
        //        conf.locale = new Locale("de");
        //        DisplayMetrics metrics = new DisplayMetrics();
        //        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //        Resources resources = new Resources(getAssets(), metrics, conf);
        //        String str = resources.getString(R.string.myo_message).toString();
        //        Locale l = new Locale("de","DE");
        //        ttobj.setLanguage(l);
        //        //------------------
        //
        //          String str = resources.getString(R.string.myo_message).toString();
        //
        //         // Only for hearing with "speak"
        //         ttobj.speak(str, TextToSpeech.QUEUE_FLUSH, null);

        //         // Save to a file with "synthesizeToFile"
        //        File outputDir = this.getCacheDir(); // context being the Activity pointer
        //        File outputFile = null;
        //        try {
        //            outputFile = File.createTempFile("instruction_german", ".wav", outputDir);
        //            HashMap<String, String> params = new HashMap<>();
        //            // This writes it to a file !!!!!!
        //            ttobj.synthesizeToFile(str, params, outputFile.getAbsolutePath());
        //            ttobj.setLanguage(l);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerBaseActivityReceiver();
        mediaPlayer = MediaPlayer.create(this, R.raw.instruction);
        mediaPlayer.start();
     }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Receiver to close this app
    public static final String FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION =
                            "com.infalia.myonabler.myobroker.FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION";

    private BaseActivityReceiver baseActivityReceiver = new BaseActivityReceiver();

    public static final IntentFilter INTENT_FILTER = createIntentFilter();

    private static IntentFilter createIntentFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION);
        return filter;
    }

    protected void registerBaseActivityReceiver() {
        registerReceiver(baseActivityReceiver, INTENT_FILTER);

    }

    @Override
    protected void onDestroy() {
        unRegisterBaseActivityReceiver();
        super.onDestroy();
    }

    protected void unRegisterBaseActivityReceiver() {
        unregisterReceiver(baseActivityReceiver);
    }

    public class BaseActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION)){
                finish();
            }
        }
    }
}
