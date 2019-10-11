package com.inledco.exoterra.device.light;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.inledco.exoterra.bean.Profile;
import com.inledco.exoterra.util.LightUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class LineChartHelper {

    public static void init(@NonNull LineChart chart) {
        XAxis xAxis = chart.getXAxis();
        YAxis axisLeft = chart.getAxisLeft();
        YAxis axisRight = chart.getAxisRight();
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setEnabled(true);
        xAxis.setAxisMaximum(24 * 60);
        xAxis.setAxisMinimum(0);
        xAxis.setLabelCount(13, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((((int) value) % 1440)/60);
            }
        });
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat df = new DecimalFormat("##0");
                return df.format(value);
            }
        };
        axisLeft.setAxisMaximum(100);
        axisLeft.setAxisMinimum(0);
        axisLeft.setLabelCount(5, true);
        axisLeft.setValueFormatter(formatter);
        axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisLeft.setTextColor(Color.WHITE);
        axisLeft.setDrawGridLines(true);
        axisLeft.setGridColor(0xFF9E9E9E);
        axisLeft.setGridLineWidth(0.75f);
        axisLeft.setDrawAxisLine(false);
        axisLeft.setAxisLineColor(Color.BLACK);
        axisLeft.setGranularity(1);
        axisLeft.setGranularityEnabled(true);
        axisLeft.setSpaceTop(0);
        axisLeft.setSpaceBottom(0);
        axisLeft.setEnabled(true);

        axisRight.setAxisMaximum(100);
        axisRight.setAxisMinimum(0);
        axisRight.setLabelCount(5, true);
        axisRight.setValueFormatter(formatter);
        axisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        axisRight.setTextColor(Color.WHITE);
        axisRight.setDrawGridLines(true);
        axisRight.setGridColor(0xFF9E9E9E);
        axisRight.setGridLineWidth(0.75f);
        axisRight.setDrawAxisLine(false);
        axisRight.setAxisLineColor(Color.BLACK);
        axisRight.setGranularity(1);
        axisRight.setGranularityEnabled(true);
        axisRight.setSpaceTop(0);
        axisRight.setSpaceBottom(0);
        axisRight.setEnabled(true);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setBorderColor(Color.CYAN);
        chart.setBorderWidth(1);
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.setMaxVisibleValueCount(0);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }

    public static void setProfile(@NonNull LineChart chart, int chnCount, String[] colors, Profile profile) {
        LineData lineData;
        List<ILineDataSet> dataSets = new ArrayList<>();
        if (colors == null || chnCount != colors.length || profile == null || !profile.isValid()) {
            lineData = new LineData(dataSets);
        } else {
            int pointCount = profile.getPointCount();
//            profile.sort();
            int[] time = profile.getTimes();
            int[][] brights = profile.getBrights();
            int[] index = new int[pointCount];
            for (int i = 0; i < pointCount; i++) {
                index[i] = i;
            }
            for (int i = pointCount-1; i > 0; i--) {
                for (int j = 0; j < i; j++) {
                    if (time[index[j]] > time[index[j+1]]) {
                        int tmp = index[j];
                        index[j] = index[j+1];
                        index[j+1] = tmp;
                    }
                }
            }

            for (int i = 0; i < chnCount; i++) {
                List<Entry> entries = new ArrayList<>();
                int ts = time[index[0]];
                int te = time[index[pointCount-1]];
                int bs = brights[index[0]][i];
                int be = brights[index[pointCount-1]][i];
                int duration = 1440 - te + ts;
                int dbrt = bs - be;
                float b0 = be + dbrt * (1440 - te) / (float) duration;
                entries.add(new Entry(0, b0));
                for (int j = 0; j < pointCount; j++) {
                    entries.add(new Entry(time[index[j]], brights[index[j]][i]));
                }
                entries.add(new Entry(1440, b0));

                String color = colors[i];
                if (color.endsWith("\0")) {
                    color = color.substring(0, color.length() - 1);
                }
                LineDataSet lineDataSet = new LineDataSet(entries, color);
                lineDataSet.setColor(LightUtil.getColorValue(color));
                lineDataSet.setCircleRadius(3.0f);
                lineDataSet.setCircleColor(LightUtil.getColorValue(color));
                lineDataSet.setDrawCircleHole(false);
                lineDataSet.setLineWidth(2.0f);
                dataSets.add(lineDataSet);
            }
            lineData = new LineData(dataSets);
        }
        chart.setData(lineData);
        chart.invalidate();
    }
}
