package com.inledco.exoterra.bean;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import cn.xlink.sdk.core.model.DataPointValueType;
import cn.xlink.sdk.core.model.XLinkDataPoint;
import cn.xlink.sdk.v5.model.XDevice;

public class Device {
    private final String TAG = "Device";

    private final int DATAPOINT_COUNT_MAX       = 200;
    private final int DATAPOINT_STRING_MAXLEN   = 64;
    private final int DATAPOINT_BINARY_MAXLEN   = 64;

    private final int INDEX_PROPERTY            = 0;
    private final int INDEX_ZONE                = 1;
    private final int INDEX_LONGITUDE           = 2;
    private final int INDEX_LATITUDE            = 3;
    private final int INDEX_DEVICE_DATETIME     = 4;
    private final int INDEX_SYNC_DATETIME       = 5;
    private final int INDEX_DAYTIME_START       = 6;
    private final int INDEX_DAYTIME_END         = 7;
    private final int INDEX_LOCATION_ENABLE     = 191;
    private final int INDEX_LOCATION_SUNRISE    = 192;
    private final int INDEX_LOCATION_SUNSET     = 193;
    private final int INDEX_LOCATION_VALID      = 194;
    private final int INDEX_CLOUDZONE           = 195;
    private final int INDEX_RSSI                = 196;
    private final int INDEX_UPGRADE_STATE       = 197;
    private final int INDEX_LOCAL_PSW           = 198;

    private XDevice mXDevice;
    private String mProperty;
    private final List<XLinkDataPoint> mDataPointList;

    public Device(@NonNull XDevice xDevice) {
        mXDevice = xDevice;
        mDataPointList = new ArrayList<>();
    }

    public XDevice getXDevice() {
        return mXDevice;
    }

    public void setXDevice(XDevice XDevice) {
        mXDevice = XDevice;
    }

    public void setProperty(String property) {
        mProperty = property;
    }

    public String getDeviceTag() {
        return mXDevice.getProductId() + "_" + mXDevice.getMacAddress();
    }

    public List<XLinkDataPoint> getDataPointList() {
        return mDataPointList;
    }

    public void setDataPointList(List<XLinkDataPoint> dataPointList) {
        mDataPointList.clear();
        mDataPointList.addAll(dataPointList);
    }

    public void setDataPoint(XLinkDataPoint dataPoint) {
        for (XLinkDataPoint dp : mDataPointList) {
            if (dp.getIndex() == dataPoint.getIndex()) {
                dp.setValue(dataPoint.getValue());
                return;
            }
        }
    }

    public XLinkDataPoint getDataPoint(int index, DataPointValueType type) {
        for (XLinkDataPoint dp : mDataPointList) {
            dp.getTypeIndex();
            if (dp.getIndex() == index && dp.getType() == type) {
                return dp;
            }
        }
        return null;
    }

    public XLinkDataPoint getDataPoint(int index) {
        for (XLinkDataPoint dp : mDataPointList) {
            if (dp.getIndex() == index) {
                return dp;
            }
        }
        return null;
    }

    public XLinkDataPoint setRawValue(int index, Object value) {
        if (index >= DATAPOINT_COUNT_MAX) {
            return null;
        }
        for (XLinkDataPoint dp : mDataPointList) {
            if (dp.getIndex() == index) {
//                if (!dp.isWritable()) {
//                    return null;
//                }
//                dp.setValue(value);
//                return dp;
                XLinkDataPoint xdp = new XLinkDataPoint(dp);
                xdp.setValue(value);
                return xdp;
            }
        }
        return null;
    }

    public XLinkDataPoint setValue(int index, DataPointValueType type, Object value) {
        if (index >= DATAPOINT_COUNT_MAX) {
            return null;
        }
        for (XLinkDataPoint dp : mDataPointList) {
            if (dp.getIndex() == index && dp.getType() == type) {
//                if (!dp.isWritable()) {
//                    return null;
//                }
//                dp.setValue(value);
//                return dp;
                XLinkDataPoint xdp = new XLinkDataPoint(dp);
                xdp.setValue(value);
                return xdp;
            }
        }
        return null;
    }

