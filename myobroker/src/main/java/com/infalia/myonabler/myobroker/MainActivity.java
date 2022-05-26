package com.infalia.myonabler.myobroker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech ttobj;
    Context ctx;
    private boolean isOQ2 = true;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerBaseActivityReceiver();




        ttobj = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                   // speakInit();
                }
            }
        });


        Log.w("Myonabler", "Start Voice");
        if(!isOQ2){
            Say(R.string.myo_message_calibrate1);

        } else{
            Log.w("Myonabler", "Say calibrate 1 from wav");
            Say(R.raw.myo_message_calibrate1);


            final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Say(R.raw.myo_message_calibrate2);
                    }
                }, 3300);
        }

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
        Log.w("Myonabler", "Destroy Main");
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

    // Say
    void Say(int resourceInt){

        //R.string.myo_find_success_message
        if (!isOQ2) {
            String succStr = ctx.getString(resourceInt).toString();
            ttobj.speak(succStr, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            try {
                player = new MediaPlayer();
                final AssetFileDescriptor afd = this.getResources().openRawResourceFd(resourceInt);
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                player.prepare();
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


//    void speakInit(){
//
//        // Hint: You must first turn the Language of your phone speech to the respective language
//
//        ctx = this;
//
//        // --------------- Manually set language -------
//        Configuration conf = getResources().getConfiguration();
//        conf.locale = new Locale("el");
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        final Resources resources = new Resources(getAssets(), metrics, conf);
//
//        String[] str_mat = new String[]{
//                "myo_message_calibrate1",  // 0
//                "myo_find_success_message", // 1
//                "try_again_myo_find_fail_message", // 2
//                "girl_wellcome_message_and_say_to_press_start", // 3
//                "girl_start_message", // 4
//                "girl_exercise_sit", // 5
//                "girl_success_exercise", // 6
//                "restart_exercise_single", // 7
//                "girl_exercise_balance",  // 8
//                "myo_message_calibrate2", // 9
//                "girl_exercise_on_toe_tips", // 10
//                "bt_pause_text",  // 11
//                "bt_start_text",  // 12
//                "bt_restart_text",  // 13
//                "bt_exit_text"};    // 14
//
//
//
//        String s = str_mat[14];
//        Integer str_int = getResources().getIdentifier(s, "string", getPackageName());
//        String str = ctx.getResources().getString(str_int).toString();
//
//        //------------------
//
//        Locale l = new Locale("el", "EL");
//        ttobj.setLanguage(l);
//        ttobj.speak(str, TextToSpeech.QUEUE_FLUSH, null);
//
//
//        File outputDir = ctx.getCacheDir(); // context being the Activity pointer
//        File outputFile = null;
//        try {
//            outputFile = File.createTempFile(s + "_en_", ".wav", outputDir);
//            HashMap<String, String> params = new HashMap<>();
//            ttobj.synthesizeToFile(str, params, outputFile.getAbsolutePath());
//            ttobj.setLanguage(l);
//
////            try {
////
////                MediaPlayer player = new MediaPlayer();
////                //player.setDataSource(String.valueOf(R.raw.instruction));
////                player.setLooping(false);
////                player.prepare();
////                player.start();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
