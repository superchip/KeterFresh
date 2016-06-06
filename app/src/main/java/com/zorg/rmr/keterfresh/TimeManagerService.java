package com.zorg.rmr.keterfresh;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by ronsegal on 5/30/2016.
 */
public class TimeManagerService extends Service
{
    //-------------------------------//

    // seconds
    private final int CHECK_BOX_TIME_INTERVAL = 1;
    private final int BOX_TIME_INTERVAL = 1;

    private static BTDeviceManger m_btDeviceManager = new BTDeviceManger();



    //-------------------------------//

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    //-------------------------------//

    public static BTDeviceManger BTDeviceMangerInsance()
    {
        return m_btDeviceManager;
    }

    //-------------------------------//

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Intent notificationIntent = new Intent(this, KeterFresh.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();

        startForeground(1337, notification);



       // On receiving a bluetooth device
        BroadcastReceiver mReceiver = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(device.getName() == null)
                        return;

                    Log.d("NAME: ",device.getName());

                    String [] deviceNameSplit = device.getName().split("_");

                    if(deviceNameSplit.length == KeterFreshBTDevice.KETER_BT_DEVICE_NAME_LENGTH)
                    {
                        // if we found a keter device
                        if(deviceNameSplit[0].toLowerCase().equals(KeterFreshBTDevice.KETER_BT_DEVICE_NAME))
                        {
                            String deviceId = deviceNameSplit[1];

                            //Handle Reset case
                            if(deviceNameSplit[2].toLowerCase().equals("idle"))
                            {
                                BTDeviceMangerInsance().ResetDevice(deviceId);
                            }


                            // Register device
                            String timestamp = deviceNameSplit[2];

                            KeterFreshBTDevice keterDevice;

                            // Check if device is already registered with the same details - ignore
                            if(m_btDeviceManager.IsDeviceRegistered(deviceId))
                            {
                                // Found the device with the same details so don't do nothing
                                keterDevice = m_btDeviceManager.GetDeviceByID(deviceId);
                                if(m_btDeviceManager.GetDeviceByID(deviceId).RawTimestamp().equals(timestamp))
                                    return;

                            }

                            KeterFreshBTDevice btDeviceToRegister = new KeterFreshBTDevice(deviceId,timestamp);


                            m_btDeviceManager.RegisterDevice(btDeviceToRegister);

                            KeterFreshNotifier.Instance().PushNotification(btDeviceToRegister,0,"Found keter device",btDeviceToRegister.toString());

                        }

                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        new Reminder(CHECK_BOX_TIME_INTERVAL);
        return START_STICKY;
    }

    //-------------------------------//

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    //-------------------------------//

    // Go over all keter fresh registered devices and check if the threshold has passed
    public void CheckTimeDifference()
    {
        int mulFactor = 1;

        for (KeterFreshBTDevice btDevice: m_btDeviceManager.GetDeviceList())
        {
            // This is just for seconds simulation - in real event will be replace with days/hours
            int secondsInterval = Seconds.secondsBetween(btDevice.InitialTimeStampDateTime(), DateTime.now()).getSeconds();

            int minutesInterval = Minutes.minutesBetween(btDevice.InitialTimeStampDateTime(), DateTime.now()).getMinutes();

            int hourInterval = Hours.hoursBetween(btDevice.InitialTimeStampDateTime(), DateTime.now()).getHours();

            int interval = hourInterval;

            if(interval >= BOX_TIME_INTERVAL)
            {
                String notifcationContent = "Hey Kate, do you miss your " + btDevice.DeviceID() + " fresh box?" +
                        " It's in the fridge for " + interval + " days now." +
                        " Consider it for dinner tonight ;)";
                KeterFreshNotifier.Instance().PushNotification(btDevice,interval,"Keter Fresh",notifcationContent);
            }


        }

        //-------------------------------//

    }

    class Reminder
    {
        Timer timer;

        //-------------------------------//

        // Simulates 24 hour of checking box status
        public Reminder(int seconds)
        {
            //
            //
            //
            //
            //
            // TODO
            timer = new Timer();
            timer.scheduleAtFixedRate(new RemindTask(),new Date(),seconds * 500);
        }

        //-------------------------------//

        class RemindTask extends TimerTask
        {
            public void run()
            {
                TimeManagerService.BTDeviceMangerInsance().ScanDevices();
                CheckTimeDifference();
            }
        }

        //-------------------------------//
    }
}

class BTDeviceManger
{
    //-------------------------------//
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    //private static BTDeviceManger m_instance = new BTDeviceManger();
    private SharedPreferences m_sharedpreferences = null;
    private BluetoothAdapter m_btAdapter;

    //-------------------------------//

    BTDeviceManger()
    {
        m_btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Try to discover bluetooth devices on startup
        ScanDevices();
    }

    //-------------------------------//

    public void Init(KeterFresh keterFresh)
    {
        SharedPreferences sharedpreferences = keterFresh.getSharedPreferences(KeterFreshBTDevice.KETER_BT_DEVICE_SHARED_PREFERNCES_NAME, Context.MODE_PRIVATE);

        m_sharedpreferences = sharedpreferences;

        //TODO - remove/add this
        //ClearAllDevices();

        KeterFreshUIManager.Instance().DisplayList();
    }

