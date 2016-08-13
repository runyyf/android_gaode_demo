package com.example.runyyf.mapmactest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.PathPlanningStrategy;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.tbt.TrafficFacilityInfo;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements LocationSource,
        AMapLocationListener ,AMap.OnMapClickListener,TextWatcher,
        PoiSearch.OnPoiSearchListener,AMapNaviListener{

    private static final String TAG = "MainActivity";
    private Button button;
    private Button cancelButton;
    private AutoCompleteTextView searchText;

    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult;


    private RelativeLayout layout;
    private ListView listView;
    private ArrayList<String> list=new ArrayList<String>();

    //高德地图初始化对象
    private AMap aMap;
    private MapView mapView;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    private AMapLocation localLocation;
    private Marker marker1;


    //导航对象
    private ArrayList<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();
    private ArrayList<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();

    private NaviLatLng mNavStart = new NaviLatLng(30.296614,120.163462);
    private NaviLatLng mNavEnd = new NaviLatLng(30.327266,120.091725);

    private com.amap.api.navi.view.RouteOverLay mRouteOverlay;
    private AMapNavi aMapNavi;

    private int searchState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

                list.clear();
                if (!searchText.getText().toString().equals("")){
                    doSearch();
                    searchState = 1 ;
                    if(layout.getVisibility() == View.GONE){
                        layout.setVisibility(View.VISIBLE);
                    }else if (layout.getVisibility() == View.VISIBLE){
                        //layout.setVisibility(View.GONE);
                    }
                }else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("地图")
                            .setMessage("请输入内容")
                            .setPositiveButton("ok",null)
                            .show();
                }
            }
        });

        cancelButton = (Button) findViewById(R.id.button2);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aMap.clear();
                searchText.setText("");
                layout.setVisibility(View.GONE);
                LatLng latLng = new LatLng(localLocation.getLatitude(),
                        localLocation.getLongitude());
                marker1=aMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.location2)));
                searchState = 0 ;
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
            }
        });


        searchText = (AutoCompleteTextView)findViewById(R.id.keyWord);
        searchText.addTextChangedListener(this);

        layout = (RelativeLayout) findViewById(R.id.layout1);
        layout.setVisibility(View.GONE);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<PoiItem> poiItems = poiResult.getPois();
                LatLonPoint latLonPoint = poiItems.get(position).getLatLonPoint();

                LatLng latLng = new LatLng(latLonPoint.getLatitude(),
                        latLonPoint.getLongitude());
                Log.i("item click things",latLng.latitude+" -"+latLng.longitude);
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
            }
        });

        //高德地图
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapInit();

    }


    private ArrayList<String>  getData(){

        return list;
    }

    int currentPage = 0 ;
    public void doSearch(){
        query = new PoiSearch.Query(searchText.getText().toString(),"","杭州");
        query.setPageSize(10);
        query.setPageNum(0);

        poiSearch = new PoiSearch(this,query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    /**
     * 高德地图相关操作初始化
     */
    public void mapInit() {

        if (aMap == null) {
            //获得aMap对象
            aMap = mapView.getMap();
            // 设置定位监听
            aMap.setLocationSource(this);
            // 设置默认定位按钮是否显示
            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            aMap.setMyLocationEnabled(true);
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            //设置地图缩放级别3-19
            aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            //设置地图点击事件监听
            aMap.setOnMapClickListener(this);
        }


        /*//系统小蓝点风格
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.pin));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        myLocationStyle.strokeWidth(0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);*/

        LatLng latLng1 = new LatLng(30.272582, 120.135109);
        marker1=aMap.addMarker(new MarkerOptions().position(latLng1)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.location2)));
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng1));

        //marker点击事件的回调
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return false;
            }
        });

        //infoWindow点击回调
        aMap.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MainActivity.this,TestActivity.class);
                intent.putExtra("startLatitude",localLocation.getLatitude());
                intent.putExtra("startLongitude",localLocation.getLongitude());
                intent.putExtra("endLatitude",marker.getPosition().latitude);
                intent.putExtra("endLongitude",marker.getPosition().longitude);
                startActivity(intent);
            }
        });


        //路径规划监听
        aMapNavi = AMapNavi.getInstance(this);
        aMapNavi.addAMapNaviListener(this);
        mRouteOverlay = new com.amap.api.navi.view.RouteOverLay(aMap,null);
    }


    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        // TODO Auto-generated method stub
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                localLocation = amapLocation;
                LatLng latLng = new LatLng(amapLocation.getLatitude(),
                        amapLocation.getLongitude());
                marker1.setPosition(latLng);
                if (searchState == 0){
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                }
                //marker1.setRotateAngle(amapLocation);
                //Log.i(TAG,"address"+amapLocation.getAddress());
                //Log.i("123",latLng.latitude+" -"+latLng.longitude);

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        // TODO Auto-generated method stub
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            //设置定位参数
            mLocationOption.setInterval(5000);
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void deactivate() {
        // TODO Auto-generated method stub
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    /**
     * 地图点击事件回调
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        Log.i(TAG,"1122334455");
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        Inputtips inputtips = new Inputtips(this, new Inputtips.InputtipsListener() {
            @Override
            public void onGetInputtips(List<Tip> tipList, int rcode) {
                if (rcode == 0 ){
                    List<String> listString = new ArrayList<String>();
                    for (int i = 0; i < tipList.size(); i++) {
                        listString.add(tipList.get(i).getName());
                    }
                    ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.route_inputs, listString);
                    searchText.setAdapter(aAdapter);
                    aAdapter.notifyDataSetChanged();
                }
            }
        });
        try {
            inputtips.requestInputtips(newText,"杭州");
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 0){
            if (result !=null && result.getQuery()!= null){
                if (result.getQuery().equals(query)){
                    poiResult = result;

                    List<PoiItem> poiItems = poiResult.getPois();
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();

                    if (poiItems != null && poiItems.size()>0){

                        MyPoiOverlay myPoiOverlay = new MyPoiOverlay(aMap,poiItems);
                        myPoiOverlay.removeFromMap();
                        myPoiOverlay.addToMap();
                        myPoiOverlay.zoomToSpan();

                        for (int i = 0 ; i<poiItems.size();i++){
                            list.add(" "+(i+1)+"."+poiItems.get(i).toString());
                        }

                        /*Log.i("get information",poiItems.get(1).getSnippet());
                        Log.i("get information",poiItems.get(1).getDirection());
                        Log.i("get information",poiItems.get(1).getAdName());
                        Log.i("get information",poiItems.get(1).toString());*/

                        ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,
                                R.layout.main_listview_item,getData());
                        listView.setAdapter(adapter);

                    }else {
                        Log.i(TAG,"onPoiSearched error");
                    }
                }
            }
        }else {
            Log.i(TAG,"onSearchPoi + rcode = "+rCode);
        }
    }


    /**
     * marker点击时跳动一下
     */
    public void jumpPoint(final Marker marker) {

        LatLng latLng = new LatLng(localLocation.getLatitude(),
                localLocation.getLongitude());

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        Point startPoint = proj.toScreenLocation(latLng);
        startPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * (localLocation.getLatitude()) + (1 - t)
                        * startLatLng.longitude;
                double lat = t * localLocation.getLongitude() + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });


    }

    private void calculateDriveRoute(){
        mStartPoints.clear();
        mEndPoints.clear();

        NaviLatLng naviLatLng = new NaviLatLng(localLocation.getLatitude(),
                localLocation.getLongitude());
        mStartPoints.add(naviLatLng);
        mEndPoints.add(mNavEnd);

        boolean isSuccess = aMapNavi.calculateDriveRoute(mStartPoints,mEndPoints,
                null, PathPlanningStrategy.DRIVING_DEFAULT);
        if (!isSuccess){
            Log.i(TAG,"driving calculateDriveRoute error");
        }

    }


    private NaviLatLng parseEditText(String text) {
        try {
            double latD = Double.parseDouble(text.split(",")[0]);
            double lonD = Double.parseDouble(text.split(",")[1]);

            return new NaviLatLng(latD, lonD);


        } catch (Exception e) {
            Toast.makeText(this, "e:" + e, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "格式:[lat],[lon]", Toast.LENGTH_SHORT).show();
        }


        return null;
    }
    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onCalculateRouteSuccess() {
        AMapNaviPath naviPath = aMapNavi.getNaviPath();
        if (naviPath == null){
            return;
        }
        Log.i(TAG,"navi路径规划成功");
        mRouteOverlay.setAMapNaviPath(naviPath);
        mRouteOverlay.addToMap();

        aMap.moveCamera(CameraUpdateFactory.zoomTo(18.0f));
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Log.i(TAG,"navi路径规划错误+"+i);
    }

    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }
}