    public Object getRawValue(int index) {
        for (XLinkDataPoint dp : mDataPointList) {
            if (dp.getIndex() == index) {
                return dp.getRawValue();
            }
        }
        return null;
    }

    public Object getValue(int index, DataPointValueType type) {
        for (XLinkDataPoint dp : mDataPointList) {
            if (dp.getIndex() == index && dp.getType() == type) {
                return dp.getValue();
            }
        }
        return null;
    }

    protected XLinkDataPoint setBoolean(int index, boolean value) {
        return setRawValue(index, (byte) (value ? 0x01 : 0x00));
    }

    protected boolean getBoolean(int index) {
        Object value = getRawValue(index);
        if (value != null) {
            if (value instanceof Boolean) {
                return (boolean) value;
            } else if (value instanceof Byte) {
                return (((byte) value) == 1 ? true : false);
            }
        }

        return false;
    }

    protected XLinkDataPoint setByte(int index, byte value) {
        return setValue(index, DataPointValueType.BYTE, value);
    }

    protected byte getByte(int index) {
        Object value = getValue(index, DataPointValueType.BYTE);
        if (value != null && value instanceof Byte) {
            return (byte) value;
        }
        return 0;
    }

    protected XLinkDataPoint setShort(int index, short value) {
        return setValue(index, DataPointValueType.SHORT, value);
    }

    protected short getShort(int index) {
        Object value = getValue(index, DataPointValueType.SHORT);
        if (value != null && value instanceof Short) {
            return (short) value;
        }
        return 0;
    }

    protected XLinkDataPoint setUShort(int index, short value) {
        return setValue(index, DataPointValueType.USHORT, value);
    }

    protected short getUShort(int index) {
        Object value = getValue(index, DataPointValueType.USHORT);
        if (value != null && value instanceof Short) {
            return (short) value;
        }
        return 0;
    }

    protected XLinkDataPoint setInt(int index, int value) {
        return setValue(index, DataPointValueType.INT, value);
    }

    protected int getInt(int index) {
        Object value = getValue(index, DataPointValueType.INT);
        if (value != null && value instanceof Integer) {
            return (int) value;
        }
        return 0;
    }

    protected XLinkDataPoint setUInt(int index, int value) {
        return setValue(index, DataPointValueType.UINT, value);
    }

    protected int getUInt(int index) {
        Object value = getValue(index, DataPointValueType.UINT);
        if (value != null && value instanceof Integer) {
            return (int) value;
        }
        return 0;
    }

    protected XLinkDataPoint setLong(int index, long value) {
        return setValue(index, DataPointValueType.LONG, value);
    }

    protected long getLong(int index) {
        Object value = getValue(index, DataPointValueType.LONG);
        if (value != null && value instanceof Long) {
            return (long) value;
        }
        return 0;
    }

    protected XLinkDataPoint setULong(int index, long value) {
        return setValue(index, DataPointValueType.ULONG, value);
    }

    protected long getULong(int index) {
        Object value = getValue(index, DataPointValueType.ULONG);
        if (value != null && value instanceof Long) {
            return (long) value;
        }
        return 0;
    }

    protected XLinkDataPoint setFloat(int index, float value) {
        return setValue(index, DataPointValueType.FLOAT, value);
    }

    protected float getFloat(int index) {
        Object value = getValue(index, DataPointValueType.FLOAT);
        if (value != null && value instanceof Float) {
            return (float) value;
        }
        return 0.0f;
    }

    protected XLinkDataPoint setDouble(int index, double value) {
        return setValue(index, DataPointValueType.DOUBLE, value);
    }

    protected double getDouble(int index) {
        Object value = getValue(index, DataPointValueType.DOUBLE);
        if (value != null && value instanceof Double) {
            return (double) value;
        }
        return 0;
    }

    protected XLinkDataPoint setString(int index, String value) {
        if (value == null || value.length() > DATAPOINT_STRING_MAXLEN) {
            return null;
        }
        return setValue(index, DataPointValueType.STRING, value);
    }

    protected String getString(int index) {
        Object value = getValue(index, DataPointValueType.STRING);
        if (value != null && value instanceof String) {
            return (String) value;
        }
        return "";
    }

