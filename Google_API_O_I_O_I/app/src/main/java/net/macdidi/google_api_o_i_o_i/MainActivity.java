package net.macdidi.google_api_o_i_o_i;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import static android.R.attr.port;
import static net.macdidi.google_api_o_i_o_i.R.id.button;
import static net.macdidi.google_api_o_i_o_i.R.id.imageView;

public class MainActivity extends AppCompatActivity  {
    private int socketerr = 0;
    private Button Notify_Button;
    private Button settingBtn;
    private Button redbutton,greenbutton;
    private Button aboutBtn;
    private ToggleButton toggleButton;
    private Switch switch1;

    //golbal variable(saved in xml)
    private String hintText;
    private String hintContentTitle;
    private String hintContentText;
    private String IP;
    private int port;
    private int cur_stat;//當前狀態對應的圓形按鈕顏色，-1代表未啟動，0代表正常(GREEN)，1代表有救護車經過(RED)

    ImageView smallred;
    ImageView smallgreen;
    Socket m_socket = null;
    String in = "";
    long[] vibrate = {0,100,200,300};   //震動時間長度參數

    //Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); //音樂Uri參數
    // uri = Uri.parse("file:///sdcard/Notifications/hangout_ringtone.m4a");
    // uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //檢查網路狀態
        checkNetStat();
        //設定ICON
        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.ic_launcher);

        Thread test = new Thread(clientSocket);
        test.start();

        if(readReacord() == 0)firRecord();//第一次開啟APP(之前沒寫過檔案)

        Notify_Button = (Button)findViewById(R.id.Notify_Button);//開啟上方訊息通知的按鈕
        Notify_Button.setOnClickListener(MsgNotice);
        settingBtn = (Button)findViewById(button);//Setting Button
        settingBtn.setOnClickListener(openSettingActivity);
        redbutton = (Button)findViewById(R.id.button3);//Center Circle Button
        redbutton.setOnClickListener(openMapActivity);
        greenbutton= (Button)findViewById(R.id.button2);//Center Circle Button
        greenbutton.setVisibility(View.VISIBLE);
        redbutton.setVisibility(View.INVISIBLE);

        smallred = (ImageView) findViewById(imageView);
        smallgreen = (ImageView) findViewById(R.id.imageView2);
        switch1 = (Switch)findViewById(R.id.switch1);
        switch1.setOnClickListener(switch_colorChange);
        aboutBtn = (Button)findViewById(R.id.About_us);
        aboutBtn.setOnClickListener(About);
        cur_stat = -1;
    }

    private void firRecord(){
        SharedPreferences sharedPreferences = getSharedPreferences("Data" , MODE_PRIVATE);
        sharedPreferences.edit().putInt("fir_write", 1).apply();
        sharedPreferences.edit().putString("hintText", "訊息提示視窗").apply();
        sharedPreferences.edit().putString("hintContentTitle", "Time is Life").apply();
        sharedPreferences.edit().putString("hintContentText", "您有提示訊息請查看").apply();
        sharedPreferences.edit().putFloat("tolerance", 10.0f).apply();
        sharedPreferences.edit().putString("IP", "127.0.0.1").apply();
        sharedPreferences.edit().putInt("port", 2222).apply();
        Toast.makeText(MainActivity.this,"紀錄檔設定完成",Toast.LENGTH_SHORT).show();
    }
    private int readReacord(){
        //讀取數據，回傳1代表該手機已有xml檔；0代表手機尚未建立xml檔
        SharedPreferences sharedPreferences = getSharedPreferences("Data" , MODE_PRIVATE);
        int fir_write = 0;
        fir_write = sharedPreferences.getInt("fir_write",0);
        //Toast.makeText(MainActivity.this,String.valueOf(fir_write),Toast.LENGTH_SHORT).show();
        hintText = sharedPreferences.getString("hintText","訊息提示視窗");
        hintContentTitle = sharedPreferences.getString("hintContentTitle","Time is Life");
        hintContentText = sharedPreferences.getString("hintContentText","hintContentText");
        IP = sharedPreferences.getString("IP","127.0.0.1");
        port = sharedPreferences.getInt("port",2222);
        return fir_write;
    }
    private Button.OnClickListener About = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(cur_stat == 0) {
                cur_stat = 1;
                greenbutton.setVisibility(View.VISIBLE);
                redbutton.setVisibility(View.GONE);
            }
            else if(cur_stat == 1){
                cur_stat = 0;
                greenbutton.setVisibility(View.GONE);
                redbutton.setVisibility(View.VISIBLE);
            }
        }
    };
    private Button.OnClickListener openSettingActivity = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent Maps = new Intent();
            Maps.setClass(MainActivity.this,SettingActivity.class);
            startActivity(Maps);
        }
    };
    private Button.OnClickListener openMapActivity = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent Maps = new Intent();
            Maps.setClass(MainActivity.this,MapsActivity.class);
            Maps.putExtra("geoinfo",in);
            startActivity(Maps);
        }
    };
    private Button.OnClickListener switch_colorChange = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(switch1.isChecked()){
                //Toast.makeText(MainActivity.this,"YES",Toast.LENGTH_SHORT).show();
                // int id = getResources().getIdentifier("@drawable/" + "smallgreenbutton.png", null, getPackageName());
                //red_green_switch.setImageResource(id);
                cur_stat = 1;
                smallred.setVisibility(View.GONE);
                smallgreen.setVisibility(View.VISIBLE);
                switch1.setText("結束行駛");
                greenbutton.setText("系統正常執行中");

                animateButton_green();
            }
            else {
                //Toast.makeText(MainActivity.this,"NO",Toast.LENGTH_SHORT).show();
                //int id = getResources().getIdentifier("@drawable/" + "smallredbutton.png", null, getPackageName());
                //red_green_switch.setImageResource(id);
                cur_stat = -1;
                smallred.setVisibility(View.VISIBLE);
                smallgreen.setVisibility(View.GONE);
                greenbutton.setVisibility(View.VISIBLE);
                redbutton.setVisibility(View.GONE);
                switch1.setText("開始行駛");
                greenbutton.setText("系統尚未執行");
            }
        }
    };
    //通知偵聽
    private Button.OnClickListener MsgNotice = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            //步驟1 : 初始化NotificationManager，取得Notification服務
            NotificationManager myNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            //步驟2 : 按下通知之後要執行的activity
            Intent notifyIntent = new Intent(MainActivity.this , MainActivity.class);
            //notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent appIntent = PendingIntent.getActivity(MainActivity.this,0,notifyIntent,0);

            //步驟3 : 建構notification
            Notification notification = null;

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                Notification.BigPictureStyle BigPicture = new Notification.BigPictureStyle();
                BigPicture.setBigContentTitle(hintText);
                //先來設定顯示的大圖片
                Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.test)).getBitmap();
                //丟進去
                BigPicture.bigPicture(bitmap);
                notification = new Notification.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher) //(小圖示)
                        .setLargeIcon(BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.ic_launcher)) // 下拉下拉清單裡面的圖示（大圖示）
                        .setTicker("通知訊息顯示在這裡")
                        .setWhen(System.currentTimeMillis()) //設置發生時間
                        .setAutoCancel(true)   // 設置通知被使用者點擊後是否清除  //notification.flags = Notification.FLAG_AUTO_CANCEL;
                        .setContentTitle(hintContentTitle)
                        .setContentText(hintContentText)
                        //.setOngoing(true)      //true使notification變為ongoing，用戶不能手動清除// notification.flags = Notification.FLAG_ONGOING_EVENT; notification.flags = Notification.FLAG_NO_CLEAR;
                        .setDefaults(Notification.DEFAULT_ALL) //使用所有默認值，比如聲音，震動，閃屏等等
                        //.setDefaults(Notification.DEFAULT_VIBRATE) //使用默認手機震動提示
                        //.setDefaults(Notification.DEFAULT_SOUND) //使用默認聲音提示
                        //.setDefaults(Notification.DEFAULT_LIGHTS) //使用默認閃光提示
                        //.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND) //使用默認閃光提示 與 默認聲音提示
                        .setContentIntent(appIntent)
                        .setVibrate(vibrate) //自訂震動長度
                        //.setSound(uri) //自訂鈴聲
                        //.setLights(0xff00ff00, 300, 1000) //自訂燈光閃爍 (ledARGB, ledOnMS, ledOffMS)
                        .setStyle(BigPicture)
                        .build();
            }
            // 將此通知放到通知欄的"Ongoing"即"正在運行"組中
            //notification.flags = Notification.FLAG_ONGOING_EVENT;

            // 表明在點擊了通知欄中的"清除通知"後，此通知不清除，
            // 經常與FLAG_ONGOING_EVENT一起使用
            //notification.flags = Notification.FLAG_NO_CLEAR;
            //notification.flags= Notification.FLAG_AUTO_CANCEL;
            //閃爍燈光
            //notification.flags = Notification.FLAG_SHOW_LIGHTS;
            // 重複的聲響,直到用戶響應。
            //notification.flags = Notification.FLAG_INSISTENT;
            // 把指定ID的通知持久的發送到狀態條上.
            myNotificationManager.notify(0, notification);
            // 取消以前顯示的一個指定ID的通知.假如是一個短暫的通知，
            // 試圖將之隱藏，假如是一個持久的通知，將之從狀態列中移走.
