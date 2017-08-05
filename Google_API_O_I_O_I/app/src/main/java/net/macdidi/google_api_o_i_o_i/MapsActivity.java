package net.macdidi.google_api_o_i_o_i;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Map;
import java.util.Random;

import static android.view.View.X;
import static android.view.View.Y;
import static net.macdidi.google_api_o_i_o_i.R.id.map;

import android.location.LocationListener;
import android.location.LocationManager;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {
    //中途點的經緯度資訊
    //一筆的格式為:$ID$經度:緯度 經度:緯度 ....
    private String temp_geoStr = "$1$120.29035,22.73327 120.29050000000001,22.733300000000003 120.29055000000001,22.73253 120.29054000000001,22.73243 120.29068000000001,22.732480000000002 120.29127000000001,22.732450000000004 120.29151000000002,22.73243 120.29240000000001,22.73222 120.29207000000001,22.729870000000002 120.29196,22.728890000000003 120.29167000000001,22.726820000000004 120.29131000000001,22.724230000000002 120.29117000000001,22.72315 120.29104000000001,22.72212 120.29102,22.721670000000003 120.29106000000002,22.721380000000003 120.29125,22.72068 120.29160000000002,22.71949 120.29191000000002,22.718500000000002 120.29219,22.71769 120.29256000000001,22.716730000000002 120.29288000000001,22.715940000000003 120.29362,22.714250000000003 120.29397000000002,22.71339 120.29418000000001,22.71282 120.29432000000001,22.712470000000003 120.29447,22.71226 120.29506,22.71159 120.29556000000001,22.711000000000002 120.29610000000001,22.710400000000003 120.29692000000001,22.70954 120.29748000000001,22.70898 120.29778,22.708740000000002 120.29806,22.708540000000003 120.29841,22.70832 120.29869000000001,22.708190000000002 120.299,22.708060000000003 120.29932000000001,22.70794 120.29959000000001,22.707880000000003 120.29984,22.70785 120.30096,22.70786 120.30213,22.707890000000003 120.30239000000002,22.707900000000002 120.30239000000002,22.70812 120.30238000000001,22.708380000000002 120.30238000000001,22.70849";
    private static String GeoStr = "";

    //單一個點的資料結構:ID、經度、緯度
    class GeoInfo {
        int id;//路徑編號
        double Lat;
        double Lng;

        //constructor
        GeoInfo() {
            id = 0;
            Lat = 0;
            Lng = 0;
        }
    }

    ;
    private ArrayList<GeoInfo> GeoPoint = new ArrayList<GeoInfo>();//所有點(包含不同路徑)的LIST
    private GoogleMap mMap;
    private Button btn;
    private LatLng StartPoint, EndPoint;//路線起點、終點marker

    //test
    private EditText testLatInput, testLngInput;
    private double tolerance;
    private ArrayList<LatLng> Single_Path_Point_Info = new ArrayList<LatLng>();//isOnPathLocation Function的參數(一條路徑)
    //定位
    private Marker nowLocation_marker;
    private LocationManager locMgr;
    private String bestProv;
    private boolean first_flag;//是否第一次定位
    //tts
    private TextToSpeech mTts;
    private static final int REQ_TTS_STATUS_CHECK = 0;

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
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.ic_launcher);
        //set data
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        tolerance = sharedPreferences.getFloat("tolerance", 15.0f);
        //get data from MainActivity
        Bundle params = getIntent().getExtras();
        if (params != null) {
            GeoStr = params.getString("geoinfo");
        } else {
            Toast.makeText(MapsActivity.this, "[錯誤訊息]無法取得導航路徑", Toast.LENGTH_SHORT).show();
        }
        //定位
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
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //定位
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

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
        }
        //地圖中心移到目前位置
        Location location = locMgr.getLastKnownLocation(bestProv);
        LatLng cur_location = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cur_location,13));//中心是起點
        //移掉前一個mark，重新定位
        if(!first_flag){
            nowLocation_marker.remove();
        }
        first_flag = false;
        nowLocation_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(cur_location).title("目前位置"));
        nowLocation_marker.setDraggable(true);
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
            //印出提示訊息
            //Toast t = Toast.makeText(MapsActivity.this,"receiveID:"+receiveID+"\n",Toast.LENGTH_LONG);
            //Toast t = Toast.makeText(MapsActivity.this,temp.length,Toast.LENGTH_LONG);
            //t.show();
            //按下按鈕後會畫出路線
            DrawLine();

            //判斷誤差
            LatLng testPoint = new LatLng(Double.parseDouble(testLatInput.getText().toString()),
                                            Double.parseDouble(testLngInput.getText().toString()));
            //在__公尺內提示
            if(PolyUtil.isLocationOnPath(testPoint,Single_Path_Point_Info,false,tolerance)){
                Toast t = Toast.makeText(MapsActivity.this,"在範圍內",Toast.LENGTH_LONG);
                t.show();
            }
            else{
                Toast t = Toast.makeText(MapsActivity.this,"不在範圍內",Toast.LENGTH_LONG);
                t.show();
            }
        }
    };
    public void DrawLine() {
        //畫出路徑
        int current_ID = 1;
        for(int i = 0 ; i < GeoPoint.size() ; ){
            PolylineOptions polylineOpt = new PolylineOptions();//要畫出的線段
            /*如何判斷不同條的路徑資訊?
                            GeoPoint是一個資料結構，包含"所有路徑"(並非單一路徑)的經度Lng、緯度Lat還有路徑ID
                            每一條完整的路徑都應該具有一個路徑編號
                       */
            boolean first_Point_flag = true;
            while(GeoPoint.get(i).id == current_ID){
                //第一個點的mark
                if(first_Point_flag == true){
                    StartPoint = new LatLng(GeoPoint.get(i).Lat,GeoPoint.get(i).Lng);
                    mMap.addMarker(new MarkerOptions().position(StartPoint).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)).title("起點"));
                    first_Point_flag = false;
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
        }
    }
    //定位
    @Override
    public void onLocationChanged(Location location) {
        // 取得座標值:緯度,經度
        LatLng cur_location = new LatLng(location.getLatitude(), location.getLongitude());
        //移掉前一個mark，重新定位
        if(!first_flag){
            nowLocation_marker.remove();
        }
        first_flag = false;
        nowLocation_marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(cur_location).title("目前位置"));
        nowLocation_marker.setDraggable(true);
    }
    protected void onResume() {
        super.onResume();
        // 如果GPS或網路定位開啟，更新位置
        if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER) || locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //為了demo 1秒更新一次位置
            locMgr.requestLocationUpdates(bestProv, 1000, 1, this);
        } else {
            // Permission was denied. Display an error message.
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
}
