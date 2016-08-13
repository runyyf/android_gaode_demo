package com.example.runyyf.mapmactest;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.PoiItem;

import java.util.List;

/**
 * 重写getBitmapDescriptor方法来改变每个
 * marker的图标
 * Created by runyyf on 16/5/31.
 */
public class MyPoiOverlay extends PoiOverlay {

    public MyPoiOverlay(AMap aMap, List<PoiItem> list) {
        super(aMap, list);
    }

    @Override
    protected BitmapDescriptor getBitmapDescriptor(int i) {
        //return super.getBitmapDescriptor(i);
        switch (i){
            case 0:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_1);
            case 1:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_2);
            case 2:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_3);
            case 3:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_4);
            case 4:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_5);
            case 5:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_6);
            case 6:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_7);
            case 7:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_8);
            case 8:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_9);
            case 9:
                return BitmapDescriptorFactory.fromResource(R.drawable.marker_10);
        }
        return BitmapDescriptorFactory.fromResource(R.drawable.map);
    }
}
