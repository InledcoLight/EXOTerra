package com.inledco.exoterra.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Profile {
    private static final String TAG = "Profile";
    public static final int POINT_COUNT_MIN = 4;
    public static final int POINT_COUNT_MAX = 10;
    private List<TimePoint> mPoints;
    private Comparator<TimePoint> mComparator;

    private Profile(List<TimePoint> points) {
        mPoints = points;
        mComparator = new Comparator<TimePoint>() {
            @Override
            public int compare(TimePoint o1, TimePoint o2) {
                return o1.getTimer() - o2.getTimer();
            }
        };
    }

    public boolean isValid() {
        if (mPoints == null || mPoints.size() < POINT_COUNT_MIN || mPoints.size() > POINT_COUNT_MAX) {
            return false;
        }
        for (TimePoint tp : mPoints) {
            if (tp == null || !tp.isValid()) {
                return false;
            }
        }
        return true;
    }

    public List<TimePoint> getPoints() {
        return mPoints;
    }

    public void sort() {
        Collections.sort(mPoints, mComparator);
    }

    public byte getPointCount() {
        return (byte) (mPoints == null ? 0 : mPoints.size());
    }

    public byte[] getTimesArray() {
        byte[] timers = new byte[mPoints.size()*2];
        for (int i = 0; i < mPoints.size(); i++) {
            int tmr = mPoints.get(i).getTimer();
            timers[2*i] = (byte) (tmr & 0x00FF);
            timers[2*i+1] = (byte) ((tmr & 0xFF00) >> 8);
        }
        return timers;
    }

    public byte[] getBrightsArray() {
        List<Byte> list = new ArrayList<>();
        for (TimePoint tp : mPoints) {
            byte[] bytes = tp.getBrights();
            for (byte b : bytes) {
                list.add(b);
            }
        }
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public int[] getTimes() {
        int[] timers = new int[mPoints.size()];
        for (int i = 0; i < mPoints.size(); i++) {
            timers[i] = mPoints.get(i).getTimer();
        }
        return timers;
    }

    public int[][] getBrights() {
        int m = mPoints.size();
        int n = mPoints.get(0).getBrights().length;
        int[][] brights = new int[m][n];
        for (int i = 0; i < m; i++) {
            byte[] bytes = mPoints.get(i).getBrights();
            for (int j = 0; j < n; j++) {
                brights[i][j] = bytes[j];
            }
        }
        return brights;
    }

    public Profile copy() {
        List<TimePoint> points = new ArrayList<>();
        points.addAll(mPoints);
        return new Profile(points);
    }

    public static class Builder {
        private final int CHANNEL_COUNT_MIN     = 1;
        private final int CHANNEL_COUNT_MAX     = 6;
        private final int POINT_COUNT_MIN       = 4;
        private final int POINT_COUNT_MAX       = 10;
        public Profile create(int chnCount, int pointCount, byte[] timers, byte[] brights) {
            if (chnCount < CHANNEL_COUNT_MIN || chnCount > CHANNEL_COUNT_MAX) {
                return null;
            }
            if (pointCount < POINT_COUNT_MIN || pointCount > POINT_COUNT_MAX) {
                return null;
            }
            if (timers == null || timers.length != 2*pointCount) {
                return null;
            }
            if (brights == null || brights.length != chnCount*pointCount) {
                return null;
            }
            for (int i = 0; i < brights.length; i++) {
                if (brights[i] < 0 || brights[i] > 100) {
                    brights[i] = 100;
                }
            }
            List<TimePoint> points = new ArrayList<>();
            for (int i = 0; i < pointCount; i++) {
                int tmr = ((timers[2*i+1]&0xFF)<<8)|(timers[2*i]&0xFF);
                if (tmr < 0 || tmr > 1439) {
                    return null;
                }
                byte[] bytes = Arrays.copyOfRange(brights, i*chnCount, (i+1)*chnCount);
                TimePoint tp = new TimePoint(tmr, bytes);
                points.add(tp);
            }
            Profile profile = new Profile(points);
            return profile;
        }

//        public Profile createDefault(int chnCount) {
//            if (chnCount < CHANNEL_COUNT_MIN || chnCount > CHANNEL_COUNT_MAX) {
//                return null;
//            }
//            List<TimePoint> points = new ArrayList<>();
//        }
    }
}
