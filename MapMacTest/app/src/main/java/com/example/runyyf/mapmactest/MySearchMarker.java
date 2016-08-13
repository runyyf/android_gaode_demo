package com.example.runyyf.mapmactest;

import com.amap.api.maps.model.Marker;

/**
 * 支持搜索页面下的marker动画效果要自己写overlay
 * Created by runyyf on 16/5/31.
 */
public class MySearchMarker {

    /**
     * 默认的搜索页最多支持10个marker
     */
    private Marker marker0;
    private Marker marker1;
    private Marker marker2;
    private Marker marker3;
    private Marker marker4;
    private Marker marker5;
    private Marker marker6;
    private Marker marker7;
    private Marker marker8;
    private Marker marker9;


    public Marker getMarkerObject(int index){
        switch (index){
            case 1:
                return marker0;
            case 2:
        }

        return null;
    }


}
