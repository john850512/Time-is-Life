package net.macdidi.google_api_o_i_o_i;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.NETWORK_PROVIDER;
import static net.macdidi.google_api_o_i_o_i.R.id.map;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {
    //中途點的經緯度資訊
    //一筆的格式為:$ID$經度:緯度 經度:緯度 ....
    private String temp_geoStr = "$1$120.29035,22.73327 120.29050000000001,22.733300000000003 120.29055000000001,22.73253 120.29054000000001,22.73243 120.29068000000001,22.732480000000002 120.29127000000001,22.732450000000004 120.29151000000002,22.73243 120.29240000000001,22.73222 120.29207000000001,22.729870000000002 120.29196,22.728890000000003 120.29167000000001,22.726820000000004 120.29131000000001,22.724230000000002 120.29117000000001,22.72315 120.29104000000001,22.72212 120.29102,22.721670000000003 120.29106000000002,22.721380000000003 120.29125,22.72068 120.29160000000002,22.71949 120.29191000000002,22.718500000000002 120.29219,22.71769 120.29256000000001,22.716730000000002 120.29288000000001,22.715940000000003 120.29362,22.714250000000003 120.29397000000002,22.71339 120.29418000000001,22.71282 120.29432000000001,22.712470000000003 120.29447,22.71226 120.29506,22.71159 120.29556000000001,22.711000000000002 120.29610000000001,22.710400000000003 120.29692000000001,22.70954 120.29748000000001,22.70898 120.29778,22.708740000000002 120.29806,22.708540000000003 120.29841,22.70832 120.29869000000001,22.708190000000002 120.299,22.708060000000003 120.29932000000001,22.70794 120.29959000000001,22.707880000000003 120.29984,22.70785 120.30096,22.70786 120.30213,22.707890000000003 120.30239000000002,22.707900000000002 120.30239000000002,22.70812 120.30238000000001,22.708380000000002 120.30238000000001,22.70849";
    private static String GeoStr = "";

    //單一個點的資料結構:ID、經度、緯度
    class GeoInfo {
        //constructor
        GeoInfo() {
            id = 0;
            Lat = 0;
            Lng = 0;
        }
        int id;//路徑編號
        double Lat;
        double Lng;
    };
    private static ArrayList<GeoInfo> GeoPoint = new ArrayList<GeoInfo>();//所有點(包含不同路徑)的LIST
    private static GoogleMap mMap;
    private Button btn;
    private static LatLng StartPoint, EndPoint;//路線起點、終點marker

    //test
    private static LatLng test_loaction;
    private EditText testLatInput, testLngInput;
    public static double tolerance;
    private ArrayList<LatLng> Single_Path_Point_Info = new ArrayList<LatLng>();//isOnPathLocation Function的參數(一條路徑)
    //定位
    private static Marker nowLocation_marker;
    private LocationManager locMgr;
    private String bestProv;
    private static boolean first_flag;//是否第一次定位
    //tts
    private static TextToSpeech mTts;
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private Handler handler;
    //timer
    Timer timer;
    TimerTask doAsynchronousTask;
    //判斷方向
    //一般用路人向量
    private static double Car_direction[] = {0,0};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contain_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        StartPoint = new LatLng(0, 0);
        EndPoint = new LatLng(0, 0);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(myListner);
        testLatInput = (EditText) findViewById(R.id.testLatInput);
        testLngInput = (EditText) findViewById(R.id.testLngInput);
        //set icon with actionbar
        ActionBar menu = getSupportActionBar();
        menu.setSubtitle("道路避讓即時警示系統");
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.ic_launcher);
        //set data
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        tolerance = sharedPreferences.getFloat("tolerance", 15.0f);
        //get data from Service
        GeoStr = Servicetest.webSocket_input_allpath;

        //定位
        test_loaction = new LatLng(0,0);
        locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        bestProv = locMgr.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locMgr.requestLocationUpdates(bestProv, 1000, 1, this);//每秒更新一次位置
        first_flag = true;
        //tts
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTts.setLanguage(Locale.CHINA);
                    mTts.speak("地圖中將顯示正在執行中的所有救護車位置與行徑路線", TextToSpeech.QUEUE_FLUSH, null,null);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MapsActivity.this, "語音系統發生錯誤", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        //handler
        handler = new Handler();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub
                //計算向量(判斷方向)
                if (!marker.getPosition().equals(test_loaction)){ //位置有更新過
                    LatLng last_loc = test_loaction;//取得前一個位置  在test_location
                    //計算向量
                    Car_direction [0] = marker.getPosition().latitude-last_loc.latitude;
                    Car_direction [1] = marker.getPosition().longitude-last_loc.longitude;
                }
                //更新test_location位置
                test_loaction = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                //模擬GPS功能，更新web的車輛位置
                if(GlobalVariable.is_simulateGPS)Servicetest.sendSimulateGPS(test_loaction);
                //更新畫面上位置資訊
                testLatInput.setText(String.valueOf(test_loaction.latitude));
                testLngInput.setText(String.valueOf(test_loaction.longitude));

                //判斷誤差，在__公尺內提示
                if(PolyUtil.isLocationOnPath(test_loaction,Single_Path_Point_Info,false,tolerance)) {
                    Toast.makeText(MapsActivity.this, "[測試模式]\n將當前位置移動到\nlat:"
                            + test_loaction.latitude + "\nlng:" + test_loaction.longitude
                            + "\n在範圍內", Toast.LENGTH_LONG).show();

                    //判斷距離
                    int min_distance = 9999999;
                    int current_ID = 1;
                    //[0][0]救護車相對駕駛第一點前後方 [0][1]救護車相對駕駛第一點左右方
                    // [1][0]救護車相對駕駛第二點前後方 [1][1]救護車相對駕駛第二點左右方
                    int site[][] = {{2,2},{2,2}};

                    for(int i = 0 ; i < GeoPoint.size() ; ) {
                        boolean first_Point_flag = true;
                        while (GeoPoint.get(i).id == current_ID) {//一條一條路徑畫
                            //第一個點的mark
                            if (first_Point_flag == true) {
                                StartPoint = new LatLng(GeoPoint.get(i).Lat, GeoPoint.get(i).Lng);
                                first_Point_flag = false;
                                int temp_distance = (int) D_jw(StartPoint.latitude, StartPoint.longitude, test_loaction.latitude, test_loaction.longitude);

                                if (temp_distance < min_distance) {
                                    min_distance = temp_distance;
                                    //Ambulance Site to Car now
                                    double between_Lat = GeoPoint.get(i).Lat - test_loaction.latitude;
                                    double between_Lng = GeoPoint.get(i).Lng - test_loaction.longitude;

                                    //第一點前後方判斷  用內積公式
                                    double dot = between_Lat * Car_direction[0] + between_Lng * Car_direction[1];
                                    double Car_direction_L = Math.sqrt(Math.pow(Car_direction[0], 2) + Math.pow(Car_direction[1], 2));
                                    double between_direction_L = Math.sqrt(Math.pow(between_Lat, 2) + Math.pow(between_Lng, 2));
                                    double theta = Math.toDegrees(Math.acos(dot / (Car_direction_L * between_direction_L)));
                                    //Toast.makeText(MapsActivity.this," "+ theta, Toast.LENGTH_LONG).show();
                                    if (theta < 67.5)
                                        site[0][0] = 1;//前方
                                    else if (theta > 112.5)
                                        site[0][0] = -1;//後方
                                    else
                                        site[0][0] = 0;//無關前後  只關左右

                                    //第一點左右方判斷  用外積公式及右手定則
                                    double Cross_direction = between_Lng * Car_direction[0] - Car_direction[1] * between_Lat;
                                    if (Cross_direction > 0 && (theta <= 157.5 && theta >= 22.5))
                                        site[0][1] = 1;//左方
                                    else if (Cross_direction < 0 && (theta <= 157.5 && theta >= 22.5))
                                        site[0][1] = -1;//右方
                                    else
                                        site[0][1] = 0;//無關左右  只關前後


                                    //第二點前後方判斷  用內積公式
                                    between_Lat = GeoPoint.get(i + 1).Lat - test_loaction.latitude;
                                    between_Lng = GeoPoint.get(i + 1).Lng - test_loaction.longitude;
                                    dot = between_Lat * Car_direction[0] + between_Lng * Car_direction[1];
                                    Car_direction_L = Math.sqrt(Math.pow(Car_direction[0], 2) + Math.pow(Car_direction[1], 2));
                                    between_direction_L = Math.sqrt(Math.pow(between_Lat, 2) + Math.pow(between_Lng, 2));
                                    theta = Math.toDegrees(Math.acos(dot / (Car_direction_L * between_direction_L)));
                                    if (theta < 67.5)
                                        site[1][0] = 1;//前方
                                    else if (theta > 112.5)
                                        site[1][0] = -1;//後方
                                    else
                                        site[1][0] = 0;//無關前後  只關左右

                                    //第二點左右方判斷  用外積公式及右手定則
                                    Cross_direction = between_Lng * Car_direction[0] - Car_direction[1] * between_Lat;
                                    if (Cross_direction > 0 && (theta <= 157.5 && theta >= 22.5))
                                        site[1][1] = 1;//左方
                                    else if (Cross_direction < 0 && (theta <= 157.5 && theta >= 22.5))
                                        site[1][1] = -1;//右方
                                    else
                                        site[1][1] = 0;//無關左右  只關前後
                                }

                            }
                            else {
                                LatLng now_pos = new LatLng(GeoPoint.get(i).Lat, GeoPoint.get(i).Lng);
                            }
                            i++;//下一個點
                            if (i == GeoPoint.size()) {//最後一個點的判斷會throwIndexOutOfBoundsException
                                break;
                            }
                        }
                        current_ID++;//下一條路徑
                    }

                    //Toast.makeText(MapsActivity.this, "第一點" +site[0][0] +" "+ site[0][1] +"\n第二點" +site[1][0] +" "+ site[1][1] , Toast.LENGTH_LONG).show();


                    //tts
                    if ((site[0][0] == 0 && site[0][1] == 0) || (site[0][0] == 0 && site[0][1] == 0)){
                        Toast.makeText(MapsActivity.this, "[測試模式]有方向有問題", Toast.LENGTH_LONG).show();
                    }
                    //else if
                    else {
                        String First_direction = get_Direction(site[0][0],site[0][1]);
                        String Second_direction = get_Direction(site[1][0],site[1][1]);
                        if (First_direction.equals(Second_direction))
                            mTts.speak("最近的救護車距離您"+min_distance+"公尺並且從您的"+First_direction+"向您靠近", TextToSpeech.QUEUE_FLUSH, null,null);
                        else if ((site[0][0] + site[1][0] == 0) && (site[0][1] + site[1][1] == 0))
                            mTts.speak("最近的救護車距離您"+min_distance+"公尺並且從您的"+First_direction+"向您靠近", TextToSpeech.QUEUE_FLUSH, null,null);
                        else
                            mTts.speak("最近的救護車距離您"+min_distance+"公尺並且從您的"+First_direction+"向您的"+Second_direction+"行駛", TextToSpeech.QUEUE_FLUSH, null,null);
                    }

                }
                else{
                    /*Toast.makeText(MapsActivity.this, "[測試模式]\n將當前位置移動到\nlat:"
                            +test_loaction.latitude+"\nlng:"+test_loaction.longitude
                            +"\n不再在範圍內", Toast.LENGTH_LONG).show();*/
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub

            }
        });

        //更新路徑timer
        callAsynchronousTask();

        //定位
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        //地圖中心移到目前位置
        //Location location = locMgr.getLastKnownLocation(bestProv);
        //第一次用網路位而不是用gps，不然會return null
        Location location = locMgr.getLastKnownLocation(NETWORK_PROVIDER);
        LatLng cur_internet_location = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cur_internet_location,13));//中心是起點

        //如果不是第一次定位就移掉前一個mark，重新定位
        if(!first_flag){
            nowLocation_marker.remove();
        }
        first_flag = false;
        nowLocation_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(cur_internet_location).title("目前位置"));
        nowLocation_marker.setDraggable(true);
        test_loaction =  cur_internet_location;

        DrawLine();//畫出路線(之後要改成定時偵測是否有新路徑，然後更新)
    }

    public void onDestroy(){
        super.onDestroy();
    }
    //按上一頁先關閉Timer
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            doAsynchronousTask.cancel();
            Toast t = Toast.makeText(MapsActivity.this,"!!!!",Toast.LENGTH_LONG);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    //actionbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                Intent SettingActivity = new Intent();
                SettingActivity.setClass(MapsActivity.this,SettingActivity.class);
                startActivity(SettingActivity);
                break;
            default:
                break;
        }
        return true;
    }

    private Button.OnClickListener myListner = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            testIsOnPath();
            DrawLine();
        }
    };
    private void testIsOnPath(){
        //印出提示訊息
        //Toast t = Toast.makeText(MapsActivity.this,"receiveID:"+receiveID+"\n",Toast.LENGTH_LONG);
        //Toast t = Toast.makeText(MapsActivity.this,temp.length,Toast.LENGTH_LONG);
        //t.show();
        //按下按鈕後會畫出路線

        testLatInput.setText(String.valueOf(test_loaction.latitude));
        testLngInput.setText(String.valueOf(test_loaction.longitude));
        //判斷誤差，在__公尺內提示
        if(PolyUtil.isLocationOnPath(test_loaction,Single_Path_Point_Info,false,tolerance)){
            Toast t = Toast.makeText(MapsActivity.this,"在範圍內",Toast.LENGTH_LONG);
            t.show();
        }
        else{
            Toast t = Toast.makeText(MapsActivity.this,"不在範圍內",Toast.LENGTH_LONG);
            t.show();
        }
    }
    public static double D_jw(double wd1,double jd1,double wd2,double jd2) //計算兩點距離(公尺)
    {
        double x,y,out;
        double PI=3.14159265;
        double R=6.371229*1e6;

        x=(jd2-jd1)*PI*R*Math.cos( ((wd1+wd2)/2) *PI/180)/180;
        y=(wd2-wd1)*PI*R/180;
        out=Math.hypot(x,y);
        return out;
    }
    public void DrawLine() {
        //畫出路徑
        int current_ID = GlobalVariable.machineID;
        int min_distance = 9999999;
        for(int i = 0 ; i < GeoPoint.size() ; ){
            PolylineOptions polylineOpt = new PolylineOptions();//要畫出的線段
            /*如何判斷不同條的路徑資訊?
                            GeoPoint是一個資料結構，包含"所有路徑"(並非單一路徑)的經度Lng、緯度Lat還有路徑ID
                            每一條完整的路徑都應該具有一個路徑編號
                       */
            boolean first_Point_flag = true;
            while(GeoPoint.get(i).id == current_ID){//一條一條路徑畫
                //第一個點的mark
                if(first_Point_flag == true){
                    StartPoint = new LatLng(GeoPoint.get(i).Lat,GeoPoint.get(i).Lng);
                    mMap.addMarker(new MarkerOptions().position(StartPoint).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).title("起點"));
                    first_Point_flag = false;
                    if( (int)D_jw(StartPoint.latitude,StartPoint.longitude,test_loaction.latitude,test_loaction.longitude) < min_distance)
                        min_distance = (int)D_jw(StartPoint.latitude,StartPoint.longitude,test_loaction.latitude,test_loaction.longitude);
                }
                //最後一個點的mark
                if(i+1 == GeoPoint.size()){
                    EndPoint = new LatLng(GeoPoint.get(i).Lat,GeoPoint.get(i).Lng);
                    mMap.addMarker(new MarkerOptions().position(EndPoint).icon((BitmapDescriptorFactory.fromResource(R.drawable.hostipal))).title("終點"));
                }
                else if(GeoPoint.get(i+1).id != current_ID){
                    EndPoint = new LatLng(GeoPoint.get(i).Lat,GeoPoint.get(i).Lng);
                    mMap.addMarker(new MarkerOptions().position(EndPoint).icon((BitmapDescriptorFactory.fromResource(R.drawable.hostipal))).title("終點"));
                }

                LatLng point = new LatLng(GeoPoint.get(i).Lat,GeoPoint.get(i).Lng);
                //Log.d("myTag",GeoPoint.get(i).Lng+ " " + GeoPoint.get(i).Lat+"\n");
                polylineOpt.add(point);
                i++;//下一個點
                if(i == GeoPoint.size()) {//最後一個點的判斷會throwIndexOutOfBoundsException
                    break;
                }
            }
            current_ID++;//下一條路徑
            //線條顏色
            Random rnd = new Random();
            polylineOpt.color(Color.rgb(rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256)));
            //線條寬度
            polylineOpt.width(13);

            mMap.addPolyline(polylineOpt);

            //tts
            //mTts.speak("最近的救護車距離您"+min_distance+"公尺", TextToSpeech.QUEUE_FLUSH, null,null);

        }
    }
    //定位
    @Override
    public void onLocationChanged(Location location) {
        // 取得座標值:緯度,經度
        //GPS更新，DEEMO因素先註解掉，可以再setting做個開關開啟功能
        /*
                LatLng cur_location = new LatLng(location.getLatitude(), location.getLongitude());
                //移掉前一個mark，重新定位
                if(!first_flag){
                    nowLocation_marker.remove();
                }
                first_flag = false;
                nowLocation_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(cur_location).title("目前位置"));
                nowLocation_marker.setDraggable(true);
                */
    }
    protected void onResume() {
        super.onResume();
        // 如果GPS或網路定位開啟，更新位置
        if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) || locMgr.isProviderEnabled(NETWORK_PROVIDER)) {
            //  確認 ACCESS_FINE_LOCATION 權限是否授權
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
            {
                //每秒更新一次位置
                locMgr.requestLocationUpdates(bestProv, 1000, 1, this);
            }
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //  確認 ACCESS_FINE_LOCATION 權限是否授權
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locMgr.removeUpdates(this);
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Criteria criteria = new Criteria();
        bestProv = locMgr.getBestProvider(criteria, true);
    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    //Timer
    Runnable renew_path_data_timer = new Runnable() {
        public void run() {
            try {
                GeoPoint.clear();
                Single_Path_Point_Info.clear();
                GeoStr = Servicetest.webSocket_input_allpath;
                String[] temp = GeoStr.split("\\$");//將字串切割成一條條路徑
                ArrayList<Integer> receiveID = new ArrayList<Integer>(); //儲存所有路徑的id編號 每一個元素都是一條的ID
                ArrayList<String> SingleGeoStr = new ArrayList<String>(); //儲存所有的所有 每一個元素都是一條完整路徑

                for(int i = 1 ; i < temp.length ; i+=2){
                    //將一條路徑的id跟中途點資訊add到ArrayList中
                    //Log.d("myTag", temp[i]+" "+temp[i+1]+" \n"+i+" "+temp.length);
                    receiveID.add( Integer.valueOf(temp[i]) );//第1  3 5 7 9...都是ID
                    SingleGeoStr.add(temp[i+1]);//2 4 6 8 10...都是路徑資訊
                }

                //分割一條完整的路徑，得到各中途點的經緯度
                for(int i = 0 ; i < SingleGeoStr.size() ; ++i) {
                    //分割成許多中途點 中途點間彼此用" "作為分割
                    String[] spiltGeoInfo = SingleGeoStr.get(i).split(" ");//中途點
                    /*
                                        //DEBUG用 查看被分割的字串，這裡分割的結果是單一路徑的所有點的經緯度
                                        for(int j = 0 ;j < spiltGeoInfo.length ; ++j){
                                        Log.d("spiltGeoInfo",spiltGeoInfo[j]+"\n");
                                        }*/

                    for(int j = 0 ; j < spiltGeoInfo.length ; ++j){
                        GeoInfo p = new GeoInfo();
                        p.id = receiveID.get(i);
                        //經緯度之間是用","作為分割
                        p.Lng = Double.parseDouble(spiltGeoInfo[j].split(",")[0]);
                        p.Lat = Double.parseDouble(spiltGeoInfo[j].split(",")[1]);
                        GeoPoint.add(p);
                        //判斷距離
                        Single_Path_Point_Info.add(new LatLng(p.Lat,p.Lng));
                        //Log.d("myTag",GeoPoint.get(j).id+" " + GeoPoint.get(j).Lng+ " " + GeoPoint.get(j).Lat +"\n");
                    }
                    DrawLine();
                }

            } catch (Exception e) {

            }
        }
    };
    public void callAsynchronousTask() {
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(renew_path_data_timer);
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every ___ ms

    }

    public static String get_Direction(int a,int b ){
        //a 表示前後  b 表示左右
        StringBuilder string = new StringBuilder();
        if (b == 1)
            string.append("右");
        else if (b == -1)
            string.append("左");

        if (a == 1)
            string.append("前");
        else if (a == -1)
            string.append("後");

        string.append("方");
        return string.toString();
    }
    //透過web更新client的位置
    public static void  chgDriverPosFromWeb(String webSocket_input_chgDriverPos){
        //Ex:(120.123,23.123)
        String Lat = webSocket_input_chgDriverPos.split("\\(")[1].split(",")[0];
        String Lng = webSocket_input_chgDriverPos.split(",")[1].split("\\)")[0];
        LatLng temp = new LatLng(Double.parseDouble(Lat),Double.parseDouble(Lng));
        Log.d("myTag",temp.latitude + " " + temp.longitude);
        //計算向量(判斷方向)
        if (!temp.equals(test_loaction)){ //位置有更新過
            LatLng last_loc = test_loaction;//取得前一個位置  在test_location
            //計算向量
            Car_direction [0] = temp.latitude-last_loc.latitude;
            Car_direction [1] = temp.longitude-last_loc.longitude;
        }
        test_loaction = temp; //更新位置

        //如果不是第一次定位就移掉前一個mark，重新定位
        if(!first_flag){
            nowLocation_marker.remove();
        }
        first_flag = false;
        nowLocation_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(test_loaction).title("目前位置"));
        nowLocation_marker.setDraggable(true);

        //判斷距離
        int min_distance = 9999999;
        int current_ID = 1;
        //[0][0]救護車相對駕駛第一點前後方 [0][1]救護車相對駕駛第一點左右方
        // [1][0]救護車相對駕駛第二點前後方 [1][1]救護車相對駕駛第二點左右方
        int site[][] = {{2,2},{2,2}};

        for(int i = 0 ; i < GeoPoint.size() ; ) {
            boolean first_Point_flag = true;
            while (GeoPoint.get(i).id == current_ID) {//一條一條路徑畫
                //第一個點的mark
                if (first_Point_flag == true) {
                    StartPoint = new LatLng(GeoPoint.get(i).Lat, GeoPoint.get(i).Lng);
                    first_Point_flag = false;
                    int temp_distance = (int) D_jw(StartPoint.latitude, StartPoint.longitude, test_loaction.latitude, test_loaction.longitude);

                    if (temp_distance < min_distance) {
                        min_distance = temp_distance;
                        //Ambulance Site to Car now
                        double between_Lat = GeoPoint.get(i).Lat - test_loaction.latitude;
                        double between_Lng = GeoPoint.get(i).Lng - test_loaction.longitude;

                        //第一點前後方判斷  用內積公式
                        double dot = between_Lat * Car_direction[0] + between_Lng * Car_direction[1];
                        double Car_direction_L = Math.sqrt(Math.pow(Car_direction[0], 2) + Math.pow(Car_direction[1], 2));
                        double between_direction_L = Math.sqrt(Math.pow(between_Lat, 2) + Math.pow(between_Lng, 2));
                        double theta = Math.toDegrees(Math.acos(dot / (Car_direction_L * between_direction_L)));
                        //Toast.makeText(MapsActivity.this," "+ theta, Toast.LENGTH_LONG).show();
                        if (theta < 67.5)
                            site[0][0] = 1;//前方
                        else if (theta > 112.5)
                            site[0][0] = -1;//後方
                        else
                            site[0][0] = 0;//無關前後  只關左右

                        //第一點左右方判斷  用外積公式及右手定則
                        double Cross_direction = between_Lng * Car_direction[0] - Car_direction[1] * between_Lat;
                        if (Cross_direction > 0 && (theta <= 157.5 && theta >= 22.5))
                            site[0][1] = 1;//左方
                        else if (Cross_direction < 0 && (theta <= 157.5 && theta >= 22.5))
                            site[0][1] = -1;//右方
                        else
                            site[0][1] = 0;//無關左右  只關前後


                        //第二點前後方判斷  用內積公式
                        between_Lat = GeoPoint.get(i + 1).Lat - test_loaction.latitude;
                        between_Lng = GeoPoint.get(i + 1).Lng - test_loaction.longitude;
                        dot = between_Lat * Car_direction[0] + between_Lng * Car_direction[1];
                        Car_direction_L = Math.sqrt(Math.pow(Car_direction[0], 2) + Math.pow(Car_direction[1], 2));
                        between_direction_L = Math.sqrt(Math.pow(between_Lat, 2) + Math.pow(between_Lng, 2));
                        theta = Math.toDegrees(Math.acos(dot / (Car_direction_L * between_direction_L)));
                        if (theta < 67.5)
                            site[1][0] = 1;//前方
                        else if (theta > 112.5)
                            site[1][0] = -1;//後方
                        else
                            site[1][0] = 0;//無關前後  只關左右

                        //第二點左右方判斷  用外積公式及右手定則
                        Cross_direction = between_Lng * Car_direction[0] - Car_direction[1] * between_Lat;
                        if (Cross_direction > 0 && (theta <= 157.5 && theta >= 22.5))
                            site[1][1] = 1;//左方
                        else if (Cross_direction < 0 && (theta <= 157.5 && theta >= 22.5))
                            site[1][1] = -1;//右方
                        else
                            site[1][1] = 0;//無關左右  只關前後
                    }

                }
                else {
                    LatLng now_pos = new LatLng(GeoPoint.get(i).Lat, GeoPoint.get(i).Lng);
                }
                i++;//下一個點
                if (i == GeoPoint.size()) {//最後一個點的判斷會throwIndexOutOfBoundsException
                    break;
                }
            }
            current_ID++;//下一條路徑
        }

        //Toast.makeText(MapsActivity.this, "第一點" +site[0][0] +" "+ site[0][1] +"\n第二點" +site[1][0] +" "+ site[1][1] , Toast.LENGTH_LONG).show();


        //tts
        if(min_distance == 9999999){
            //什麼都不做
        }
        else if ((site[0][0] == 0 && site[0][1] == 0) || (site[0][0] == 0 && site[0][1] == 0)){
            //Toast.makeText(MapsActivity.this, "[測試模式]有方向有問題", Toast.LENGTH_LONG).show();
        }
        //else if
        else {
            String First_direction = get_Direction(site[0][0],site[0][1]);
            String Second_direction = get_Direction(site[1][0],site[1][1]);
            if (First_direction.equals(Second_direction))
                mTts.speak("最近的救護車距離您"+min_distance+"公尺並且從您的"+First_direction+"向您靠近", TextToSpeech.QUEUE_FLUSH, null,null);
            else if ((site[0][0] + site[1][0] == 0) && (site[0][1] + site[1][1] == 0))
                mTts.speak("最近的救護車距離您"+min_distance+"公尺並且從您的"+First_direction+"向您靠近", TextToSpeech.QUEUE_FLUSH, null,null);
            else
                mTts.speak("最近的救護車距離您"+min_distance+"公尺並且從您的"+First_direction+"向您的"+Second_direction+"行駛", TextToSpeech.QUEUE_FLUSH, null,null);
        }
    }
}
