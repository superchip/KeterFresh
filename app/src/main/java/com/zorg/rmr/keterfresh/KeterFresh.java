package com.zorg.rmr.keterfresh;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Seconds;

public class KeterFresh extends AppCompatActivity
{
    //-----------Instance---------------//

    static final int BT_TURNON_REQUEST = 15;  // The request code

    private static KeterFresh ins;

    private boolean m_active = true;


    //-------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Do this everytime the app created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keter_fresh);
        ins = this;

        // Init UI
        KeterFreshUIManager.Instance().Init();


        // Init BT Device Manager
        TimeManagerService.BTDeviceMangerInsance().Init(this);



        // Turning on bluetooth
        if(!TimeManagerService.BTDeviceMangerInsance().BTAdapter().isEnabled())
        {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivityForResult(turnOn, BT_TURNON_REQUEST);
        }


        // If Service is not running, begin all
        if(isMyServiceRunning(TimeManagerService.class)) return;

        // Timer Service Init
        startService(new Intent(getBaseContext(), TimeManagerService.class));

        // Notifications init
        KeterFreshNotifier.Instance().Init(this,(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));


        // For Testing Purposes - Registering bt devices

        // Reseting
        TimeManagerService.BTDeviceMangerInsance().ClearAllDevices();
        KeterFreshNotifier.Instance().ClearAll();



/*
        KeterFreshBTDevice btDToRegister = new KeterFreshBTDevice("625kfagd", "006");
        TimeManagerService.BTDeviceMangerInsance().RegisterDevice(btDToRegister);

        KeterFreshBTDevice btDToRegister1 = new KeterFreshBTDevice("fsiuh323j", "045");
        TimeManagerService.BTDeviceMangerInsance().RegisterDevice(btDToRegister1);
*/
        KeterFreshBTDevice btDToRegister2 = new KeterFreshBTDevice("Mom's TakeAway", "032");
        TimeManagerService.BTDeviceMangerInsance().RegisterDevice(btDToRegister2);

    }// end on create

    //-------------------------------//

    public static KeterFresh  getInstace(){
        return ins;
    }

    //-------------------------------//

    public boolean IsActive()
    {
        return m_active;
    }

    protected void onStop()
    {
        super.onStop();
        m_active = false;
    }

    protected void onPause() {
        super.onPause();
        m_active = false;
    }

    protected void onResume()
    {
        super.onResume();
        m_active = true;
    }

    protected void onStart()
    {
        super.onStart();
        m_active = true;
    }

    //-------------------------------//

    public void updateTheTextView(final String t) {
        KeterFresh.this.runOnUiThread(new Runnable() {
            public void run() {
                TextView textV1 = (TextView) findViewById(R.id.btDisplay);
                textV1.setText(t);
            }
        });
    }

    //-------------------------------//

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //-------------------------------//

}