//              mNotificationManager.cancel(0);

            //取消以前顯示的所有通知.
//              mNotificationManager.cancelAll();
        }
    };


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(socketerr == 0)
            Toast.makeText(getApplicationContext(),in, Toast.LENGTH_LONG).show();
            else Toast.makeText(getApplicationContext(),"Cannot connect to server~" + in, Toast.LENGTH_LONG).show();
        }
    };

    Runnable clientSocket = new Runnable() {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(IP);
                Log.e("Socket", "Client: Connecting...");
                try {
                    m_socket= new Socket(serverAddr, port);
                    DataOutputStream output = new DataOutputStream( m_socket.getOutputStream() );
                    DataInputStream input = new DataInputStream( m_socket.getInputStream() );
                        //模式 0
                        output.writeUTF("0");//輸出模式 0 = 導航    1=車輛請求所有救護車路徑    2=教護車抵達刪除路徑
                        output.writeUTF("$1$22.734056,120.2836698  22.718653,120.307041");  // $救護車代號$  起點坐標終點坐標
                        in = input.readUTF(); //這裡是模式0  會獲得該救護車的導航路徑
                    //模式 1
                    //output.writeUTF("1");     //輸出模式
                    //in = input.readUTF();      //這裡是模式1  會獲得所有救護車路徑  格式 =>  #$救護車代號$路徑#$救護車代號$路徑
                                            //模式 2
                                            //output.writeUTF("2");    //輸出模式
                                           // output.writeUTF("1");    //救護車代號 => 之後server會刪掉該救護車的路徑
                    socketerr = 0;
                } catch (Exception e) {
                    Log.e("Socket", "Client: Error", e);
                    socketerr = 1;
                } finally {
                    m_socket.close();
                }
            } catch (Exception e) {
            }
            handler.sendEmptyMessage(0);
        }
    };
    //check Internet
    public void checkNetStat(){
        //先取得此CONNECTIVITY_SERVICE
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        //取得網路相關資訊
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        //判斷是否有網路
        if (networkInfo == null || !networkInfo.isConnected()){
            Toast.makeText(MainActivity.this,"請確認可連上網路在執行本程式",Toast.LENGTH_SHORT).show();
            MainActivity.this.finish();
        }
    }
    //anim
    public void animateButton_red() {
        // Load the animation
        // anim參數設定
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        double animationDuration = 1.6 * 1000;
        myAnim.setDuration((long)animationDuration);
        // Use custom animation interpolator to achieve the bounce effect
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.14, 30.0);//Amplitude,Frequency
        myAnim.setInterpolator(interpolator);



        // Animate the button
        //Button button = (Button)findViewById(R.id.button3);
        redbutton.startAnimation(myAnim);
        // Run button animation again after it finished
        myAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {}
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            @Override
            public void onAnimationEnd(Animation arg0) {
                if(cur_stat == 0) animateButton_red();
                else if(cur_stat == 1) animateButton_green();
            }
        });
    }
    public void animateButton_green() {
        // Load the animation
        // anim參數設定
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        double animationDuration = 3.0 * 1000;
        myAnim.setDuration((long)animationDuration);
        // Use custom animation interpolator to achieve the bounce effect
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.14, 30.0);//Amplitude,Frequency
        myAnim.setInterpolator(interpolator);

        // Animate the button
        //Button button = (Button)findViewById(R.id.button3);
        greenbutton.startAnimation(myAnim);
        // Run button animation again after it finished
        myAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {}
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            @Override
            public void onAnimationEnd(Animation arg0) {
                if(cur_stat == 1) animateButton_green();
                else if(cur_stat == 0)  animateButton_red();
            }
        });
    }
}