package net.macdidi.google_api_o_i_o_i;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.LocationManager.NETWORK_PROVIDER;
import static net.macdidi.google_api_o_i_o_i.MapsActivity.D_jw;
import static net.macdidi.google_api_o_i_o_i.MapsActivity.get_Direction;


public class Servicetest extends Service implements TextToSpeech.OnInitListener, LocationListener {
    private static WebSocketClient client;
    private Handler handler;
    public static String webSocket_input_allpath;
    public static String webSocket_input_hospital;
    public static String webSocket_input_chgDriverPos;
    private TextToSpeech mTts;//tts

    //定位
    private LocationManager locMgr;
    private String bestProv;
    private LatLng cur_internet_location;
    private LatLng cur_GPS_location;
    //Timer
    Timer timer;
    TimerTask doAsynchronousTask;
    private boolean fir_flag = true;
    //字串切割&判斷方向
    private String GeoStr = "";
    //單一個點的資料結構:ID、經度、緯度
    public static class GeoInfo {
        //constructor
        GeoInfo() {
            id = 0;
            Lat = 0;
            Lng = 0;
            path = new ArrayList<>();
        }
        ArrayList<LatLng> path ;
        int id;//路徑編號
        double Lat;
        double Lng;
    };
    public static ArrayList<GeoInfo> GeoPoint = new ArrayList<GeoInfo>();//所有點(包含不同路徑)的LIST
    public static LatLng StartPoint;//路線起點
    //一般用路人向量
    private static double Car_direction[] = {0,0};
    //test
    private static LatLng test_loaction;
    public double tolerance;
    public static ArrayList<LatLng> Single_Path_Point_Info = new ArrayList<LatLng>();//isOnPathLocation Function的參數(一條路徑)
    private static boolean first_flag;//是否第一次定位
    public static LatLng temp = new LatLng(0,0);

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.CHINA);
            mTts.setSpeechRate(GlobalVariable.speakRate);
            mTts.speak("功能已開啟，您可以再通知欄再次開啟程式", TextToSpeech.QUEUE_FLUSH, null,null);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(Servicetest.this, "語音系統發生錯誤", Toast.LENGTH_LONG).show();
            }
        }
        //set data
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        tolerance = sharedPreferences.getFloat("tolerance", 15.0f);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        handler = new Handler();
        mTts = new TextToSpeech(this, this);
        try {
            initSocketClient();
        } catch (URISyntaxException e) {
            Toast.makeText(Servicetest.this,"[WebSocket]初始化失敗"+e,Toast.LENGTH_SHORT).show();
        }
        connect();

        //定位
        locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        bestProv = locMgr.getBestProvider(criteria, true);
        //確認有無權限，權限許可無法在Service請求，先在MainActivity做好
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

        //第一次用網路位而不是用gps，不然會return null
        Log.d("tag","GPS");
        Location location = locMgr.getLastKnownLocation(NETWORK_PROVIDER);
        cur_internet_location = new LatLng(location.getLatitude(),location.getLongitude());
        cur_GPS_location = cur_internet_location;
        test_loaction = cur_internet_location;
        //Toast.makeText(this, cur_internet_location.latitude+" "+cur_internet_location.longitude, Toast.LENGTH_SHORT).show();

        callAsynchronousTask();//定時呼叫傳送訊息的function
        GlobalVariable.is_MapActivity_open = false;
    }
    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);//Service沒有UIThread，要自己實做這個function
    }

    public void onDestroy(){
        closeConnect();//關閉連線
        handler.removeCallbacks(sent_request_timer);
        doAsynchronousTask.cancel();
        Toast.makeText(this, "Service stop", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }
    //Timer

    Runnable sent_request_timer = new Runnable() {
        public void run() {
            try {
                if(client != null) {//先確定websocket是否初始化
                    //定期發送請求路徑指令給server
                    if (client.isOpen()) {
                        client.send("[client ID:"+GlobalVariable.machineID+"]request allpath");
                        //一般車輛會傳送當前位置，救護車端不用(記得註解掉)
                        //如果是用真實GPS模式就會定期傳送
                        if(fir_flag){
                            client.send("[client ID:"+GlobalVariable.machineID+"]send itselfPosition:"+cur_internet_location);
                            fir_flag = false;
                        }
                        if(!GlobalVariable.is_simulateGPS){//如果不是demo模式就 一直傳真正的GPS位置
                            client.send("[client ID:"+GlobalVariable.machineID+"]send itselfPosition:"+cur_GPS_location);
                        }
                    } else {
                        //Toast.makeText(Servicetest.this, "[WebSocket]尚未連線", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Toast.makeText(Servicetest.this, "[WebSocket]尚未連線", Toast.LENGTH_SHORT).show();
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
                handler.post(sent_request_timer);
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every ___ ms
    }
    //连接
    private void connect() {
        new Thread(){
            @Override
            public void run() {
                client.connect();
            }
        }.start();
    }
    //断开连接
    private void closeConnect() {
        try {
            client.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            //websocket server 和 client 都不可reuse
            client = null;
        }
    }
    //websocket 初始化
    private void initSocketClient() throws URISyntaxException {
        if(client == null){
            client = new WebSocketClient(URI.create("ws://"+GlobalVariable.IP+":"+GlobalVariable.port+"/WebSocket/websocket")) {
                //UI更新必須使用mainthread或UI thread
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    //mTts.speak("與伺服器連線成功，功能已開啟", TextToSpeech.QUEUE_FLUSH, null,null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            client.send("[client ID:" + GlobalVariable.machineID + "]clientOnOpen");
                            Toast.makeText(Servicetest.this,"[WebSocket]連線成功",Toast.LENGTH_SHORT).show();
                            Log.d("tag","opened connection");
                        }
                    });
                }
                @Override
                public void onMessage(final String s) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(Servicetest.this,s,Toast.LENGTH_SHORT).show();
                            //字串切割
                            //Toast.makeText(Servicetest.this,s,Toast.LENGTH_SHORT).show();
                            if(!s.equals("[allpath]")){
                                if (s.split("]")[0].equals("[allpath")) {
                                    webSocket_input_allpath = s.split("]")[1];
                                }
                            }
                            if(!s.equals("[hospital]")) {
                                //Toast.makeText(Servicetest.this,webSocket_input_allpath,Toast.LENGTH_SHORT).show();
                                if (s.split("]")[0].equals("[hospital"))
                                    webSocket_input_hospital = s.split("]")[1];
                                //Toast.makeText(Servicetest.this,webSocket_input,Toast.LENGTH_SHORT).show();
                            }
                            if (s.split(":")[0].equals("[web]chgDriverPos")){
                                webSocket_input_chgDriverPos = s.split(":")[1];
                                if(GlobalVariable.is_simulateGPS){
                                    string_judge();
                                    chgDriverPosFromWeb(webSocket_input_chgDriverPos);
                                    if(GlobalVariable.is_MapActivity_open == true) {
                                        MapsActivity.chgDriverPosFromWeb(webSocket_input_chgDriverPos);
                                    }
                                }
                            }
                            Log.d("tag", s);
                            //服务端消息
                                /*
                                                                initmsg += s + "\n";
                                                                Message msg = new Message();
                                                                msg.what = 1;
                                                                myhandler.sendMessage(msg);
                                                                Log.d(tag,"received:" + s);
                                                                */
                        }
                    });
                }
                @Override
                public void onClose(int i, String s, boolean remote) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Servicetest.this,"[WebSocket]關閉連線",Toast.LENGTH_SHORT).show();

                            //连接断开，remote判定是客户端断开还是服务端断开
                            //Log.d(tag,"Connection closed by " + ( remote ? "remote peer" : "us" ) + ", info=" + s);
                            //
                            //closeConnect();
                        }
                    });
                    handler.removeCallbacks(sent_request_timer);
                    doAsynchronousTask.cancel();
                }
                @Override
                public void onError(final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Servicetest.this,"[WebSocket]連線失敗，請檢察連線設定",Toast.LENGTH_SHORT).show();
                        }
                    });
                    handler.removeCallbacks(sent_request_timer);
                    doAsynchronousTask.cancel();
                }
            };
        }
    }
    //字串切割
    private void string_judge(){
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
            GeoInfo p = new GeoInfo();
            p.id = receiveID.get(i);
            for (int j = 0; j < spiltGeoInfo.length; ++j) {
                //經緯度之間是用","作為分割
                p.Lng = Double.parseDouble(spiltGeoInfo[j].split(",")[0]);
                p.Lat = Double.parseDouble(spiltGeoInfo[j].split(",")[1]);
                p.path.add(new LatLng(p.Lat,p.Lng));

                //判斷距離
                Single_Path_Point_Info.add(new LatLng(p.Lat, p.Lng));
                //Log.d("myTag",GeoPoint.get(j).id+" " + GeoPoint.get(j).Lng+ " " + GeoPoint.get(j).Lat +"\n");
            }
            GeoPoint.add(p);
        }
    }

    //透過web更新client的位置
    public void  chgDriverPosFromWeb(String webSocket_input_chgDriverPos){
        //Ex:(120.123,23.123)
        String Lat = webSocket_input_chgDriverPos.split("\\(")[1].split(",")[0];
        String Lng = webSocket_input_chgDriverPos.split(",")[1].split("\\)")[0];
        temp = new LatLng(Double.parseDouble(Lat),Double.parseDouble(Lng));
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

        }
        first_flag = false;

        //判斷距離
        int min_distance = 9999999;
        int current_ID = 1;
        //[0][0]救護車相對駕駛第一點前後方 [0][1]救護車相對駕駛第一點左右方
        // [1][0]救護車相對駕駛第二點前後方 [1][1]救護車相對駕駛第二點左右方
        int site[][] = {{2,2},{2,2}};

        double between_direction_L=0;
        double between_direction_L_next=0;
        Log.d("myTag","before for");

        for(int i = 0 ; i < GeoPoint.size() ; i++) {
            boolean first_Point_flag = true;
            //Log.d("myTag","i");
            for(int j=0 ; j<GeoPoint.get(i).path.size() ; j++){
                //第一個點的mark
                if (first_Point_flag == true) {
                    StartPoint = new LatLng(GeoPoint.get(i).path.get(j).latitude, GeoPoint.get(i).path.get(j).longitude);
                    first_Point_flag = false;
                    int temp_distance = (int) D_jw(StartPoint.latitude, StartPoint.longitude, test_loaction.latitude, test_loaction.longitude);

                    if (temp_distance < min_distance) {
                        min_distance = temp_distance;
                        //Ambulance Site to Car now
                        double between_Lat = GeoPoint.get(i).path.get(j).latitude - test_loaction.latitude;
                        double between_Lng = GeoPoint.get(i).path.get(j).longitude - test_loaction.longitude;

                        //第一點前後方判斷  用內積公式
                        double dot = between_Lat * Car_direction[0] + between_Lng * Car_direction[1];
                        double Car_direction_L = Math.sqrt(Math.pow(Car_direction[0], 2) + Math.pow(Car_direction[1], 2));
                        between_direction_L = Math.sqrt(Math.pow(between_Lat, 2) + Math.pow(between_Lng, 2));
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
                        between_Lat = GeoPoint.get(i).path.get(j+1).latitude - test_loaction.latitude;
                        between_Lng = GeoPoint.get(i).path.get(j+1).longitude - test_loaction.longitude;
                        dot = between_Lat * Car_direction[0] + between_Lng * Car_direction[1];
                        Car_direction_L = Math.sqrt(Math.pow(Car_direction[0], 2) + Math.pow(Car_direction[1], 2));
                        between_direction_L_next = Math.sqrt(Math.pow(between_Lat, 2) + Math.pow(between_Lng, 2));
                        theta = Math.toDegrees(Math.acos(dot / (Car_direction_L * between_direction_L_next)));
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
                    LatLng now_pos = new LatLng(GeoPoint.get(i).path.get(j).latitude, GeoPoint.get(i).path.get(j).longitude);
                }

                if (j == GeoPoint.get(i).path.size()) {//最後一個點的判斷會throwIndexOutOfBoundsException
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
            if(min_distance < tolerance) {

                if (First_direction.equals(Second_direction) && between_direction_L > between_direction_L_next)
                    mTts.speak("最近的救護車距離您" + min_distance + "公尺並且從您的" + First_direction + "向您靠近", TextToSpeech.QUEUE_FLUSH, null, null);
                else if (First_direction.equals(Second_direction) && between_direction_L < between_direction_L_next) {/*ignored*/} else if (site[0][0] == site[1][0] && site[0][0] == -1) {/*都是後方 距離沒縮短 ignored*/} else if (site[0][0] == site[1][0] && site[0][0] == 1 && site[0][1] != site[1][1])
                    mTts.speak("最近的救護車距離您" + min_distance + "公尺並且從您的" + First_direction + "向您的" + Second_direction + "行駛", TextToSpeech.QUEUE_FLUSH, null, null);
                else if (site[0][0] == 1 && site[1][0] == 0 && site[0][1] != site[1][1])
                    mTts.speak("最近的救護車距離您" + min_distance + "公尺並且從您的" + First_direction + "向您的" + Second_direction + "行駛", TextToSpeech.QUEUE_FLUSH, null, null);
                else if (site[0][0] == 0 && site[1][0] == 1 && site[0][1] != site[1][1])
                    mTts.speak("最近的救護車距離您" + min_distance + "公尺並且從您的" + First_direction + "向您的" + Second_direction + "行駛", TextToSpeech.QUEUE_FLUSH, null, null);
                else if ((site[0][0] == 0 && site[1][0] == -1) || (site[1][0] == 0 && site[0][0] == -1) || ((site[0][1] == site[1][1]) && site[0][1] != 0)) {/*ignored*/} else if ((site[0][0] == 1 && site[0][1] == 0 && site[1][1] != 0) || (site[1][0] == 1 && site[1][1] == 0 && site[0][1] != 0))
                    mTts.speak("最近的救護車距離您" + min_distance + "公尺並且從您的" + First_direction + "向您的" + Second_direction + "行駛", TextToSpeech.QUEUE_FLUSH, null, null);
                else if ((site[0][0] == -1 && site[0][1] == 0 && site[1][1] != 0) || (site[1][0] == -1 && site[1][1] == 0 && site[0][1] != 0)) {/*ignored*/} else
                    mTts.speak("最近的救護車距離您" + min_distance + "公尺並且從您的" + First_direction + "向您靠近", TextToSpeech.QUEUE_FLUSH, null, null);

                //講話RRRRRRRRRRRRRRRRRRRRRR
                GlobalVariable.cur_stat = 1;
                MainActivity.change_cur_state();
            }
            else{
                GlobalVariable.cur_stat = 0;
                MainActivity.change_cur_state();
            }
        }
    }
    //定位
    @Override
    public void onLocationChanged(Location location) {
        // 取得座標值:緯度,經度
        if(!GlobalVariable.is_simulateGPS) {
            cur_GPS_location = new LatLng(location.getLatitude(), location.getLongitude());
            Toast.makeText(this, cur_GPS_location.latitude + " " + cur_GPS_location.longitude, Toast.LENGTH_SHORT).show();
        }
    }
    //simulateGPS
    public static void sendSimulateGPS(LatLng position){
        if (client.isOpen()) {
            client.send("[client ID:" + GlobalVariable.machineID + "]send itselfPosition: (" + position.latitude + "," + position.longitude +")");
        }
    }
    //關閉系統功能時發送訊息給server，清空網頁的車輛marker
    public static void closeConnectMsg(){
        if (client.isOpen()) {
            client.send("[client ID:" + GlobalVariable.machineID + "]close Connection");
        }
    }
}
