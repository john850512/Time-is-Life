package net.macdidi.google_api_o_i_o_i;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

import static net.macdidi.google_api_o_i_o_i.GlobalVariable.cur_stat;
import static net.macdidi.google_api_o_i_o_i.GlobalVariable.hintContentText;
import static net.macdidi.google_api_o_i_o_i.GlobalVariable.hintContentTitle;
import static net.macdidi.google_api_o_i_o_i.GlobalVariable.hintText;
import static net.macdidi.google_api_o_i_o_i.R.id.imageView;
import static net.macdidi.google_api_o_i_o_i.Servicetest.closeConnectMsg;

public class MainActivity extends AppCompatActivity  {
    private static Button redbutton,greenbutton;
    private Switch switch1;
    private ImageView smallred;
    private ImageView smallgreen;



    long[] vibrate = {0,100,200,300};   //震動時間長度參數

    //Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); //音樂Uri參數
    // uri = Uri.parse("file:///sdcard/Notifications/hangout_ringtone.m4a");
    // uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ring);

    //tts
    private static TextToSpeech mTts;
    private static final int REQ_TTS_STATUS_CHECK = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set icon with actionbar
        ActionBar menu = getSupportActionBar();
        menu.setSubtitle("道路避讓即時警示系統");
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.ic_launcher);

        if(readReacord() == 0)firRecord();//第一次開啟APP(之前沒寫過檔案)

        redbutton = (Button)findViewById(R.id.button3);//Center Circle Button
        redbutton.setOnClickListener(openMapActivity);
        greenbutton= (Button)findViewById(R.id.button2);//Center Circle Button
        greenbutton.setOnClickListener(openMapActivity);
        greenbutton.setVisibility(View.VISIBLE);
        redbutton.setVisibility(View.INVISIBLE);

        smallred = (ImageView) findViewById(imageView);
        smallgreen = (ImageView) findViewById(R.id.imageView2);
        switch1 = (Switch)findViewById(R.id.switch1);
        switch1.setOnClickListener(switch_colorChange);

        //tts
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTts.setLanguage(Locale.CHINA);
                    mTts.setSpeechRate(GlobalVariable.speakRate);
                    mTts.speak("歡迎使用道路避讓即時警示系統，請點擊按鈕開啟功能", TextToSpeech.QUEUE_FLUSH, null, null);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "語音系統發生錯誤", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        //定位權限請求，先做之後就不用做了
        //定位
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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
                SettingActivity.setClass(MainActivity.this,SettingActivity.class);
                startActivity(SettingActivity);
                break;
            default:
                break;
        }
        return true;
    }
    private void firRecord(){
        SharedPreferences sharedPreferences = getSharedPreferences("Data" , MODE_PRIVATE);
        sharedPreferences.edit().putInt("fir_write", 1).apply();
        sharedPreferences.edit().putString("hintText", "訊息提示視窗").apply();
        sharedPreferences.edit().putString("hintContentTitle", "Time is Life").apply();
        sharedPreferences.edit().putString("hintContentText", "您有提示訊息請查看").apply();
        sharedPreferences.edit().putFloat("tolerance", 500.0f).apply();
        sharedPreferences.edit().putString("IP", "192.168.1.143").apply();
        sharedPreferences.edit().putInt("port", 8080).apply();
        sharedPreferences.edit().putInt("machineID", GlobalVariable.machineID).apply();
        Toast.makeText(MainActivity.this,"紀錄檔設定完成",Toast.LENGTH_SHORT).show();
    }
    private int readReacord(){
        //讀取數據，回傳1代表該手機已有xml檔；0代表手機尚未建立xml檔
        SharedPreferences sharedPreferences = getSharedPreferences("Data" , MODE_PRIVATE);
        int fir_write = 0;
        fir_write = sharedPreferences.getInt("fir_write",0);
        //Toast.makeText(MainActivity.this,String.valueOf(fir_write),Toast.LENGTH_SHORT).show();
        GlobalVariable.hintText = sharedPreferences.getString("hintText","訊息提示視窗");
        GlobalVariable.hintContentTitle = sharedPreferences.getString("hintContentTitle","Time is Life");
        GlobalVariable.hintContentText = sharedPreferences.getString("hintContentText","hintContentText");
        GlobalVariable.IP = sharedPreferences.getString("IP","192.168.1.143");
        GlobalVariable.port = sharedPreferences.getInt("port",8080);
        sharedPreferences.edit().putInt("machineID", GlobalVariable.machineID).apply();
        return fir_write;
    }
    public static void change_cur_state(){
        if(cur_stat == 0) {
            //正常狀態
            greenbutton.setVisibility(View.VISIBLE);
            redbutton.setVisibility(View.INVISIBLE);
        }
        else if(cur_stat == 1){
            //警示狀態
            greenbutton.setVisibility(View.INVISIBLE);
            redbutton.setVisibility(View.VISIBLE);
        }
    }
    private Button.OnClickListener openMapActivity = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            //開啟地圖
            Intent OpenMapActivity = new Intent();
            OpenMapActivity.setClass(MainActivity.this,MapsActivity.class);
            startActivity(OpenMapActivity);
        }
    };
    private Button.OnClickListener switch_colorChange = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(switch1.isChecked()){
                //Toast.makeText(MainActivity.this,"YES",Toast.LENGTH_SHORT).show();
                // int id = getResources().getIdentifier("@drawable/" + "smallgreenbutton.png", null, getPackageName());
                //red_green_switch.setImageResource(id);
                cur_stat = 0;
                smallred.setVisibility(View.INVISIBLE);
                smallgreen.setVisibility(View.VISIBLE);
                //switch1.setText("結束行駛");
                greenbutton.setText("系統正常執行中");

                animateButton_green();//開啟動畫
                startService(new Intent(MainActivity.this, Servicetest.class));// 開啟service
                notifyMsg();//開啟通知欄顯示
                //moveTaskToBack(true);//返回手機主頁面
            }
            else {
                //Toast.makeText(MainActivity.this,"NO",Toast.LENGTH_SHORT).show();
                //int id = getResources().getIdentifier("@drawable/" + "smallredbutton.png", null, getPackageName());
                //red_green_switch.setImageResource(id);

                cur_stat = -1;
                smallred.setVisibility(View.VISIBLE);
                smallgreen.setVisibility(View.INVISIBLE);
                greenbutton.setVisibility(View.VISIBLE);
                redbutton.setVisibility(View.INVISIBLE);
                //switch1.setText("開始行駛");
                greenbutton.setText("系統尚未執行");
                //發送關閉連線的訊息
                closeConnectMsg();
                //關閉service
                stopService(new Intent(MainActivity.this, Servicetest.class));
            }
        }
    };

    //通知欄顯示
    private void notifyMsg(){
        //步驟1 : 初始化NotificationManager，取得Notification服務
        NotificationManager myNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //步驟2 : 按下通知之後要執行的activity
        Intent notifyIntent = new Intent(MainActivity.this , MainActivity.class);
        //notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //notifyIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent appIntent = PendingIntent.getActivity(MainActivity.this,0,notifyIntent,0);

        //步驟3 : 建構notification
        Notification notification = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification.BigPictureStyle BigPicture = new Notification.BigPictureStyle();
            BigPicture.setBigContentTitle(hintText);
            //先來設定顯示的大圖片
            Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ambulancebigpic)).getBitmap();
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
                if(cur_stat == 1) animateButton_red();
                else if(cur_stat == 0) animateButton_green();
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
                if(cur_stat == 0) animateButton_green();
                else if(cur_stat == 1)  animateButton_red();
            }
        });
    }
    //權限請求的結果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 1 &&
                permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            Toast.makeText(MainActivity.this, "請開啟權限再使用本系統", Toast.LENGTH_LONG).show();
        }
    }
}