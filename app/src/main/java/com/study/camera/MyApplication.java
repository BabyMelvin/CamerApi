package com.study.camera;

import android.app.Application;
import android.os.StrictMode;

/**
 * Created by dell on 2017/11/9.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //避免android.os.FileUriExposedException: file:///storage/emulated/0/test.txt exposed beyond app through Intent.getData()


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
}
