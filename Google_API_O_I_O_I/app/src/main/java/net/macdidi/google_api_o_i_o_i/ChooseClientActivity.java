package net.macdidi.google_api_o_i_o_i;

/**
 * Created by user on 2017/8/13.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ChooseClientActivity extends AppCompatActivity {
    private Button carClientBtn,ambulanceClientBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_client);
        //set icon with actionbar
        ActionBar menu = getSupportActionBar();
        menu.setSubtitle("道路避讓即時警示系統");
        menu.setDisplayShowHomeEnabled(true);
        menu.setIcon(R.mipmap.ic_launcher);

        carClientBtn = (Button)findViewById(R.id.button7);
        ambulanceClientBtn = (Button)findViewById(R.id.button6);
        carClientBtn.setOnClickListener(openMainActivity);
        ambulanceClientBtn.setOnClickListener(openMainActivity);
    }
    private Button.OnClickListener openMainActivity = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent Maps = new Intent();
            Maps.setClass(ChooseClientActivity.this,MainActivity.class);
            startActivity(Maps);
        }
    };
}
