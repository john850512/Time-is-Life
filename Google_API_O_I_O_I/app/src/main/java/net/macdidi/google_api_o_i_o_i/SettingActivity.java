package net.macdidi.google_api_o_i_o_i;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static net.macdidi.google_api_o_i_o_i.R.id.testLatInput;

public class SettingActivity extends AppCompatActivity {
    private EditText EditText00,EditText01,EditText02,EditText03;
    private Button SettingBtn,RecoverBtn;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting2);
        //set icon with actionbar
        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.ic_launcher);
        EditText00 = (EditText) findViewById(R.id.editText00);
        EditText01 = (EditText) findViewById(R.id.editText01);
        EditText02 = (EditText) findViewById(R.id.editText02);
        EditText03 = (EditText) findViewById(R.id.editText03);
        SettingBtn = (Button) findViewById(R.id.button4);
        SettingBtn.setOnClickListener(Setting);
        RecoverBtn = (Button) findViewById(R.id.button5);
        RecoverBtn.setOnClickListener(Recover);
        //設置edit text中的value
        sharedPreferences = getSharedPreferences("Data" , MODE_PRIVATE);
        EditText00.setText(sharedPreferences.getString("hintText","Not Value"));
        EditText01.setText(sharedPreferences.getString("hintContentTitle","Not Value"));
        EditText02.setText(sharedPreferences.getString("hintContentText","Not Value"));
        EditText03.setText(String.valueOf(sharedPreferences.getFloat("tolerance",10.0f)));
    }
    private Button.OnClickListener Setting = new Button.OnClickListener(){
        public void onClick(View v){
            sharedPreferences.edit().putString("hintText", EditText00.getText().toString()).apply();
            sharedPreferences.edit().putString("hintContentTitle", EditText01.getText().toString()).apply();
            sharedPreferences.edit().putString("hintContentText",EditText02.getText().toString()).apply();
            sharedPreferences.edit().putFloat("tolerance", Float.parseFloat(EditText03.getText().toString())).apply();

            Intent Main = new Intent();
            Main.setClass(SettingActivity.this,MainActivity.class);
            startActivity(Main);
            SettingActivity.this.finish();
        }
    };
    private Button.OnClickListener Recover = new Button.OnClickListener(){
        public void onClick(View v){
            sharedPreferences.edit().putString("hintText", "訊息提示視窗").apply();
            sharedPreferences.edit().putString("hintContentTitle", "Time is Life").apply();
            sharedPreferences.edit().putString("hintContentText", "您有提示訊息請查看").apply();
            sharedPreferences.edit().putFloat("tolerance", 10.0f).apply();
            Intent Main = new Intent();
            Main.setClass(SettingActivity.this,MainActivity.class);
            startActivity(Main);
            SettingActivity.this.finish();
        }
    };

}
