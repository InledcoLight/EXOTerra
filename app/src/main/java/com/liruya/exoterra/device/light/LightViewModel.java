package com.liruya.exoterra.device.light;

import com.liruya.exoterra.bean.EXOLedstrip;
import com.liruya.exoterra.bean.Profile;
import com.liruya.exoterra.device.DeviceViewModel;

import java.util.ArrayList;
import java.util.List;

import cn.xlink.sdk.core.model.XLinkDataPoint;

public class LightViewModel extends DeviceViewModel<EXOLedstrip> {
    public void setMode(@EXOLedstrip.Mode int mode) {
        setDeviceDatapoint(getData().setMode(mode));
    }

    public void setPower(boolean power) {
        setDeviceDatapoint(getData().setPower(power));
    }

    public void setBright(int chn, int bright) {
        setDeviceDatapoint(getData().setBright(chn, bright));
    }

    public void setAllBrights(int[] brights) {
        setDeviceDatapoints(getData().setAllBrights(brights));
    }

    public void setCustomBrights(final int idx, byte[] brights) {
//        setApplicationSetDatapoint(getData().setCustomBrights(idx, brights));
        setDeviceDatapoint(getData().setCustomBrights(idx, brights));
    }

    public void setGisEnable(boolean enable) {
        setDeviceDatapoint(getData().setGisEnable(enable));
    }

    public void setGisEnable(boolean enable, float lon, float lat) {
        final List<XLinkDataPoint> dps = new ArrayList<>();
        XLinkDataPoint dp1 = getData().setGisEnable(enable);
        XLinkDataPoint dp2 = getData().setLongitude(lon);
        XLinkDataPoint dp3 = getData().setLatitude(lat);
        if (dp1 != null && dp2 != null && dp3 != null) {
            dps.add(dp1);
            dps.add(dp2);
            dps.add(dp3);
            setDeviceDatapoints(dps);
        }
    }

    public void setSunrise(int sunrise) {
        setDeviceDatapoint(getData().setSunrise(sunrise));
    }

    public void setSunriseRamp(int ramp) {
        setDeviceDatapoint(getData().setSunriseRamp(ramp));
    }

    public void setSunriseAndRamp(int sunrise, int ramp) {
        XLinkDataPoint dp1 = getData().setSunrise(sunrise);
        XLinkDataPoint dp2 = getData().setSunriseRamp(ramp);
        if (dp1 != null && dp2 != null) {
            List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp1);
            dps.add(dp2);
            setDeviceDatapoints(dps);
        }
    }

    public void setDayBrights(byte[] brights) {
        setDeviceDatapoint(getData().setDayBrights(brights));
    }

    public void setSunset(int sunset) {
        setDeviceDatapoint(getData().setSunset(sunset));
    }

    public void setSunsetRamp(int ramp) {
        setDeviceDatapoint(getData().setSunsetRamp(ramp));
    }

    public void setSunsetAndRamp(int sunset, int ramp) {
        XLinkDataPoint dp1 = getData().setSunset(sunset);
        XLinkDataPoint dp2 = getData().setSunsetRamp(ramp);
        if (dp1 != null && dp2 != null) {
            List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp1);
            dps.add(dp2);
            setDeviceDatapoints(dps);
        }
    }

    public void setNightBrights(byte[] brights) {
        setDeviceDatapoint(getData().setNightBrights(brights));
    }

    public void setTurnoffEnable(boolean enable) {
        setDeviceDatapoint(getData().setTurnoffEnable(enable));
    }

    public void setTurnoffTime(int time) {
        setDeviceDatapoint(getData().setTurnoffTime(time));
    }

    public void setTurnoff(boolean enable, int time) {
        XLinkDataPoint dp1 = getData().setTurnoffEnable(enable);
        XLinkDataPoint dp2 = getData().setTurnoffTime(time);
        if (dp1 != null && dp2 != null) {
            List<XLinkDataPoint> dps = new ArrayList<>();
            dps.add(dp1);
            dps.add(dp2);
            setDeviceDatapoints(dps);
        }
    }

    public void setProfile(int idx, Profile profile) {
        setDeviceDatapoints(getData().setProfile(idx, profile));
    }

    public void setProfileName(int idx, String name) {
        setApplicationSetDatapoint(getData().setProfileName(idx, name));
    }

    public void setSelectProfile(int select) {
        setDeviceDatapoint(getData().setSelectProfile(select));
    }
}
