package com.ff.homestatus.leancloud;


import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

/**
 * Created by 孵孵 on 2018/2/3 0003.
 */

public class MyLeanCloudApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"InfRbFwr5TqUPnfrPBtOFX83-gzGzoHsz","0bwem0chvQWd2JCxhtLAWwS6");
    }
}
