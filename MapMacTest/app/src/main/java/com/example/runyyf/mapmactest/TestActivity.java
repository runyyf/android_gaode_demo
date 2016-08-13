package com.example.runyyf.mapmactest;

import android.os.Bundle;

import com.amap.api.navi.AMapNaviView;

/**
 * Created by runyyf on 16/6/1.
 */
public class TestActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_navi);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
    }
}
