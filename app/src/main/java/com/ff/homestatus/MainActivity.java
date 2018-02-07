package com.ff.homestatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我的百度地图首页
 * @author jing__jie
 *
 */
public class MainActivity extends Activity implements OnClickListener {
    private OnMarkerClickListener markListener=null;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private Button ib_large,ib_small,ib_mode,ib_loc,ib_traffic,ib_marker,joinTeam;
    private EditText editTeam;
    //模式切换，正常模式
    private boolean modeFlag = true;
    //当前地图缩放级别
    private float zoomLevel;
    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    //是否第一次定位，如果是第一次定位的话要将自己的位置显示在地图 中间
    private boolean isFirstLocation = true;
    //创建自己的箭头定位
    private BitmapDescriptor bitmapDescriptor;
    //经纬度
    double mLatitude;
    double mLongitude;
    //方向传感器监听
    private float mLastX;
    private List<User> infos;
    //显示marker
    private boolean showMarker = false;

    private List<String> teams=new ArrayList<>();
    ListView listView;
    private String currentTeam;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makePermission();// 请求权限

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);


        //初始化控件
        initView();
        //初始化地图
        initMap();
        //定位
        initLocation();
        //创建自己的定位图标，结合方向传感器，定位的时候显示自己的方向
        initMyLoc();
        //创建marker信息
        setMarkerInfo("1");
    }
    private void setMarkerInfo(final String team) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.0.105:8080/HomeStatus/servlet/getLoc?team="+team)
                            .build();
                    final Response response = client.newCall(request).execute();
                    final String responseData = response.body().string();
                    Gson gson = new Gson();
                    if(responseData.equals("NULL")){
                        return;
                    }
                    infos=gson.fromJson(responseData, new TypeToken<List<User>>(){}.getType());
                    for(User u:infos){
                        if(AVUser.getCurrentUser().getUsername().equals(u.getUsername())){
                            infos.remove(u);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void initMyLoc() {
        //初始化图标
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round);
    }
    private void initMap() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        // 不显示缩放比例尺
        mMapView.showZoomControls(false);
        // 不显示百度地图Logo
        mMapView.removeViewAt(1);
        //百度地图
        mBaiduMap = mMapView.getMap();
        // 改变地图状态
        MapStatus mMapStatus = new MapStatus.Builder().zoom(15).build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        //设置地图状态改变监听器
        mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {


            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
            }
            @Override
            public void onMapStatusChange(MapStatus arg0) {
                //当地图状态改变的时候，获取放大级别
                zoomLevel = arg0.zoom;
            }
        });
        //地图点击事件
        mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                return false;
            }
            @Override
            public void onMapClick(LatLng arg0) {
            }
        });
    }
    private void initLocation() {
        //定位客户端的设置
        mLocationClient = new LocationClient(this);
        mLocationListener = new MyLocationListener();
        //注册监听
        mLocationClient.registerLocationListener(mLocationListener);
        //配置定位
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//坐标类型
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//打开Gps
        option.setScanSpan(2000);//1000毫秒定位一次
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);

    }
    private void initView() {
        //地图控制按钮
        ib_large = (Button)findViewById(R.id.ib_large);
        ib_large.setOnClickListener(this);
        ib_small = (Button)findViewById(R.id.ib_small);
        ib_small.setOnClickListener(this);
        ib_mode = (Button)findViewById(R.id.ib_mode);
        ib_mode.setOnClickListener(this);
        ib_loc = (Button)findViewById(R.id.ib_loc);
        ib_loc.setOnClickListener(this);
        ib_marker = (Button)findViewById(R.id.ib_marker);
        ib_marker.setOnClickListener(this);
        joinTeam=(Button)findViewById(R.id.joinTeam);
        joinTeam.setOnClickListener(this);
        editTeam=(EditText)findViewById(R.id.editTeam);
        listView = (ListView) findViewById(R.id.list_view);

        AVQuery<AVObject> query = new AVQuery<>("Team");
        query.whereEqualTo("username", AVUser.getCurrentUser().getUsername());
        // 如果这样写，第二个条件将覆盖第一个条件，查询只会返回 priority = 1 的结果
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                for(int x=0;x<list.size();++x){
                    currentTeam=list.get(x).get("teamID").toString();
                    teams.add(list.get(x).get("teamID").toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, teams);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String team = teams.get(position);
                        currentTeam=team;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder()
                                            .url("http://192.168.0.105:8080/HomeStatus/servlet/changeTeam?username="+AVUser.getCurrentUser().getUsername()+"team="+currentTeam)
                                            .build();
                                    final Response response = client.newCall(request).execute();
                                    final String responseData = response.body().string();
                                    Gson gson = new Gson();
                                    if(responseData.equals("NULL")){
                                        infos=null;
                                        return;
                                    }
                                    infos=gson.fromJson(responseData, new TypeToken<List<User>>(){}.getType());
                                    for(User u:infos){
                                        if(AVUser.getCurrentUser().getUsername().equals(u.getUsername())){
                                            infos.remove(u);
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        Toast.makeText(MainActivity.this, team,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_large:
                if (zoomLevel < 18) {
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomIn());
                    ib_small.setEnabled(true);
                } else {
                    showInfo("已经放至最大，可继续滑动操作");
                    ib_large.setEnabled(false);
                }
                showInfo(x+"");
                break;
            case R.id.ib_small:
                if (zoomLevel > 6) {
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomOut());
                    ib_large.setEnabled(true);
                } else {
                    ib_small.setEnabled(false);
                    showInfo("已经缩至最小，可继续滑动操作");
                }
                break;
            case R.id.ib_mode://卫星模式和普通模式
                /*if(modeFlag){
                    modeFlag = false;
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                    showInfo("开启卫星模式");
                }else{
                    modeFlag = true;
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    showInfo("开启普通模式");
                }*/

                /*final AVObject testObject = new AVObject("_Conversation");
                testObject.put("objectId","1");
                testObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e == null){
                            Log.d("saved",testObject.getObjectId());
                            final AVObject Object = new AVObject("Group");
                            Object.put("group","1");
                            Object.put("conversationID",testObject.getObjectId());
                            Object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if(e == null){
                                        Log.d("saved",testObject.getObjectId());
                                    }else{
                                        Log.d("saved",testObject.getObjectId());
                                    }
                                }
                            });
                        }else{
                            Log.d("saved",testObject.getObjectId());
                        }
                    }
                });*/

                /*AVQuery<AVObject> query = new AVQuery<>("Group");
                query.whereEqualTo("group", "1");
                // 如果这样写，第二个条件将覆盖第一个条件，查询只会返回 priority = 1 的结果
                query.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {
                        Log.d("getID",list.get(0).get("conversationID").toString());
                    }
                });*/

                /*final AVObject Object = new AVObject("Team2Conversation");
                Object.put("team",editTeam.getText().toString());
                Object.put("conversationID","conversationid");
                Object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if(e == null){
                            Log.d("saved","ok");
                        }else{
                            Log.d("saved","maobing");
                        }
                    }
                });*/

                break;
            case R.id.ib_loc:
                isFirstLocation = true;
                showInfo("返回自己位置");
                break;
            case R.id.ib_marker:
                if(!showMarker){
                    //显示marker
                    showInfo("显示覆盖物");
                    addOverlay();
                    showMarker = true;
                }else{
                    //关闭显示marker
                    showInfo("关闭覆盖物");
                    mBaiduMap.clear();
                    showMarker = false;
                }
                break;
            case R.id.joinTeam:
                final AVQuery<AVObject> startDateQuery = new AVQuery<>("Team");
                startDateQuery.whereEqualTo("teamID", editTeam.getText().toString());

                final AVQuery<AVObject> endDateQuery = new AVQuery<>("Team");
                endDateQuery.whereEqualTo("username",AVUser.getCurrentUser().getUsername());

                AVQuery<AVObject> query = AVQuery.and(Arrays.asList(startDateQuery, endDateQuery));
                query.findInBackground(new FindCallback<AVObject>() {
                    @Override
                    public void done(List<AVObject> list, AVException e) {
                        if(list.size()<=0){
                            final AVObject joinObject = new AVObject("Team");
                            joinObject.put("teamID",editTeam.getText().toString());
                            joinObject.put("username",AVUser.getCurrentUser().getUsername());
                            joinObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if(e == null){
                                        Log.d("saved","ok");
                                    }else{
                                        Log.d("saved","maobing");
                                    }
                                }
                            });
                        }
                    }
                });


            default:
                break;
        }
    }
    //显示marker
    private int x=0;
    private void addOverlay() {
        //清空地图
        mBaiduMap.clear();
        //创建marker的显示图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round);
        LatLng latLng = null;
        Marker marker;
        OverlayOptions options;
        if(infos!=null){
            for(User info:infos){
                //获取经纬度
                latLng = new LatLng(Double.parseDouble(info.getLat()),Double.parseDouble(info.getLng()));
                //设置marker
                options = new MarkerOptions()
                        .position(latLng)//设置位置
                        .icon(bitmap)//设置图标样式
                        .zIndex(9) // 设置marker所在层级
                        .draggable(false); // 设置手势拖拽;
                //添加marker
                marker = (Marker) mBaiduMap.addOverlay(options);
                //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
                Bundle bundle = new Bundle();
                //info必须实现序列化接口
                bundle.putSerializable("info", info);

                marker.setExtraInfo(bundle);
            }
        }

        //将地图显示在最后一个marker的位置
        /*MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);*/
        //添加marker点击事件的监听
        if(markListener!=null){
            mBaiduMap.removeMarkerClickListener(markListener); //防止监听重叠
        }
        markListener= new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //从marker中获取info信息
                Bundle bundle = marker.getExtraInfo();
                User u = (User) bundle.getSerializable("info");
                //将信息显示在界面上
                //Log.d("info",infoUtil.toString());
                Log.d("info","clickTest:"+u.getUsername());
                return true;
            }
        };
        mBaiduMap.setOnMarkerClickListener(markListener);
    }
    //自定义的定位监听
    private class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            //将获取的location信息给百度map
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mLastX)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(data);
            //更新经纬度
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            //配置定位图层显示方式，使用自己的定位图标
            MyLocationConfiguration configuration = new MyLocationConfiguration(LocationMode.NORMAL, true, bitmapDescriptor);
            mBaiduMap.setMyLocationConfigeration(configuration);
            if(isFirstLocation){
                //获取经纬度
                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
                //mBaiduMap.setMapStatus(status);//直接到中间
                mBaiduMap.animateMapStatus(status);//动画的方式到中间
                isFirstLocation = false;
                showInfo("位置：" + location.getAddrStr());
            }
            upToMyServer(AVUser.getCurrentUser().getUsername(),location.getLatitude()+"",location.getLongitude()+"");
            setMarkerInfo(currentTeam);
            mBaiduMap.clear();
            addOverlay();

        }

    }
    public void upToMyServer(final String username, final String lat, final String lng){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://192.168.0.105:8080/HomeStatus/servlet/changeLoc?username="+username+"&lat="+lat+"&lng="+lng)
                            .build();
                    final Response response = client.newCall(request).execute();
                    final String responseData = response.body().string();
                    Log.d("upToMyServer","changeLocResult: "+responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 在这里进行 UI 操作，将结果显示到界面上
                            //text.setText(responseData);

                        }
                    });



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void makePermission(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissionStr = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissionStr, 1);//requestCode=1
        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int x = 0; x < grantResults.length && x < permissions.length; ++x) {
                        if (grantResults[x] != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "同意啊", Toast.LENGTH_SHORT).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permissions[x]}, 1);   //requestCode=1
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        if(mLocationClient.isStarted()){
            mLocationClient.stop();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    //显示消息
    private void showInfo(String str){
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }
}