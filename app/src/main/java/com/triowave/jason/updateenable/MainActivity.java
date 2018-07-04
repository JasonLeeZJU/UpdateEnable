package com.triowave.jason.updateenable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long downloadId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CheckBox mCheckBox = findViewById(R.id.AutoUpateCheck);
        final SharedPreferences sharedPreferences = getSharedPreferences("autoUpdateCheck",MODE_PRIVATE);
        mCheckBox.setChecked(sharedPreferences.getBoolean("autoUpdateCheckState",true));

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("autoUpdateCheckState",!sharedPreferences.getBoolean
                        ("autoUpdateCheckState",false));
                edit.apply();
            }
        });

        updateStart();
   }

   private BroadcastReceiver mReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
           Log.i(TAG, "onReceive: 接收到结束");
       }
   };


    private void updateStart() {
            CheckBox checkBox = findViewById(R.id.AutoUpateCheck);
            if (checkBox.isChecked()) {
                //Log.i(TAG, "updateStart: 自动检查更新打开，自动检查更新");
                Toast.makeText(this, R.string.start_auto_update, Toast.LENGTH_SHORT).show();
                String jsonHttpURL = getString(R.string.update_json_address);
                UpdateAppUtils.checkAndUpdate(this, jsonHttpURL);
            }
        }


    public void buttonOnClick(View view) {
        Toast.makeText(this, "开始检查更新", Toast.LENGTH_SHORT).show();
        String jsonHttpURL = getString(R.string.update_json_address);
        UpdateAppUtils.checkAndUpdate(this, jsonHttpURL);
    }

}
