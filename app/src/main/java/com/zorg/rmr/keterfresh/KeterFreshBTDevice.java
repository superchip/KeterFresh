package com.zorg.rmr.keterfresh;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Created by ronsegal on 5/29/2016.
 */
public class KeterFreshBTDevice implements Serializable
{
    //----------Instance--------------//
    private String    m_deviceID;
    private DateTime m_initialDateTime;
    private String m_rawTimeStamp;


    public static final String KETER_BT_DEVICE_NAME = "keter-fresh";
    public static final String KETER_BT_DEVICE_SHARED_PREFERNCES_NAME = "KeterDevices";
    public static final int KETER_BT_DEVICE_NAME_LENGTH = 3;

    //-------------------------------//
    public KeterFreshBTDevice(String id, DateTime timestamp)
    {
        setDeviceID(id);
        m_initialDateTime = timestamp;
    }

    //-------------------------------//
    public KeterFreshBTDevice(String id, String timestamp)
    {
        setDeviceID(id);
        setDeviceRawTimestamp(timestamp);
        setDeviceInitialDateTime(timestamp);
    }
    //-------------------------------//

    public KeterFreshBTDevice(String id)
    {
        setDeviceID(id);
    }

    //-------------------------------//

    public void ResetDevice()
    {
        m_initialDateTime =  null;
    }
    //-------------------------------//


    //------------Getters-------------//
    public String  DeviceID() {return m_deviceID;}
    //-------------------------------//
    public DateTime InitialTimeStampDateTime() {return m_initialDateTime;}
    //-------------------------------//
    public String toString()
    {
        return "KETER DEVICE ID[" + this.m_deviceID + "] LOCKED TIME: [" + this.m_initialDateTime + "]";
    }
    //-------------------------------//
    public String RawTimestamp()
    {
        return m_rawTimeStamp;
    }


    //------------Setters-------------//

    private void setDeviceID(String id)
    {
        m_deviceID = id;
    }

    //-------------------------------//

    public void setDeviceRawTimestamp(String timestamp)
    {
        m_rawTimeStamp = timestamp;
    }

    private void setDeviceInitialDateTime(String timeStamp)
    {
        if(timeStamp == null) return;



        try
        {
            int timeCountFromBox = Integer.parseInt(timeStamp);
            m_initialDateTime = DateTime.now().minusHours(timeCountFromBox);
        }
        catch (Exception exp)
        {
            try
            {
                m_initialDateTime = new DateTime(timeStamp);
            }
            catch (IllegalArgumentException e)
            {
                m_initialDateTime = DateTime.now();
            }


        }


    }

    //-------------------------------//

}