    //-------------------------------//

    public void ResetDevice(String id)
    {
        if(IsDeviceRegistered(id))
        {
            m_sharedpreferences.edit().putString(id,"0");
            m_sharedpreferences.edit().apply();
        }
    }

    //-------------------------------//

    public void DeleteDevice(String id)
    {
        if(IsDeviceRegistered(id))
        {
            m_sharedpreferences.edit().remove(id);
            m_sharedpreferences.edit().apply();
        }
    }


    //-------------------------------//

    public void ClearAllDevices()
    {
        if(m_sharedpreferences == null) return;

        SharedPreferences.Editor editor = m_sharedpreferences.edit();
        editor.clear();
        editor.apply();
    }

    //-------------------------------//

    public BluetoothAdapter BTAdapter(){return m_btAdapter;}


    //-------------------------------//

    public void RegisterDevice(KeterFreshBTDevice device)
    {
        if(m_sharedpreferences == null) return;

        SharedPreferences.Editor editor = m_sharedpreferences.edit();
        editor.putString(device.DeviceID(),device.InitialTimeStampDateTime().toString());
        editor.apply();

        KeterFreshNotifier.Instance().RegisterDevice(device,0);

        KeterFreshUIManager.Instance().DisplayList();

        LOGGER.info("Register Device\n===========\n" + device);

    }

    //-------------------------------//

    public KeterFreshBTDevice GetDeviceByID(String id)
    {
        String empytString = "";
        String timeStamp = m_sharedpreferences.getString(id,empytString);

        if(timeStamp == "")
            return null;

        return new KeterFreshBTDevice(id,timeStamp);
    }

    //-------------------------------//

    public List<KeterFreshBTDevice> GetDeviceList()
    {
        if(m_sharedpreferences == null) return new ArrayList<>();

        Map<String,?> map = m_sharedpreferences.getAll();

        if(map == null)
            return null;

        List<KeterFreshBTDevice> btDevicesList = new ArrayList<>();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            btDevicesList.add(new KeterFreshBTDevice((String)pair.getKey(),(String)pair.getValue()));
            it.remove(); // avoids a ConcurrentModificationException
        }

        return btDevicesList;
    }

    public void ScanDevices()
    {
        m_btAdapter.cancelDiscovery();
        m_btAdapter.startDiscovery();
    }

    //-------------------------------//

    public boolean IsDeviceRegistered(String id)
    {
        return m_sharedpreferences.getAll().containsKey(id);
    }
}


//-------------------------------//

class KeterFreshNotifier
{
    private static KeterFreshNotifier m_instance = new KeterFreshNotifier();
    private NotificationManager m_notificationManager;
    private KeterFresh m_keterFresh;

    private final int MAX_NOTIFICATION_COUNT = 5;

    private Map<String,Integer> m_notfiedDeviceInfo;

    //-------------------------------//

    private void KeterFreshNotifier() {}

    //-------------------------------//

    public static KeterFreshNotifier Instance()
    {
        return m_instance;
    }

    //-------------------------------//

    public void Init(KeterFresh keterFresh, NotificationManager notificationManager)
    {
        m_notfiedDeviceInfo = new HashMap<String,Integer>();
        m_notificationManager = notificationManager;
        m_keterFresh = keterFresh;
    }

    //-------------------------------//

    public void RegisterDevice(KeterFreshBTDevice btDevice, int notificationCount)
    {
        m_notfiedDeviceInfo.put(btDevice.DeviceID(),notificationCount);
    }

    public void ClearAll()
    {
        m_notfiedDeviceInfo.clear();
    }

    //-------------------------------//


    public int getDeviceNotificationsCount(KeterFreshBTDevice btDevice)
    {
        int value = -1;

        if(m_notfiedDeviceInfo.containsKey(btDevice.DeviceID()))
        {
            value = m_notfiedDeviceInfo.get(btDevice.DeviceID());
        }

        return value;
    }



    //-------------------------------//

    public void PushNotification(KeterFreshBTDevice btDevice, int notificationCount, String notificationTitle, String notificationtText)
    {
        int interval = getDeviceNotificationsCount(btDevice);
        if(interval == notificationCount)
        {
            return;
        }
        else
        {
            RegisterDevice(btDevice,notificationCount);
        }

        if(KeterFresh.getInstace().IsActive())
            return;

        String titleText = notificationTitle;

        // Large Icon
        Bitmap bm = BitmapFactory.decodeResource(m_keterFresh.getResources(), R.drawable.keter_notification);

        Notification notify = new NotificationCompat.Builder(m_keterFresh)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentTitle(titleText)
                .setContentText(notificationtText)
                .setSmallIcon(R.drawable.keter_notification)
                .setLargeIcon(bm)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLights(Color.RED, 1, 1)
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationtText))
                .build();

        m_notificationManager.notify(0, notify);
    }

    //-------------------------------//
}


