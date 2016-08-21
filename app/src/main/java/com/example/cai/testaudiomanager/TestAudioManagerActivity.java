package com.example.cai.testaudiomanager;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestAudioManagerActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart = null;
    Button btnStop = null;
    TextView textInfo = null;
    String fileDir = "/sdcard/";
    boolean keepRun = true;
    final static String TAG = TestAudioManagerActivity.class.getSimpleName();
    AudioRunnable ar = new AudioRunnable();

    AudioManager am ;

    StringBuilder info = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_audio_manager);
        fileDir += this.getPackageName() + "/" + "audioevents";

        am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);

        initView();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.deskclock.ALARM_ALERT");
        filter.addAction("com.android.deskclock.ALARM_DONE");
        registerReceiver(mReceiver, filter);
    }

    private void initView() {
        btnStart = (Button)findViewById(R.id.btn_startService);
        btnStop = (Button)findViewById(R.id.btn_stopService);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }


    public void writeFile(String fileName,String writestr) throws IOException {
        try{

            FileOutputStream fout =openFileOutput(fileName, MODE_APPEND);

            byte [] bytes = writestr.getBytes();

            fout.write(bytes);

            fout.close();
        }

        catch(Exception e){
            e.printStackTrace();
        }
    }

    //读数据
    public String readFile(String fileName) throws IOException{
        String res="";
        try{
            FileInputStream fin = openFileInput(fileName);
            int length = fin.available();
            byte [] buffer = new byte[length];
            fin.read(buffer);
            fin.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return res;

    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener(){
        public void onAudioFocusChange(int focusChange) {
            String str = System.currentTimeMillis() + " ";
            switch(focusChange){
                case AudioManager.AUDIOFOCUS_LOSS:
                    str = str + "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    str = str + "AUDIOFOCUS_LOSS_TRANSIENT";
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    str = str + "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    str = str + "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    //重新获得焦点，且符合播放条件，开始播放

                    break;
            }
            Log.i(TAG,str);
            info.append(str);
            info.append("\n");
        }};


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_startService){

            //获取焦点，设置runnable参数为true，置空stringbuilder
            am.requestAudioFocus(mAudioFocusListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            keepRun = true;
            info.delete(0,info.length());
            ar.run();

        } else if(view.getId() == R.id.btn_stopService){
            //丢弃焦点，设置runnable参数为false，写入文件，清空
            am.abandonAudioFocus(mAudioFocusListener);
            keepRun = false;
            try {
                writeFile(fileDir,info.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            info.delete(0,info.length());
        }
    }

    private class AudioRunnable implements Runnable {

        @Override
        public void run() {
            while(keepRun){

                /*
                STREAM_ALARM 警报
                STREAM_MUSIC 音乐回放即媒体音量
                STREAM_NOTIFICATION 窗口顶部状态栏Notification,
                STREAM_RING 铃声
                STREAM_SYSTEM 系统
                STREAM_VOICE_CALL 通话
                STREAM_DTMF 双音多频,不是很明白什么东西
                 */
                String str = System.currentTimeMillis() + " mode " + am.getMode()
                        + " RingerMode " + am.getRingerMode()
                        + " isMusicActive " + am.isMusicActive()
                        +" isSpeakerphoneOn " + am.isSpeakerphoneOn();
                info.append(str);
                info.append("\n");
                Log.i(TAG,System.currentTimeMillis() + " " + str);

                str = "STREAM_ALARM " + am.getStreamVolume(AudioManager.STREAM_ALARM)
                        + "/STREAM_MUSIC " + am.getStreamVolume(AudioManager.STREAM_MUSIC)
                        + "/STREAM_NOTIFICATION " + am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
                        + "/STREAM_RING " + am.getStreamVolume(AudioManager.STREAM_RING)
                        + "/STREAM_SYSTEM" + am.getStreamVolume(AudioManager.STREAM_SYSTEM)
                        + "/STREAM_VOICE_CALL " + am.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
                        + "/STREAM_DTMF " + am.getStreamVolume(AudioManager.STREAM_DTMF);
                info.append(str);
                info.append("\n");
                Log.i(TAG,System.currentTimeMillis() + " " + str);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public int isPhoning(){
        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        return manager.getCallState();
    }

    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"AlarmActivity - Broadcast Receiver - " + System.currentTimeMillis() +" " + action);
            if (action.equals(ALARM_SNOOZE_ACTION)) {
                info.append(System.currentTimeMillis() +" "+ action);
            } else if (action.equals(ALARM_DISMISS_ACTION)) {
                info.append(System.currentTimeMillis() +" "+ action);
            } else if (action.equals("com.android.deskclock.ALARM_ALERT")) {
                info.append(System.currentTimeMillis() +" "+ action);
            } else if(action.equals("com.android.deskclock.ALARM_DONE")) {
                info.append(System.currentTimeMillis() +" "+ action);
            } else {
             Log.i(TAG,"Unknown broadcast in AlarmActivity: " + System.currentTimeMillis() +" " + action);
            }
            info.append("\n");
        }
    };
}
