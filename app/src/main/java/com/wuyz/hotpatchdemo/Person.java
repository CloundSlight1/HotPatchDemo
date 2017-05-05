package com.wuyz.hotpatchdemo;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/5/4.
 */

public class Person {

    public void speak(Context context) {
        Toast.makeText(context, "old", Toast.LENGTH_SHORT).show();
    }
}
