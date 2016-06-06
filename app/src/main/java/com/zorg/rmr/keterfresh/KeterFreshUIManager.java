package com.zorg.rmr.keterfresh;

import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ronsegal on 5/31/2016.
 */
public class KeterFreshUIManager
{
    private static KeterFreshUIManager m_instance = new KeterFreshUIManager();
    List<HashMap<String, String>> m_values;
    SimpleAdapter m_adapter;

    int index = 0;
    int [] imageID = {R.drawable.blue_box,R.drawable.red_box};
    int [] colorImageID = {R.drawable.initial,R.drawable.middle,R.drawable.due};

    public void Init()
    {
        // Get ListView object from xml
        ListView listView = (ListView) KeterFresh.getInstace().findViewById(R.id.keterlist);

        // create the grid item mapping
        String[] from = new String[] {"img", "title", "content","colorImage"};
        int[] to = new int[] { R.id.img, R.id.title, R.id.content, R.id.colorImage};

        // prepare the list of all records
        m_values = new ArrayList<HashMap<String, String>>();

//        HashMap<String, String> item1 = new HashMap<String, String>();
//        item1.put("img", Integer.toString(R.drawable.keter_notification));
//        item1.put("title", "titletitletitletitlev");
//        item1.put("content", "contentcontentcontentcontentcontent");

//        fillMaps.add(item1);

        m_adapter = new SimpleAdapter(KeterFresh.getInstace(), m_values, R.layout.list_single, from, to);

        // Assign adapter to ListView
        listView.setAdapter(m_adapter);
    }

    private KeterFreshUIManager()
    {

    }

    public void DisplayList()
    {
        m_values.clear();

        for (KeterFreshBTDevice device : TimeManagerService.BTDeviceMangerInsance().GetDeviceList())
        {
            KeterFreshUIManager.Instance().UpdateList(device);
        }

        m_adapter.notifyDataSetChanged();

    }

    public void UpdateList(KeterFreshBTDevice deviceInfo)
    {
        //int count = KeterFreshNotifier.Instance().getDeviceNotificationsCount(deviceInfo);

        int hourInterval = Hours.hoursBetween(deviceInfo.InitialTimeStampDateTime(), DateTime.now()).getHours();

        int imageBoxColor = 0;
        if(hourInterval < 2)
        {
            imageBoxColor = 0;
        }
        if(hourInterval >= 2 && hourInterval <= 3)
        {
            imageBoxColor = 1;
        }
        if(hourInterval > 3)
        {
            imageBoxColor = 2;
        }

        //idle
        String content = "Ready for use";

        if(deviceInfo.RawTimestamp().equals("0"))
        {
            content = "Active";
        }

        else if(hourInterval == 1)
        {
            content = hourInterval + " day in the fridge";
        }

        else if(hourInterval != 0)
        {
            content = hourInterval + " days in the fridge";
        }

        HashMap<String, String> item = new HashMap<String, String>();
        item.put("img", Integer.toString(imageID[index]));
        item.put("title", deviceInfo.DeviceID());
        item.put("content", content);
        item.put("colorImage", Integer.toString(colorImageID[imageBoxColor]));

        if(index == 0)
            index = 1;
        else
            index = 0;

        m_values.add(item);
    }



    public static KeterFreshUIManager Instance() {return m_instance;}

}