    protected XLinkDataPoint setByteArray(int index, byte[] value) {
        if (value == null || value.length > DATAPOINT_BINARY_MAXLEN) {
            return null;
        }
        return setValue(index, DataPointValueType.BYTE_ARRAY, value);
    }

    protected byte[] getByteArray(int index) {
        Object value = getValue(index, DataPointValueType.BYTE_ARRAY);
        if (value != null && value instanceof byte[]) {
            return (byte[]) value;
        }
        return null;
    }

    /*
        Device common datapoints
     */

    public String getProperty() {
        return getString(INDEX_PROPERTY);
    }

    public short getZone() {
        return getShort(INDEX_ZONE);
    }

    public XLinkDataPoint setZone(short zone) {
        if (zone < -1200 || zone > 1200 || zone%100<= -60 || zone%100 >= 60) {
            return null;
        }
        return setShort(INDEX_ZONE, zone);
    }

    public float getLongitude() {
        return getFloat(INDEX_LONGITUDE);
    }

    public XLinkDataPoint setLongitude(float longitude) {
        if (longitude < -180 || longitude > 180) {
            return null;
        }
        return setFloat(INDEX_LONGITUDE, longitude);
    }

    public float getLatitude() {
        return getFloat(INDEX_LATITUDE);
    }

    public XLinkDataPoint setLatitude(float latitude) {
        if (latitude < -60 || latitude > 60) {
            return null;
        }
        return setFloat(INDEX_LATITUDE, latitude);
    }

    public String getDeviceDatetime() {
        return getString(INDEX_DEVICE_DATETIME);
    }

    public XLinkDataPoint setSyncDatetime() {
        final byte[] array = new byte[10];
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        array[0] = (byte) (year & 0xFF);
        array[1] = (byte) ((year >> 8) & 0xFF);
        array[2] = (byte) calendar.get(Calendar.MONTH);
        array[3] = (byte) calendar.get(Calendar.DATE);
        array[4] = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        array[5] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        array[6] = (byte) calendar.get(Calendar.MINUTE);
        array[7] = (byte) calendar.get(Calendar.SECOND);
        final int rawZone = TimeZone.getDefault().getRawOffset() / 60000;
        final int zone = (rawZone/60)*100 + (rawZone%60);
        array[8] = (byte) (zone & 0xFF);
        array[9] = (byte) ((zone>>8) & 0xFF);
        return setByteArray(INDEX_SYNC_DATETIME, array);
    }

    public XLinkDataPoint setDaytimeStart(int dayStart) {
        return setUShort(INDEX_DAYTIME_START, (short) dayStart);
    }

    public int getDaytimeStart() {
        return getUShort(INDEX_DAYTIME_START);
    }

    public XLinkDataPoint setDaytimeEnd(int dayEnd) {
        return setUShort(INDEX_DAYTIME_END, (short) dayEnd);
    }

    public int getDaytimeEnd() {
        return getUShort(INDEX_DAYTIME_END);
    }

    public XLinkDataPoint setLocationEnable(boolean enable) {
        return setBoolean(INDEX_LOCATION_ENABLE, enable);
    }

    public boolean getLocationEnable() {
        return getBoolean(INDEX_LOCATION_ENABLE);
    }

    public int getLocationSunrise() {
        return getUShort(INDEX_LOCATION_SUNRISE);
    }

    public int getLocationSunset() {
        return getUShort(INDEX_LOCATION_SUNSET);
    }

    public boolean getLocationValid() {
        return getBoolean(INDEX_LOCATION_VALID);
    }

    public int getDeviceZoneIndex() {
        return INDEX_ZONE;
    }

    public int getDeviceDatetimeIndex() {
        return INDEX_DEVICE_DATETIME;
    }

    public byte getUpgradeState() {
        return getByte(INDEX_UPGRADE_STATE);
    }

    public int getLocalPsw() {
        int psw = getUInt(INDEX_LOCAL_PSW);
        return psw;
    }

    public int getCloudZone() {
        return getShort(INDEX_CLOUDZONE);
    }

    public int getRssi() {
        return getShort(INDEX_RSSI);
    }

    public XLinkDataPoint setLocalPsw(int psw) {
        return setUInt(INDEX_LOCAL_PSW, psw);
    }
}
