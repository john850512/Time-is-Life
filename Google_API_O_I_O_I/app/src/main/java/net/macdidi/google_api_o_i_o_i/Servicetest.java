package net.macdidi.google_api_o_i_o_i;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class Servicetest extends Service implements TextToSpeech.OnInitListener{
    private WebSocketClient client;
    private Handler handler;
    public static String webSocket_input_allpath;
    public static String webSocket_input_hospital;
    private TextToSpeech mTts;//tts
    public Servicetest() {
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(Servicetest.this, "語音系統發生錯誤", Toast.LENGTH_LONG).show();
            }
        }
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
        callAsynchronousTask();
    }
    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);//Service沒有UIThread，要自己實做這個function
    }

    public void onDestroy(){
        closeConnect();//關閉連線
        handler.removeCallbacks(sent_request_timer);
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
                        client.send("[client ID:"+MainActivity.machineID+"]request allpath");
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
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
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
            client = new WebSocketClient(URI.create("ws://"+"192.168.1.143"+":"+"8080"+"/WebSocket/websocket")) {
                //UI更新必須使用mainthread或UI thread
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    //mTts.speak("與伺服器連線成功，功能已開啟", TextToSpeech.QUEUE_FLUSH, null,null);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Servicetest.this,"[WebSocket]連線成功",Toast.LENGTH_SHORT).show();
                            //连接成功
                            mTts.speak("功能已開啟，您可以再通知欄再次開啟程式", TextToSpeech.QUEUE_FLUSH, null,null);
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
                            if(s.split("]")[0].equals("[allpath")) webSocket_input_allpath = s.split("]")[1];
                            //Toast.makeText(Servicetest.this,webSocket_input_allpath,Toast.LENGTH_SHORT).show();
                            if(s.split("]")[0].equals("[hospital")) webSocket_input_hospital = s.split("]")[1];
                            //Toast.makeText(Servicetest.this,webSocket_input,Toast.LENGTH_SHORT).show();
                            Log.d("tag",s);
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
                }
                @Override
                public void onError(final Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Servicetest.this,"[WebSocket]連線失敗，請檢察連線設定",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
        }
    }
}
