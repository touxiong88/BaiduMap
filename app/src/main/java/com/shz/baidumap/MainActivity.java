package com.shz.baidumap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
{
    public LocationClient mLocationClient;
    private TextView positionText;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private boolean isFirstLocate  = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*实例化 定位客户端*/
        mLocationClient = new LocationClient(getApplicationContext());
        /*注册回调方法,显示定位结果信息*/
        mLocationClient.registerLocationListener(new MyLocationListener());
        /*初始化SDK 为了显示地图*/
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        /*获取百度map控件*/
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        positionText = findViewById(R.id.position_text_view);

        /*统一申请权限*/
        List<String>permissionList  = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String [] permissions= permissionList.toArray(new String[permissionList.
                                                                 size()]);
            /*使用ActivityCompat 统一申请权限 */
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            /*开始定位*/
            requestLocation();
        }
    }

    private void requestLocation(){
        initLocation();
        /*开始定位*/
        mLocationClient.start();
    }
/*设置5000ms更新一次坐标位置信息*/
    private void initLocation(){
        LocationClientOption option = new  LocationClientOption();
        option.setScanSpan(5000);
//        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

    }

    @Override /*重写Activity 方法返回申请权限结果*/
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch(requestCode){
        case 1:
            if(grantResults.length > 0) {
                for (int result:grantResults) {
                    if(result != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"必须同意所有权限才能使用本程序",
                                       Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                requestLocation();
            }else {
                Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
    }

    }
/*将当前位置显示在地图上*/
    private void navigateTo(BDLocation location){
        if(isFirstLocate){
//            /*获取经纬度*/
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
            mBaiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(16f);
            mBaiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        /*获取当前位置 并显示到地图上*/
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        mBaiduMap.setMyLocationData(locationData);
    }

    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            /*显示当前位置地图*/
            if(bdLocation.getLocType() ==BDLocation.TypeGpsLocation
                    ||bdLocation.getLocType() == BDLocation.TypeNetWorkException){
                navigateTo(bdLocation);
            }


            StringBuilder currentPostion = new StringBuilder();
            currentPostion.append("精度:").append(bdLocation.getLatitude()).append("\n");
            currentPostion.append("纬度: ").append(bdLocation.getLongitude()).append("\n");
            currentPostion.append("国家: ").append(bdLocation.getCountry()).append("\n");
            currentPostion.append("省: ").append(bdLocation.getProvince()).append("\n");
            currentPostion.append("市: ").append(bdLocation.getCity()).append("\n");
            currentPostion.append("区: ").append(bdLocation.getDistrict()).append("\n");
            currentPostion.append("街道: ").append(bdLocation.getStreet()).append("\n");
            currentPostion.append("定位方式: ");
            if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                currentPostion.append("GPS");
            }else {
                currentPostion.append("网络");
            }
            positionText.setText(currentPostion);




        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mMapView.onDestroy();
        mBaiduMap.setMyLocationEnabled(false);
    }
}
