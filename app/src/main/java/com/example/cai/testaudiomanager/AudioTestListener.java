package com.example.cai.testaudiomanager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by ltp on 2016/8/21.
 */
public class AudioTestListener extends Service {

    private IBinder mBinder = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
